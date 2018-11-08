import java.io.*;
import java.net.*;
import java.util.Arrays;

enum Flag {
    SEND,
    RECEIVE,
    TIMEOUT,
    FINISH
}

public class SRSocket {
    private static final int SO_TIMEOUT = 100;
    private static final int SR_DATA_LENGTH = 1024;
    private static final int UDP_DATA_LENGTH = 1468;

    private int port;
    private InetAddress address;
    private boolean connected = false;

    private DatagramSocket server;
    private SRSenderWindow senderWindow;
    private SRReceiverWindow receiverWindow;

    private byte[] sendBuffer;
    private byte[] receiveBuffer;
    private DatagramPacket sendWrapper;
    private DatagramPacket receiveWrapper;


    public SRSocket(int port) throws SocketException {
        server = new DatagramSocket(port);
        senderWindow = new SRSenderWindow();
        receiverWindow = new SRReceiverWindow();

        server.setSoTimeout(SO_TIMEOUT);
    }

    public SRSocket(int localPort, InetAddress address, int port) throws SocketException {
        server = new DatagramSocket(localPort);
        senderWindow = new SRSenderWindow();
        receiverWindow = new SRReceiverWindow();

        server.setSoTimeout(SO_TIMEOUT);

        this.address = address;
        this.port = port;
        this.connected = true;

        this.sendBuffer = new byte[UDP_DATA_LENGTH];
        this.receiveBuffer = new byte[UDP_DATA_LENGTH];
        this.sendWrapper = new DatagramPacket(sendBuffer, UDP_DATA_LENGTH, address, port);
        this.receiveWrapper = new DatagramPacket(receiveBuffer, UDP_DATA_LENGTH);
    }

    public void connect(InetAddress address, int port) throws ConnectException {
        int timeoutCount = 0;
        int MAX_TIMEOUT_COUNT = 5;
        SRPacket hello = new SRPacket(0, SRPacketType.HELLO);

        try {
            while (timeoutCount < MAX_TIMEOUT_COUNT) {
                try {
                    sendTo(hello, address, port);
                    while (true) {
                        SRPacket recved = receiveFrom(address, port);
                        if (recved.getType().equals(SRPacketType.HELLO)) {
                            sendTo(hello, address, port);

                            this.address = address;
                            this.port = port;
                            this.connected = true;

                            this.sendBuffer = new byte[UDP_DATA_LENGTH];
                            this.receiveBuffer = new byte[UDP_DATA_LENGTH];
                            this.sendWrapper = new DatagramPacket(sendBuffer, UDP_DATA_LENGTH, address, port);
                            this.receiveWrapper = new DatagramPacket(receiveBuffer, UDP_DATA_LENGTH);
                            return;
                        }
                    }
                } catch (SocketTimeoutException e) {
                    timeoutCount++;
                }
            }
            throw new ConnectException("Connect timeout.");
        } catch (IOException e) {
            throw new ConnectException("Connect failed.");
        }
    }

    public void write(byte[] data, int offset, int length) throws IOException {
        if (!connected) {
            throw new ConnectException("Not connected.");
        }

        Flag flag = Flag.SEND;
        int end = offset + length;
        int sendLength, sendOffset = offset;

        while (!flag.equals(Flag.FINISH)) {
            switch (flag) {
                case SEND:
                    while (sendOffset < end) {
                        int seq = senderWindow.getSeqNum();
                        if (seq < 0) {
                            break;
                        }
                        sendLength = Integer.min(SR_DATA_LENGTH, end - sendOffset);
                        SRPacket srPacket = new SRPacket(seq, data, sendOffset, sendLength);
                        send(srPacket);
                        senderWindow.add(srPacket);
                        sendOffset += sendLength;
                    }

                    if (sendOffset >= end && senderWindow.isEmpty()) {
                        flag = Flag.FINISH;
                    } else {
                        flag = Flag.RECEIVE;
                    }
                    break;
                case RECEIVE:
                    try {
                        while (true) {
                            SRPacket recved = receive();
                            if (recved.getType().equals(SRPacketType.ACK)) {
                                senderWindow.remove(recved.getSeq());
                                if (!senderWindow.isFull()) {
                                    flag = Flag.SEND;
                                    break;
                                }
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        flag = Flag.TIMEOUT;
                    }
                    break;
                case TIMEOUT:
                    senderWindow.resendTimeout(server, sendWrapper);
                    if (!senderWindow.isFull()) {
                        flag = Flag.SEND;
                    } else {
                        flag = Flag.RECEIVE;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public byte[] read() throws IOException {
        try {
            SRPacket packet = receive();
            while (receiverWindow.accept(packet)) {
                send(new SRPacket(packet.getSeq(), SRPacketType.ACK));
                packet = receive();
            }
        } catch (SocketTimeoutException e) {

        }

        int offset = 0;
        byte[] data = new byte[0];
        SRPacket[] recved = receiverWindow.read();
        for(int i = 0; i < recved.length; i++) {
            byte[] packetData = recved[i].getData();
            while (data.length - offset < packetData.length) {
                data = Arrays.copyOf(data, data.length + SR_DATA_LENGTH);
            }
            System.arraycopy(packetData, 0, data, offset, packetData.length);
            offset += packetData.length;
        }
        return data;
    }

    private void send(SRPacket packet) throws IOException {
        sendTo(packet, address, port);
    }

    private SRPacket receive() throws IOException {
        return receiveFrom(address, port);
    }

    private void sendTo(SRPacket packet, InetAddress address, int port) throws IOException {
        sendWrapper.setAddress(address);
        sendWrapper.setPort(port);
        sendWrapper.setData(packet.toBytes());
        server.send(sendWrapper);
    }

    private SRPacket receiveFrom(InetAddress address, int port) throws IOException {
        receiveWrapper.setLength(UDP_DATA_LENGTH);
        server.receive(receiveWrapper);
        while (!receiveWrapper.getAddress().equals(address) || receiveWrapper.getPort() != port) {
            receiveWrapper.setLength(UDP_DATA_LENGTH);
            server.receive(receiveWrapper);
        }
        return new SRPacket(receiveWrapper.getData());
    }
}
