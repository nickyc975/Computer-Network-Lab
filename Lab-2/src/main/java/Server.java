import java.net.InetAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        SRSocket socket = new SRSocket(8081, InetAddress.getLocalHost(), 8080);
        byte[] data = socket.read();
        while (data.length <= 0) {
            data = socket.read();
        }
        System.out.println(new String(data));
    }
}
