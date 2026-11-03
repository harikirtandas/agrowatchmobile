package com.example.agrowatchmobile.modelo;

// Una siembra (GET /siembras y GET /siembras/{id}). Trae embebidos los nombres del
// cultivo, el lote y la campaña, así no hace falta pedirlos aparte.
public class Siembra {
    private int id;
    private int campaniaId;
    private int loteId;
    private int cultivoId;
    private String cultivoNombre;
    private String loteNombre;
    private String campaniaNombre;
    private double haSembradas;
    private String fechaSiembra;
    private String variedad;
    private Double rindeEsperado;

    public int getId() { return id; }
    public int getCampaniaId() { return campaniaId; }
    public int getLoteId() { return loteId; }
    public int getCultivoId() { return cultivoId; }
    public String getCultivoNombre() { return cultivoNombre; }
    public String getLoteNombre() { return loteNombre; }
    public String getCampaniaNombre() { return campaniaNombre; }
    public double getHaSembradas() { return haSembradas; }
    public String getFechaSiembra() { return fechaSiembra; }
    public String getVariedad() { return variedad; }
    public Double getRindeEsperado() { return rindeEsperado; }
}
