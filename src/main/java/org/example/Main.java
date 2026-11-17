package org.example;

public class Main {
    public static void main(String[] args) {
        int port = 1220;
        Server server = new Server(port);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}
