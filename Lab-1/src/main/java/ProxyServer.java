import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
    /*
     * Proxy server port
     */
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ServerSocket proxy = new ServerSocket(PORT);

        // If launched with arguments, parse file to build fire wall.
        if (args.length > 0) {
            FireWall.parseFireWall(args[0]);
        }

        while (true) {
            Socket client = proxy.accept();
            new Thread(new ProxyThread().init(client)).start();
        }
    }
}
