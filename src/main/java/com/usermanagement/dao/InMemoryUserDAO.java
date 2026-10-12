package com.usermanagement.dao;

import com.usermanagement.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-Memory Data Access Object implementation for fast prototype demonstration
 * without needing an external database setup.
 */
public class InMemoryUserDAO implements IUserDAO {
    private static final Map<Integer, User> userMap = new ConcurrentHashMap<>();
    private static final AtomicInteger idGenerator = new AtomicInteger(100);

    public InMemoryUserDAO() {
        // Pre-populate with initial sample users if empty
        if (userMap.isEmpty()) {
            addUserWithId(new User("Alice Smith", "alice.smith@university.edu", "Admin", "Active"));
            addUserWithId(new User("Bob Johnson", "bob.johnson@student.edu", "Student", "Active"));
            addUserWithId(new User("Carol Williams", "carol.williams@faculty.edu", "Faculty", "Active"));
            addUserWithId(new User("David Brown", "david.brown@student.edu", "Student", "Inactive"));
        }
    }

    private void addUserWithId(User user) {
        int newId = idGenerator.getAndIncrement();
        user.setId(newId);
        userMap.put(newId, user);
    }

    @Override
    public synchronized User createUser(User user) {
        int newId = idGenerator.getAndIncrement();
        user.setId(newId);
        userMap.put(newId, user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public User getUserById(int id) {
        return userMap.get(id);
    }

    @Override
    public synchronized boolean updateUser(User user) {
        if (userMap.containsKey(user.getId())) {
            userMap.put(user.getId(), user);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean deleteUser(int id) {
        return userMap.remove(id) != null;
    }
}
