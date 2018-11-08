package net;

class SRPacketWrapper {
    private static final int TIMEOUT = 300;

    private long time;
    private SRPacket packet;

    SRPacketWrapper(final SRPacket packet) {
        this.packet = packet;
        this.time = System.currentTimeMillis();
    }

    SRPacket getPacket() {
        return packet;
    }

    void resetTime() {
        this.time = System.currentTimeMillis();
    }

    boolean isTimeout() {
        return System.currentTimeMillis() - time >= TIMEOUT;
    }
}
