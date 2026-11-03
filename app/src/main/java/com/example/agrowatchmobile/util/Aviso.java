package com.example.agrowatchmobile.util;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.agrowatchmobile.R;

// El cartel a pantalla completa de layout/aviso.xml: "Cargando…", un error con Reintentar,
// o un mensaje para un estado vacío. Tapa la lista mientras está visible.
public class Aviso {

    private final View grupo;
    private final ProgressBar progreso;
    private final TextView texto;
    private final Button reintentar;

    public Aviso(Activity activity) {
        grupo = activity.findViewById(R.id.grupoAviso);
        progreso = activity.findViewById(R.id.pbAviso);
        texto = activity.findViewById(R.id.tvAviso);
        reintentar = activity.findViewById(R.id.btnReintentar);
    }

    public void cargando() {
        mostrar(texto.getContext().getString(R.string.cargando), true, null);
    }

    public void mensaje(String mensaje) {
        mostrar(mensaje, false, null);
    }

    public void error(String mensaje, Runnable alReintentar) {
        mostrar(mensaje, false, alReintentar);
    }

    public void ocultar() {
        grupo.setVisibility(View.GONE);
    }

    private void mostrar(String mensaje, boolean conProgreso, Runnable alReintentar) {
        grupo.setVisibility(View.VISIBLE);
        progreso.setVisibility(conProgreso ? View.VISIBLE : View.GONE);
        texto.setText(mensaje);
        reintentar.setVisibility(alReintentar == null ? View.GONE : View.VISIBLE);
        reintentar.setOnClickListener(alReintentar == null ? null : v -> alReintentar.run());
    }
}
