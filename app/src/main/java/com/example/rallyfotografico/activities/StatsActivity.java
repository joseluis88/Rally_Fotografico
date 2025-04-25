package com.example.rallyfotografico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.services.FirebaseService;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Actividad que muestra estadísticas generales del concurso:
 * - Número total de fotos subidas
 * - Número total de votos emitidos
 * También permite acceder a la actividad de gráficos (RankingActivity).
 */
public class StatsActivity extends AppCompatActivity {

    // Elementos de la interfaz
    private TextView tvTotalFotos, tvTotalVotos;
    private Button btnVerGraficos;

    // Instancia del servicio de Firebase centralizado
    private final FirebaseService firebase = FirebaseService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Referencias a los elementos de la UI
        tvTotalFotos = findViewById(R.id.tvTotalFotos);
        tvTotalVotos = findViewById(R.id.tvTotalVotos);
        btnVerGraficos = findViewById(R.id.btnVerGraficos);

        // Consulta para obtener la cantidad total de fotos subidas
        firebase.getFirestore().collection("fotos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalFotos = queryDocumentSnapshots.size();
                    tvTotalFotos.setText("Total de fotos subidas: " + totalFotos);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(StatsActivity.this, "Error al cargar el total de fotos", Toast.LENGTH_SHORT).show());

        // Consulta para sumar los votos de todas las fotos
        firebase.getFirestore().collection("fotos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long totalVotos = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Long votos = doc.getLong("votos");
                        if (votos != null)
                            totalVotos += votos;
                    }
                    tvTotalVotos.setText("Total de votos emitidos: " + totalVotos);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(StatsActivity.this, "Error al cargar el total de votos", Toast.LENGTH_SHORT).show());

        // Botón para ir a la pantalla de gráficos con el top 5
        btnVerGraficos.setOnClickListener(v -> {
            startActivity(new Intent(StatsActivity.this, RankingActivity.class));
        });
    }
}
