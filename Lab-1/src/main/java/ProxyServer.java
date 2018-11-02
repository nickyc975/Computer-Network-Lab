import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ServerSocket proxy = new ServerSocket(PORT);
        if (args.length > 0) {
            parseFireWall(args[0]);
        }
        while (true) {
            Socket client = proxy.accept();
            new Thread(new ProxyThread().init(client)).start();
        }
    }

    private static void parseFireWall(String filePath) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
        while ((line = reader.readLine()) != null) {
            String[] content = line.split("\\s");
            if (content.length != 3) {
                continue;
            }

            switch (content[0]) {
                case "host":
                    switch (content[1]) {
                        case "white":
                            FireWall.addHostWhiteListItem(content[2]);
                            break;
                        case "black":
                            FireWall.addHostBlackListItem(content[2]);
                            break;
                        default:
                            break;
                    }
                    break;
                case "user":
                    switch (content[1]) {
                        case "white":
                            FireWall.addUserWhiteListItem(content[2]);
                            break;
                        case "black":
                            FireWall.addUserBlackListItem(content[2]);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
