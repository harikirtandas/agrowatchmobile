package com.example.agrowatchmobile.avisos;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.agrowatchmobile.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.atomic.AtomicInteger;

// Recibe las notificaciones de Firebase Cloud Messaging. A esta clase no la llama ninguna
// Activity nuestra: la despierta el sistema cuando llega un mensaje, incluso con la app
// cerrada. Por eso todo lo que necesita lo resuelve sola.
public class AvisosService extends FirebaseMessagingService {

    // El mismo id va en el manifiesto, para que las notificaciones que muestra el sistema
    // (con la app en segundo plano) caigan en este canal y no en uno sin nombre.
    public static final String CANAL_ID = "avisos";

    private static final String TAG = "AvisosService";

    // Un id distinto por notificacion: con uno fijo, cada mensaje nuevo pisaria al anterior.
    private static final AtomicInteger PROXIMO_ID = new AtomicInteger(1);

    // Desde Android 8 toda notificacion tiene que pertenecer a un canal, y si no existe el
    // sistema la descarta sin avisar. Es publico y estatico porque tambien se llama al
    // entrar al panel: cuando la app esta en segundo plano el sistema muestra el aviso sin
    // pasar por este servicio, y para entonces el canal ya tiene que estar creado.
    public static void crearCanal(Context contexto) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel canal = new NotificationChannel(
                CANAL_ID,
                contexto.getString(R.string.avisos_canal_nombre),
                NotificationManager.IMPORTANCE_DEFAULT);
        canal.setDescription(contexto.getString(R.string.avisos_canal_descripcion));
        NotificationManager gestor = contexto.getSystemService(NotificationManager.class);
        if (gestor != null) {
            // Crearlo dos veces no duplica nada: si ya existe, el sistema lo ignora.
            gestor.createNotificationChannel(canal);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        crearCanal(this);
    }

    // La direccion de ESTA instalacion, que Google asigna y renueva solo (cambia al
    // reinstalar o al borrar los datos de la app). No hace falta guardarla en ningun lado
    // porque mandamos por tema y no por token; queda en el log para poder mostrarla.
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i(TAG, "Token nuevo de FCM: " + token);
    }

    // Llego un mensaje con la app abierta. En segundo plano este metodo no se ejecuta: ahi
    // la notificacion la arma el sistema con los datos del mensaje.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage mensaje) {
        super.onMessageReceived(mensaje);

        RemoteMessage.Notification aviso = mensaje.getNotification();
        String titulo = aviso != null && aviso.getTitle() != null
                ? aviso.getTitle()
                : getString(R.string.app_name);
        // Un mensaje puede venir como "notification" (lo que manda la consola de Firebase)
        // o solo con datos sueltos; se contemplan los dos.
        String texto = aviso != null && aviso.getBody() != null
                ? aviso.getBody()
                : mensaje.getData().get("body");

        if (texto == null || texto.isEmpty()) {
            Log.w(TAG, "Mensaje sin cuerpo: no hay nada que mostrar");
            return;
        }
        mostrar(titulo, texto);
    }

    private void mostrar(String titulo, String texto) {
        // Desde Android 13 mostrar notificaciones necesita permiso del usuario. Si lo nego,
        // notify() no hace nada: se corta antes para dejarlo asentado en el log.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Sin permiso POST_NOTIFICATIONS: no se muestra el aviso");
            return;
        }

        NotificationCompat.Builder constructor = new NotificationCompat.Builder(this, CANAL_ID)
                .setSmallIcon(R.drawable.ic_aviso)
                .setColor(ContextCompat.getColor(this, R.color.verde_medio))
                .setContentTitle(titulo)
                .setContentText(texto)
                // Sin esto un texto largo se corta en una linea.
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Que desaparezca al tocarla, en vez de quedar pegada en la barra.
                .setAutoCancel(true);

        NotificationManagerCompat.from(this)
                .notify(PROXIMO_ID.getAndIncrement(), constructor.build());
    }
}