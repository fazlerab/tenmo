package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.TEUser;
import com.techelevator.tenmo.model.User;

import java.util.List;

public interface UserDao {

    List<User> findAll();

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);

    List<TEUser> getOtherUsers(Long myId);
}
