package com.example.rallyfotografico.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rallyfotografico.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StatsActivity extends AppCompatActivity {

    private TextView tvTotalFotos, tvTotalVotos;
    private Button btnVerGraficos;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        tvTotalFotos = findViewById(R.id.tvTotalFotos);
        tvTotalVotos = findViewById(R.id.tvTotalVotos);
        btnVerGraficos = findViewById(R.id.btnVerGraficos);
        db = FirebaseFirestore.getInstance();

        // Contar el total de fotos
        db.collection("fotos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalFotos = queryDocumentSnapshots.size();
                    tvTotalFotos.setText("Total de fotos subidas: " + totalFotos);
                });

        // Sumar los votos de todas las fotos
        db.collection("fotos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long totalVotos = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Long votos = doc.getLong("votos");
                        if (votos != null)
                            totalVotos += votos;
                    }
                    tvTotalVotos.setText("Total de votos emitidos: " + totalVotos);
                });

        btnVerGraficos.setOnClickListener(v -> {
            // Implementa la navegación o despliegue de gráficos
        });
    }
}
