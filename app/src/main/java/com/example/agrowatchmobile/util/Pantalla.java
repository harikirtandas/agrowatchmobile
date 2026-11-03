package com.example.agrowatchmobile.util;

import android.graphics.Color;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.agrowatchmobile.R;

// Lo que todas las pantallas hacen igual con las barras del sistema (R5), para no copiarlo
// en cada Activity.
public final class Pantalla {

    private Pantalla() {
    }

    // Antes de setContentView. La barra de estado queda sobre el verde oscuro de la barra
    // superior, así que sus íconos van claros; la de navegación, sobre el fondo claro.
    public static void bordeABorde(AppCompatActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
    }

    // Después de setContentView. Espera un layout con la raíz R.id.main y la barra superior
    // R.id.barraSuperior (layout/barra_superior.xml).
    //  - La barra superior se estira por debajo de la barra de estado (padding arriba).
    //  - La raíz deja lugar a la barra de navegación y al teclado (padding abajo).
    public static void prepararBarras(AppCompatActivity activity, boolean conAtras) {
        Toolbar barra = activity.findViewById(R.id.barraSuperior);
        activity.setSupportActionBar(barra);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // el título lo pone el layout, junto al logo
            actionBar.setDisplayHomeAsUpEnabled(conAtras);
        }
        if (conAtras) {
            barra.setNavigationOnClickListener(v -> activity.getOnBackPressedDispatcher().onBackPressed());
        }

        View raiz = activity.findViewById(R.id.main);
        int arribaOriginal = barra.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets sistema = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            barra.setPadding(barra.getPaddingLeft(), arribaOriginal + sistema.top,
                    barra.getPaddingRight(), barra.getPaddingBottom());
            v.setPadding(sistema.left, 0, sistema.right, Math.max(sistema.bottom, teclado.bottom));
            return insets;
        });
    }
}
