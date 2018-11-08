public enum SRPacketType {
    ACK((byte) 'A'),
    END((byte) 'E'),
    DATA((byte) 'D'),
    HELLO((byte) 'H');

    private byte value;

    SRPacketType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static SRPacketType fromValue(byte value) {
        for (SRPacketType type : SRPacketType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
}
