package com.example.agrowatchmobile;

import static org.junit.Assert.assertEquals;

import com.example.agrowatchmobile.semaforo.Semaforo;

import org.junit.Test;

// Las reglas del semáforo tienen que dar lo mismo que semaforoDeMargen() y anguloAguja()
// de la web.
public class SemaforoTest {

    @Test
    public void margenNegativoOCeroEsRojo() {
        assertEquals(Semaforo.ROJO, Semaforo.deMargen(-10, 100));
        assertEquals(Semaforo.ROJO, Semaforo.deMargen(0, 100));
    }

    @Test
    public void margenDesdeElVeintePorCientoEsVerde() {
        assertEquals(Semaforo.VERDE, Semaforo.deMargen(20, 100));
        assertEquals(Semaforo.VERDE, Semaforo.deMargen(90, 100));
    }

    @Test
    public void margenPositivoPeroBajoEsAmarillo() {
        assertEquals(Semaforo.AMARILLO, Semaforo.deMargen(19.99, 100));
        // Con margen pero sin ingreso no puede ser verde.
        assertEquals(Semaforo.AMARILLO, Semaforo.deMargen(10, 0));
    }

    @Test
    public void laAgujaVaDeTopeATope() {
        assertEquals(-90f, Semaforo.anguloAguja(-1), 0.01f);
        assertEquals(-90f, Semaforo.anguloAguja(-5), 0.01f); // fuera de rango queda en el tope
        assertEquals(90f, Semaforo.anguloAguja(1), 0.01f);
    }

    @Test
    public void laAgujaCaeEnLosCortesDeColor() {
        assertEquals(-Semaforo.CORTE, Semaforo.anguloAguja(0), 0.01f);
        assertEquals(Semaforo.CORTE, Semaforo.anguloAguja(0.2), 0.01f);
        assertEquals(0f, Semaforo.anguloAguja(0.1), 0.01f);
    }
}
