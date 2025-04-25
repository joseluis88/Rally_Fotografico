package com.example.rallyfotografico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rallyfotografico.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Actividad que representa el menú de inicio para el público general.
 * Desde aquí se puede acceder a: galería, votación y ranking.
 */
public class PublicHomeActivity extends AppCompatActivity {

    private Button btnGaleria, btnVotacion, btnRanking;
    private FirebaseFirestore db;
    private DocumentReference infoPublicaRef;

    // Formato para interpretar las fechas almacenadas en Firestore
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_home);

        // Asociar las vistas a sus elementos en el XML
        btnGaleria = findViewById(R.id.btnGaleria);
        btnVotacion = findViewById(R.id.btnVotacion);
        btnRanking = findViewById(R.id.btnRanking);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
        infoPublicaRef = db.collection("configuracion_publica").document("info");

        // Validar si el botón de votación debe estar disponible según las fechas
        validarFechasVotacion();

        // Navegar a la galería de fotos admitidas
        btnGaleria.setOnClickListener(v -> {
            Intent intent = new Intent(PublicHomeActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        // Navegar a la actividad de votación (si está activa)
        btnVotacion.setOnClickListener(v -> {
            Intent intent = new Intent(PublicHomeActivity.this, VotingActivity.class);
            startActivity(intent);
        });

        // Navegar al ranking de fotos más votadas
        btnRanking.setOnClickListener(v -> {
            Intent intent = new Intent(PublicHomeActivity.this, RankingActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Consulta Firestore para obtener las fechas de inicio y fin de la votación.
     * Si la votación aún no ha comenzado o ya ha finalizado, se desactiva el botón de votación.
     */
    private void validarFechasVotacion() {
        db.collection("configuracion").document("rally")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String fechaInicioStr = snapshot.getString("fechaInicioVotacion");
                    String fechaFinStr = snapshot.getString("fechaFinVotacion");
                    try {
                        Date hoy = new Date(); // Fecha actual
                        Date inicio = sdf.parse(fechaInicioStr); // Inicio de votación
                        Date fin = sdf.parse(fechaFinStr);       // Fin de votación

                        // Si la fecha actual no está dentro del rango de votación, se desactiva el botón
                        if (inicio != null && fin != null) {
                            if (hoy.before(inicio) || hoy.after(fin)) {
                                btnVotacion.setEnabled(false);
                                btnVotacion.setAlpha(0.5f);
                                btnVotacion.setText("Votación no disponible");
                            }
                        }
                    } catch (ParseException e) {
                        // Si hay error al parsear las fechas, se desactiva el botón con mensaje de error
                        btnVotacion.setEnabled(false);
                        btnVotacion.setText("Error en fechas de votación");
                    }
                })
                .addOnFailureListener(e -> {
                    // Si falla la consulta a Firestore, se desactiva el botón con mensaje de error
                    btnVotacion.setEnabled(false);
                    btnVotacion.setText("Error al cargar configuración");
                });
    }
}
