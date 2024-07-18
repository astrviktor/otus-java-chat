package ru.otus.java.chat.server;

public interface AuthenticationProvider {
    void initialize();
    boolean authenticate(ClientHandler clientHandler, String login, String password);
    boolean registration(ClientHandler clientHandler, String login, String password, String username);
}
