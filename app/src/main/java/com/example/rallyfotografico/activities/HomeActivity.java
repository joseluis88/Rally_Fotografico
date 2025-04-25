package com.example.rallyfotografico.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.example.rallyfotografico.R;
import com.example.rallyfotografico.services.FirebaseService;
import com.google.firebase.firestore.DocumentReference;

/**
 * Actividad principal del inicio de la app.
 * Desde aquí se accede al registro, login, modo público y a las bases del concurso.
 * También carga una imagen personalizada y un título personalizado desde Firestore si están configurados.
 */
public class HomeActivity extends AppCompatActivity {

    // Elementos de la interfaz
    private Button btnRegistro, btnLogin, btnEntrarPublico, btnBases;
    private ImageView ivImagenInicio;
    private TextView tvTitulo; // Título dinámico

    // Servicios y referencias de Firestore
    private final FirebaseService firebaseService = FirebaseService.getInstance();
    private DocumentReference configRef;
    private String urlImagenInicio = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Establece el modo noche según el sistema
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Solicita permiso para notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }

        // Inicializa elementos visuales
        btnRegistro = findViewById(R.id.btnRegistro);
        btnLogin = findViewById(R.id.btnLogin);
        btnEntrarPublico = findViewById(R.id.btnEntrarPublico);
        btnBases = findViewById(R.id.btnBases);
        ivImagenInicio = findViewById(R.id.ivImagenInicio);
        tvTitulo = findViewById(R.id.tvTitulo); // Nuevo título dinámico

        // Referencia al documento de configuración
        configRef = firebaseService.getFirestore().collection("configuracion").document("rally");

        // Botón para ir a RegistroActivity
        btnRegistro.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, RegistroActivity.class)));

        // Botón para ir a LoginActivity
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LoginActivity.class)));

        // Botón para entrar en modo público
        btnEntrarPublico.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, PublicHomeActivity.class)));

        // Botón para ver las bases del concurso
        btnBases.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, BasesActivity.class)));

        // Clic sobre la imagen de inicio: la muestra en pantalla completa si existe
        ivImagenInicio.setOnClickListener(v -> {
            if (urlImagenInicio != null && !urlImagenInicio.isEmpty()) {
                Intent intent = new Intent(HomeActivity.this, PreviewImageActivity.class);
                intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_URI, urlImagenInicio);
                startActivity(intent);
            }
        });
    }

    /**
     * Al volver a esta actividad, se vuelve a cargar la imagen y el título de inicio.
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarImagenDeInicio();
        cargarTituloDeInicio();
    }

    /**
     * Carga la URL de la imagen definida por el administrador desde Firestore.
     */
    private void cargarImagenDeInicio() {
        configRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("imagenInicio")) {
                urlImagenInicio = documentSnapshot.getString("imagenInicio");
                Log.d("HomeActivity", "URL imagen cargada: " + urlImagenInicio);

                if (urlImagenInicio != null && !urlImagenInicio.isEmpty()) {
                    Glide.with(this)
                            .load(urlImagenInicio)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(ivImagenInicio);
                }
            } else {
                Log.d("HomeActivity", "No hay imagenInicio en Firestore.");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al cargar imagen de inicio", Toast.LENGTH_SHORT).show();
            Log.e("HomeActivity", "Error al cargar desde Firestore: ", e);
        });
    }

    /**
     * Carga el título de inicio definido por el administrador desde Firestore.
     */
    private void cargarTituloDeInicio() {
        configRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("tituloInicio")) {
                String tituloInicio = documentSnapshot.getString("tituloInicio");
                if (tituloInicio != null && !tituloInicio.isEmpty()) {
                    tvTitulo.setText(tituloInicio);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al cargar el título", Toast.LENGTH_SHORT).show();
            Log.e("HomeActivity", "Error al obtener tituloInicio", e);
        });
    }
}
