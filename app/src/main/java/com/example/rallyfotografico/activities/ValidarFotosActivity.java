package com.example.rallyfotografico.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.adapters.ValidarFotosAdapter;
import com.example.rallyfotografico.models.Foto;
import com.example.rallyfotografico.services.FirebaseService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actividad utilizada por los administradores para validar (admitir o rechazar) las fotos subidas por los participantes.
 */
public class ValidarFotosActivity extends AppCompatActivity {

    // Vista que contiene la lista de fotos pendientes
    private RecyclerView rvFotosPendientes;

    // Lista de objetos Foto pendientes de validación
    private List<Foto> fotoList;

    // Adaptador que maneja la visualización de las fotos y los botones de validar
    private ValidarFotosAdapter adapter;

    // Texto que se muestra si no hay fotos pendientes
    private TextView tvSinFotos;

    // Servicio de Firebase encapsulado
    private final FirebaseService firebase = FirebaseService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validar_fotos);

        // Referencias a elementos de la interfaz
        rvFotosPendientes = findViewById(R.id.rvFotosPendientes);
        rvFotosPendientes.setLayoutManager(new LinearLayoutManager(this));
        fotoList = new ArrayList<>();
        tvSinFotos = findViewById(R.id.tvSinFotos);

        // Configuración del adaptador y acciones para admitir o rechazar
        adapter = new ValidarFotosAdapter(fotoList, new ValidarFotosAdapter.OnFotoValidationListener() {
            @Override
            public void onAdmitirClick(Foto foto) {
                updateFotoEstado(foto, "admitida"); // Cambia estado a admitida
            }

            @Override
            public void onRechazarClick(Foto foto) {
                updateFotoEstado(foto, "rechazada"); // Cambia estado a rechazada
            }
        });

        rvFotosPendientes.setAdapter(adapter);

        // Carga las fotos pendientes desde Firestore
        loadFotosPendientes();
    }

    /**
     * Carga desde Firestore todas las fotos cuyo estado sea "pendiente".
     * Actualiza la lista y muestra/oculta el mensaje de "sin fotos".
     */
    private void loadFotosPendientes() {
        firebase.getFirestore().collection("fotos")
                .whereEqualTo("estado", "pendiente")
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

                    adapter.notifyDataSetChanged(); // Actualiza la vista del RecyclerView

                    if (fotoList.isEmpty()) {
                        // Si no hay fotos pendientes, se muestra el mensaje correspondiente
                        tvSinFotos.setVisibility(View.VISIBLE);
                        rvFotosPendientes.setVisibility(View.GONE);
                    } else {
                        tvSinFotos.setVisibility(View.GONE);
                        rvFotosPendientes.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ValidarFotosActivity.this, "Error al cargar fotos", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Actualiza el estado de una foto (admitida o rechazada) en Firestore.
     * @param foto La foto que se desea actualizar.
     * @param nuevoEstado El nuevo estado que se asignará ("admitida" o "rechazada").
     */
    private void updateFotoEstado(Foto foto, String nuevoEstado) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("estado", nuevoEstado);

        firebase.getFirestore().collection("fotos").document(foto.getId())
                .update(updateMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ValidarFotosActivity.this, "Foto actualizada a " + nuevoEstado, Toast.LENGTH_SHORT).show();
                    loadFotosPendientes(); // Recarga la lista para reflejar los cambios
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ValidarFotosActivity.this, "Error al actualizar foto", Toast.LENGTH_SHORT).show();
                });
    }
}
