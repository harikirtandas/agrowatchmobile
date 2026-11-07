package com.example.agrowatchmobile.lote;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import com.example.agrowatchmobile.R;
import com.example.agrowatchmobile.api.AgroApi;
import com.example.agrowatchmobile.api.ApiCliente;
import com.example.agrowatchmobile.api.ErrorApi;
import com.example.agrowatchmobile.api.LlamadaApi;
import com.example.agrowatchmobile.costo.NuevoCostoActivity;
import com.example.agrowatchmobile.modelo.Costo;
import com.example.agrowatchmobile.modelo.Margen;
import com.example.agrowatchmobile.modelo.Siembra;
import com.example.agrowatchmobile.semaforo.GaugeView;
import com.example.agrowatchmobile.semaforo.Semaforo;
import com.example.agrowatchmobile.util.Aviso;
import com.example.agrowatchmobile.util.Formato;
import com.example.agrowatchmobile.util.Pantalla;

import java.util.ArrayList;
import java.util.List;

// El detalle de un lote: su semáforo, los números de la proyección y los costos cargados.
// Llega desde el panel con el id de la siembra como extra (GET /siembras/{id}).
// "Cargar costo" abre NuevoCostoActivity y, si se guardó, se vuelve a pedir todo.
public class LoteActivity extends AppCompatActivity {

    public static final String EXTRA_SIEMBRA_ID = "siembra_id";
    public static final String EXTRA_LOTE_NOMBRE = "lote_nombre";

    private AgroApi api;
    private int siembraId;
    private String loteNombre;
    private final List<Costo> costos = new ArrayList<>();
    private CostosAdapter adaptador;
    private Aviso aviso;

    private TextView tvTituloLote, tvSubtituloLote, tvPctLote, tvEstadoLote, tvRinde, tvFaltaDato, tvSinCostos, tvPistaBorrar;
    private TextView tvIngreso, tvCostoTotal, tvCostoHa, tvMargenLote;
    private View tarjetaNumeros;
    private GaugeView gaugeLote;

