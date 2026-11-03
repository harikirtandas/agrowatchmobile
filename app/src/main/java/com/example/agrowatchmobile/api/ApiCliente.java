package com.example.agrowatchmobile.api;

import android.content.Context;

import com.example.agrowatchmobile.sesion.Sesion;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Arma Retrofit una sola vez, como en AppAlumnos.
public final class ApiCliente {

    // La ÚNICA dirección de la API en toda la app (R7): https, con /api/ y la barra final.
    // La APK entregada tiene que apuntar al despliegue, nunca a localhost ni a 10.0.2.2.
    private static final String URL_BASE = "https://api-rest-agro.up.railway.app/api/";

    private static AgroApi api;

    private ApiCliente() {
    }

    public static synchronized AgroApi get(Context contexto) {
        if (api == null) {
            // getApplicationContext(): el cliente vive toda la app y no tiene que retener una Activity.
            Context app = contexto.getApplicationContext();

            // A diferencia de AppAlumnos, esta API pide un token en cada pedido. El interceptor
            // agrega "Authorization: Bearer <token>" a TODOS los pedidos, igual que api.js en la
            // web: es la API la que decide qué rutas son protegidas, no la app.
            OkHttpClient http = new OkHttpClient.Builder()
                    .addInterceptor(cadena -> {
                        String token = Sesion.token(app);
                        Request pedido = cadena.request();
                        if (token != null) {
                            pedido = pedido.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                        }
                        return cadena.proceed(pedido);
                    })
                    .build();

            // La API responde en snake_case (ha_sembradas) y los modelos están en camelCase
            // (haSembradas): esta política hace la traducción sin un @SerializedName por campo.
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASE)
                    .client(http)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            api = retrofit.create(AgroApi.class);
        }
        return api;
    }
}
