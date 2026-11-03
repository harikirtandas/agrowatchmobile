package com.example.agrowatchmobile.sesion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.agrowatchmobile.R;
import com.example.agrowatchmobile.login.LoginActivity;

// Lo que la app recuerda entre aperturas, en SharedPreferences: el token de la API, el
// nombre del usuario y el último campo elegido en el panel (como "establecimientoActual"
// en el localStorage de la web).
public final class Sesion {

    private static final String ARCHIVO = "sesion";
    private static final String CLAVE_TOKEN = "token";
    private static final String CLAVE_NOMBRE = "nombre";
    private static final String CLAVE_CAMPO = "campo";

    private Sesion() {
    }

    private static SharedPreferences preferencias(Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public static void guardar(Context contexto, String token, String nombre) {
        preferencias(contexto).edit()
                .putString(CLAVE_TOKEN, token)
                .putString(CLAVE_NOMBRE, nombre)
                .apply();
    }

    public static String token(Context contexto) {
        return preferencias(contexto).getString(CLAVE_TOKEN, null);
    }

    public static boolean iniciada(Context contexto) {
        return token(contexto) != null;
    }

    public static String nombre(Context contexto) {
        return preferencias(contexto).getString(CLAVE_NOMBRE, "");
    }

    public static int campoElegido(Context contexto) {
        return preferencias(contexto).getInt(CLAVE_CAMPO, -1);
    }

    public static void elegirCampo(Context contexto, int establecimientoId) {
        preferencias(contexto).edit().putInt(CLAVE_CAMPO, establecimientoId).apply();
    }

    public static void cerrar(Context contexto) {
        preferencias(contexto).edit().clear().apply();
    }

    // Borra la sesión y abre el ingreso vaciando la pila: Atrás no vuelve a una pantalla
    // que necesitaba la sesión.
    public static void irAlIngreso(Activity desde, boolean porVencimiento) {
        cerrar(desde);
        if (porVencimiento) {
            Toast.makeText(desde, R.string.sesion_vencida, Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent(desde, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        desde.startActivity(intent);
        desde.finish();
    }
}
