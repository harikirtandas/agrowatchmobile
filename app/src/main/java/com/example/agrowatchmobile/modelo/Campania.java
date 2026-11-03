package com.example.agrowatchmobile.modelo;

// Una campaña (GET /campanias). "cerrada" llega como 0 o 1 desde MySQL.
public class Campania {
    private int id;
    private String nombre;
    private String fechaInicio;
    private String fechaFin;
    private int cerrada;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public boolean isCerrada() { return cerrada != 0; }
}
