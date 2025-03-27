package com.example.rallyfotografico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.rallyfotografico.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class HomeActivity extends AppCompatActivity {

    private Button btnRegistro, btnLogin, btnGaleria;
    private TextView tvInfoRally;
    private FirebaseFirestore db;
    private DocumentReference configRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnRegistro = findViewById(R.id.btnRegistro);
        btnLogin = findViewById(R.id.btnLogin);
        btnGaleria = findViewById(R.id.btnGaleria);
        tvInfoRally = findViewById(R.id.tvInfoRally);

        db = FirebaseFirestore.getInstance();
        // Se asume que la configuración está en la colección "configuracion" y el documento "rally"
        configRef = db.collection("configuracion").document("rally");

        // Cargar la configuración del rally y mostrarla en la pantalla
        loadConfig();

        btnRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnGaleria.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
    }

    private void loadConfig() {
        configRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Obtener todos los parámetros configurados
                Double tamañoMaximoMB = documentSnapshot.getDouble("tamañoMaximoMB");
                String resolucion = documentSnapshot.getString("resolucion");
                String tipoImagen = documentSnapshot.getString("tipoImagen");
                String fechaLimite = documentSnapshot.getString("fechaLimite");
                Long limiteFotos = documentSnapshot.getLong("limiteFotos");
                String fechaInicioVotacion = documentSnapshot.getString("fechaInicioVotacion");
                String fechaFinVotacion = documentSnapshot.getString("fechaFinVotacion");

                // Construir el mensaje de información del rally
                StringBuilder infoBuilder = new StringBuilder();
                infoBuilder.append("Bases del concurso:\n");
                infoBuilder.append("- Los participantes deben subir fotos según las directrices establecidas.\n\n");
                infoBuilder.append("Configuración del Concurso:\n");
                if (tamañoMaximoMB != null) {
                    infoBuilder.append("Tamaño máximo: ").append(tamañoMaximoMB).append(" MB\n");
                }
                if (resolucion != null) {
                    infoBuilder.append("Resolución: ").append(resolucion).append("\n");
                }
                if (tipoImagen != null) {
                    infoBuilder.append("Tipo de imagen: ").append(tipoImagen).append("\n");
                }
                if (fechaLimite != null) {
                    infoBuilder.append("Fecha límite de recepción: ").append(fechaLimite).append("\n");
                }
                if (limiteFotos != null) {
                    infoBuilder.append("Número máximo de fotos: ").append(limiteFotos).append("\n");
                }
                if (fechaInicioVotacion != null && fechaFinVotacion != null) {
                    infoBuilder.append("Votaciones: Desde ").append(fechaInicioVotacion)
                            .append(" hasta ").append(fechaFinVotacion).append("\n");
                }
                infoBuilder.append("\n¡Participa y demuestra tu creatividad!");

                tvInfoRally.setText(infoBuilder.toString());
            } else {
                tvInfoRally.setText("La configuración del rally no ha sido establecida.");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(HomeActivity.this, "Error al cargar la configuración del rally", Toast.LENGTH_SHORT).show();
            Log.e("HomeActivity", "loadConfig error: ", e);
        });
    }
}
