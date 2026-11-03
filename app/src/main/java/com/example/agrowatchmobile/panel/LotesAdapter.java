package com.example.agrowatchmobile.panel;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.agrowatchmobile.R;
import com.example.agrowatchmobile.semaforo.Semaforo;
import com.example.agrowatchmobile.util.Formato;

import java.util.List;

// La lista de lotes del panel. Como AppAlumnos usa un ArrayAdapter, pero con un layout propio
// (item_lote.xml) en vez de simple_list_item_1, porque cada fila muestra varios datos y un color.
public class LotesAdapter extends ArrayAdapter<FilaLote> {

    public LotesAdapter(Context contexto, List<FilaLote> filas) {
        super(contexto, 0, filas);
    }

    @NonNull
    @Override
    public View getView(int posicion, View vista, @NonNull ViewGroup padre) {
        if (vista == null) {
            vista = LayoutInflater.from(getContext()).inflate(R.layout.item_lote, padre, false);
        }
        FilaLote fila = getItem(posicion);
        Context c = getContext();

        TextView tvNombre = vista.findViewById(R.id.tvNombre);
        TextView tvDetalle = vista.findViewById(R.id.tvDetalle);
        TextView tvMargen = vista.findViewById(R.id.tvMargen);
        TextView tvEstado = vista.findViewById(R.id.tvEstado);
        View vPunto = vista.findViewById(R.id.vPunto);

        tvNombre.setText(fila.getLote().getNombre());

        if (fila.getSiembra() == null) {
            tvDetalle.setText(R.string.lote_sin_siembra);
        } else {
            tvDetalle.setText(c.getString(R.string.lote_cultivo_ha,
                    fila.getSiembra().getCultivoNombre(),
                    Formato.numero(fila.getSiembra().getHaSembradas(), 1)));
        }

        Double margenHa = fila.getMargenUsdPorHa();
        tvMargen.setText(margenHa == null
                ? c.getString(R.string.sin_dato)
                : c.getString(R.string.lote_margen_ha, c.getString(R.string.importe_usd, Formato.numero(margenHa, 0))));

        // Sin margen pero con motivo (422 de la API): se muestra el motivo en vez del cultivo.
        if (fila.getMargen() == null && fila.getMotivo() != null) {
            tvDetalle.setText(fila.getMotivo());
        }

        int color = ContextCompat.getColor(c, Semaforo.color(fila.getEstado()));
        vPunto.setBackgroundTintList(ColorStateList.valueOf(color));
        tvEstado.setText(Semaforo.texto(fila.getEstado()));
        tvEstado.setTextColor(color);
        return vista;
    }
}
