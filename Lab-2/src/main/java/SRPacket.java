import java.util.Arrays;

public class SRPacket {
    public static final byte ACK = (byte) 'A';
    public static final byte END = (byte) 'E';
    public static final byte DATA = (byte) 'D';

    private int seq;
    private byte type;
    private int length;
    private byte[] data;

    public SRPacket(byte seq) {
        this.seq = seq;
        this.type = ACK;
        this.length = 0;
        this.data = new byte[0];
    }

    public SRPacket(int seq, byte[] data, int offset, int length) {
        this.seq = seq;
        this.type = DATA;
        this.length = length;
        this.data = new byte[length];
        System.arraycopy(data, offset, this.data, 0, length);
    }

    public SRPacket(byte[] packet) {
        seq = packet[0] & 0xFF;
        type = packet[1];
        length = ((packet[2] << 8) & 0xFF) + packet[3] & 0xFF;
        data = Arrays.copyOfRange(packet, 4, length + 4);
    }

    public int getSeq() {
        return seq;
    }

    public byte getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return length;
    }

    public byte[] toBytes() {
        byte[] packet = new byte[length + 4];
        packet[0] = (byte) (seq & 0xFF);
        packet[1] = type;
        packet[2] = (byte) ((length >> 8) & 0xFF);
        packet[3] = (byte) (length & 0xFF);
        System.arraycopy(data, 0, packet, 4, length);
        return packet;
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(seq + "" + (type & 0xFF));
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        } else if (object == this) {
            return true;
        } else if (object instanceof SRPacket) {
            SRPacket that = (SRPacket) object;
            return that.seq == this.seq && that.type == this.type;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "seq: " + seq + "\ntype: " + (char) type + "\ndata length: " + length + "\n";
    }
}
