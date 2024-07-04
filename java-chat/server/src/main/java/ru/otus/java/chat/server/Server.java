package ru.otus.java.chat.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                subscribe(new ClientHandler(this, socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        broadcastMessage("В чат зашел: " + clientHandler.getUsername());
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Из чата вышел: " + clientHandler.getUsername());
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized void personalMessage(ClientHandler from, String message) {
        String[] words = message.split(" ", 3);
        if (words.length != 3) {
            return;
        }

        String username = words[1];
        message = words[2];

        for (ClientHandler c : clients) {
            if (c.getUsername().equals(username)) {
                c.sendMessage("Личное сообщение от " + from.getUsername() + ": " + message);
                from.sendMessage("Личное сообщение для " + c.getUsername() + ": " + message);
                return;
            }
        }

        from.sendMessage("Личное сообщение не отправлено, пользователь не в сети");
    }
}
