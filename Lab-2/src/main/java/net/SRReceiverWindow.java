package net;

import java.util.Arrays;

class SRReceiverWindow {
    private static final int MAX_SEQ = 128;
    private static final int WINDOW_SIZE = 64;

    private SRPacket[] window;
    private int EXPECTED_SEQ = 0;

    SRReceiverWindow() {
        window = new SRPacket[WINDOW_SIZE];
        Arrays.fill(window, null);
    }

    void add(SRPacket packet) {
        if (isExpected(packet)) {
            window[packet.getSeq() % WINDOW_SIZE] = packet;
        }
    }

    boolean shouldACK(SRPacket packet) {
        return isExpected(packet) || hasReceived(packet);
    }

    SRPacket[] read() {
        int length = 0;
        int pos = EXPECTED_SEQ % WINDOW_SIZE;
        SRPacket[] recved = new SRPacket[WINDOW_SIZE];

        while (window[pos] != null) {
            recved[length] = window[pos];
            EXPECTED_SEQ = (window[pos].getSeq() + 1) % MAX_SEQ;
            window[pos] = null;
            pos = EXPECTED_SEQ % WINDOW_SIZE;
            length++;
        }

        return Arrays.copyOfRange(recved, 0, length);
    }

    private int calDistance(int high, int low) {
        int distance = high - low;
        return distance >= 0 ? distance : distance + MAX_SEQ;
    }

    private boolean isExpected(SRPacket packet) {
        return calDistance(packet.getSeq(), EXPECTED_SEQ) < WINDOW_SIZE;
    }

    private boolean hasReceived(SRPacket packet) {
        return calDistance(EXPECTED_SEQ, packet.getSeq()) < WINDOW_SIZE;
    }
}
