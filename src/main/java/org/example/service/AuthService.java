package org.example.service;

public interface AuthService {
    boolean auth(String login, String password);
    boolean register(String login, String password);
}
