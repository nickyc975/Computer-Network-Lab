import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class ProxyThread implements Runnable {
    private static final int BUFFER_SIZE = 4096;
    private static final int SOCKET_TIMEOUT = 200;
    private static final int MAX_TIMEOUT_COUNT = 17;
    private static final String CONNECT = "CONNECT";
    private static final byte[] CONNECTION_ESTABLISHED = "HTTP/1.1 200 Connection established\r\n\r\n".getBytes(StandardCharsets.UTF_8);

    private Socket client;
    private byte[] buffer;

    /**
     * Initialize thread.
     *
     * @param client socket connected with client.
     * @return this thread.
     */
    public ProxyThread init(Socket client) {
        this.client = client;
        if (this.buffer == null) {
            this.buffer = new byte[BUFFER_SIZE];
        }
        return this;
    }

    /**
     * Forward data between server and client.
     * <p>
     * Support both http and https connections.
     */
    @Override
    public void run() {
        int length;
        Socket server;
        HttpRequestHeader request = null;
        InputStream ClientToProxy, ServerToProxy;
        OutputStream ProxyToClient, ProxyToServer;

        try {
            /*
             * Validate user host with fire wall.
             */
            String clientHost = client.getInetAddress().getHostAddress();
            if (!FireWall.validateUser(clientHost)) {
                client.close();
                System.out.println("Blocked user: " + clientHost + "\n");
                return;
            }

            ClientToProxy = client.getInputStream();
            ProxyToClient = client.getOutputStream();

            /*
             * Parse http request header.
             * Assume that the length of header is less that 4096 byte.
             */
            length = ClientToProxy.read(buffer);
            request = new HttpRequestHeader(buffer, length);

            /*
             * Validate requesting server host with fire wall.
             */
            if (!FireWall.validateHost(request.getHost())) {
                client.close();
                System.out.println("Blocked host: " + request.getHost() + "\n");
                return;
            }
            System.out.print(request);

            /*
             * Connect to server.
             */
            server = new Socket(request.getHost(), request.getPort());
            ServerToProxy = server.getInputStream();
            ProxyToServer = server.getOutputStream();

            /*
             * Set socket reading timeout or the thread will be blocked for a long time.
             */
            client.setSoTimeout(SOCKET_TIMEOUT);
            server.setSoTimeout(SOCKET_TIMEOUT);

            /*
             * Handle http request and https request differently.
             *
             * For http request, forward all read bytes to server.
             * For https request, response "Connection established" to client.
             */
            if (request.getMethod().equals(CONNECT)) {
                ProxyToClient.write(CONNECTION_ESTABLISHED);
            } else {
                ProxyToServer.write(buffer, 0, length);
            }

            /*
             * Start to transform data between client and server.
             *
             * "timeoutCount" is used to end thread automatically.
             * If socket reading keeps timeout or reading 0 byte data for some times,
             * we can assert that the transmission is finished and then close the connection.
             */
            int timeoutCount = 0;
            while (!client.isClosed() && !server.isClosed() && timeoutCount < MAX_TIMEOUT_COUNT) {
                try {
                    length = ClientToProxy.read(buffer);
                    if (length > 0) {
                        timeoutCount = 0;
                        do {
                            ProxyToServer.write(buffer, 0, length);
                            length = ClientToProxy.read(buffer);
                        } while (length > 0);
                    } else {
                        timeoutCount++;
                    }
                } catch (SocketTimeoutException e) {
                    timeoutCount++;
                }

                try {
                    length = ServerToProxy.read(buffer);
                    if (length > 0) {
                        timeoutCount = 0;
                        do {
                            ProxyToClient.write(buffer, 0, length);
                            length = ServerToProxy.read(buffer);
                        } while (length > 0);
                    } else {
                        timeoutCount++;
                    }
                } catch (SocketTimeoutException e) {
                    timeoutCount++;
                }
            }

            client.close();
            server.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.print("Request detail:\r\n\t");
            if (request != null) {
                System.err.print(
                        request.toString()
                                .replaceAll("\r\n", "\r\n\t")
                                .replaceAll("\r\n\t$", "\r\n")
                );
            } else {
                System.err.println("Parse request failed.\r\n");
            }
        }
    }
}
