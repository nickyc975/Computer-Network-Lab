import java.net.InetAddress;

public class Client {
    public static void main(String[] args) throws Exception {
        SRSocket socket = new SRSocket(8080, InetAddress.getLocalHost(), 8081);
        byte[] data = "hello".getBytes();
        socket.write(data,0, data.length);
    }
}
