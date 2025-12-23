/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.repository.UserRepository;
import java.util.List;

/**
 *
 * @author David
 */
public class UserService {
    private final UserRepository repo;

    
    public UserService() {
        this.repo = new UserRepository();
    }

    public List<User> execute(String filter) {
        return repo.findAll(filter);
    }
}
