package com.greenaura.repository;

import com.greenaura.dao.UserDAO;
import com.greenaura.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final UserDAO userDAO;

    public UserRepository(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean register(User user) {
        return userDAO.registerUser(user);
    }

    public User findByEmail(String email) {
        return userDAO.getUserByEmail(email);
    }

    public boolean emailExists(String email) {
        return userDAO.emailExists(email);
    }
}
