import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class GBNSocket {
    private static final int LENGTH = 1472;
    private static final int WINDOW_SIZE = 128;

    private DatagramSocket server;

    public GBNSocket(final int port) throws SocketException {
        server = new DatagramSocket(port);
    }

    public void send(byte[] data) throws IOException {
        int length;
        int offset = 0;
        byte[] buffer = new byte[LENGTH];
        DatagramPacket packet = new DatagramPacket(buffer, LENGTH);

        while (offset < data.length) {
            length = Integer.min(LENGTH, data.length - offset);
            packet.setData(buildPacket(data, offset, length));
            server.send(packet);
            offset += length;
        }
    }

    public byte[] receive(InetAddress address, int port) throws IOException {
        return null;
    }

    private byte[] buildPacket(byte[] data, int offset, int length) {
        byte[] packet = new byte[length + 4];
        packet[0] = getSeqNum();
        packet[1] = (byte) ((length >> 8) & 0xFF);
        packet[2] = (byte) (length & 0xFF);
        System.arraycopy(data, offset, packet, 3, length);
        packet[packet.length - 1] = (byte) 0xFF;
        return packet;
    }

    private byte getSeqNum() {
        return 0;
    }
}
