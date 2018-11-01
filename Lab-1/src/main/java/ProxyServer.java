import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ServerSocket proxy = new ServerSocket(PORT);

        while (true) {
            Socket client = proxy.accept();
            new Thread(new ProxyThread().init(client)).start();
        }
    }
}
