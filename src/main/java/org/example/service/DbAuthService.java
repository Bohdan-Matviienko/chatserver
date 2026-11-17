package org.example.service;

import org.example.config.DatabaseManager;
import org.example.entity.User;
import org.example.repository.AuthRepository;
import org.example.repository.AuthRepositorySqlite;
import org.example.utils.PasswordUtils;

import java.util.Optional;

public class DbAuthService implements AuthService {
    private final AuthRepository authRepository;

    public DbAuthService(DatabaseManager databaseManager) {
        this.authRepository = new AuthRepositorySqlite(databaseManager);
    }

    @Override
    public boolean auth(String login, String password) {
        Optional<User> optionalUser = authRepository.findUserByUserName(login);
        if (optionalUser.isPresent()) {
            return PasswordUtils.checkPassword(password, optionalUser.get().getPassword());
        } else {
            return false;
        }
    }

    @Override
    public boolean register(String login, String password) {
        if (authRepository.findUserByUserName(login).isPresent()) {
            return false;
        }
        User user = new User(login, PasswordUtils.hashPassword(password));
        authRepository.save(user);
        return true;
    }
}
