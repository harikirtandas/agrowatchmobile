package com.example.agrowatchmobile.modelo;

// Cuerpo de POST /siembras/{id}/costos. Son los siete campos que exige costos.json en la API;
// la fecha va como "AAAA-MM-DD".
public class CostoPedido {
    private final String categoria;
    private final String descripcion;
    private final double cantidad;
    private final String unidad;
    private final double precioUnitario;
    private final String moneda;
    private final String fecha;

    public CostoPedido(String categoria, String descripcion, double cantidad, String unidad,
                       double precioUnitario, String moneda, String fecha) {
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.precioUnitario = precioUnitario;
        this.moneda = moneda;
        this.fecha = fecha;
    }
}
