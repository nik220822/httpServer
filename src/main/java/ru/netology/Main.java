package ru.netology;

public class Main {
    static final int PORT = 9999;
    static final int TREADSNUMBER = 64;

    public static void main(String[] args) {
        (new Server()).start(PORT, TREADSNUMBER);
    }
}
