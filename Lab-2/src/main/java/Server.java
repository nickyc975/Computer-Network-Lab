import net.SRSocket;

import java.net.InetAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        SRSocket socket = new SRSocket(8081, 0.2, 0.2, false);
        socket.connect(InetAddress.getLocalHost(), 8080);
        byte[] data;
        while (true) {
            data = socket.read();
            if (data.length > 0) {
                for (int i = 0; i < data.length; i++) {
                    System.out.println((data[i] & 0xFF) + "\n");
                }
            }
        }
    }
}
