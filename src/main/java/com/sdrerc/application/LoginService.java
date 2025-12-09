/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.repository.UserRepository;
/**
 *
 * @author David
 */
public class LoginService {
    private final UserRepository repository;

    public LoginService() {
        this.repository = new UserRepository();
    }

    public User login(String username, String password) throws Exception {
        if (username.isBlank() || password.isBlank()) {
            throw new Exception("Debe ingresar usuario y contraseña.");
        }

        User user = repository.login(username, password);

        if (user == null) {
            throw new Exception("Credenciales incorrectas.");
        }

        return user;
    }
}
