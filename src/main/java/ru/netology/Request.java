package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class Request {
    String requestLineMethod;
    String requestLinePath;
    String requestLineHTTPversion;
    ArrayList<String> headers;
    String body;

    public String getRequestLinePath() {
        return requestLinePath;
    }

    public String getRequestLineMethod() {
        return requestLineMethod;
    }

    Request() {
    }

    private Request(String requestLineMethod, String requestLinePath, String requestLineHTTPversion, ArrayList<String> headers, String body) {
        this.requestLineMethod = requestLineMethod;
        this.requestLinePath = requestLinePath;
        this.requestLineHTTPversion = requestLineHTTPversion;
        this.headers = headers;
        this.body = body;
    }

    public Request parse(BufferedReader in) throws IOException {
        final var parts = in.readLine().split(" ");
        if (parts.length != 3) {
            return null;
        }
        String header;
        while (!(header = in.readLine()).isEmpty()) {
            headers.add(header);
        }
        in.readLine();
        body = in.readLine();
        return new Request(parts[0], parts[1], parts[2], headers, body);
    }
}