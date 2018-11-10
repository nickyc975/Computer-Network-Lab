package net;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

enum Flag {
    SEND,
    RECEIVE,
    TIMEOUT,
    FINISH
}

public class SRSocket {
    private static final int SO_TIMEOUT = 100;
    private static final int SR_DATA_LENGTH = 1468;
    private static final int UDP_DATA_LENGTH = 1472;

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

    Random random = new Random();
    private boolean view = false;
    private double PktLossRatio = 0.2;
    private double ACKLossRatio = 0.2;


    public SRSocket(int port) throws SocketException {
        server = new DatagramSocket(port);
        senderWindow = new SRSenderWindow();
        receiverWindow = new SRReceiverWindow();

        server.setSoTimeout(SO_TIMEOUT);
    }

    public SRSocket(int port, double PktLossRatio, double ACKLossRatio, boolean view) throws IOException {
        server = new DatagramSocket(port);
        senderWindow = new SRSenderWindow();
        receiverWindow = new SRReceiverWindow();

        server.setSoTimeout(SO_TIMEOUT);

        this.view = view;
        this.PktLossRatio = PktLossRatio;
        this.ACKLossRatio = ACKLossRatio;
    }

    public void connect(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.connected = true;

        this.sendBuffer = new byte[UDP_DATA_LENGTH];
        this.receiveBuffer = new byte[UDP_DATA_LENGTH];
        this.sendWrapper = new DatagramPacket(sendBuffer, UDP_DATA_LENGTH, address, port);
        this.receiveWrapper = new DatagramPacket(receiveBuffer, UDP_DATA_LENGTH);
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
                        log("send packet: " + srPacket.getSeq() + "\n");
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
                    receive();
                    flag = Flag.TIMEOUT;
                    break;
                case TIMEOUT:
                    SRPacketWrapper[] timeout = senderWindow.getTimeoutPkts();
                    for (SRPacketWrapper packet : timeout) {
                        log("resend packet: " + packet.getPacket().getSeq() + "\n");
                        send(packet.getPacket());
                        packet.resetTime();
                    }

                    flag = Flag.SEND;
                    break;
                default:
                    flag = Flag.FINISH;
                    break;
            }
        }
    }

    public byte[] read() throws IOException {
        receive();
        int offset = 0;
        byte[] data = new byte[0];
        SRPacket[] recved = receiverWindow.read();
        for(int i = 0; i < recved.length; i++) {
            log("read packet: " + recved[i].getSeq() + "\n");
            byte[] packetData = recved[i].getData();
            while (data.length - offset < packetData.length) {
                data = Arrays.copyOf(data, packetData.length + offset);
            }
            System.arraycopy(packetData, 0, data, offset, packetData.length);
            offset += packetData.length;
        }
        return data;
    }

    private boolean shouldLossACK() {
        int bound = (int) (ACKLossRatio * 100);
        int value = random.nextInt() % 101;
        value = value < 0 ? -value : value;
        return value < bound;
    }

    private boolean shouldLossPkt() {
        int bound = (int) (PktLossRatio * 100);
        int value = random.nextInt() % 101;
        value = value < 0 ? -value : value;
        return value < bound;
    }

    private void log(String s) {
        if (view) {
            System.out.println(s);
        }
    }

    private void send(SRPacket packet) throws IOException {
        sendTo(packet, address, port);
    }

    private void receive() throws IOException {
        try {
            SRPacket packet = receiveFrom(address, port);
            while (true) {
                switch (packet.getType()) {
                    case ACK:
                        if (packet.getType().equals(SRPacketType.ACK)) {
                            log("receive ack: " + packet.getSeq() + "\n");
                            senderWindow.remove(packet.getSeq());
                        }
                        break;
                    case DATA:
                        if (receiverWindow.shouldACK(packet)) {
                            if (shouldLossPkt()) {
                                log("loss packet: " + packet.getSeq() + "\n");
                            } else {
                                log("receive packet: " + packet.getSeq() + "\n");
                                receiverWindow.add(packet);
                                if (shouldLossACK()) {
                                    log("loss ack: " + packet.getSeq() + "\n");
                                } else {
                                    log("send ack: " + packet.getSeq() + "\n");
                                    send(new SRPacket(packet.getSeq(), SRPacketType.ACK));
                                }
                            }
                        } else {
                            log("rejected packet: " + packet.getSeq() + "\n");
                        }
                        break;
                    case HELLO:
                        break;
                    case END:
                        break;
                    default:
                        break;
                }
                packet = receiveFrom(address, port);
            }
        } catch (SocketTimeoutException e) {

        }
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
