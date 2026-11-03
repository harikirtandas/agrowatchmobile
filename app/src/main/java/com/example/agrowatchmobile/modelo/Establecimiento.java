package com.example.agrowatchmobile.modelo;

// Un campo del usuario (GET /establecimientos).
public class Establecimiento {
    private int id;
    private String nombre;
    private String localidad;
    private String provincia;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLocalidad() { return localidad; }
    public String getProvincia() { return provincia; }

    // El Spinner del panel usa toString() para mostrar cada opción.
    @Override
    public String toString() {
        return nombre;
    }
}
