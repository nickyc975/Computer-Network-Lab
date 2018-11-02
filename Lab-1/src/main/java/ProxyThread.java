import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ProxyThread implements Runnable{
    private final int BUFFER_SIZE = 4096;
    private final int SOCKET_TIMEOUT = 200;
    private final int MAX_TIMEOUT_COUNT = 17;
    private final String CONNECT = "CONNECT";
    private final byte[] CONNECTION_ESTABLISHED = "HTTP/1.1 200 Connection established\r\n\r\n".getBytes();

    private Socket client;
    private byte[] buffer;

    public ProxyThread init(Socket client) {
        this.client = client;
        if (this.buffer == null) {
            this.buffer = new byte[BUFFER_SIZE];
        }
        return this;
    }

    @Override
    public void run() {
        int length;
        Socket server;
        HttpRequestHeader request = null;
        InputStream ClientToProxy, ServerToProxy;
        OutputStream ProxyToClient, ProxyToServer;

        try {
            String clientHost = client.getInetAddress().getHostAddress();
            if (!FireWall.validateUser(clientHost)) {
                client.close();
                System.out.println("Blocked user: " + clientHost + "\n");
                return;
            }
            ClientToProxy = client.getInputStream();
            ProxyToClient = client.getOutputStream();

            length = ClientToProxy.read(buffer);
            request = new HttpRequestHeader(buffer, length);
            if (!FireWall.validateHost(request.getHost())) {
                client.close();
                System.out.println("Blocked host: " + request.getHost() + "\n");
                return;
            }
            System.out.print(request);

            server = new Socket(request.getHost(), request.getPort());
            ServerToProxy = server.getInputStream();
            ProxyToServer = server.getOutputStream();

            client.setSoTimeout(SOCKET_TIMEOUT);
            server.setSoTimeout(SOCKET_TIMEOUT);

            if (request.getMethod().equals(CONNECT)) {
                ProxyToClient.write(CONNECTION_ESTABLISHED);
            } else {
                ProxyToServer.write(buffer, 0, length);
            }

            int timeOutCount = 0;
            while (!client.isClosed() && !server.isClosed() && timeOutCount < MAX_TIMEOUT_COUNT) {
                try {
                    length = ClientToProxy.read(buffer);
                    if (length > 0) {
                        timeOutCount = 0;
                        do {
                            ProxyToServer.write(buffer, 0, length);
                            length = ClientToProxy.read(buffer);
                        } while (length > 0);
                    } else {
                        timeOutCount++;
                    }
                } catch (SocketTimeoutException e) {
                    timeOutCount++;
                }

                try {
                    length = ServerToProxy.read(buffer);
                    if (length > 0) {
                        timeOutCount = 0;
                        do {
                            ProxyToClient.write(buffer, 0, length);
                            length = ServerToProxy.read(buffer);
                        } while (length > 0);
                    } else {
                        timeOutCount++;
                    }
                } catch (SocketTimeoutException e) {
                    timeOutCount++;
                }
            }

            client.close();
            server.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.print("Request detail:\r\n\t");
            if (request != null) {
                System.err.print(request.toString().replaceAll("\n", "\n\t"));
            } else {
                System.err.println("Parse request failed.\r\n");
            }
        }
    }
}
