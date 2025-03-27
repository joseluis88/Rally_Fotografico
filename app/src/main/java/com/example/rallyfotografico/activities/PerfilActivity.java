package com.example.rallyfotografico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.adapters.FotoAdapter;
import com.example.rallyfotografico.models.Foto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class PerfilActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etRol;
    private Button btnCerrarSesion, btnAdminDashboard, btnSubirFotos, btnEditarPerfil;
    private RecyclerView rvMisFotos;
    private TextView tvMisFotos;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    // Solo se mostrarán las fotos que tengan estado "admitida"
    private static final boolean ONLY_ADMITTED_PHOTOS = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etRol = findViewById(R.id.etRol);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnAdminDashboard = findViewById(R.id.btnAdminDashboard);
        btnSubirFotos = findViewById(R.id.btnSubirFotos);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        rvMisFotos = findViewById(R.id.rvMisFotos);
        tvMisFotos = findViewById(R.id.tvMisFotos);

        // Ocultar botones y sección por defecto
        btnAdminDashboard.setVisibility(View.GONE);
        btnSubirFotos.setVisibility(View.GONE);
        btnEditarPerfil.setVisibility(View.GONE);
        tvMisFotos.setVisibility(View.GONE);
        rvMisFotos.setVisibility(View.GONE);

        // Cargar datos del usuario desde Firestore
        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String email = documentSnapshot.getString("email");
                        String rol = documentSnapshot.getString("rol");

                        etNombre.setText(nombre);
                        etEmail.setText(email);
                        etRol.setText(rol);

                        // Si el usuario es participante, mostrar su sección de fotos
                        if (rol != null && rol.equalsIgnoreCase("participante")) {
                            btnSubirFotos.setVisibility(View.VISIBLE);
                            tvMisFotos.setVisibility(View.VISIBLE);
                            rvMisFotos.setVisibility(View.VISIBLE);
                            cargarFotosUsuario(ONLY_ADMITTED_PHOTOS);
                        } else if (rol != null && rol.equalsIgnoreCase("administrador")) {
                            btnAdminDashboard.setVisibility(View.VISIBLE);
                            tvMisFotos.setVisibility(View.GONE);
                            rvMisFotos.setVisibility(View.GONE);
                        }
                        // Mostrar botón de editar perfil para todos
                        btnEditarPerfil.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PerfilActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show());

        // Botón Panel de Administración
        btnAdminDashboard.setOnClickListener(v -> startActivity(new Intent(PerfilActivity.this, AdminDashboardActivity.class)));

        // Botón Subir Fotos
        btnSubirFotos.setOnClickListener(v -> startActivity(new Intent(PerfilActivity.this, UploadPhotoActivity.class)));

        // Botón Editar Perfil
        btnEditarPerfil.setOnClickListener(v -> startActivity(new Intent(PerfilActivity.this, EditarPerfilActivity.class)));

        // Botón Cerrar Sesión
        btnCerrarSesion.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(PerfilActivity.this, HomeActivity.class));
            finish();
        });

        rvMisFotos.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Carga las fotos del usuario. Si ONLY_ADMITTED_PHOTOS es true, filtra por estado "admitida".
     */
    private void cargarFotosUsuario(boolean onlyAdmitted) {
        db.collection("fotos")
                .whereEqualTo("idUsuario", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Foto> fotoList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Foto foto = doc.toObject(Foto.class);
                        if (foto != null) {
                            foto.setId(doc.getId());
                            fotoList.add(foto);
                            // Solo se agregan las fotos que están admitidas si se requiere filtrado
                           /* if (!onlyAdmitted || "admitida".equalsIgnoreCase(foto.getEstado())) {
                                fotoList.add(foto);
                            }*/
                        }
                    }
                    // Configurar el adaptador con el botón de eliminar habilitado
                    FotoAdapter fotoAdapter = new FotoAdapter(fotoList, foto -> eliminarFoto(foto), true);
                    rvMisFotos.setAdapter(fotoAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PerfilActivity.this, "Error al cargar fotografías", Toast.LENGTH_SHORT).show());
    }

    // Método para eliminar la foto, con confirmación
    private void eliminarFoto(Foto foto) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Foto")
                .setMessage("¿Estás seguro de que deseas eliminar esta foto? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    db.collection("fotos").document(foto.getId())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                String ownerId = documentSnapshot.getString("idUsuario");
                                if (ownerId != null && ownerId.equals(userId)) {
                                    db.collection("fotos").document(foto.getId())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                FirebaseStorage.getInstance().getReferenceFromUrl(foto.getUrl())
                                                        .delete()
                                                        .addOnSuccessListener(aVoid2 -> {
                                                            Toast.makeText(PerfilActivity.this, "Foto eliminada correctamente", Toast.LENGTH_SHORT).show();
                                                            cargarFotosUsuario(ONLY_ADMITTED_PHOTOS);
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(PerfilActivity.this, "Error al eliminar la foto del almacenamiento", Toast.LENGTH_SHORT).show());
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(PerfilActivity.this, "Error al eliminar la foto", Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(PerfilActivity.this, "No puedes eliminar una foto que no es tuya", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(PerfilActivity.this, "Error al validar la foto", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos del usuario
        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String email = documentSnapshot.getString("email");
                        String rol = documentSnapshot.getString("rol");
                        etNombre.setText(nombre);
                        etEmail.setText(email);
                        etRol.setText(rol);

                        // Si el usuario es participante, recargar las fotos para actualizar su estado
                        if (rol != null && rol.equalsIgnoreCase("participante")) {
                            cargarFotosUsuario(ONLY_ADMITTED_PHOTOS);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PerfilActivity.this, "Error al recargar datos", Toast.LENGTH_SHORT).show());
    }

}
