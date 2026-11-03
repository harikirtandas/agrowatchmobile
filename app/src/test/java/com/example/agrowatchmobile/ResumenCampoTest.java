package com.example.agrowatchmobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.agrowatchmobile.modelo.Lote;
import com.example.agrowatchmobile.modelo.Margen;
import com.example.agrowatchmobile.modelo.Siembra;
import com.example.agrowatchmobile.panel.FilaLote;
import com.example.agrowatchmobile.panel.ResumenCampo;
import com.example.agrowatchmobile.semaforo.Semaforo;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// ResumenCampo tiene que dar los mismos números que resumenAgregado() de dashboard.js.
// Los modelos se arman desde JSON, igual que llegan de la API (con decimales como texto).
public class ResumenCampoTest {

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private FilaLote fila(double haLote, Double haSembradas, Double margenUsd, Double ingresoUsd, String semaforo) {
        Lote lote = gson.fromJson("{\"id\":1,\"nombre\":\"L\",\"ha_totales\":\"" + haLote + "\"}", Lote.class);
        Siembra siembra = haSembradas == null ? null
                : gson.fromJson("{\"id\":1,\"ha_sembradas\":\"" + haSembradas + "\"}", Siembra.class);
        FilaLote fila = new FilaLote(lote, siembra);
        if (margenUsd != null) {
            fila.setMargen(gson.fromJson("{\"ha_sembradas\":\"" + haSembradas + "\","
                    + "\"margen_bruto_usd\":\"" + margenUsd + "\","
                    + "\"ingreso_esperado_usd\":\"" + ingresoUsd + "\","
                    + "\"semaforo\":\"" + semaforo + "\"}", Margen.class));
        }
        return fila;
    }

    @Test
    public void sumaHectareasYPonderaElMargenPorHectarea() {
        List<FilaLote> filas = Arrays.asList(
                fila(100, 100.0, 10000.0, 40000.0, Semaforo.VERDE), // 100 US$/ha
                fila(300, 300.0, -3000.0, 60000.0, Semaforo.ROJO),  // -10 US$/ha
                fila(50, null, null, null, null));                 // sin siembra

        ResumenCampo r = ResumenCampo.de(filas);

        assertEquals(450, r.getHectareas(), 0.001);
        assertEquals(400, r.getHaSembradas(), 0.001);
        assertEquals(7000, r.getMargenTotalUsd(), 0.001);
        // Ponderado: 7000 / 400 ha = 17,5 (un promedio simple daría 45).
        assertEquals(17.5, r.getMargenHaUsd(), 0.001);
        assertEquals(7000.0 / 100000, r.getMargenPct(), 0.0001);
        assertEquals(Semaforo.AMARILLO, r.getEstado()); // 7% de margen
        assertEquals(1, r.getLotesEnAlerta());
        assertEquals(3, r.getLotes());
    }

    @Test
    public void sinMargenesNoHaySemaforo() {
        ResumenCampo r = ResumenCampo.de(Collections.singletonList(fila(80, 80.0, null, null, null)));

        assertNull(r.getMargenTotalUsd());
        assertNull(r.getMargenPct());
        assertNull(r.getEstado());
        assertEquals(80, r.getHaSembradas(), 0.001);
    }

    @Test
    public void ordenaRojoPrimeroYSinDatosAlFinal() {
        List<FilaLote> filas = new ArrayList<>(Arrays.asList(
                fila(10, null, null, null, null),
                fila(10, 10.0, 500.0, 1000.0, Semaforo.VERDE),
                fila(10, 10.0, -5.0, 1000.0, Semaforo.ROJO)));

        filas.sort(FilaLote.POR_SEVERIDAD);

        assertEquals(Semaforo.ROJO, filas.get(0).getEstado());
        assertEquals(Semaforo.VERDE, filas.get(1).getEstado());
        assertNull(filas.get(2).getEstado());
    }
}
