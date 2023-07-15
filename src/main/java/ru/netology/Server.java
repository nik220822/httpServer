package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int port;
    private int threadsNumber;
    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    Server(int port, int threadsNumber) {
        this.port = port;
        this.threadsNumber = threadsNumber;
    }

    void start() {
        try (final var serverSocket = new ServerSocket(port)) {
            ExecutorService threadPool = Executors.newFixedThreadPool(threadsNumber);

            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    threadPool.submit(() -> {
                        try {
                            acceptRequest(socket);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void acceptRequest(Socket socket) throws Exception {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");
            final var path = parts[1];

            if (parts.length != 3) {
                socket.close();
            }
            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 NotFound\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                socket.close();
            }
            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            if (path.equals("/classic.html")) {
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
                socket.close();
            }

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
    }
}