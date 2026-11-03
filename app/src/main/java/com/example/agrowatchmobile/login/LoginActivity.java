package com.example.agrowatchmobile.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agrowatchmobile.R;
import com.example.agrowatchmobile.api.ApiCliente;
import com.example.agrowatchmobile.api.ErrorApi;
import com.example.agrowatchmobile.api.LlamadaApi;
import com.example.agrowatchmobile.modelo.LoginPedido;
import com.example.agrowatchmobile.modelo.LoginRespuesta;
import com.example.agrowatchmobile.panel.PanelActivity;
import com.example.agrowatchmobile.sesion.Sesion;
import com.example.agrowatchmobile.util.Pantalla;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// La puerta de la app: ingreso con el mismo usuario y contraseña que la web (POST /auth/login).
// Si ya hay una sesión guardada pasa directo al panel. En los dos casos hace finish(), así
// Atrás desde el panel sale de la app en vez de volver acá (R6).
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private TextView tvMensaje;
    private Button btnIngresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Sesion.iniciada(this)) {
            irAlPanel();
            return;
        }
        Pantalla.bordeABorde(this);
        setContentView(R.layout.activity_login);
        Pantalla.prepararBarras(this, false);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvMensaje = findViewById(R.id.tvMensaje);
        btnIngresar = findViewById(R.id.btnIngresar);

        btnIngresar.setOnClickListener(v -> ingresar());
        etPassword.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                ingresar();
                return true;
            }
            return false;
        });
        // El registro sigue siendo de la web: se abre en el navegador.
        findViewById(R.id.btnRegistro).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_web)))));
    }

    private void ingresar() {
        String email = texto(etEmail);
        String password = texto(etPassword);

        // Validación en la app antes de mandar nada (R3).
        tilEmail.setError(Patterns.EMAIL_ADDRESS.matcher(email).matches() ? null : getString(R.string.login_error_email));
        tilPassword.setError(password.isEmpty() ? getString(R.string.login_error_password) : null);
        if (tilEmail.getError() != null || tilPassword.getError() != null) {
            return;
        }

        esperando(true);
        ApiCliente.get(this).login(new LoginPedido(email, password)).enqueue(new LlamadaApi<LoginRespuesta>(this) {
            @Override
            protected void alRecibir(LoginRespuesta respuesta) {
                Sesion.guardar(LoginActivity.this, respuesta.getToken(), respuesta.getUsuario().getNombre());
                irAlPanel();
            }

            @Override
            protected void alFallar(ErrorApi error) {
                esperando(false);
                mostrarError(error.texto(LoginActivity.this));
            }

            // En el ingreso, un 401 no es una sesión vencida: es email o contraseña incorrectos.
            @Override
            protected void alVencerSesion(ErrorApi error) {
                esperando(false);
                mostrarError(error.getMensaje());
            }
        });
    }

    // Mientras se espera la respuesta el botón queda deshabilitado: dos toques no mandan dos pedidos (R5).
    private void esperando(boolean si) {
        btnIngresar.setEnabled(!si);
        btnIngresar.setText(si ? R.string.login_ingresando : R.string.login_ingresar);
        if (si) {
            tvMensaje.setVisibility(View.GONE);
        }
    }

    private void mostrarError(String mensaje) {
        tvMensaje.setText(mensaje);
        tvMensaje.setVisibility(View.VISIBLE);
    }

    private void irAlPanel() {
        startActivity(new Intent(this, PanelActivity.class));
        finish();
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
