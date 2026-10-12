package com.usermanagement.dao;

import com.usermanagement.model.User;
import java.util.List;

/**
 * Data Access Object Interface for User CRUD operations.
 */
public interface IUserDAO {
    User createUser(User user);
    List<User> getAllUsers();
    User getUserById(int id);
    boolean updateUser(User user);
    boolean deleteUser(int id);
}
