package com.example.rallyfotografico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.services.FirebaseService;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Actividad encargada del registro de nuevos usuarios.
 * Incluye validaciones visuales en tiempo real, verificación de unicidad del seudónimo,
 * creación del usuario en FirebaseAuth y almacenamiento de datos en Firestore.
 */
public class RegistroActivity extends AppCompatActivity {

    private EditText etNombre, etSeudonimo, etEmail, etPassword, etConfirmarPassword;
    private ImageView ivTogglePassword, ivToggleConfirmPassword;
    private Button btnRegistro;
    private ProgressBar progressBar;

    private boolean passwordVisible = false;
    private boolean confirmVisible = false;

    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicialización de componentes
        firebaseService = FirebaseService.getInstance();

        etNombre = findViewById(R.id.etNombre);
        etSeudonimo = findViewById(R.id.etSeudonimo);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword);
        btnRegistro = findViewById(R.id.btnRegistro);
        progressBar = findViewById(R.id.progressBar);

        // Listener del botón de registro
        btnRegistro.setOnClickListener(v -> validarYRegistrarUsuario());

        // Configuración de iconos de visibilidad de contraseña
        setupToggleVisibility();

        // Validación en tiempo real de campos
        setupRealtimeValidation();
    }

    /**
     * Valida los campos del formulario y lanza la creación del usuario si todo es correcto.
     */
    private void validarYRegistrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String seudonimo = etSeudonimo.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmarPassword = etConfirmarPassword.getText().toString();

        // Validaciones básicas
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(seudonimo) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmarPassword)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmarPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(password)) {
            Toast.makeText(this, "La contraseña debe tener al menos 4 caracteres, una mayúscula, una minúscula, un número y un carácter especial.", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Verificamos que el seudonimo no esté en uso
        firebaseService.getFirestore().collection("usuarios")
                .whereEqualTo("seudonimo", seudonimo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "El seudónimo ya está en uso. Elige otro.", Toast.LENGTH_SHORT).show();
                    } else {
                        crearUsuario(nombre, seudonimo, email, password);
                    }
                });
    }

    /**
     * Registra un nuevo usuario en Firebase Authentication y guarda sus datos en Firestore.
     */
    private void crearUsuario(String nombre, String seudonimo, String email, String password) {
        firebaseService.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        String userId = firebaseService.getAuth().getCurrentUser().getUid();

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("nombre", nombre);
                        userMap.put("seudonimo", seudonimo);
                        userMap.put("email", email);
                        userMap.put("rol", "participante");

                        firebaseService.getFirestore().collection("usuarios").document(userId).set(userMap)
                                .addOnCompleteListener(t -> {
                                    if (t.isSuccessful()) {
                                        // Guardar seudónimo como displayName en FirebaseAuth
                                        firebaseService.getAuth().getCurrentUser().updateProfile(
                                                new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(seudonimo)
                                                        .build()
                                        );
                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Error al guardar datos en Firestore", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Valida si una contraseña cumple con los requisitos de seguridad.
     */
    private boolean isPasswordValid(String password) {
        Pattern mayuscula = Pattern.compile("[A-Z]");
        Pattern minuscula = Pattern.compile("[a-z]");
        Pattern especial = Pattern.compile("[^a-zA-Z0-9]");
        Pattern numero = Pattern.compile("[0-9]");
        return password.length() >= 4 &&
                mayuscula.matcher(password).find() &&
                minuscula.matcher(password).find() &&
                numero.matcher(password).find() &&
                especial.matcher(password).find();
    }

    /**
     * Configura los iconos de visibilidad de los campos de contraseña.
     */
    private void setupToggleVisibility() {
        ivTogglePassword.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            etPassword.setInputType(passwordVisible ?
                    android.text.InputType.TYPE_CLASS_TEXT :
                    android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etPassword.setSelection(etPassword.getText().length());
            ivTogglePassword.setImageResource(passwordVisible ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off);
        });

        ivToggleConfirmPassword.setOnClickListener(v -> {
            confirmVisible = !confirmVisible;
            etConfirmarPassword.setInputType(confirmVisible ?
                    android.text.InputType.TYPE_CLASS_TEXT :
                    android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etConfirmarPassword.setSelection(etConfirmarPassword.getText().length());
            ivToggleConfirmPassword.setImageResource(confirmVisible ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off);
        });
    }

    /**
     * Valida en tiempo real los campos de email y contraseñas mientras el usuario escribe.
     */
    private void setupRealtimeValidation() {
        // Validación de email
        etEmail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                    etEmail.setBackgroundResource(R.drawable.edittext_ok);
                } else {
                    etEmail.setBackgroundResource(R.drawable.edittext_error);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Validación de contraseña
        etPassword.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (isPasswordValid(s.toString())) {
                    etPassword.setBackgroundResource(R.drawable.edittext_ok);
                } else {
                    etPassword.setBackgroundResource(R.drawable.edittext_error);
                }

                // Comparar contraseñas en tiempo real
                if (!etConfirmarPassword.getText().toString().isEmpty()) {
                    if (etPassword.getText().toString().equals(etConfirmarPassword.getText().toString())) {
                        etConfirmarPassword.setBackgroundResource(R.drawable.edittext_ok);
                    } else {
                        etConfirmarPassword.setBackgroundResource(R.drawable.edittext_error);
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Validación de confirmación de contraseña
        etConfirmarPassword.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(etPassword.getText().toString()) && !s.toString().isEmpty()) {
                    etConfirmarPassword.setBackgroundResource(R.drawable.edittext_ok);
                } else {
                    etConfirmarPassword.setBackgroundResource(R.drawable.edittext_error);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }
}
