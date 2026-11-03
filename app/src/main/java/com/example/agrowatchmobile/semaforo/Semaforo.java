package com.example.agrowatchmobile.semaforo;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import com.example.agrowatchmobile.R;

// Las reglas del semáforo, iguales a las del frontend web (semaforo.js y dashboard.js) y a
// CalculoMargen::semaforo() de la API. Si cambia el umbral en la API, hay que cambiarlo acá.
public final class Semaforo {

    public static final String VERDE = "verde";
    public static final String AMARILLO = "amarillo";
    public static final String ROJO = "rojo";

    // Margen sobre ingreso a partir del cual el lote está "Bien" (CalculoMargen::UMBRAL_VERDE).
    public static final double UMBRAL_VERDE = 0.20;

    // Ángulo donde termina el arco rojo y empieza el amarillo (y simétrico del otro lado).
    public static final float CORTE = 25.7f;

    private Semaforo() {
    }

    // Misma lógica de 3 ramas que semaforoDeMargen() de la web. Se usa para el semáforo del
    // campo entero, que la API no calcula: cada lote trae el suyo, el agregado se arma acá.
    public static String deMargen(double margen, double ingreso) {
        if (margen <= 0) {
            return ROJO;
        }
        if (ingreso > 0 && margen >= UMBRAL_VERDE * ingreso) {
            return VERDE;
        }
        return AMARILLO;
    }

    // Ángulo de la aguja en grados (0 = arriba, -90 = izquierda, 90 = derecha), copiado de
    // anguloAguja() de la web:
    //   rojo:     margen de -100% a 0%  ->  -90° a -25,7°
    //   amarillo: de 0% a 20%           ->  -25,7° a 25,7°
    //   verde:    de 20% a 100%         ->  25,7° a 90°
    public static float anguloAguja(double pct) {
        double p = Math.max(-1, Math.min(1, pct)); // fuera de ±100% la aguja queda en el tope
        double angulo;
        if (p <= 0) {
            angulo = -90 + (p + 1) * (90 - CORTE);
        } else if (p < UMBRAL_VERDE) {
            angulo = -CORTE + (p / UMBRAL_VERDE) * (2 * CORTE);
        } else {
            angulo = CORTE + ((p - UMBRAL_VERDE) / (1 - UMBRAL_VERDE)) * (90 - CORTE);
        }
        return (float) angulo;
    }

    @ColorRes
    public static int color(String estado) {
        if (VERDE.equals(estado)) return R.color.semaforo_verde;
        if (AMARILLO.equals(estado)) return R.color.semaforo_amarillo;
        if (ROJO.equals(estado)) return R.color.semaforo_rojo;
        return R.color.semaforo_gris;
    }

    @StringRes
    public static int texto(String estado) {
        if (VERDE.equals(estado)) return R.string.estado_verde;
        if (AMARILLO.equals(estado)) return R.string.estado_amarillo;
        if (ROJO.equals(estado)) return R.string.estado_rojo;
        return R.string.estado_sin_datos;
    }

    // Para ordenar la lista: rojo primero, después amarillo, verde y sin datos al final.
    public static int severidad(String estado) {
        if (ROJO.equals(estado)) return 0;
        if (AMARILLO.equals(estado)) return 1;
        if (VERDE.equals(estado)) return 2;
        return 3;
    }
}
