package com.example.rallyfotografico.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.rallyfotografico.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Actividad que muestra las bases del concurso fotográfico tanto para participantes
 * como para el público votante.
 * Las bases se cargan dinámicamente desde Firestore (documento "rally" de la colección "configuracion").
 */
public class BasesActivity extends AppCompatActivity {

    // TextViews donde se muestran las bases para participantes y público
    private TextView tvBasesParticipantes, tvBasesPublico;

    // Instancia de Firestore y referencia al documento de configuración
    private FirebaseFirestore db;
    private DocumentReference configRef;

    /**
     * Método que se ejecuta al crear la actividad.
     * Establece el modo nocturno automático, inicializa vistas y carga datos desde Firebase.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Establece el modo nocturno basado en el sistema del dispositivo
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bases);

        // Enlaza las vistas del layout
        tvBasesParticipantes = findViewById(R.id.tvBasesParticipantes);
        tvBasesPublico = findViewById(R.id.tvBasesPublico);

        // Inicializa Firebase Firestore
        db = FirebaseFirestore.getInstance();
        configRef = db.collection("configuracion").document("rally");

        // Llama al método que carga y muestra las bases
        cargarBases();
    }

    /**
     * Carga las bases del concurso desde Firestore y las muestra en pantalla.
     * Separa la información entre participantes y público.
     */
    private void cargarBases() {
        configRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {

                // Construye el texto para PARTICIPANTES
                StringBuilder participantes = new StringBuilder();
                participantes.append("📸 BASES PARA PARTICIPANTES\n\n");
                participantes.append("- ").append(documentSnapshot.getString("mensajeParticipantes")).append("\n\n");
                participantes.append("📷 Tamaño máximo permitido: ")
                        .append(documentSnapshot.getDouble("tamañoMaximoMB")).append(" MB\n");
                participantes.append("📐 Resolución máxima: ")
                        .append(documentSnapshot.getString("resolucion")).append("\n");
                participantes.append("🖼️ Formatos permitidos: ")
                        .append(documentSnapshot.getString("tipoImagen")).append("\n");
                participantes.append("🗓️ Fecha límite de recepción: ")
                        .append(documentSnapshot.getString("fechaLimite")).append("\n");
                participantes.append("📸 Límite de fotos por participante: ")
                        .append(documentSnapshot.getLong("limiteFotos")).append("\n");

                // Muestra el texto en el TextView correspondiente
                tvBasesParticipantes.setText(participantes.toString());

                // Construye el texto para el PÚBLICO
                StringBuilder publico = new StringBuilder();
                publico.append("👥 BASES PARA EL PÚBLICO\n\n");
                publico.append("- ").append(documentSnapshot.getString("mensajePublico")).append("\n\n");
                publico.append("🔢 Votos permitidos: ")
                        .append(documentSnapshot.getLong("votosPublico")).append("\n\n");
                publico.append("🗳️ Inicio de votaciones: ")
                        .append(documentSnapshot.getString("fechaInicioVotacion")).append("\n");
                publico.append("🏁 Fin de votaciones: ")
                        .append(documentSnapshot.getString("fechaFinVotacion")).append("\n");

                // Muestra el texto en el TextView correspondiente
                tvBasesPublico.setText(publico.toString());

            } else {
                // Si no se encuentra el documento
                Toast.makeText(this, "No se encontró la configuración del concurso.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // En caso de error al cargar datos
            Toast.makeText(this, "Error al cargar las bases del concurso", Toast.LENGTH_SHORT).show();
            Log.e("BasesActivity", "Error al obtener bases", e);
        });
    }
}
