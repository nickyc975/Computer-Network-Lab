import java.util.Arrays;

class SRReceiverWindow {
    private static final int MAX_SEQ = 128;
    private static final int WINDOW_SIZE = 64;

    private int EXPECTED_SEQ = 0;
    private int MIN_RECVED_SEQ = MAX_SEQ;
    private SRPacket[] window;

    SRReceiverWindow() {
        window = new SRPacket[WINDOW_SIZE];
        Arrays.fill(window, null);
    }

    void add(SRPacket packet) {
        int pos = packet.getSeq() % WINDOW_SIZE;
        if (window[pos] == null) {
            window[pos] = packet;
            MIN_RECVED_SEQ = Integer.min(MIN_RECVED_SEQ, packet.getSeq());
        }
    }

    boolean readable() {
        return MIN_RECVED_SEQ == EXPECTED_SEQ;
    }

    SRPacket[] read() {
        if (readable()) {
            SRPacket[] recved = new SRPacket[WINDOW_SIZE];
            int length = 0, pos = EXPECTED_SEQ % WINDOW_SIZE;

            do {
                recved[length] = window[pos];
                window[pos] = null;
                pos = (pos + 1) % WINDOW_SIZE;
                length++;
            } while (
                    length < WINDOW_SIZE
                    && window[pos] != null
                    && window[pos].getSeq() == recved[length - 1].getSeq() + 1
            );

            EXPECTED_SEQ = recved[length - 1].getSeq() + 1;
            for (int i = 0; i < WINDOW_SIZE; i++) {
                if (window[i] != null) {
                    MIN_RECVED_SEQ = window[i].getSeq();
                    break;
                }
            }
            if (MIN_RECVED_SEQ <= EXPECTED_SEQ) {
                MIN_RECVED_SEQ = MAX_SEQ;
            }

            return Arrays.copyOfRange(recved, 0, length);
        }
        return new SRPacket[0];
    }
}
