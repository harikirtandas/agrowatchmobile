package com.example.agrowatchmobile.lote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.agrowatchmobile.R;
import com.example.agrowatchmobile.modelo.Costo;
import com.example.agrowatchmobile.util.Formato;

import java.util.Arrays;
import java.util.List;

// La lista de costos de un lote (item_costo.xml).
public class CostosAdapter extends ArrayAdapter<Costo> {

    private final List<String> categoriasValores;
    private final String[] categoriasEtiquetas;

    public CostosAdapter(Context contexto, List<Costo> costos) {
        super(contexto, 0, costos);
        categoriasValores = Arrays.asList(contexto.getResources().getStringArray(R.array.categorias_valores));
        categoriasEtiquetas = contexto.getResources().getStringArray(R.array.categorias_etiquetas);
    }

    @NonNull
    @Override
    public View getView(int posicion, View vista, @NonNull ViewGroup padre) {
        if (vista == null) {
            vista = LayoutInflater.from(getContext()).inflate(R.layout.item_costo, padre, false);
        }
        Costo costo = getItem(posicion);
        Context c = getContext();

        ((TextView) vista.findViewById(R.id.tvDescripcion)).setText(costo.getDescripcion());
        ((TextView) vista.findViewById(R.id.tvDetalleCosto)).setText(c.getString(R.string.costo_detalle,
                etiquetaDe(costo.getCategoria()),
                Formato.numero(costo.getCantidad(), 2),
                costo.getUnidad(),
                Formato.fecha(costo.getFecha())));

        ((TextView) vista.findViewById(R.id.tvImporte)).setText(importe(c, costo));
        return vista;
    }

    // "US$ 1.234,50" o "$ 1.234,50" según la moneda. También lo usa el diálogo de borrado.
    static String importe(Context c, Costo costo) {
        String importe = Formato.numero(costo.getImporte(), 2);
        return "USD".equals(costo.getMoneda())
                ? c.getString(R.string.importe_usd, importe)
                : c.getString(R.string.importe_ars, importe);
    }

    // "fertilizante" (valor de la API) -> "Fertilizante" (texto de strings.xml).
    private String etiquetaDe(String categoria) {
        int i = categoriasValores.indexOf(categoria);
        return i >= 0 ? categoriasEtiquetas[i] : categoria;
    }
}
