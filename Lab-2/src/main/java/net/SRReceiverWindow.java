package net;

import java.util.Arrays;

class SRReceiverWindow {
    private static final int MAX_SEQ = 128;
    private static final int WINDOW_SIZE = 63;

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
        int pos = EXPECTED_SEQ % WINDOW_SIZE;
        if (window[pos] != null) {
            int length = 0;
            SRPacket[] recved = new SRPacket[WINDOW_SIZE];

            do {
                recved[length] = window[pos];
                window[pos] = null;
                pos = (pos + 1) % WINDOW_SIZE;
                length++;
            } while (
                    length < WINDOW_SIZE
                            && window[pos] != null
                            && window[pos].getSeq() == (recved[length - 1].getSeq() + 1) % MAX_SEQ
            );

            EXPECTED_SEQ = (recved[length - 1].getSeq() + 1) % MAX_SEQ;
            return Arrays.copyOfRange(recved, 0, length);
        }
        return new SRPacket[0];
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
