package com.example.agrowatchmobile.panel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.agrowatchmobile.R;
import com.example.agrowatchmobile.api.ApiCliente;
import com.example.agrowatchmobile.api.ErrorApi;
import com.example.agrowatchmobile.lote.LoteActivity;
import com.example.agrowatchmobile.modelo.Campania;
import com.example.agrowatchmobile.modelo.Establecimiento;
import com.example.agrowatchmobile.semaforo.GaugeView;
import com.example.agrowatchmobile.semaforo.Semaforo;
import com.example.agrowatchmobile.sesion.Sesion;
import com.example.agrowatchmobile.util.Aviso;
import com.example.agrowatchmobile.util.Formato;
import com.example.agrowatchmobile.util.Pantalla;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// El panel: el semáforo del campo elegido, los tres indicadores y la lista de lotes.
// Tocar un lote con siembra abre LoteActivity con el id de la siembra.
public class PanelActivity extends AppCompatActivity implements CargaPanel.Oyente {

    private final List<FilaLote> filas = new ArrayList<>();
    private LotesAdapter adaptador;
    private CargaPanel carga;
    private List<Establecimiento> campos = new ArrayList<>();
    private int campoActualId = -1;

    private Aviso aviso;
    private View grupoCampo;
    private Spinner spCampo;
    private GaugeView gaugeCampo;
    private TextView tvCampania, tvPctCampo, tvEstadoCampo, tvFraseCampo;
    private TextView tvSembradas, tvSembradasTotal, tvMargenBruto, tvMargenHa, tvAlerta, tvSinLotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pantalla.bordeABorde(this);
        setContentView(R.layout.activity_panel);
        Pantalla.prepararBarras(this, false);

        aviso = new Aviso(this);

        ListView lvLotes = findViewById(R.id.lvLotes);
        View encabezado = LayoutInflater.from(this).inflate(R.layout.panel_encabezado, lvLotes, false);
        lvLotes.addHeaderView(encabezado, null, false);
        adaptador = new LotesAdapter(this, filas);
        lvLotes.setAdapter(adaptador);
        lvLotes.setOnItemClickListener((padre, vista, posicion, id) ->
                abrirLote((FilaLote) padre.getItemAtPosition(posicion)));

        grupoCampo = encabezado.findViewById(R.id.grupoCampo);
        spCampo = encabezado.findViewById(R.id.spCampo);
        gaugeCampo = encabezado.findViewById(R.id.gaugeCampo);
        tvCampania = encabezado.findViewById(R.id.tvCampania);
        tvPctCampo = encabezado.findViewById(R.id.tvPctCampo);
        tvEstadoCampo = encabezado.findViewById(R.id.tvEstadoCampo);
        tvFraseCampo = encabezado.findViewById(R.id.tvFraseCampo);
        tvSembradas = encabezado.findViewById(R.id.tvSembradas);
        tvSembradasTotal = encabezado.findViewById(R.id.tvSembradasTotal);
        tvMargenBruto = encabezado.findViewById(R.id.tvMargenBruto);
        tvMargenHa = encabezado.findViewById(R.id.tvMargenHa);
        tvAlerta = encabezado.findViewById(R.id.tvAlerta);
        tvSinLotes = encabezado.findViewById(R.id.tvSinLotes);

        TextView tvTituloBarra = findViewById(R.id.tvTituloBarra);
        String nombre = Sesion.nombre(this);
        if (!nombre.isEmpty()) {
            tvTituloBarra.setText(nombre);
        }

