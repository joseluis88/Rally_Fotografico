package com.example.rallyfotografico.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.adapters.FotoAdapter;
import com.example.rallyfotografico.models.Foto;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView rvFotos;
    private TextView tvNoFotos;
    private FirebaseFirestore db;
    private List<Foto> fotoList;
    private FotoAdapter fotoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Inicializamos los elementos de la vista
        rvFotos = findViewById(R.id.rvFotos);
        tvNoFotos = findViewById(R.id.tvNoFotos);

        rvFotos.setLayoutManager(new LinearLayoutManager(this));

        // Inicializamos Firestore
        db = FirebaseFirestore.getInstance();
        fotoList = new ArrayList<>();

        // Configuramos el adaptador sin botón de eliminación
        fotoAdapter = new FotoAdapter(fotoList, null, false); // false para no mostrar el botón eliminar
        rvFotos.setAdapter(fotoAdapter);

        // Cargamos las fotos desde Firebase Firestore
        loadFotos();
    }

    private void loadFotos() {
        db.collection("fotos")
                .whereEqualTo("estado", "admitida") // Solo las fotos admitidas
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fotoList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Foto foto = doc.toObject(Foto.class);
                        if (foto != null) {
                            foto.setId(doc.getId());
                            fotoList.add(foto);
                        }
                    }

                    // Mostrar u ocultar el mensaje según el estado de la lista
                    if (fotoList.isEmpty()) {
                        tvNoFotos.setVisibility(View.VISIBLE);
                        rvFotos.setVisibility(View.GONE);
                    } else {
                        tvNoFotos.setVisibility(View.GONE);
                        rvFotos.setVisibility(View.VISIBLE);
                    }

                    // Notificamos al adaptador que los datos han cambiado
                    fotoAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(GalleryActivity.this, "Error al cargar fotografías", Toast.LENGTH_SHORT).show());
    }
}
