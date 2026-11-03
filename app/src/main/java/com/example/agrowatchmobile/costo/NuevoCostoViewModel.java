package com.example.agrowatchmobile.costo;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.agrowatchmobile.api.ApiCliente;
import com.example.agrowatchmobile.api.ErrorApi;
import com.example.agrowatchmobile.api.LlamadaApi;
import com.example.agrowatchmobile.modelo.Costo;
import com.example.agrowatchmobile.modelo.CostoPedido;

// El POST del costo vive acá y no en la Activity. Al girar la pantalla Android destruye la
// Activity y crea otra, pero el ViewModel sobrevive: el pedido en curso no se pierde, la
// Activity nueva ve que se está guardando (botón deshabilitado) y no se puede mandar dos
// veces el mismo costo (R6). En las pantallas que sólo leen no hace falta: ahí se vuelve a pedir.
public class NuevoCostoViewModel extends AndroidViewModel {

    public enum Estado { LIBRE, GUARDANDO, GUARDADO, ERROR }

    private final MutableLiveData<Estado> estado = new MutableLiveData<>(Estado.LIBRE);
    private ErrorApi ultimoError;

    public NuevoCostoViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Estado> getEstado() {
        return estado;
    }

    public ErrorApi getUltimoError() {
        return ultimoError;
    }

    public void guardar(int siembraId, CostoPedido pedido) {
        if (estado.getValue() == Estado.GUARDANDO) {
            return; // ya hay uno en camino
        }
        estado.setValue(Estado.GUARDANDO);
        ApiCliente.get(getApplication()).crearCosto(siembraId, pedido)
                .enqueue(new LlamadaApi<Costo>(getApplication()) {
                    @Override
                    protected void alRecibir(Costo creado) {
                        estado.setValue(Estado.GUARDADO);
                    }

                    @Override
                    protected void alFallar(ErrorApi error) {
                        ultimoError = error;
                        estado.setValue(Estado.ERROR);
                    }

                    // El ViewModel no puede abrir pantallas: deja el 401 como error y la
                    // Activity decide (vuelve al ingreso).
                    @Override
                    protected void alVencerSesion(ErrorApi error) {
                        alFallar(error);
                    }
                });
    }
}
