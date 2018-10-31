import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponseHeader {
    private String version;
    private String statusCode;
    private String statusPhrase;
    private Map<String, String> headers = new HashMap<>();
    private List<String> others = new ArrayList<>();

    public HttpResponseHeader(final byte[] request, final int length) {
        byte[] pattern = "\r\n\r\n".getBytes();
        int headerEnd = PatternFinder.find(pattern, request, pattern.length, length);
        String headerString = new String(request, 0, headerEnd, StandardCharsets.UTF_8);
        String[] headerArray = headerString.split("\r\n");

        String[] requestLine = headerArray[0].split("\\s", 3);
        version = requestLine[0];
        statusCode = requestLine[1];
        statusPhrase = requestLine[2];
        for (int i = 1; i < headerArray.length; i++) {
            String[] headerLine = headerArray[i].split("\\s*:\\s*", 1);
            if (headerLine.length > 1) {
                headers.put(headerLine[0], headerLine[1]);
            } else {
                others.add(headerLine[0]);
            }
        }
    }

    public String getVersion() {
        return version;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusPhrase() {
        return statusPhrase;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(version + " " + statusCode + " " + statusPhrase + "\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(entry.getKey());
            builder.append(": ");
            builder.append(entry.getValue());
            builder.append("\r\n");
        }
        for (String other: others) {
            builder.append(other);
            builder.append("\r\n");
        }
        builder.append("\r\n");
        return builder.toString();
    }
}
