package com.example.agrowatchmobile.modelo;

// El usuario que devuelve POST /auth/login. Sólo se usa el nombre, para saludar.
public class Usuario {
    private int id;
    private String nombre;
    private String apellido;
    private String email;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
}
