package org.example;

import org.example.config.DatabaseManager;
import org.example.enums.Command;
import org.example.repository.MessageRepositorySqlite;
import org.example.service.AuthService;
import org.example.service.DbAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server implements Runnable {
    private final int port;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients;
    private final DatabaseManager databaseManager;
    private final AuthService authService;
    private final MessageRepositorySqlite messageRepository;

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Ініціалізація бази даних SQLite та сервісу авторизації (Лаб 08-09)
        this.databaseManager = new DatabaseManager();
        this.databaseManager.initializeDatabase();
        this.authService = new DbAuthService(databaseManager);
        this.messageRepository = new MessageRepositorySqlite(databaseManager);

        // Тестові дані (додаються, якщо їх ще немає в БД)
        authService.register("user01", "1111");
        authService.register("user02", "2222");
        authService.register("user03", "3333");
        authService.register("user04", "4444");
    }

    public AuthService getAuthService() {
        return authService;
    }

    public MessageRepositorySqlite getMessageRepository() {
        return messageRepository;
    }

    @Override
    public void run() {
        Socket socket;
        System.out.println("Сервер запущено на порту " + port);
        while (true) {
            try {
                System.out.println("Очікуємо клієнта...");
                socket = serverSocket.accept();
                System.out.println("Клієнт приєднався: " + socket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(this, socket);
                Thread thread = new Thread(clientHandler);
                thread.setDaemon(true);
                thread.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized boolean isUserOnline(String username) {
        return clients.stream().anyMatch(c -> username.equals(c.getUserName()));
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastUserList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Remove: " + clientHandler.getUserName());
        broadcastUserList();
    }

    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMsg(msg);
        }
    }

    public synchronized void privateMsg(ClientHandler sender, String receiverName, String text) {
        boolean found = false;
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUserName().equals(receiverName)) {
                clientHandler.sendMsg("[Приватно від " + sender.getUserName() + "]: " + text);
                found = true;
                break;
            }
        }
        if (found) {
            sender.sendMsg("[Приватно для " + receiverName + "]: " + text);
            System.out.println("Приватне повідомлення від " + sender.getUserName() + " до " + receiverName);
        } else {
            sender.sendMsg("Система: Користувача " + receiverName + " немає в мережі.");
        }
    }

    // Розсилка списку та кількості користувачів онлайн (Індивідуальне завдання Лаб 08-09)
    public synchronized void broadcastUserList() {
        String usersJoined = clients.stream()
                .map(ClientHandler::getUserName)
                .collect(Collectors.joining(" "));
        String cmd = Command.USER_LIST.getValue() + " " + usersJoined;
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMsg(cmd);
        }
    }
}
