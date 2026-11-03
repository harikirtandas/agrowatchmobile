package com.example.agrowatchmobile.panel;

import com.example.agrowatchmobile.semaforo.Semaforo;

import java.util.List;

// El resumen del campo entero, pasado a Java desde resumenAgregado() de dashboard.js.
// La API calcula el margen de cada lote; el del campo lo arma el cliente, así que tiene que
// dar lo mismo que en la web.
public class ResumenCampo {

    private final int lotes;
    private final int lotesEnAlerta;
    private final double hectareas;
    private final double haSembradas;
    private final Double margenTotalUsd;
    private final Double margenHaUsd;
    private final Double margenPct;
    private final String estado;

    private ResumenCampo(int lotes, int lotesEnAlerta, double hectareas, double haSembradas,
                         Double margenTotalUsd, Double margenHaUsd, Double margenPct, String estado) {
        this.lotes = lotes;
        this.lotesEnAlerta = lotesEnAlerta;
        this.hectareas = hectareas;
        this.haSembradas = haSembradas;
        this.margenTotalUsd = margenTotalUsd;
        this.margenHaUsd = margenHaUsd;
        this.margenPct = margenPct;
        this.estado = estado;
    }

    public static ResumenCampo de(List<FilaLote> filas) {
        int enAlerta = 0;
        int conMargen = 0;
        double hectareas = 0;
        double haSembradas = 0;
        double haSembradasConMargen = 0;
        double margenTotal = 0;
        double ingresoTotal = 0;

        for (FilaLote fila : filas) {
            hectareas += fila.getLote().getHaTotales();
            if (fila.getSiembra() != null) {
                haSembradas += fila.getSiembra().getHaSembradas();
            }
            if (fila.getMargen() != null) {
                conMargen++;
                haSembradasConMargen += fila.getMargen().getHaSembradas();
                margenTotal += fila.getMargen().getMargenBrutoUsd();
                ingresoTotal += fila.getMargen().getIngresoEsperadoUsd();
            }
            if (Semaforo.ROJO.equals(fila.getEstado())) {
                enAlerta++;
            }
        }

        // Margen por hectárea PONDERADO: total de margen ÷ total de hectáreas sembradas con
        // margen. Un promedio simple pesaría igual un lote de 10 ha que uno de 500.
        boolean hayMargen = conMargen > 0;
        return new ResumenCampo(
                filas.size(),
                enAlerta,
                hectareas,
                haSembradas,
                hayMargen ? margenTotal : null,
                haSembradasConMargen > 0 ? margenTotal / haSembradasConMargen : null,
                ingresoTotal > 0 ? margenTotal / ingresoTotal : null,
                hayMargen ? Semaforo.deMargen(margenTotal, ingresoTotal) : null);
    }

    public int getLotes() { return lotes; }
    public int getLotesEnAlerta() { return lotesEnAlerta; }
    public double getHectareas() { return hectareas; }
    public double getHaSembradas() { return haSembradas; }
    public Double getMargenTotalUsd() { return margenTotalUsd; }
    public Double getMargenHaUsd() { return margenHaUsd; }
    public Double getMargenPct() { return margenPct; }
    public String getEstado() { return estado; }
}
