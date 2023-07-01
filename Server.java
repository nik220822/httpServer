package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// ОДНОПОТОЧНАЯ ВЕРСИЯ СЕРВЕРА С HANDLES
public class Server {
    static final int PORT = 9999;
    static final int TREADSNUMBER = 64;
    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private ConcurrentHashMap<String, Handler> handlers = new ConcurrentHashMap<>();

    void listen(int port) {
        final ExecutorService threadPool = Executors.newFixedThreadPool(TREADSNUMBER);
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    acceptRequest(socket);
                } catch (Exception e) {
                    throw new Exception(e);
                }
            }
        } catch (Exception ignored) {
        }
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
            Request request = new Request();
            request = request.parse(in);
            String key = request.getRequestLineMethod() + request.getRequestLinePath();
            Handler handler = handlers.get(key);
            handler.handle(request, out);

            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 NotFound\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
//                socket.close();
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
//                socket.close();
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

    void addHandler(String requestMethod, String path, Handler handler) {
        handlers.put(requestMethod + path, handler);
    }
}