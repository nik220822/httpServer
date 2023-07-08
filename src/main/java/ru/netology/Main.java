package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public static void main(String[] args) throws Exception {
        final var server = new Server();
        server.addHandler("GET", "/classic.html", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream out) throws IOException {
                String requestLinePath = request.getRequestLinePath();
                final var filePath = Path.of(".", "serverData", requestLinePath);
                final var mimeType = Files.probeContentType(filePath);
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
//                socket.close();
            }
        });
        for (String validPath :
                validPaths) {
            server.addHandler("GET", validPath, new Handler() {
                @Override
                public void handle(Request request, BufferedOutputStream out) throws IOException {
                    String requestLinePath = request.getRequestLinePath();
                    final var filePath = Path.of(".", "serverData", requestLinePath);
                    final var mimeType = Files.probeContentType(filePath);
                    final var length = Files.size(filePath);
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, out);
                    out.flush();
                }
            });
        }
        server.addHandler("POST", "/messages", new Handler() {
            @Override
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
