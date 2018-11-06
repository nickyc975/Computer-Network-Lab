import java.util.Arrays;

public class GBNPacket {
    private byte seq;
    private int length;
    private byte[] data;

    public GBNPacket(byte seq, byte[] data, int length) {
        this.seq = seq;
        this.data = data;
        this.length = length;
    }

    public GBNPacket(byte[] packet) {
        seq = packet[0];
        length = packet[1] * 256 + packet[2];
        data = Arrays.copyOfRange(packet, 3, packet.length);
    }

    public byte[] toBytes() {
        return null;
    }
}