        // Los datos se piden en cada onCreate, también al girar la pantalla: la API es la que
        // sabe el estado del campo (el mismo criterio que AppAlumnos en la Semana 14).
        carga = new CargaPanel(this, this);
        cargar();
    }

    // Al volver de un lote (por ejemplo, después de cargar un costo) el semáforo puede haber
    // cambiado: se vuelve a pedir.
    @Override
    protected void onRestart() {
        super.onRestart();
        cargar();
    }

    private void cargar() {
        aviso.cargando();
        carga.cargarCampos();
    }

    @Override
    public void alCargarCampos(List<Establecimiento> campos, Campania campania) {
        this.campos = campos;
        if (campos.isEmpty()) {
            aviso.mensaje(getString(R.string.panel_sin_campos));
            return;
        }
        tvCampania.setText(campania == null
                ? getString(R.string.panel_sin_campania)
                : getString(R.string.panel_campania, campania.getNombre()));

        ArrayAdapter<Establecimiento> opciones =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, campos);
        opciones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCampo.setAdapter(opciones);
        spCampo.setSelection(posicionDelCampoGuardado(), false);
        grupoCampo.setVisibility(campos.size() > 1 ? View.VISIBLE : View.GONE);
        spCampo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
                Establecimiento elegido = campos.get(posicion);
                // El Spinner avisa también la selección inicial: ese campo ya se está cargando.
                if (elegido.getId() == campoActualId) {
                    return;
                }
                Sesion.elegirCampo(PanelActivity.this, elegido.getId());
                cargarLotesDe(elegido);
            }

            @Override
            public void onNothingSelected(AdapterView<?> padre) {
            }
        });
        cargarLotesDe(campos.get(spCampo.getSelectedItemPosition()));
    }

    private int posicionDelCampoGuardado() {
        int guardado = Sesion.campoElegido(this);
        for (int i = 0; i < campos.size(); i++) {
            if (campos.get(i).getId() == guardado) {
                return i;
            }
        }
        return 0;
    }

    private void cargarLotesDe(Establecimiento campo) {
        campoActualId = campo.getId();
        aviso.cargando();
        carga.cargarLotes(campo.getId());
    }

    @Override
    public void alCargarLotes(List<FilaLote> nuevas) {
        aviso.ocultar();
        filas.clear();
        filas.addAll(nuevas);
        adaptador.notifyDataSetChanged();
        tvSinLotes.setVisibility(nuevas.isEmpty() ? View.VISIBLE : View.GONE);
        mostrarResumen(ResumenCampo.de(nuevas));
    }

    private void mostrarResumen(ResumenCampo resumen) {
        Double pct = resumen.getMargenPct();
        gaugeCampo.setMargenPct(pct);
        tvPctCampo.setText(pct == null
                ? getString(R.string.sin_dato)
                : getString(R.string.porcentaje, Formato.numero(pct * 100, 1)));
        tvEstadoCampo.setText(Semaforo.texto(resumen.getEstado()));
        tvEstadoCampo.setTextColor(ContextCompat.getColor(this, Semaforo.color(resumen.getEstado())));
        tvFraseCampo.setText(frase(pct));

        tvSembradas.setText(getString(R.string.unidad_ha, Formato.numero(resumen.getHaSembradas(), 1)));
        tvSembradasTotal.setText(getString(R.string.panel_sembradas_total, Formato.numero(resumen.getHectareas(), 1)));
        tvMargenBruto.setText(resumen.getMargenTotalUsd() == null
                ? getString(R.string.sin_dato)
                : getString(R.string.importe_usd, Formato.numero(resumen.getMargenTotalUsd(), 0)));
        tvMargenHa.setText(resumen.getMargenHaUsd() == null
                ? ""
                : getString(R.string.panel_margen_ha, getString(R.string.importe_usd, Formato.numero(resumen.getMargenHaUsd(), 0))));
        tvAlerta.setText(getString(R.string.panel_en_alerta_valor, resumen.getLotesEnAlerta(), resumen.getLotes()));
        // En rojo sólo si hay algún lote en alerta; "0 de 6" no es una alarma.
        tvAlerta.setTextColor(ContextCompat.getColor(this,
                resumen.getLotesEnAlerta() > 0 ? R.color.rojo_texto : R.color.negro_texto));
    }

    // La misma frase que fraseSemaforo() de la web.
    private String frase(Double pct) {
        if (pct == null) {
            return getString(R.string.frase_sin_datos);
        }
        int entero = (int) Math.round(pct * 100);
        return entero >= 0 ? getString(R.string.frase_margen, entero) : getString(R.string.frase_perdida);
    }

    @Override
    public void alFallar(ErrorApi error) {
        aviso.error(error.texto(this), this::cargar);
    }

    private void abrirLote(FilaLote fila) {
        if (fila.getSiembra() == null) {
            Toast.makeText(this, R.string.panel_lote_sin_siembra, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, LoteActivity.class);
        intent.putExtra(LoteActivity.EXTRA_SIEMBRA_ID, fila.getSiembra().getId());
        intent.putExtra(LoteActivity.EXTRA_LOTE_NOMBRE, fila.getLote().getNombre());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_panel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.accionActualizar) {
            cargar();
            return true;
        }
        if (item.getItemId() == R.id.accionSalir) {
            salir();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Igual que logout() de api.js: se avisa a la API para que invalide el token, y la sesión
    // local se borra aunque ese pedido falle (sin red, la app igual tiene que poder salir).
    private void salir() {
        ApiCliente.get(this).logout("Bearer " + Sesion.token(this)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
            }
        });
        Sesion.irAlIngreso(this, false);
    }
}
