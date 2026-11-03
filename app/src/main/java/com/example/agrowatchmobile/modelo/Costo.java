package com.example.agrowatchmobile.modelo;

// Un costo cargado en una siembra (GET /siembras/{id}/costos y respuesta del POST).
public class Costo {
    private int id;
    private int siembraId;
    private String categoria;
    private String descripcion;
    private double cantidad;
    private String unidad;
    private double precioUnitario;
    private String moneda;
    private String fecha;

    public int getId() { return id; }
    public int getSiembraId() { return siembraId; }
    public String getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion; }
    public double getCantidad() { return cantidad; }
    public String getUnidad() { return unidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public String getMoneda() { return moneda; }
    public String getFecha() { return fecha; }

    public double getImporte() {
        return cantidad * precioUnitario;
    }
}
