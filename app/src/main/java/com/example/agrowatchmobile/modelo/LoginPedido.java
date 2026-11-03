package com.example.agrowatchmobile.modelo;

// Cuerpo de POST /auth/login.
public class LoginPedido {
    private final String email;
    private final String password;

    public LoginPedido(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
