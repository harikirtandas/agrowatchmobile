package com.example.agrowatchmobile.modelo;

// Un lote de un campo (GET /lotes?establecimiento_id=X).
// Los decimales llegan como texto ("120.50"); Gson los convierte a double igual.
public class Lote {
    private int id;
    private int establecimientoId;
    private String nombre;
    private double haTotales;
    private double haEfectivas;

    public int getId() { return id; }
    public int getEstablecimientoId() { return establecimientoId; }
    public String getNombre() { return nombre; }
    public double getHaTotales() { return haTotales; }
    public double getHaEfectivas() { return haEfectivas; }
}
