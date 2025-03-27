package com.example.rallyfotografico.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.adapters.ValidarFotosAdapter;
import com.example.rallyfotografico.models.Foto;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidarFotosActivity extends AppCompatActivity {

    private RecyclerView rvFotosPendientes;
    private FirebaseFirestore db;
    private List<Foto> fotoList;
    private ValidarFotosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validar_fotos);

        rvFotosPendientes = findViewById(R.id.rvFotosPendientes);
        rvFotosPendientes.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        fotoList = new ArrayList<>();

        // Se instancia el adaptador, implementando la interfaz para gestionar las acciones de validación
        adapter = new ValidarFotosAdapter(fotoList, new ValidarFotosAdapter.OnFotoValidationListener() {
            @Override
            public void onAdmitirClick(Foto foto) {
                updateFotoEstado(foto, "admitida");
            }

            @Override
            public void onRechazarClick(Foto foto) {
                updateFotoEstado(foto, "rechazada");
            }
        });
        rvFotosPendientes.setAdapter(adapter);

        loadFotosPendientes();
    }

    // Consulta Firestore para obtener las fotos pendientes
    private void loadFotosPendientes() {
        db.collection("fotos")
                .whereEqualTo("estado", "pendiente")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fotoList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Foto foto = doc.toObject(Foto.class);
                        foto.setId(doc.getId());
                        fotoList.add(foto);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ValidarFotosActivity.this, "Error al cargar fotos", Toast.LENGTH_SHORT).show();
                });
    }

    // Actualiza el estado de la foto en Firestore
    private void updateFotoEstado(Foto foto, String nuevoEstado) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("estado", nuevoEstado);
        db.collection("fotos").document(foto.getId())
                .update(updateMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ValidarFotosActivity.this, "Foto actualizada a " + nuevoEstado, Toast.LENGTH_SHORT).show();
                    loadFotosPendientes();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ValidarFotosActivity.this, "Error al actualizar foto", Toast.LENGTH_SHORT).show();
                });
    }
}
