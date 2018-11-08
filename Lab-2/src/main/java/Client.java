import net.SRSocket;

import java.net.InetAddress;

public class Client {
    public static void main(String[] args) throws Exception {
        SRSocket socket = new SRSocket(8080, 0.2, 0.2, true);
        socket.connect(InetAddress.getLocalHost(), 8081);
        byte[] data = new byte[1];
        for (int i = 0; i < 256; i++) {
            data[0] = (byte) i;
            socket.write(data,0, data.length);
        }
    }
}
