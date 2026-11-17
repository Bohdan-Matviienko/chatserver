package org.example;

import org.example.enums.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket socket;
    private DataOutputStream out;
    private String userName;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            OutputStream sout = this.socket.getOutputStream();
            out = new DataOutputStream(sout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String line;
        String[] parts;
        try (DataInputStream in = new DataInputStream(this.socket.getInputStream())) {
            // Цикл 1: Авторизація та реєстрація (Лаб 07 + Лаб 08-09)
            while ((line = in.readUTF()) != null) {
                if (line.startsWith(Command.LOGIN.getValue())) {
                    parts = line.split(" ");
                    if (parts.length >= 3 && server.getAuthService().auth(parts[1], parts[2])) {
                        if (server.isUserOnline(parts[1])) {
                            sendMsg(Command.LOGIN_FAIL.getValue() + " Користувач вже в мережі");
                            continue;
                        }
                        sendMsg(Command.LOGIN_OK.getValue());
                        userName = parts[1];

                        // Надсилаємо останні повідомлення з історії БД (Індивідуальне завдання)
                        List<String> history = server.getMessageRepository().getRecentMessages(15);
                        if (!history.isEmpty()) {
                            sendMsg("--- Історія останніх повідомлень ---");
                            for (String oldMsg : history) {
                                sendMsg(oldMsg);
                            }
                            sendMsg("------------------------------------");
                        }

                        server.subscribe(this);
                        server.broadcastMsg("Система: " + userName + " приєднався до чату");
                        break;
                    } else {
                        sendMsg(Command.LOGIN_FAIL.getValue());
                    }
                } else if (line.startsWith(Command.REGISTER.getValue())) {
                    parts = line.split(" ");
                    if (parts.length >= 3 && server.getAuthService().register(parts[1], parts[2])) {
                        sendMsg(Command.REG_OK.getValue());
                    } else {
                        sendMsg(Command.REG_FAIL.getValue());
                    }
                } else if (line.startsWith(Command.FINISH.getValue())) {
                    closeConnection(in);
                    return;
                }
            }

            // Цикл 2: Основна робота з повідомленнями (Лаб 07 + Індивідуальне завдання Лаб 08-09)
            while ((line = in.readUTF()) != null) {
                if (line.startsWith(Command.FINISH.getValue())) {
                    server.unsubscribe(this);
                    server.broadcastMsg("Система: " + userName + " покинув чат");
                    closeConnection(in);
                    break;
                } else if (line.startsWith(Command.PRIVATE.getValue())) {
                    parts = line.split(" ");
                    if (parts.length >= 3) {
                        String targetUser = parts[1];
                        StringBuilder msg = new StringBuilder();
                        for (int i = 2; i < parts.length; i++) {
                            msg.append(parts[i]).append(" ");
                        }
                        String text = msg.toString().trim();
                        server.privateMsg(this, targetUser, text);
                    }
                } else if ("/clear".equalsIgnoreCase(line.trim()) && "user01".equals(userName)) {
                    // Функція модератора для user01
                    server.broadcastMsg("Система [Модератор " + userName + "]: Увага! Дотримуйтесь правил чату.");
                } else {
                    server.getMessageRepository().saveMessage(userName, line);
                    server.broadcastMsg(userName + ": " + line);
                }
            }
        } catch (IOException e) {
            if (userName != null) {
                server.unsubscribe(this);
                server.broadcastMsg("Система: " + userName + " від'єднався");
            }
        }
    }

    private void closeConnection(DataInputStream in) {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
