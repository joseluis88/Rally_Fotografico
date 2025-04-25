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

/**
 * Actividad que muestra una galería de fotografías que han sido "admitidas" por los administradores.
 * Utiliza un RecyclerView para mostrar la lista y se conecta a Firebase Firestore para obtener los datos.
 */
public class GalleryActivity extends AppCompatActivity {

    // Componentes de la interfaz
    private RecyclerView rvFotos;
    private TextView tvNoFotos;

    // Firestore y datos
    private FirebaseFirestore db;
    private List<Foto> fotoList;
    private FotoAdapter fotoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Inicializa elementos de la interfaz
        rvFotos = findViewById(R.id.rvFotos);
        tvNoFotos = findViewById(R.id.tvNoFotos);
        rvFotos.setLayoutManager(new LinearLayoutManager(this)); // Disposición vertical

        // Inicializa Firestore y la lista de fotos
        db = FirebaseFirestore.getInstance();
        fotoList = new ArrayList<>();

        // Crea el adaptador. El segundo parámetro es null y el tercero false para no mostrar botón eliminar
        fotoAdapter = new FotoAdapter(fotoList, null, false);
        rvFotos.setAdapter(fotoAdapter);

        // Carga las fotos desde Firebase
        loadFotos();
    }

    /**
     * Obtiene las fotos desde Firestore donde el estado sea "admitida".
     * Actualiza el RecyclerView con los datos o muestra un mensaje si no hay fotos.
     */
    private void loadFotos() {
        db.collection("fotos")
                .whereEqualTo("estado", "admitida") // Solo mostrar fotos con estado "admitida"
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fotoList.clear(); // Limpia la lista actual
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Foto foto = doc.toObject(Foto.class);
                        if (foto != null) {
                            foto.setId(doc.getId()); // Asigna el ID del documento
                            fotoList.add(foto);
                        }
                    }

                    // Si no hay fotos, muestra mensaje de "sin fotos"
                    if (fotoList.isEmpty()) {
                        tvNoFotos.setVisibility(View.VISIBLE);
                        rvFotos.setVisibility(View.GONE);
                    } else {
                        tvNoFotos.setVisibility(View.GONE);
                        rvFotos.setVisibility(View.VISIBLE);
                    }

                    // Notifica al adaptador que los datos han cambiado
                    fotoAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Muestra un mensaje de error si la carga falla
                    Toast.makeText(GalleryActivity.this, "Error al cargar fotografías", Toast.LENGTH_SHORT).show();
                });
    }
}
