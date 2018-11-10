import net.SRSocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        int emptyCount = 0, MAX_EMPTY_COUNT = 50;
        FileInputStream imgReader = new FileInputStream(new File("Lab-2/src/main/java/input.jpg"));
        FileOutputStream txtWriter = new FileOutputStream(new File("Lab-2/src/main/java/output.txt"));
        SRSocket socket = new SRSocket(8080, 0, 0, true);
        socket.connect(InetAddress.getByName("192.168.43.75"), 8080);

        byte[] data = socket.read();
        while (emptyCount < MAX_EMPTY_COUNT) {
            if (data.length > 0) {
                txtWriter.write(data);
                emptyCount = 0;
            } else {
                emptyCount++;
            }
            data = socket.read();
        }

        byte[] buffer = new byte[56789];
        int length = imgReader.read(buffer);
        while (length != -1) {
            socket.write(buffer, 0, length);
            length = imgReader.read(buffer);
        }
        imgReader.close();
        txtWriter.close();
    }
}
