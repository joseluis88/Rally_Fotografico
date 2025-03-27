package com.example.rallyfotografico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rallyfotografico.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnGestionUsuarios, btnConfigRally, btnValidarFotos, btnVerEstadisticas;
    private static final String ROOT_ADMIN_EMAIL = "moto_castrol@hotmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);
        btnConfigRally = findViewById(R.id.btnConfigRally);
        btnValidarFotos = findViewById(R.id.btnValidarFotos);
        btnVerEstadisticas = findViewById(R.id.btnVerEstadisticas);

        // Inicializamos FirebaseAuth y Firestore
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Consultamos el documento del usuario para obtener su rol
        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("rol");
                    if (role == null || !role.equalsIgnoreCase("administrador")) {
                        Toast.makeText(AdminDashboardActivity.this, "Acceso denegado", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    // Si el usuario es administrador, verificamos si es el administrador principal
                    if (!currentUser.getEmail().equals(ROOT_ADMIN_EMAIL)) {
                        // Administrador promovido: ocultamos el botón de "Gestionar Usuarios"
                        btnGestionUsuarios.setVisibility(Button.GONE);
                    } else {
                        // Usuario principal: configuramos el listener para gestionar usuarios
                        btnGestionUsuarios.setOnClickListener(v -> {
                            startActivity(new Intent(AdminDashboardActivity.this, GestionUsuariosActivity.class));
                        });
                    }
                    // Configuración de los otros botones (para ambos tipos de administradores)
                    btnConfigRally.setOnClickListener(v -> {
                        startActivity(new Intent(AdminDashboardActivity.this, ConfigRallyActivity.class));
                    });

                    btnValidarFotos.setOnClickListener(v -> {
                        startActivity(new Intent(AdminDashboardActivity.this, ValidarFotosActivity.class));
                    });

                    btnVerEstadisticas.setOnClickListener(v -> {
                        startActivity(new Intent(AdminDashboardActivity.this, StatsActivity.class));
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminDashboardActivity.this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
