package com.example.agrowatchmobile.modelo;

// La proyección de una siembra (GET /siembras/{id}/margen). La calcula la API en
// CalculoMargen.php; la app sólo la muestra. Se modelan nada más los campos que se usan.
public class Margen {
    private int siembraId;
    private double haSembradas;
    private double costoTotalUsd;
    private double costoPorHaUsd;
    private double ingresoEsperadoUsd;
    private double margenBrutoUsd;
    // null cuando no hay ingreso esperado (no se puede dividir por cero).
    private Double margenPct;
    private double rindeIndiferenciaKgHa;
    // "verde", "amarillo" o "rojo".
    private String semaforo;
    private double costoTotalArs;
    private double ingresoEsperadoArs;
    private double margenBrutoArs;

    public int getSiembraId() { return siembraId; }
    public double getHaSembradas() { return haSembradas; }
    public double getCostoTotalUsd() { return costoTotalUsd; }
    public double getCostoPorHaUsd() { return costoPorHaUsd; }
    public double getIngresoEsperadoUsd() { return ingresoEsperadoUsd; }
    public double getMargenBrutoUsd() { return margenBrutoUsd; }
    public Double getMargenPct() { return margenPct; }
    public double getRindeIndiferenciaKgHa() { return rindeIndiferenciaKgHa; }
    public String getSemaforo() { return semaforo; }
    public double getCostoTotalArs() { return costoTotalArs; }
    public double getIngresoEsperadoArs() { return ingresoEsperadoArs; }
    public double getMargenBrutoArs() { return margenBrutoArs; }
}
