import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
    /*
     * Proxy server port
     */
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        // If launched with arguments, parse file to build fire wall.
        if (args.length > 0) {
            System.out.println("Using fire wall rules: " + args[0]);
            FireWall.parseFireWall(args[0]);
        }

        System.out.println("Starting proxy server...");
        ServerSocket proxy = new ServerSocket(PORT);
        System.out.println("Proxy server started, listening local port: " + PORT);

        while (true) {
            Socket client = proxy.accept();
            new Thread(new ProxyThread().init(client)).start();
        }
    }
}
