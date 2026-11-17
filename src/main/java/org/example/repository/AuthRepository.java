package org.example.repository;

import org.example.entity.User;
import java.util.Optional;

public interface AuthRepository {
    void save(User user);
    Optional<User> findUserByUserName(String userName);
}
