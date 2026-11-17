package org.example.service;

import org.example.utils.PasswordUtils;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {
    private final List<User> users;

    private static class User {
        String login;
        String password;

        public User(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }

    public SimpleAuthService() {
        users = new ArrayList<>();
        users.add(new User("user01", PasswordUtils.hashPassword("1111")));
        users.add(new User("user02", PasswordUtils.hashPassword("2222")));
        users.add(new User("user03", PasswordUtils.hashPassword("3333")));
        users.add(new User("user04", PasswordUtils.hashPassword("4444")));
    }

    @Override
    public boolean auth(String login, String password) {
        for (User user : users) {
            if (user.login.equals(login) && PasswordUtils.checkPassword(password, user.password)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean register(String login, String password) {
        for (User user : users) {
            if (user.login.equals(login)) {
                return false;
            }
        }
        users.add(new User(login, PasswordUtils.hashPassword(password)));
        return true;
    }
}
