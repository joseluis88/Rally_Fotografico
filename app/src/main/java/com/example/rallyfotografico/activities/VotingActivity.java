package com.example.rallyfotografico.activities;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.adapters.VotingAdapter;
import com.example.rallyfotografico.models.Foto;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Actividad encargada del sistema de votaciones públicas.
 * Permite al usuario votar hasta 5 fotos admitidas durante el periodo de votación configurado.
 */
public class VotingActivity extends AppCompatActivity {

    private RecyclerView rvVoting;
    private TextView tvVotosRestantes;
    private VotingAdapter votingAdapter;
    private List<Foto> fotoList;
    private FirebaseFirestore db;
    private int votosRestantes = 5; // Límite máximo de votos por dispositivo
    private String deviceId;        // ID único del dispositivo (no requiere login)

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        // Inicialización de vistas
        tvVotosRestantes = findViewById(R.id.tvVotosRestantes);
        rvVoting = findViewById(R.id.rvVoting);
        rvVoting.setLayoutManager(new LinearLayoutManager(this));

        // Inicialización de Firestore y listas
        db = FirebaseFirestore.getInstance();
        fotoList = new ArrayList<>();

        // ID del dispositivo como identificador único del votante
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Adaptador para mostrar las fotos y permitir votar
        votingAdapter = new VotingAdapter(fotoList, foto -> voteForPhoto(foto));
        rvVoting.setAdapter(votingAdapter);

        // Validar que las votaciones están dentro del rango de fechas permitido
        validarFechasDeVotacion();
    }

    /**
     * Comprueba si la fecha actual se encuentra dentro del rango de fechas de votación definido.
     * Si es válido, carga las fotos y los votos restantes del usuario.
     */
    private void validarFechasDeVotacion() {
        db.collection("configuracion").document("rally")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String inicioStr = snapshot.getString("fechaInicioVotacion");
                        String finStr = snapshot.getString("fechaFinVotacion");

                        try {
                            Date fechaActual = new Date();
                            Date fechaInicio = sdf.parse(inicioStr);
                            Date fechaFin = sdf.parse(finStr);

                            if (fechaInicio == null || fechaFin == null) {
                                Toast.makeText(this, "Fechas de votación no configuradas.", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }

                            // Validaciones de rango
                            if (fechaActual.before(fechaInicio)) {
                                Toast.makeText(this, "La votación aún no ha comenzado.", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            } else if (fechaActual.after(fechaFin)) {
                                Toast.makeText(this, "Las votaciones han finalizado.", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            } else {
                                // Dentro del rango: verificar votos y cargar fotos
                                checkVotosRestantes();
                                loadVotingPhotos();
                            }

                        } catch (ParseException e) {
                            Toast.makeText(this, "Error al interpretar fechas de votación.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "No se pudo verificar el rango de fechas de votación.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Verifica cuántas veces ha votado el dispositivo y actualiza el número de votos restantes.
     */
    private void checkVotosRestantes() {
        db.collection("fotos")
                .whereEqualTo("estado", "admitida")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    int votosHechos = 0;
                    for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                        List<String> votantes = (List<String>) doc.get("votantes");
                        if (votantes != null && votantes.contains(deviceId)) {
                            votosHechos++;
                        }
                    }
                    votosRestantes = Math.max(0, 5 - votosHechos);
                    tvVotosRestantes.setText("Votos restantes: " + votosRestantes);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar votos", Toast.LENGTH_SHORT).show());
    }

    /**
     * Carga todas las fotos admitidas desde Firestore para mostrarlas en la pantalla de votación.
     */
    private void loadVotingPhotos() {
        db.collection("fotos")
                .whereEqualTo("estado", "admitida")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fotoList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Foto foto = doc.toObject(Foto.class);
                        if (foto != null) {
                            foto.setId(doc.getId());
                            fotoList.add(foto);
                        }
                    }
                    votingAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(VotingActivity.this, "Error al cargar fotos para votar", Toast.LENGTH_SHORT).show());
    }

    /**
     * Ejecuta el proceso de votación para una foto, verificando si el usuario ya ha votado.
     * Si no ha votado, incrementa el contador de votos y guarda al votante.
     *
     * @param foto Foto a la que se emitirá el voto
     */
    private void voteForPhoto(Foto foto) {
        if (votosRestantes <= 0) {
            Toast.makeText(VotingActivity.this, "No tienes votos disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("fotos").document(foto.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> votantes = (List<String>) documentSnapshot.get("votantes");
                    if (votantes != null && votantes.contains(deviceId)) {
                        Toast.makeText(VotingActivity.this, "Ya has votado esta foto", Toast.LENGTH_SHORT).show();
                    } else {
                        // Registrar voto
                        db.collection("fotos").document(foto.getId())
                                .update("votos", FieldValue.increment(1),
                                        "votantes", FieldValue.arrayUnion(deviceId))
                                .addOnSuccessListener(aVoid -> {
                                    votosRestantes--;
                                    tvVotosRestantes.setText("Votos restantes: " + votosRestantes);
                                    Toast.makeText(VotingActivity.this, "Voto emitido", Toast.LENGTH_SHORT).show();
                                    loadVotingPhotos(); // Actualiza lista para reflejar el voto
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(VotingActivity.this, "Error al votar", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(VotingActivity.this, "Error al validar el voto", Toast.LENGTH_SHORT).show());
    }
}
