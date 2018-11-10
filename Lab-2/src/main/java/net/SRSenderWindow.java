package net;

import java.util.Arrays;

class SRSenderWindow {
    private static final int MAX_SEQ = 128;
    private static final int WINDOW_SIZE = 64;

    private int QUEUING = 0;
    private int NEXT_SEQ = 0;
    private int SEND_BASE = 0;
    private SRPacketWrapper[] window;

    SRSenderWindow () {
        window = new SRPacketWrapper[WINDOW_SIZE];
        Arrays.fill(window, null);
    }

    void add(SRPacket packet) {
        int pos = packet.getSeq() % WINDOW_SIZE;
        window[pos] = new SRPacketWrapper(packet);
    }

    void remove(int seq) {
        int pos = seq % WINDOW_SIZE;
        window[pos] = null;
        if (seq == SEND_BASE) {
            while (window[pos] == null && calDistance(NEXT_SEQ, SEND_BASE) > 0) {
                SEND_BASE = (SEND_BASE + 1) % MAX_SEQ;
                pos = SEND_BASE % WINDOW_SIZE;
            }
            QUEUING = calDistance(NEXT_SEQ, SEND_BASE);
        }
    }

    int getSeqNum() {
        if (QUEUING < WINDOW_SIZE) {
            int curSEQ = NEXT_SEQ;
            NEXT_SEQ = (NEXT_SEQ + 1) % MAX_SEQ;
            QUEUING = calDistance(NEXT_SEQ, SEND_BASE);
            return curSEQ;
        }
        return -1;
    }

    boolean isEmpty() {
        return QUEUING == 0;
    }

    SRPacketWrapper[] getTimeoutPkts() {
        int count = 0;
        SRPacketWrapper[] timeout = new SRPacketWrapper[WINDOW_SIZE];
        for (SRPacketWrapper packet : window) {
            if (packet != null && packet.isTimeout()) {
                timeout[count] = packet;
                count++;
            }
        }
        return Arrays.copyOfRange(timeout, 0, count);
    }

    private int calDistance(int high, int low) {
        int distance = high - low;
        return distance >= 0 ? distance : distance + MAX_SEQ;
    }
}