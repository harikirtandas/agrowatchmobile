package com.example.agrowatchmobile.api;

import com.example.agrowatchmobile.modelo.Campania;
import com.example.agrowatchmobile.modelo.Costo;
import com.example.agrowatchmobile.modelo.CostoPedido;
import com.example.agrowatchmobile.modelo.Establecimiento;
import com.example.agrowatchmobile.modelo.LoginPedido;
import com.example.agrowatchmobile.modelo.LoginRespuesta;
import com.example.agrowatchmobile.modelo.Lote;
import com.example.agrowatchmobile.modelo.Margen;
import com.example.agrowatchmobile.modelo.Siembra;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Todos los endpoints de agrowatch-api que usa la app, en un solo lugar.
// Las rutas son relativas a ApiCliente.URL_BASE: van SIN barra inicial, si no Retrofit
// descarta el "/api/" de la URL base.
public interface AgroApi {

    @POST("auth/login")
    Call<LoginRespuesta> login(@Body LoginPedido pedido);

    // Responde 204 sin cuerpo: Void. El token va explícito porque la sesión local se borra
    // enseguida, antes de que el interceptor de ApiCliente llegue a leerlo.
    @POST("auth/logout")
    Call<Void> logout(@Header("Authorization") String autorizacion);

    @GET("establecimientos")
    Call<List<Establecimiento>> establecimientos();

    @GET("campanias")
    Call<List<Campania>> campanias();

    @GET("lotes")
    Call<List<Lote>> lotes(@Query("establecimiento_id") int establecimientoId);

    @GET("siembras")
    Call<List<Siembra>> siembras(@Query("campania_id") int campaniaId);

    @GET("siembras/{id}")
    Call<Siembra> siembra(@Path("id") int siembraId);

    @GET("siembras/{id}/margen")
    Call<Margen> margen(@Path("id") int siembraId);

    @GET("siembras/{id}/costos")
    Call<List<Costo>> costos(@Path("id") int siembraId);

    @POST("siembras/{id}/costos")
    Call<Costo> crearCosto(@Path("id") int siembraId, @Body CostoPedido pedido);
}
