import net.SRSocket;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        FileInputStream reader = new FileInputStream(new File("Lab-2/src/main/java/input.jpg"));
        SRSocket socket = new SRSocket(8081, 0, 0, true);
        socket.connect(InetAddress.getLocalHost(), 8080);

        byte[] buffer = new byte[2048];
        int length = reader.read(buffer);
        while (length != -1) {
            socket.write(buffer, 0, length);
            length = reader.read(buffer);
        }
        reader.close();
    }
}
