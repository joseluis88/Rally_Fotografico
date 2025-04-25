package com.example.rallyfotografico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.services.FirebaseService;
import com.google.firebase.auth.FirebaseUser;

/**
 * Actividad encargada del inicio de sesión de usuarios.
 * Permite al usuario autenticarse con correo y contraseña usando Firebase.
 * También incluye una opción para mostrar/ocultar la contraseña ingresada.
 */
public class LoginActivity extends AppCompatActivity {

    // Campos de entrada de usuario
    private EditText etEmail, etPassword;
    private ImageView ivTogglePasswordLogin;
    private Button btnLogin;
    private ProgressBar progressBar;

    // Controla la visibilidad del texto de la contraseña
    private boolean isPasswordVisible = false;

    // Instancia del servicio Firebase personalizado
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa la instancia de FirebaseService
        firebaseService = FirebaseService.getInstance();

        // Referencias a los elementos de la interfaz
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePasswordLogin = findViewById(R.id.ivTogglePasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Listener para mostrar/ocultar contraseña
        ivTogglePasswordLogin.setOnClickListener(v -> togglePasswordVisibility());

        // Listener para iniciar sesión
        btnLogin.setOnClickListener(v -> loginUsuario());
    }

    /**
     * Alterna la visibilidad de la contraseña entre texto plano y oculto.
     * También cambia el icono del botón correspondiente.
     */
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePasswordLogin.setImageResource(R.drawable.ic_visibility_off);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePasswordLogin.setImageResource(R.drawable.ic_visibility_on);
        }

        // Mantiene el cursor al final
        etPassword.setSelection(etPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    /**
     * Valida los campos de email y contraseña, y si están correctos,
     * intenta iniciar sesión usando FirebaseAuth.
     * Muestra la barra de progreso mientras se realiza la autenticación.
     */
    private void loginUsuario() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validación de campos vacíos
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Ingrese email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Intento de inicio de sesión con Firebase
        firebaseService.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseService.getAuth().getCurrentUser();
                        if (user != null) {
                            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, PerfilActivity.class));
                            finish(); // Cierra la pantalla de login
                        }
                    } else {
                        Toast.makeText(this, "Error en la autenticación: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
