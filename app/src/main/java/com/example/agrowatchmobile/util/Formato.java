package com.example.agrowatchmobile.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Números y fechas con el formato argentino que usa la web (1.234,5 y 20/09/2026).
// Fechas con SimpleDateFormat y no con java.time: java.time pide API 26 y la app arranca en 24.
public final class Formato {

    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    private static final String PATRON_PANTALLA = "dd/MM/yyyy";
    private static final String PATRON_API = "yyyy-MM-dd";

    private Formato() {
    }

    public static String numero(double valor, int decimales) {
        NumberFormat formato = NumberFormat.getNumberInstance(ARGENTINA);
        formato.setMinimumFractionDigits(decimales);
        formato.setMaximumFractionDigits(decimales);
        return formato.format(valor);
    }

    // "2026-09-20" (como viene de la API) -> "20/09/2026". Si no se puede leer, se deja como vino.
    public static String fecha(String iso) {
        Calendar dia = leer(iso, PATRON_API);
        return dia == null ? iso : fechaPantalla(dia);
    }

    public static String fechaPantalla(Calendar dia) {
        return formateador(PATRON_PANTALLA).format(dia.getTime());
    }

    public static String fechaApi(Calendar dia) {
        return formateador(PATRON_API).format(dia.getTime());
    }

    // "20/09/2026" -> el día, o null si no es una fecha válida.
    public static Calendar leerFechaPantalla(String texto) {
        return leer(texto, PATRON_PANTALLA);
    }

    private static Calendar leer(String texto, String patron) {
        if (texto == null) {
            return null;
        }
        try {
            Calendar dia = Calendar.getInstance();
            dia.setTime(formateador(patron).parse(texto));
            return dia;
        } catch (ParseException e) {
            return null;
        }
    }

    // Uno nuevo por uso: SimpleDateFormat no se puede compartir entre hilos.
    private static SimpleDateFormat formateador(String patron) {
        SimpleDateFormat formato = new SimpleDateFormat(patron, ARGENTINA);
        formato.setLenient(false); // "31/02/2026" no es una fecha, no la corre al 3 de marzo
        return formato;
    }
}