    // NuevoCostoActivity devuelve RESULT_OK si la API guardó el costo.
    private final ActivityResultLauncher<Intent> nuevoCosto =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::alVolverDelCosto);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pantalla.bordeABorde(this);
        setContentView(R.layout.activity_lote);
        Pantalla.prepararBarras(this, true);

        api = ApiCliente.get(this);
        siembraId = getIntent().getIntExtra(EXTRA_SIEMBRA_ID, -1);
        loteNombre = getIntent().getStringExtra(EXTRA_LOTE_NOMBRE);
        aviso = new Aviso(this);

        ListView lvCostos = findViewById(R.id.lvCostos);
        View encabezado = LayoutInflater.from(this).inflate(R.layout.lote_encabezado, lvCostos, false);
        lvCostos.addHeaderView(encabezado, null, false);
        adaptador = new CostosAdapter(this, costos);
        lvCostos.setAdapter(adaptador);
        // Toque largo sobre un costo -> pregunta si se borra.
        lvCostos.setOnItemLongClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item instanceof Costo) {
                confirmarBorrado((Costo) item);
                return true;
            }
            return false;
        });

        tvTituloLote = encabezado.findViewById(R.id.tvTituloLote);
        tvSubtituloLote = encabezado.findViewById(R.id.tvSubtituloLote);
        gaugeLote = encabezado.findViewById(R.id.gaugeLote);
        tvPctLote = encabezado.findViewById(R.id.tvPctLote);
        tvEstadoLote = encabezado.findViewById(R.id.tvEstadoLote);
        tvRinde = encabezado.findViewById(R.id.tvRinde);
        tvFaltaDato = encabezado.findViewById(R.id.tvFaltaDato);
        tarjetaNumeros = encabezado.findViewById(R.id.tarjetaNumeros);
        tvIngreso = encabezado.findViewById(R.id.tvIngreso);
        tvCostoTotal = encabezado.findViewById(R.id.tvCostoTotal);
        tvCostoHa = encabezado.findViewById(R.id.tvCostoHa);
        tvMargenLote = encabezado.findViewById(R.id.tvMargenLote);
        tvSinCostos = encabezado.findViewById(R.id.tvSinCostos);
        tvPistaBorrar = encabezado.findViewById(R.id.tvPistaBorrar);
        encabezado.findViewById(R.id.btnCargarCosto).setOnClickListener(v -> abrirNuevoCosto());

        if (loteNombre != null) {
            ((TextView) findViewById(R.id.tvTituloBarra)).setText(loteNombre);
        }
        cargar();
    }

    // Primero la siembra (si no existe o no es del usuario, la API responde 404 y no hay más que
    // mostrar); después, el margen y los costos a la vez.
    private void cargar() {
        aviso.cargando();
        api.siembra(siembraId).enqueue(new LlamadaApi<Siembra>(this) {
            @Override
            protected void alRecibir(Siembra siembra) {
                aviso.ocultar();
                mostrarSiembra(siembra);
                cargarMargen();
                cargarCostos();
            }

            @Override
            protected void alFallar(ErrorApi error) {
                // Sin respuesta se puede reintentar; un 404 no se arregla reintentando.
                if (error.huboRespuesta()) {
                    aviso.mensaje(error.texto(LoteActivity.this));
                } else {
                    aviso.error(error.texto(LoteActivity.this), LoteActivity.this::cargar);
                }
            }
        });
    }

    private void mostrarSiembra(Siembra siembra) {
        loteNombre = siembra.getLoteNombre();
        tvTituloLote.setText(getString(R.string.lote_titulo, siembra.getLoteNombre(), siembra.getCultivoNombre()));
        tvSubtituloLote.setText(getString(R.string.lote_subtitulo,
                siembra.getCampaniaNombre(), Formato.numero(siembra.getHaSembradas(), 1)));
    }

    private void cargarMargen() {
        api.margen(siembraId).enqueue(new LlamadaApi<Margen>(this) {
            @Override
            protected void alRecibir(Margen margen) {
                mostrarMargen(margen);
            }

            @Override
            protected void alFallar(ErrorApi error) {
                // 422: falta un dato para calcular. Se dice cuál; los costos se ven igual, y
                // cargar uno desde acá puede ser justo lo que faltaba.
                mostrarMargen(null);
                tvFaltaDato.setText(error.getStatus() == 422
                        ? getString(R.string.lote_falta_dato, error.getMensaje())
                        : error.texto(LoteActivity.this));
                tvFaltaDato.setVisibility(View.VISIBLE);
            }
        });
    }

    private void mostrarMargen(Margen m) {
        tvFaltaDato.setVisibility(View.GONE);
        tarjetaNumeros.setVisibility(m == null ? View.GONE : View.VISIBLE);
        String estado = m == null ? null : m.getSemaforo();
        Double pct = m == null ? null : m.getMargenPct();

        gaugeLote.setMargenPct(pct);
        tvPctLote.setText(pct == null
                ? getString(R.string.sin_dato)
                : getString(R.string.porcentaje, Formato.numero(pct * 100, 1)));
        tvEstadoLote.setText(Semaforo.texto(estado));
        tvEstadoLote.setTextColor(ContextCompat.getColor(this, Semaforo.color(estado)));
        if (m == null) {
            tvRinde.setText(R.string.sin_dato);
            return;
        }
        // La API da el rinde en kg/ha; la web lo muestra en quintales (÷ 100).
        tvRinde.setText(getString(R.string.lote_qq_ha, Formato.numero(m.getRindeIndiferenciaKgHa() / 100, 1)));
        tvIngreso.setText(usdYPesos(m.getIngresoEsperadoUsd(), m.getIngresoEsperadoArs()));
        tvCostoTotal.setText(usdYPesos(m.getCostoTotalUsd(), m.getCostoTotalArs()));
        tvCostoHa.setText(getString(R.string.importe_usd, Formato.numero(m.getCostoPorHaUsd(), 0)));
        tvMargenLote.setText(usdYPesos(m.getMargenBrutoUsd(), m.getMargenBrutoArs()));
    }

    // El cálculo es en dólares; los pesos son sólo para mostrar, igual que en la web.
    private String usdYPesos(double usd, double ars) {
        return getString(R.string.importe_usd, Formato.numero(usd, 0)) + "\n"
                + getString(R.string.importe_ars, Formato.numero(ars, 0));
    }

    private void cargarCostos() {
        api.costos(siembraId).enqueue(new LlamadaApi<List<Costo>>(this) {
            @Override
            protected void alRecibir(List<Costo> nuevos) {
                costos.clear();
                costos.addAll(nuevos);
                adaptador.notifyDataSetChanged();
                tvSinCostos.setText(R.string.lote_sin_costos);
                tvSinCostos.setVisibility(nuevos.isEmpty() ? View.VISIBLE : View.GONE);
                tvPistaBorrar.setVisibility(nuevos.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            protected void alFallar(ErrorApi error) {
                tvSinCostos.setText(error.texto(LoteActivity.this));
                tvSinCostos.setVisibility(View.VISIBLE);
                tvPistaBorrar.setVisibility(View.GONE);
            }
        });
    }

    private void abrirNuevoCosto() {
        Intent intent = new Intent(this, NuevoCostoActivity.class);
        intent.putExtra(NuevoCostoActivity.EXTRA_SIEMBRA_ID, siembraId);
        intent.putExtra(NuevoCostoActivity.EXTRA_LOTE_NOMBRE, loteNombre);
        nuevoCosto.launch(intent);
    }

    private void alVolverDelCosto(ActivityResult resultado) {
        if (resultado.getResultCode() == RESULT_OK) {
            Toast.makeText(this, R.string.lote_costo_guardado, Toast.LENGTH_SHORT).show();
            cargarMargen();
            cargarCostos();
        }
    }

    private void confirmarBorrado(Costo costo) {
        // Se muestra cuál costo es, para no borrar otro por error.
        new AlertDialog.Builder(this)
                .setTitle(R.string.lote_borrar_pregunta)
                .setMessage(getString(R.string.lote_borrar_detalle,
                        costo.getDescripcion(), CostosAdapter.importe(this, costo)))
                .setPositiveButton(R.string.lote_borrar, (dialogo, boton) -> borrarCosto(costo))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void borrarCosto(Costo costo) {
        api.borrarCosto(costo.getId()).enqueue(new LlamadaApi<Void>(this) {
            @Override
            protected void alRecibir(Void cuerpo) {
                Toast.makeText(LoteActivity.this, R.string.lote_costo_borrado, Toast.LENGTH_SHORT).show();
                // Igual que después de un alta: el margen y la lista cambiaron.
                cargarMargen();
                cargarCostos();
            }

            @Override
            protected void alFallar(ErrorApi error) {
                Toast.makeText(LoteActivity.this, error.texto(LoteActivity.this), Toast.LENGTH_LONG).show();
                // 404: ya lo habían borrado (por ejemplo, desde la web). Se recarga para que
                // deje de aparecer en la lista.
                if (error.getStatus() == 404) {
                    cargarMargen();
                    cargarCostos();
                }
            }
        });
    }
}
