package com.example.agrowatchmobile.costo;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.agrowatchmobile.R;
import com.example.agrowatchmobile.api.ErrorApi;
import com.example.agrowatchmobile.modelo.CostoPedido;
import com.example.agrowatchmobile.sesion.Sesion;
import com.example.agrowatchmobile.util.Formato;
import com.example.agrowatchmobile.util.Pantalla;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

// Alta de un costo en la siembra de un lote: POST /siembras/{id}/costos.
// La abre LoteActivity con la Activity Result API; si la API guarda el costo, devuelve
// RESULT_OK y el lote se recarga. Si la API responde un error, el formulario queda como estaba.
public class NuevoCostoActivity extends AppCompatActivity {

    public static final String EXTRA_SIEMBRA_ID = "siembra_id";
    public static final String EXTRA_LOTE_NOMBRE = "lote_nombre";

    // Los mismos límites que costos.json en la API.
    private static final int MAX_DESCRIPCION = 120;
    private static final int MAX_UNIDAD = 10;

    private static final String ESTADO_MENSAJE = "mensaje";

    private NuevoCostoViewModel viewModel;
    private int siembraId;

    private Spinner spCategoria;
    private TextInputLayout tilDescripcion, tilCantidad, tilUnidad, tilPrecio, tilFecha;
    private TextInputEditText etDescripcion, etCantidad, etUnidad, etPrecio, etFecha;
    private RadioGroup rgMoneda;
    private TextView tvMensaje;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pantalla.bordeABorde(this);
        setContentView(R.layout.activity_nuevo_costo);
        Pantalla.prepararBarras(this, true);

        siembraId = getIntent().getIntExtra(EXTRA_SIEMBRA_ID, -1);
        String loteNombre = getIntent().getStringExtra(EXTRA_LOTE_NOMBRE);
        ((TextView) findViewById(R.id.tvLote)).setText(loteNombre);
        ((TextView) findViewById(R.id.tvTituloBarra)).setText(R.string.costo_titulo);

        spCategoria = findViewById(R.id.spCategoria);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        tilCantidad = findViewById(R.id.tilCantidad);
        tilUnidad = findViewById(R.id.tilUnidad);
        tilPrecio = findViewById(R.id.tilPrecio);
        tilFecha = findViewById(R.id.tilFecha);
        etDescripcion = findViewById(R.id.etDescripcion);
        etCantidad = findViewById(R.id.etCantidad);
        etUnidad = findViewById(R.id.etUnidad);
        etPrecio = findViewById(R.id.etPrecio);
        etFecha = findViewById(R.id.etFecha);
        rgMoneda = findViewById(R.id.rgMoneda);
        tvMensaje = findViewById(R.id.tvMensaje);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Sólo la primera vez: al girar, cada campo recupera solo lo que tenía escrito.
        if (savedInstanceState == null) {
            etFecha.setText(Formato.fechaPantalla(Calendar.getInstance()));
        } else {
            // El mensaje de abajo no se guarda solo como los avisos de los campos.
            CharSequence mensaje = savedInstanceState.getCharSequence(ESTADO_MENSAJE);
            if (mensaje != null) {
                tvMensaje.setText(mensaje);
                tvMensaje.setVisibility(View.VISIBLE);
            }
        }
        etFecha.setOnClickListener(v -> elegirFecha());
        btnGuardar.setOnClickListener(v -> guardar());

