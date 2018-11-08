package net;

import java.util.Arrays;

class SRSenderWindow {
    private static final int MAX_SEQ = 128;
    private static final int WINDOW_SIZE = 63;

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
        if (window[pos] == null) {
            window[pos] = new SRPacketWrapper(packet);
        }
    }

    void remove(int seq) {
        window[seq % WINDOW_SIZE] = null;
        if (seq == SEND_BASE) {
            SEND_BASE = (SEND_BASE + 1) % MAX_SEQ;
            updateQueuing();
        }
    }

    int getSeqNum() {
        if (QUEUING < WINDOW_SIZE) {
            int curSEQ = NEXT_SEQ;
            NEXT_SEQ = (NEXT_SEQ + 1) % MAX_SEQ;
            updateQueuing();
            return curSEQ;
        }
        return -1;
    }

    boolean isFull() {
        return QUEUING >= WINDOW_SIZE;
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

    private void updateQueuing() {
        int queuing = NEXT_SEQ - SEND_BASE;
        QUEUING = queuing >= 0 ? queuing : queuing + MAX_SEQ;
    }
}