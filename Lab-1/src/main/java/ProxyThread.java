import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ProxyThread implements Runnable{
    private final int BUFFER_SIZE = 4096;
    private final int SOCKET_TIMEOUT = 200;
    private final int MAX_TIMEOUT_COUNT = 15;
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
        InputStream ClientToProxy, ServerToProxy;
        OutputStream ProxyToClient, ProxyToServer;

        try {
            ClientToProxy = client.getInputStream();
            ProxyToClient = client.getOutputStream();

            length = ClientToProxy.read(buffer);
            HttpRequestHeader request = new HttpRequestHeader(buffer, length);
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
                    timeOutCount = 0;
                    while (length != -1) {
                        ProxyToServer.write(buffer, 0, length);
                        length = ClientToProxy.read(buffer);
                    }
                } catch (SocketTimeoutException e) {
                    timeOutCount++;
                }

                try {
                    length = ServerToProxy.read(buffer);
                    timeOutCount = 0;
                    while (length != -1) {
                        ProxyToClient.write(buffer, 0, length);
                        length = ServerToProxy.read(buffer);
                    }
                } catch (SocketTimeoutException e) {
                    timeOutCount++;
                }
            }

            client.close();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
