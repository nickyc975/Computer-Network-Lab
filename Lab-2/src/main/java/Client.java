import net.SRSocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;

import static java.lang.Thread.sleep;

public class Client {
    public static void main(String[] args) throws Exception {
        int emptyCount = 0, MAX_EMPTY_COUNT = 50;
        FileInputStream txtReader = new FileInputStream(new File("Lab-2/src/main/java/input.txt"));
        FileOutputStream imgWriter = new FileOutputStream(new File("Lab-2/src/main/java/output.jpg"));
        SRSocket socket = new SRSocket(8080, 0, 0, true);
        socket.connect(InetAddress.getByName("192.168.43.162"), 8080);

        byte[] buffer = new byte[56789];
        int length = txtReader.read(buffer);
        while (length != -1) {
            socket.write(buffer, 0, length);
            length = txtReader.read(buffer);
        }

        sleep(500);

        byte[] data = socket.read();
        while (emptyCount < MAX_EMPTY_COUNT) {
            if (data.length > 0) {
                imgWriter.write(data);
                emptyCount = 0;
            } else {
                emptyCount++;
            }
            data = socket.read();
        }
        imgWriter.close();
        txtReader.close();
    }
}
