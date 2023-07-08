package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
//import org.apache.http.client.utils.URLEncodedUtils;

public class Request {
    private String requestLineMethod;
    private String requestLinePath;
    String requestLineHTTPversion;
    ArrayList<String> headers;
    String body;
    BufferedOutputStream out;

    public BufferedOutputStream getOut() {
        return out;
    }

    public String getRequestLinePath() {
        return requestLinePath;
    }

    public String getRequestLineMethod() {
        return requestLineMethod;
    }

    Request() {
    }

    public Request parse(BufferedInputStream in) throws IOException {
        final var bufferLimit = 4096;

        in.mark(bufferLimit);
        final var buffer = new byte[bufferLimit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            badRequest(out);
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            badRequest(out);
        }

        final var allowedMethods = List.of("GET", "POST");

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            badRequest(out);
        }
        System.out.println(method);

        final var path = requestLine[1];
        if (!path.startsWith("/")) {
            badRequest(out);
        }
        System.out.println(path);
        requestLineMethod = method;
        requestLinePath = path;
        return this;
    }

    NameValuePair getQueryParam(String name) throws URISyntaxException {
        List<NameValuePair> queryParams = getQueryParams();
        for (NameValuePair queryParam : queryParams
        ) {
            if (Objects.equals(queryParam.getName(), name)) {
                return queryParam;
            }
        }
        return null;
    }

    List<NameValuePair> getQueryParams() throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(requestLinePath);
        return uriBuilder.getQueryParams();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}