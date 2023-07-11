package ru.netology;

public class Main {

    public static void main(String[] args) {
        int port = 9999;
        int threadsNumber = 64;
        (new Server(port, threadsNumber)).start();
    }
}