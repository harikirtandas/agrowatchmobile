package com.example.agrowatchmobile.panel;

import android.content.Context;

import com.example.agrowatchmobile.api.AgroApi;
import com.example.agrowatchmobile.api.ApiCliente;
import com.example.agrowatchmobile.api.ErrorApi;
import com.example.agrowatchmobile.api.LlamadaApi;
import com.example.agrowatchmobile.modelo.Campania;
import com.example.agrowatchmobile.modelo.Establecimiento;
import com.example.agrowatchmobile.modelo.Lote;
import com.example.agrowatchmobile.modelo.Margen;
import com.example.agrowatchmobile.modelo.Siembra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Los pedidos que necesita el panel, en orden. Se separan de PanelActivity porque son una
// cadena de callbacks (cada pedido depende del anterior) y la Activity queda sólo con la pantalla.
//
//   cargarCampos():  GET /establecimientos  ->  GET /campanias
//   cargarLotes():   GET /lotes?establecimiento_id  ->  GET /siembras?campania_id
//                    ->  GET /siembras/{id}/margen por cada lote con siembra (en paralelo)
public class CargaPanel {

    public interface Oyente {
        // campania es null si no hay ninguna abierta.
        void alCargarCampos(List<Establecimiento> campos, Campania campania);

        void alCargarLotes(List<FilaLote> filas);

        void alFallar(ErrorApi error);
    }

    private final Context contexto;
    private final AgroApi api;
    private final Oyente oyente;
    private Campania campania;
    // Si se cambia de campo antes de que termine la carga anterior, la vieja se descarta
    // (lo mismo que "cargaActual" en dashboard.js).
    private int cargaActual = 0;

    public CargaPanel(Context contexto, Oyente oyente) {
        this.contexto = contexto;
        this.api = ApiCliente.get(contexto);
        this.oyente = oyente;
    }

    public void cargarCampos() {
        api.establecimientos().enqueue(new LlamadaApi<List<Establecimiento>>(contexto) {
            @Override
            protected void alRecibir(List<Establecimiento> campos) {
                api.campanias().enqueue(new LlamadaApi<List<Campania>>(contexto) {
                    @Override
                    protected void alRecibir(List<Campania> campanias) {
                        campania = campaniaVigente(campanias);
                        oyente.alCargarCampos(campos, campania);
                    }

                    @Override
                    protected void alFallar(ErrorApi error) {
                        oyente.alFallar(error);
                    }
                });
            }

            @Override
            protected void alFallar(ErrorApi error) {
                oyente.alFallar(error);
            }
        });
    }

    public void cargarLotes(int establecimientoId) {
        int estaCarga = ++cargaActual;
        api.lotes(establecimientoId).enqueue(new LlamadaApi<List<Lote>>(contexto) {
            @Override
            protected void alRecibir(List<Lote> lotes) {
                if (estaCarga != cargaActual) return;
                if (campania == null) {
                    // Sin campaña abierta no hay siembras: los lotes se ven grises.
                    entregar(estaCarga, armarFilas(lotes, Collections.emptyList()));
                    return;
                }
                api.siembras(campania.getId()).enqueue(new LlamadaApi<List<Siembra>>(contexto) {
                    @Override
                    protected void alRecibir(List<Siembra> siembras) {
                        if (estaCarga != cargaActual) return;
                        cargarMargenes(estaCarga, armarFilas(lotes, siembras));
                    }

                    @Override
                    protected void alFallar(ErrorApi error) {
                        oyente.alFallar(error);
                    }
                });
            }

            @Override
            protected void alFallar(ErrorApi error) {
                oyente.alFallar(error);
            }
        });
    }

    // La campaña abierta más reciente, el mismo criterio que el panel de la web.
    private static Campania campaniaVigente(List<Campania> campanias) {
        Campania vigente = null;
        for (Campania c : campanias) {
            if (!c.isCerrada() && (vigente == null || c.getFechaInicio().compareTo(vigente.getFechaInicio()) > 0)) {
                vigente = c;
            }
        }
        return vigente;
    }

    // GET /siembras?campania_id trae las siembras de TODOS los campos del usuario: se cruzan
    // con los lotes del campo elegido.
    private static List<FilaLote> armarFilas(List<Lote> lotes, List<Siembra> siembras) {
        List<FilaLote> filas = new ArrayList<>();
        for (Lote lote : lotes) {
            Siembra delLote = null;
            for (Siembra s : siembras) {
                if (s.getLoteId() == lote.getId()) {
                    delLote = s;
                    break;
                }
            }
            filas.add(new FilaLote(lote, delLote));
        }
        return filas;
    }

    // Un pedido de margen por cada lote con siembra, todos a la vez; cuando vuelve el último
    // se entrega la lista. Si un margen falla (422: faltan costos o rinde), ese lote queda gris
    // con el motivo, y los demás se muestran igual.
    private void cargarMargenes(int estaCarga, List<FilaLote> filas) {
        List<FilaLote> conSiembra = new ArrayList<>();
        for (FilaLote fila : filas) {
            if (fila.getSiembra() != null) {
                conSiembra.add(fila);
            }
        }
        if (conSiembra.isEmpty()) {
            entregar(estaCarga, filas);
            return;
        }

        int[] pendientes = {conSiembra.size()}; // un array para poder modificarlo desde los callbacks
        for (FilaLote fila : conSiembra) {
            api.margen(fila.getSiembra().getId()).enqueue(new LlamadaApi<Margen>(contexto) {
                @Override
                protected void alRecibir(Margen margen) {
                    fila.setMargen(margen);
                    terminarUno();
                }

                @Override
                protected void alFallar(ErrorApi error) {
                    fila.setMotivo(error.getMensaje());
                    terminarUno();
                }

                private void terminarUno() {
                    pendientes[0]--;
                    if (pendientes[0] == 0) {
                        entregar(estaCarga, filas);
                    }
                }
            });
        }
    }

    private void entregar(int estaCarga, List<FilaLote> filas) {
        if (estaCarga != cargaActual) return;
        filas.sort(FilaLote.POR_SEVERIDAD);
        oyente.alCargarLotes(filas);
    }
}
