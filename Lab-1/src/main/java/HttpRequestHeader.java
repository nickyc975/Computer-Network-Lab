import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestHeader {
    private final int HTTP_PORT = 80;
    private final int HTTPS_PORT = 443;

    private String method;
    private String URL;
    private String version;
    private Map<String, String> headers = new HashMap<>();

    public HttpRequestHeader(final byte[] request, final int length) {
        byte[] pattern = "\r\n\r\n".getBytes();
        int headerEnd = PatternFinder.find(pattern, request, pattern.length, length);
        String headerString = new String(request, 0, headerEnd, StandardCharsets.UTF_8);
        String[] headerArray = headerString.split("\r\n");

        String[] requestLine = headerArray[0].split("\\s", 3);
        method = requestLine[0];
        URL = requestLine[1];
        version = requestLine[2];
        for (int i = 1; i < headerArray.length; i++) {
            String[] headerLine = headerArray[i].split("\\s*:\\s*", 2);
            headers.put(headerLine[0], headerLine[1]);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getURL() {
        return URL;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHost() {
        return headers.get("Host").split(":")[0];
    }

    public int getPort() {
        String[] spliced = headers.get("Host").split(":");
        if (spliced.length == 2) {
            return Integer.parseInt(spliced[1]);
        } else {
            if (method.equals("CONNECT")) {
                return HTTPS_PORT;
            } else {
                return HTTP_PORT;
            }
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(method + " " + URL + " " + version + "\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(entry.getKey());
            builder.append(": ");
            builder.append(entry.getValue());
            builder.append("\r\n");
        }
        builder.append("\r\n");
        return builder.toString();
    }
}
