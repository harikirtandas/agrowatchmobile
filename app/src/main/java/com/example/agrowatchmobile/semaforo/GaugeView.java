package com.example.agrowatchmobile.semaforo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.agrowatchmobile.R;

// El velocímetro del semáforo, el mismo dibujo que el <svg> de semaforo.html: tres arcos
// (rojo, amarillo, verde) y una aguja. La web lo arma en una caja de 220 x 130 con el centro
// en (110, 110) y radio 90; acá se usan las mismas medidas, escaladas al ancho de la vista.
public class GaugeView extends View {

    private static final float ANCHO_BASE = 220f;
    private static final float ALTO_BASE = 130f;
    private static final float RADIO = 90f;
    private static final float GROSOR_ARCO = 18f;
    private static final float LARGO_AGUJA = 76f;

    private final Paint pincelArco = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelAguja = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF caja = new RectF();
    private final int colorRojo;
    private final int colorAmarillo;
    private final int colorVerde;

    // null = todavía no hay margen: se dibujan los arcos sin aguja.
    private Double margenPct;

    public GaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        colorRojo = ContextCompat.getColor(context, R.color.semaforo_rojo);
        colorAmarillo = ContextCompat.getColor(context, R.color.semaforo_amarillo);
        colorVerde = ContextCompat.getColor(context, R.color.semaforo_verde);

        pincelArco.setStyle(Paint.Style.STROKE);
        pincelArco.setStrokeCap(Paint.Cap.ROUND);
        pincelAguja.setColor(ContextCompat.getColor(context, R.color.negro_texto));
        pincelAguja.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setMargenPct(@Nullable Double margenPct) {
        this.margenPct = margenPct;
        invalidate(); // pide que se vuelva a dibujar con el valor nuevo
    }

    // Alto proporcional al ancho, como el viewBox del SVG.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ancho = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(ancho, Math.round(ancho * ALTO_BASE / ANCHO_BASE));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float escala = getWidth() / ANCHO_BASE;
        float cx = 110 * escala;
        float cy = 110 * escala;
        float radio = RADIO * escala;
        caja.set(cx - radio, cy - radio, cx + radio, cy + radio);
        pincelArco.setStrokeWidth(GROSOR_ARCO * escala);

        // Canvas mide los ángulos desde las 3 en punto y en sentido horario; el semáforo los
        // mide desde arriba. Por eso cada ángulo del semáforo se corre -90°.
        float corte = Semaforo.CORTE;
        dibujarArco(canvas, colorRojo, -90, -corte);
        dibujarArco(canvas, colorAmarillo, -corte, corte);
        dibujarArco(canvas, colorVerde, corte, 90);

        if (margenPct != null) {
            double angulo = Math.toRadians(Semaforo.anguloAguja(margenPct));
            float largo = LARGO_AGUJA * escala;
            pincelAguja.setStrokeWidth(4 * escala);
            canvas.drawLine(cx, cy,
                    cx + (float) (Math.sin(angulo) * largo),
                    cy - (float) (Math.cos(angulo) * largo),
                    pincelAguja);
        }
        canvas.drawCircle(cx, cy, 7 * escala, pincelAguja);
    }

    private void dibujarArco(Canvas canvas, int color, float desde, float hasta) {
        pincelArco.setColor(color);
        canvas.drawArc(caja, desde - 90, hasta - desde, false, pincelArco);
    }
}
