package com.example.rallyfotografico.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.adapters.FotoAdapter;
import com.example.rallyfotografico.models.Foto;
import com.example.rallyfotografico.services.FirebaseService;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PerfilActivity extends AppCompatActivity {

    // Declaración de vistas y variables
    private EditText etNombre, etEmail, etRol, etSeudonimo;
    private Button btnCerrarSesion, btnAdminDashboard, btnSubirFotos, btnEditarPerfil, btnCambiarImagenInicio;
    private RecyclerView rvMisFotos;
    private TextView tvMisFotos;
    private Switch switchNotificaciones;
    private LinearLayout layoutSwitch;

    private final FirebaseService firebase = FirebaseService.getInstance();
    private String userId;
    private static final boolean ONLY_ADMITTED_PHOTOS = true;
    private static final int PICK_IMAGE_REQUEST = 100;
    private final String ADMIN_EMAIL = "moto_castrol@hotmail.com";
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Validar si hay usuario autenticado
        FirebaseUser currentUser = firebase.getAuth().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        // Solicitud de permisos de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }

        userId = currentUser.getUid();

        // Inicialización de vistas
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etRol = findViewById(R.id.etRol);
        etSeudonimo = findViewById(R.id.etSeudonimo);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnAdminDashboard = findViewById(R.id.btnAdminDashboard);
        btnSubirFotos = findViewById(R.id.btnSubirFotos);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnCambiarImagenInicio = findViewById(R.id.btnSubirImagenInicio);
        switchNotificaciones = findViewById(R.id.switchNotificaciones);
        layoutSwitch = findViewById(R.id.layoutSwitch);
        rvMisFotos = findViewById(R.id.rvMisFotos);
        tvMisFotos = findViewById(R.id.tvMisFotos);

        rvMisFotos.setLayoutManager(new LinearLayoutManager(this));

        // Ocultar botones y campos por defecto
        btnAdminDashboard.setVisibility(View.GONE);
        btnSubirFotos.setVisibility(View.GONE);
        btnEditarPerfil.setVisibility(View.GONE);
        tvMisFotos.setVisibility(View.GONE);
        rvMisFotos.setVisibility(View.GONE);
        btnCambiarImagenInicio.setVisibility(View.GONE);
        etSeudonimo.setVisibility(View.GONE);
        layoutSwitch.setVisibility(View.GONE);

        // Cargar datos del usuario desde Firestore
        firebase.getFirestore().collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Mostrar datos en los campos
                        String nombre = documentSnapshot.getString("nombre");
                        String email = documentSnapshot.getString("email");
                        String rol = documentSnapshot.getString("rol");
                        String seudonimo = documentSnapshot.getString("seudonimo");
                        boolean notificaciones = Boolean.TRUE.equals(documentSnapshot.getBoolean("notificacionesActivas"));

                        etNombre.setText(nombre);
                        etEmail.setText(email);
                        etRol.setText(rol);
                        etSeudonimo.setText(seudonimo);

                        if ("participante".equalsIgnoreCase(rol)) {
                            // Mostrar funcionalidades para participantes
                            etSeudonimo.setVisibility(View.VISIBLE);
                            validarDisponibilidadSubida();
                            tvMisFotos.setVisibility(View.VISIBLE);
                            rvMisFotos.setVisibility(View.VISIBLE);
                            cargarFotosUsuario(ONLY_ADMITTED_PHOTOS);
                        } else if ("administrador".equalsIgnoreCase(rol)) {
                            // Mostrar funcionalidades para administradores
                            btnAdminDashboard.setVisibility(View.VISIBLE);
                            layoutSwitch.setVisibility(View.VISIBLE);
                            switchNotificaciones.setChecked(notificaciones);

                            if (notificaciones) guardarTokenFCM();
                            else desactivarTokenFCM();

                            switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                firebase.getFirestore().collection("usuarios").document(userId)
                                        .update("notificacionesActivas", isChecked);
                                if (isChecked) guardarTokenFCM();
                                else desactivarTokenFCM();
                            });

                            // Mostrar botón de imagen si es admin principal
                            if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
                                btnCambiarImagenInicio.setVisibility(View.VISIBLE);
                            }
                        }

                        btnEditarPerfil.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PerfilActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show());

        // Botones de navegación
        btnAdminDashboard.setOnClickListener(v -> startActivity(new Intent(this, AdminDashboardActivity.class)));
        btnSubirFotos.setOnClickListener(v -> startActivity(new Intent(this, UploadPhotoActivity.class)));
        btnEditarPerfil.setOnClickListener(v -> startActivity(new Intent(this, EditarPerfilActivity.class)));
        btnCerrarSesion.setOnClickListener(v -> {
            firebase.getAuth().signOut();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        // Subir imagen de inicio (admin principal)
        btnCambiarImagenInicio.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Selecciona imagen de inicio"), PICK_IMAGE_REQUEST);
        });
    }

    // Guardar token FCM del administrador para recibir notificaciones
    private void guardarTokenFCM() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            firebase.getFirestore().collection("usuarios")
                    .document(userId)
                    .update("fcmToken", token);
        });
    }

    // Eliminar token FCM del administrador
    private void desactivarTokenFCM() {
        firebase.getFirestore().collection("usuarios").document(userId)
                .update("fcmToken", null);
    }

    // Resultado del selector de imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            subirImagenDeInicio(imageUri);
        }
    }

    // Subir imagen a Firebase Storage y guardar la URL en Firestore
    private void subirImagenDeInicio(Uri uri) {
        if (uri == null) return;
        firebase.getStorage().getReference("imagenes_inicio/inicio.jpg")
                .putFile(uri)
                .addOnSuccessListener(taskSnapshot -> firebase.getStorage().getReference("imagenes_inicio/inicio.jpg")
                        .getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            firebase.getFirestore().collection("configuracion").document("rally")
                                    .update("imagenInicio", downloadUri.toString())
                                    .addOnSuccessListener(unused -> Toast.makeText(this, "Imagen de inicio actualizada", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la URL", Toast.LENGTH_SHORT).show());
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show());
    }

    // Validar si aún se permite subir fotos (según fecha límite)
    private void validarDisponibilidadSubida() {
        firebase.getFirestore().collection("configuracion").document("rally")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String fechaLimiteStr = documentSnapshot.getString("fechaLimite");
                    try {
                        Date fechaActual = new Date();
                        Date fechaLimite = sdf.parse(fechaLimiteStr);
                        if (fechaLimite != null && fechaActual.before(fechaLimite)) {
                            btnSubirFotos.setVisibility(View.VISIBLE);
                        } else {
                            btnSubirFotos.setVisibility(View.GONE);
                        }
                    } catch (ParseException e) {
                        Toast.makeText(this, "Error al validar la fecha límite.", Toast.LENGTH_SHORT).show();
                        btnSubirFotos.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "No se pudo verificar la fecha límite.", Toast.LENGTH_SHORT).show();
                    btnSubirFotos.setVisibility(View.GONE);
                });
    }

    // Cargar fotos del usuario (filtradas o no por estado)
    private void cargarFotosUsuario(boolean onlyAdmitted) {
        firebase.getFirestore().collection("fotos")
                .whereEqualTo("idUsuario", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Foto> fotoList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Foto foto = doc.toObject(Foto.class);
                        if (foto != null) {
                            foto.setId(doc.getId());
                            fotoList.add(foto);
                        }
                    }
                    FotoAdapter fotoAdapter = new FotoAdapter(fotoList, foto -> eliminarFoto(foto), true);
                    rvMisFotos.setAdapter(fotoAdapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar fotografías", Toast.LENGTH_SHORT).show());
    }

    // Confirmar y eliminar foto del usuario
    private void eliminarFoto(Foto foto) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Foto")
                .setMessage("¿Estás seguro de que deseas eliminar esta foto?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    firebase.getFirestore().collection("fotos").document(foto.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> firebase.getStorage().getReferenceFromUrl(foto.getUrl())
                                    .delete()
                                    .addOnSuccessListener(aVoid2 -> {
                                        Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show();
                                        cargarFotosUsuario(ONLY_ADMITTED_PHOTOS);
                                    }));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Refrescar información del usuario cada vez que vuelve a la pantalla
    @Override
    protected void onResume() {
        super.onResume();
        firebase.getFirestore().collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String email = documentSnapshot.getString("email");
                        String rol = documentSnapshot.getString("rol");
                        String seudonimo = documentSnapshot.getString("seudonimo");

                        etNombre.setText(nombre);
                        etEmail.setText(email);
                        etRol.setText(rol);
                        etSeudonimo.setText(seudonimo);

                        if ("participante".equalsIgnoreCase(rol)) {
                            etSeudonimo.setVisibility(View.VISIBLE);
                            cargarFotosUsuario(ONLY_ADMITTED_PHOTOS);
                        }
                    }
                });
    }
}
