package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        final var server = new Server();
        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                String content = "GET Handler was used";
                responseStream.write(("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + "text/plain" + "\r\n" +
                        "Content-Length: " + content.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n").getBytes());
                responseStream.write(content.getBytes());
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                String content = "POST Handler was used";
                responseStream.write(("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + "text/plain" + "\r\n" +
                        "Content-Length: " + content.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n").getBytes());
                responseStream.write(content.getBytes());
            }
        });
        server.listen(9999);
    }
}
