package com.example.agrowatchmobile.modelo;

// Respuesta de POST /auth/login: { "token", "expira", "usuario" }.
public class LoginRespuesta {
    private String token;
    private String expira;
    private Usuario usuario;

    public String getToken() { return token; }
    public String getExpira() { return expira; }
    public Usuario getUsuario() { return usuario; }
}
