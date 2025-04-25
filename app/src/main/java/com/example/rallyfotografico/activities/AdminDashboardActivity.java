package com.example.rallyfotografico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Actividad del panel de control del administrador.
 * Desde aquí se pueden acceder a diferentes funcionalidades administrativas como:
 * - Gestión de usuarios (solo el admin principal)
 * - Configuración del rally
 * - Validación de fotos
 * - Estadísticas del concurso
 */
public class AdminDashboardActivity extends AppCompatActivity {

    // Botones del panel
    private Button btnGestionUsuarios, btnConfigRally, btnValidarFotos, btnVerEstadisticas;

    // Email del administrador principal con permisos extra (root admin)
    private static final String ROOT_ADMIN_EMAIL = "moto_castrol@hotmail.com";

    // Instancia del servicio Firebase personalizado
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Inicializa FirebaseService
        firebaseService = FirebaseService.getInstance();

        // Asocia los botones con sus vistas del layout
        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);
        btnConfigRally = findViewById(R.id.btnConfigRally);
        btnValidarFotos = findViewById(R.id.btnValidarFotos);
        btnVerEstadisticas = findViewById(R.id.btnVerEstadisticas);

        // Obtiene instancias de FirebaseAuth y Firestore
        FirebaseAuth mAuth = firebaseService.getAuth();
        FirebaseFirestore db = firebaseService.getFirestore();

        // Obtiene el usuario actualmente autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Si no hay usuario autenticado, se cierra la actividad
        if (currentUser == null) {
            Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Verifica el rol del usuario en la base de datos
        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("rol");

                    // Si no tiene rol o no es "administrador", se deniega el acceso
                    if (role == null || !role.equalsIgnoreCase("administrador")) {
                        Toast.makeText(AdminDashboardActivity.this, "Acceso denegado", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Si el usuario no es el admin principal, se oculta el botón de gestión de usuarios
                    if (!currentUser.getEmail().equals(ROOT_ADMIN_EMAIL)) {
                        btnGestionUsuarios.setVisibility(Button.GONE);
                    } else {
                        // Si es el admin principal, se habilita el botón
                        btnGestionUsuarios.setOnClickListener(v -> {
                            startActivity(new Intent(AdminDashboardActivity.this, GestionUsuariosActivity.class));
                        });
                    }

                    // Acción para botón de configuración del rally
                    btnConfigRally.setOnClickListener(v -> {
                        startActivity(new Intent(AdminDashboardActivity.this, ConfigRallyActivity.class));
                    });

                    // Acción para botón de validación de fotos
                    btnValidarFotos.setOnClickListener(v -> {
                        startActivity(new Intent(AdminDashboardActivity.this, ValidarFotosActivity.class));
                    });

                    // Acción para botón de estadísticas
                    btnVerEstadisticas.setOnClickListener(v -> {
                        startActivity(new Intent(AdminDashboardActivity.this, StatsActivity.class));
                    });
                })
                .addOnFailureListener(e -> {
                    // Error al consultar los datos del usuario en Firestore
                    Toast.makeText(AdminDashboardActivity.this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
