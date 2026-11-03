package com.example.agrowatchmobile.panel;

import com.example.agrowatchmobile.modelo.Lote;
import com.example.agrowatchmobile.modelo.Margen;
import com.example.agrowatchmobile.modelo.Siembra;
import com.example.agrowatchmobile.semaforo.Semaforo;

import java.util.Comparator;

// Un lote del panel con lo que se sabe de él en la campaña: su siembra (si tiene) y su margen
// (si se pudo calcular). Es la misma "fila" que arma cargarEstablecimiento() en dashboard.js.
public class FilaLote {

    // rojo primero, después amarillo, verde y sin datos; dentro de cada color, el peor margen primero.
    public static final Comparator<FilaLote> POR_SEVERIDAD = Comparator
            .comparingInt((FilaLote f) -> Semaforo.severidad(f.getEstado()))
            .thenComparingDouble(FilaLote::margenPctParaOrdenar);

    private final Lote lote;
    private final Siembra siembra;
    private Margen margen;
    // Por qué no hay margen (por ejemplo, el 422 "Falta cargar costos"), para mostrarlo.
    private String motivo;

    public FilaLote(Lote lote, Siembra siembra) {
        this.lote = lote;
        this.siembra = siembra;
    }

    public Lote getLote() { return lote; }
    public Siembra getSiembra() { return siembra; }
    public Margen getMargen() { return margen; }
    public String getMotivo() { return motivo; }

    public void setMargen(Margen margen) { this.margen = margen; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    // "verde", "amarillo", "rojo", o null sin margen.
    public String getEstado() {
        return margen == null ? null : margen.getSemaforo();
    }

    // Margen en US$ por hectárea sembrada, o null.
    public Double getMargenUsdPorHa() {
        if (margen == null || margen.getHaSembradas() <= 0) {
            return null;
        }
        return margen.getMargenBrutoUsd() / margen.getHaSembradas();
    }

    private double margenPctParaOrdenar() {
        if (margen == null || margen.getMargenPct() == null) {
            return Double.NEGATIVE_INFINITY;
        }
        return margen.getMargenPct();
    }
}
