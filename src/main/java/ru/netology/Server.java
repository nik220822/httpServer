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
    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    void start(int port, int threadsNumber) {
        final ExecutorService threadPool = Executors.newFixedThreadPool(threadsNumber);
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    threadPool.execute(() -> {
                                Socket socket;
                                try {
                                    socket = serverSocket.accept();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    acceptRequest(socket);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                } catch (Exception e) {
                    throw new Exception(e);
//                    threadPool.shutdown();
//                    serverSocket.close();
                } finally {
//                    threadPool.shutdown();
                }
            }
        } catch (Exception e) {
            threadPool.shutdown();
//            serverSocket.close();
        }
//        threadPool.shutdown();
    }

    void acceptRequest(Socket socket) throws Exception {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
// read only request line for simplicity
// must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                socket.close();
            }

            final var path = parts[1];
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

            // special case for classic
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
    }
}