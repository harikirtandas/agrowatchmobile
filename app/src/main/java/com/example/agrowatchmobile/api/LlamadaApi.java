package com.example.agrowatchmobile.api;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.agrowatchmobile.sesion.Sesion;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// El Callback de Retrofit con las decisiones de siempre ya tomadas, para no repetirlas en
// cada pantalla:
//  - onResponse con 2xx -> alRecibir(cuerpo);
//  - onResponse con error -> alFallar() con el mensaje de la API; un 401 cierra la sesión;
//  - onFailure (no hubo respuesta) -> alFallar() con "Sin conexión".
// Si la Activity ya se cerró (por ejemplo, se giró la pantalla), la respuesta se ignora:
// la Activity nueva vuelve a pedir sus datos en onCreate.
public abstract class LlamadaApi<T> implements Callback<T> {

    private static final String TAG = "API";

    private final Context contexto;

    protected LlamadaApi(Context contexto) {
        this.contexto = contexto;
    }

    protected abstract void alRecibir(T cuerpo);

    protected abstract void alFallar(ErrorApi error);

    // 401: el token venció o lo revocaron. Por defecto se borra la sesión y se vuelve al ingreso.
    protected void alVencerSesion(ErrorApi error) {
        if (contexto instanceof Activity) {
            Sesion.irAlIngreso((Activity) contexto, true);
        }
    }

    @Override
    public final void onResponse(Call<T> call, Response<T> response) {
        if (pantallaCerrada()) {
            return;
        }
        if (response.isSuccessful()) {
            alRecibir(response.body());
            return;
        }
        ErrorApi error = ErrorApi.de(response, contexto);
        Log.w(TAG, call.request().url() + " -> " + error.getStatus() + " " + error.getMensaje());
        if (error.getStatus() == 401) {
            alVencerSesion(error);
        } else {
            alFallar(error);
        }
    }

    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        Log.e(TAG, "Sin respuesta de " + call.request().url(), t);
        if (pantallaCerrada()) {
            return;
        }
        alFallar(ErrorApi.sinRespuesta(contexto));
    }

    private boolean pantallaCerrada() {
        return contexto instanceof Activity && ((Activity) contexto).isDestroyed();
    }
}
