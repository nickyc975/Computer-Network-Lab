import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

class SRSenderWindow {
    private static final int MAX_SEQ = 128;
    private static final int WINDOW_SIZE = 64;

    private int NEXT_SEQ = 0;
    private int SEND_BASE = 0;
    private SRPacketWrapper[] window;

    SRSenderWindow () {
        window = new SRPacketWrapper[WINDOW_SIZE];
        Arrays.fill(window, null);
    }

    void add(SRPacket packet) {
        int pos = packet.getSeq() % WINDOW_SIZE;
        if (window[pos] == null) {
            window[pos] = new SRPacketWrapper(packet);
        }
    }

    void remove(int seq) {
        for (int i = 0; i < WINDOW_SIZE; i++) {
            SRPacket packet = window[i].getPacket();
            if (packet != null && packet.getSeq() == seq) {
                window[i] = null;
                if (seq == SEND_BASE) {
                    SEND_BASE++;
                }
                return;
            }
        }
    }

    int getSeqNum() {
        int queuingCount = NEXT_SEQ - SEND_BASE;
        queuingCount = queuingCount >= 0 ? queuingCount : queuingCount + MAX_SEQ;
        if (queuingCount < WINDOW_SIZE) {
            int curSEQ = NEXT_SEQ;
            NEXT_SEQ = (NEXT_SEQ + 1) % MAX_SEQ;
            return curSEQ;
        }
        return -1;
    }

    void resendTimeout(final DatagramSocket server, final DatagramPacket udpPacket) throws IOException {
        for (int i = 0; i < WINDOW_SIZE; i++) {
            if (window[i] != null && window[i].isTimeout()) {
                udpPacket.setData(window[i].getPacket().toBytes());
                server.send(udpPacket);
                window[i].resetTime();
            }
        }
    }

    boolean isFull() {
        int queuingCount = NEXT_SEQ - SEND_BASE;
        queuingCount = queuingCount >= 0 ? queuingCount : queuingCount + MAX_SEQ;
        return queuingCount >= WINDOW_SIZE;
    }

    boolean isEmpty() {
        int queuingCount = NEXT_SEQ - SEND_BASE;
        queuingCount = queuingCount >= 0 ? queuingCount : queuingCount + MAX_SEQ;
        return queuingCount == 0;
    }
}


class SRPacketWrapper {
    private static final int TIMEOUT = 500;

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
