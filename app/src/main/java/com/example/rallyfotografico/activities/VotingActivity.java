package com.example.rallyfotografico.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.rallyfotografico.R;
import com.example.rallyfotografico.models.Foto;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

public class VotingActivity extends AppCompatActivity {

    private ImageView ivFotoVotacion;
    private Button btnVotar;
    private TextView tvVotosRestantes;
    private FirebaseFirestore db;
    private Foto currentFoto;
    private int votosRestantes = 5; // Límite de votos del usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        ivFotoVotacion = findViewById(R.id.ivFotoVotacion);
        btnVotar = findViewById(R.id.btnVotar);
        tvVotosRestantes = findViewById(R.id.tvVotosRestantes);
        db = FirebaseFirestore.getInstance();

        loadRandomFoto();

        btnVotar.setOnClickListener(v -> {
            if (votosRestantes > 0 && currentFoto != null) {
                db.collection("fotos").document(currentFoto.getId())
                        .update("votos", FieldValue.increment(1))
                        .addOnSuccessListener(aVoid -> {
                            votosRestantes--;
                            tvVotosRestantes.setText("Votos restantes: " + votosRestantes);
                            Toast.makeText(VotingActivity.this, "Voto emitido", Toast.LENGTH_SHORT).show();
                            loadRandomFoto();
                        });
            } else {
                Toast.makeText(VotingActivity.this, "No tienes votos disponibles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRandomFoto() {
        db.collection("fotos")
                .whereEqualTo("estado", "admitida")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    if (!docs.isEmpty()) {
                        int index = new Random().nextInt(docs.size());
                        DocumentSnapshot doc = docs.get(index);
                        currentFoto = doc.toObject(Foto.class);
                        currentFoto.setId(doc.getId());
                        Glide.with(VotingActivity.this)
                                .load(currentFoto.getUrl())
                                .into(ivFotoVotacion);
                    }
                });
    }
}