        viewModel = new ViewModelProvider(this).get(NuevoCostoViewModel.class);
        viewModel.getEstado().observe(this, this::mostrarEstado);
    }

    // Los watchers se registran acá y no en onCreate: al girar, Android restaura el texto de cada
    // campo con setText() entre onCreate y onPostCreate, y eso borraría los avisos restaurados.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        limpiarErrorAlEscribir(etDescripcion, tilDescripcion);
        limpiarErrorAlEscribir(etCantidad, tilCantidad);
        limpiarErrorAlEscribir(etUnidad, tilUnidad);
        limpiarErrorAlEscribir(etPrecio, tilPrecio);
        limpiarErrorAlEscribir(etFecha, tilFecha);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tvMensaje.getVisibility() == View.VISIBLE) {
            outState.putCharSequence(ESTADO_MENSAJE, tvMensaje.getText());
        }
    }

    private void mostrarEstado(NuevoCostoViewModel.Estado estado) {
        boolean guardando = estado == NuevoCostoViewModel.Estado.GUARDANDO;
        btnGuardar.setEnabled(!guardando);
        btnGuardar.setText(guardando ? R.string.costo_guardando : R.string.costo_guardar);

        if (estado == NuevoCostoViewModel.Estado.GUARDADO) {
            setResult(RESULT_OK);
            finish();
        } else if (estado == NuevoCostoViewModel.Estado.ERROR) {
            mostrarError(viewModel.getUltimoError());
            viewModel.errorMostrado();
        } else if (guardando) {
            tvMensaje.setVisibility(View.GONE);
        }
    }

    private void mostrarError(ErrorApi error) {
        if (error.getStatus() == 401) {
            Sesion.irAlIngreso(this, true);
            return;
        }
        // En un 422 la API dice qué campo está mal: se marca en el campo correspondiente.
        Map<String, TextInputLayout> campos = new HashMap<>();
        campos.put("descripcion", tilDescripcion);
        campos.put("cantidad", tilCantidad);
        campos.put("unidad", tilUnidad);
        campos.put("precio_unitario", tilPrecio);
        campos.put("fecha", tilFecha);
        for (Map.Entry<String, String> e : error.getErrores().entrySet()) {
            TextInputLayout campo = campos.get(e.getKey());
            if (campo != null) {
                campo.setError(e.getValue());
            }
        }
        tvMensaje.setText(error.texto(this));
        tvMensaje.setVisibility(View.VISIBLE);
    }

    private void elegirFecha() {
        Calendar actual = fechaElegida();
        if (actual == null) {
            actual = Calendar.getInstance();
        }
        new DatePickerDialog(this,
                (vista, anio, mes, dia) -> {
                    Calendar elegida = Calendar.getInstance();
                    elegida.set(anio, mes, dia);
                    etFecha.setText(Formato.fechaPantalla(elegida));
                },
                actual.get(Calendar.YEAR), actual.get(Calendar.MONTH), actual.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void guardar() {
        String descripcion = texto(etDescripcion);
        String unidad = texto(etUnidad);
        Double cantidad = numero(etCantidad);
        Double precio = numero(etPrecio);
        Calendar fecha = fechaElegida();

        // Validación en la app antes de mandar nada (R3): las mismas reglas que costos.json.
        tilDescripcion.setError(validarTexto(descripcion, MAX_DESCRIPCION));
        tilUnidad.setError(validarTexto(unidad, MAX_UNIDAD));
        tilCantidad.setError(cantidad == null ? getString(R.string.costo_error_requerido)
                : cantidad <= 0 ? getString(R.string.costo_error_cantidad) : null);
        tilPrecio.setError(precio == null ? getString(R.string.costo_error_requerido)
                : precio < 0 ? getString(R.string.costo_error_precio) : null);
        tilFecha.setError(fecha == null ? getString(R.string.costo_error_requerido) : null);

        if (hayErrores()) {
            tvMensaje.setText(R.string.costo_error_revisar);
            tvMensaje.setVisibility(View.VISIBLE);
            return;
        }

        String categoria = getResources().getStringArray(R.array.categorias_valores)[spCategoria.getSelectedItemPosition()];
        String moneda = getString(rgMoneda.getCheckedRadioButtonId() == R.id.rbUsd ? R.string.moneda_usd : R.string.moneda_ars);
        viewModel.guardar(siembraId, new CostoPedido(
                categoria, descripcion, cantidad, unidad, precio, moneda, Formato.fechaApi(fecha)));
    }

    private String validarTexto(String valor, int maximo) {
        if (valor.isEmpty()) return getString(R.string.costo_error_requerido);
        if (valor.length() > maximo) return getString(R.string.costo_error_largo, maximo);
        return null;
    }

    private boolean hayErrores() {
        return tilDescripcion.getError() != null || tilUnidad.getError() != null || tilCantidad.getError() != null
                || tilPrecio.getError() != null || tilFecha.getError() != null;
    }

    // Borra el aviso rojo del campo en cuanto el usuario escribe algo. Si era el último aviso,
    // también se oculta el mensaje de abajo. Un "Sin conexión" no tiene avisos en los campos,
    // así que sigue visible aunque se escriba.
    private void limpiarErrorAlEscribir(TextInputEditText campo, TextInputLayout til) {
        campo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int a, int d) { }
            @Override public void onTextChanged(CharSequence s, int i, int a, int d) { }
            @Override public void afterTextChanged(Editable s) {
                if (til.getError() == null) return;
                til.setError(null);
                if (!hayErrores()) {
                    tvMensaje.setVisibility(View.GONE);
                }
            }
        });
    }

    private Calendar fechaElegida() {
        return Formato.leerFechaPantalla(texto(etFecha));
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    // Acepta coma o punto decimal: "12,5" y "12.5" son lo mismo. null si está vacío o no es un número.
    private static Double numero(TextInputEditText campo) {
        try {
            return Double.parseDouble(texto(campo).replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
