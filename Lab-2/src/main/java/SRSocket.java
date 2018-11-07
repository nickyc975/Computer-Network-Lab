import java.io.IOException;
import java.io.Writer;
import java.net.*;

enum Flag {
    SEND,
    RECEIVE,
    TIMEOUT
}

public class SRSocket {
    private static final int SO_TIMEOUT = 100;
    private static final int SR_DATA_LENGTH = 1468;
    private static final int UDP_DATA_LENGTH = 1472;

    private DatagramSocket server;
    private SRSenderWindow senderWindow;
    private SRReceiverWindow receiverWindow;


    public SRSocket(final int port) throws SocketException {
        server = new DatagramSocket(port);
        senderWindow = new SRSenderWindow();
        receiverWindow = new SRReceiverWindow();

        server.setSoTimeout(SO_TIMEOUT);
    }

    public void send(byte[] data, InetAddress address, int port) throws IOException {
        int length;
        int offset = 0;
        byte[] buffer = new byte[UDP_DATA_LENGTH];
        DatagramPacket udpPacket = new DatagramPacket(buffer, UDP_DATA_LENGTH, address, port);

        Flag flag = Flag.SEND;
        while (offset < data.length) {
            switch (flag) {
                case SEND:
                    int seq = senderWindow.getSeqNum();
                    if (seq >= 0) {
                        length = Integer.min(SR_DATA_LENGTH, data.length - offset);
                        SRPacket srPacket = new SRPacket(seq, data, offset, length);
                        udpPacket.setData(srPacket.toBytes());
                        server.send(udpPacket);
                        senderWindow.add(srPacket);
                        offset += length;
                    } else {
                        flag = Flag.RECEIVE;
                    }
                    break;
                case RECEIVE:
                    try {
                        server.receive(udpPacket);
                        while (udpPacket.getLength() > 0) {
                            SRPacket srPacket = new SRPacket(udpPacket.getData());
                            udpPacket.setLength(UDP_DATA_LENGTH);
                            handlePacket(srPacket);
                            server.receive(udpPacket);
                        }
                        flag = Flag.TIMEOUT;
                    } catch (SocketTimeoutException e) {
                        flag = Flag.TIMEOUT;
                    }
                    break;
                case TIMEOUT:
                    senderWindow.resendTimeout(server, udpPacket);
                    flag = Flag.SEND;
                    break;
                default:
                    break;
            }
        }
    }

    private void handlePacket(SRPacket packet) {
        switch (packet.getType()) {
            case SRPacket.ACK:
                senderWindow.remove(packet.getSeq());
                break;
            case SRPacket.DATA:
                break;
            case SRPacket.END:
                break;
            default:
                break;
        }
    }

    public void receive(Writer writer, InetAddress address, int port) throws IOException {
        byte[] buffer = new byte[UDP_DATA_LENGTH];
        DatagramPacket udpPacket = new DatagramPacket(buffer, UDP_DATA_LENGTH);

        server.receive(udpPacket);
    }
}
