package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    Socket socket;
    static final int TREADSNUMBER = 64;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    void listen(int port) throws Exception {
        final ExecutorService threadPool = Executors.newFixedThreadPool(TREADSNUMBER);
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try {
                    threadPool.execute(() -> {
                                try {
                                    acceptRequest(socket);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                } catch (Exception e) {
                    throw new Exception(e);
                }
            }
        }
    }

    void acceptRequest(Socket socket) throws Exception {
        try (
//                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            Request request = new Request();
            request = request.parse(in);
            String requestLineMethod = request.getRequestLineMethod();
            String requestLinePath = request.getRequestLinePath();
            Handler handler = handlers.get(requestLineMethod).get(requestLinePath);
            if (handler == null) {
                out.write((
                        "HTTP/1.1 404 The server can't handle your request\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                socket.close();
            }
            assert handler != null;
            handler.handle(request, out);
        }
    }

    public void addHandler(String requestLineMethod, String requestLinePath, Handler handler) {
        var methodHashMap = handlers.get(requestLineMethod);
        if (methodHashMap == null) {
            methodHashMap = new ConcurrentHashMap<>();
            handlers.put(requestLineMethod, methodHashMap);
        }
        if (!methodHashMap.containsKey(requestLinePath)) {
            methodHashMap.put(requestLinePath, handler);
        }
    }
}