package com.example.agrowatchmobile.api;

import android.content.Context;

import com.example.agrowatchmobile.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

// Un error de un pedido, ya traducido para mostrarlo. Hay dos casos (R3):
//  - no hubo respuesta (sin red, API caída): status SIN_RESPUESTA;
//  - la API respondió un error: su status y su mensaje.
public class ErrorApi {

    public static final int SIN_RESPUESTA = 0;

    private final int status;
    private final String mensaje;
    // Sólo en un 422: { "campo": "mensaje" } con lo que está mal de cada campo.
    private final Map<String, String> errores;

    private ErrorApi(int status, String mensaje, Map<String, String> errores) {
        this.status = status;
        this.mensaje = mensaje;
        this.errores = errores;
    }

    public static ErrorApi sinRespuesta(Context contexto) {
        return new ErrorApi(SIN_RESPUESTA, contexto.getString(R.string.error_sin_conexion), Collections.emptyMap());
    }

    // La API manda los errores como { "error": { "status", "mensaje", "errores"? } }
    // (index.php de agrowatch-api). Distinto del { "error": "texto" } de AppAlumnos.
    public static ErrorApi de(Response<?> response, Context contexto) {
        String mensaje = contexto.getString(R.string.error_sin_detalle);
        Map<String, String> errores = new HashMap<>();
        try (ResponseBody cuerpo = response.errorBody()) {
            if (cuerpo != null) {
                JsonObject error = JsonParser.parseString(cuerpo.string())
                        .getAsJsonObject()
                        .getAsJsonObject("error");
                if (error.has("mensaje")) {
                    mensaje = error.get("mensaje").getAsString();
                }
                if (error.has("errores") && error.get("errores").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> campo : error.getAsJsonObject("errores").entrySet()) {
                        errores.put(campo.getKey(), campo.getValue().getAsString());
                    }
                }
            }
        } catch (Exception e) {
            // Un cuerpo que no es el JSON esperado (por ejemplo, una página HTML del proveedor):
            // queda el mensaje genérico con el código.
        }
        return new ErrorApi(response.code(), mensaje, errores);
    }

    public int getStatus() {
        return status;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Map<String, String> getErrores() {
        return errores;
    }

    public boolean huboRespuesta() {
        return status != SIN_RESPUESTA;
    }

    // Lo que se muestra en pantalla: "Error 422: Datos inválidos." o el aviso de sin conexión.
    public String texto(Context contexto) {
        return huboRespuesta() ? contexto.getString(R.string.error_codigo, status, mensaje) : mensaje;
    }
}
