import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ProxyServer {
    private static final int PORT = 8080;
    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;
    private static final int BUFFER_SIZE = 4096;
    private static final int SOCKET_TIMEOUT = 200;

    public static void main(String[] args) throws IOException {
        ServerSocket proxy = new ServerSocket(PORT);

        while (true) {
            int len;
            HttpRequestHeader request;
            HttpResponseHeader response;
            byte[] buffer = new byte[BUFFER_SIZE];

            Socket client = proxy.accept();
            client.setSoTimeout(SOCKET_TIMEOUT);
            InputStream ClientToProxy = client.getInputStream();
            OutputStream ProxyToClient = client.getOutputStream();
            if ((len = ClientToProxy.read(buffer)) != -1) {
                request = new HttpRequestHeader(buffer, len);

                System.out.print(request);

                Socket server;
                if (request.getMethod().equals("CONNECT")) {
                    server = new Socket(request.getHeaders().get("Host"), HTTPS_PORT);
                    ProxyToClient.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                } else {
                    server = new Socket(request.getHeaders().get("Host"), HTTP_PORT);
                }

                server.setSoTimeout(SOCKET_TIMEOUT);
                InputStream ServerToProxy = server.getInputStream();
                OutputStream ProxyToServer = server.getOutputStream();

                ProxyToServer.write(buffer, 0, len);

                try {
                    len = ClientToProxy.read(buffer);
                    while (len != -1) {
                        ProxyToServer.write(buffer, 0, len);
                        len = ClientToProxy.read(buffer);
                    }
                } catch (SocketTimeoutException e) {

                }

                try {
                    len = ServerToProxy.read(buffer);
                    // response = new HttpResponseHeader(buffer, len);
                    // System.out.print(response);
                    while (len != -1) {
                        ProxyToClient.write(buffer, 0, len);
                        len = ServerToProxy.read(buffer);
                    }
                } catch (SocketTimeoutException e) {

                }
                server.close();
            }
            client.close();
        }
    }
}
