package com.example.rallyfotografico.notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.activities.ValidarFotosActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Servicio que se encarga de recibir y procesar mensajes push enviados mediante Firebase Cloud Messaging (FCM).
 * Este servicio se activa automáticamente cuando la app recibe un mensaje, incluso en segundo plano.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Método que se ejecuta automáticamente al recibir un nuevo mensaje desde FCM.
     * Aquí se construye y muestra una notificación al usuario.
     *
     * @param message Objeto RemoteMessage recibido desde Firebase, que puede contener título, cuerpo y datos.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // Extraer el título y cuerpo de la notificación desde el mensaje recibido
        String titulo = message.getNotification().getTitle();
        String cuerpo = message.getNotification().getBody();

        // Intent para abrir la actividad de validación de fotos al pulsar la notificación
        Intent intent = new Intent(this, ValidarFotosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Evita duplicar actividad si ya está abierta

        // Crear PendingIntent con flags seguros para Android 12+
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Construcción de la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "canalFotos")
                .setContentTitle(titulo)                     // Título mostrado
                .setContentText(cuerpo)                     // Cuerpo de la notificación
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Icono pequeño de la app
                .setAutoCancel(true)                        // Se cierra al pulsar
                .setContentIntent(pendingIntent);           // Acción al pulsar

        // Obtener el sistema de notificaciones
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Crear canal de notificación para Android 8+ (Oreo o superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    "canalFotos",                         // ID del canal
                    "Fotos Subidas",                      // Nombre visible del canal
                    NotificationManager.IMPORTANCE_HIGH  // Nivel de importancia
            );
            manager.createNotificationChannel(canal);     // Registrar canal
        }

        // Mostrar la notificación (ID = 1)
        manager.notify(1, builder.build());
    }
}
