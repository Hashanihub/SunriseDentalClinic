package com.sunrise.dental.service;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.User;

import java.util.List;

public interface UserService {

    User createUser(User user) throws ValidationException, DatabaseException;

    User getUserById(int userId) throws ResourceNotFoundException, DatabaseException;

    List<User> getAllUsers() throws DatabaseException;

    User updateUser(User user) throws ValidationException, ResourceNotFoundException, DatabaseException;

    boolean deactivateUser(int userId) throws ResourceNotFoundException, DatabaseException;

    boolean usernameExists(String username) throws DatabaseException;
}