package ru.otus.java.chat.server;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
    private class User {
        private String login;
        private String password;
        private String username;
        private Role role;

        public User(String login, String password, String username, Role role) {
            this.login = login;
            this.password = password;
            this.username = username;
            this.role = role;
        }
    }

    private Server server;
    private List<User> users;

    public InMemoryAuthenticationProvider(Server server) {
        this.server = server;
        this.users = new ArrayList<>();
        this.users.add(new User("login1", "pass1", "bob", Role.ADMIN));
        this.users.add(new User("login2", "pass2", "user2", Role.USER));
        this.users.add(new User("login3", "pass3", "user3", Role.USER));
    }

    @Override
    public void initialize() {
        System.out.println("Сервис аутентификации запущен: In-Memory режим");
    }

    private User getUserByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u;
            }
        }
        return null;
    }

    private boolean isLoginAlreadyExist(String login) {
        for (User u : users) {
            if (u.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsernameAlreadyExist(String username) {
        for (User u : users) {
            if (u.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean authenticate(ClientHandler clientHandler, String login, String password) {
        User authUser = getUserByLoginAndPassword(login, password);
        if (authUser == null) {
            clientHandler.sendMessage("Некорретный логин/пароль");
            return false;
        }
        if (server.isUsernameBusy(authUser.username)) {
            clientHandler.sendMessage("Указанная учетная запись уже занята");
            return false;
        }
        clientHandler.setUser(authUser.username, authUser.role);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/authok " + authUser.username);
        return true;
    }

    @Override
    public boolean registration(ClientHandler clientHandler, String login, String password, String username) {
        if (login.trim().length() < 3 || password.trim().length() < 6 || username.trim().length() < 1) {
            clientHandler.sendMessage("Логин 3+ символа, Пароль 6+ символов, Имя пользователя 1+ символ");
            return false;
        }
        if (isLoginAlreadyExist(login)) {
            clientHandler.sendMessage("Указанный логин уже занят");
            return false;
        }
        if (isUsernameAlreadyExist(username)) {
            clientHandler.sendMessage("Указанное имя пользователя уже занято");
            return false;
        }
        User user = new User(login, password, username, Role.USER);
        users.add(user);
        clientHandler.setUser(user.username, user.role);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/regok " + username);
        return true;
    }
}