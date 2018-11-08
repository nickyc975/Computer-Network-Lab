import net.SRSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;

public class Client {
    public static void main(String[] args) throws Exception {
        int emptyCount = 0, MAX_EMPTY_COUNT = 200;
        FileOutputStream writer = new FileOutputStream(new File("Lab-2/src/main/java/output.jpg"));
        SRSocket socket = new SRSocket(8080, 0, 0, true);
        socket.connect(InetAddress.getLocalHost(), 8081);

        byte[] data = socket.read();
        while (emptyCount < MAX_EMPTY_COUNT) {
            if (data.length > 0) {
                writer.write(data);
                emptyCount = 0;
            } else {
                emptyCount++;
            }
            data = socket.read();
        }
        writer.close();
    }
}
