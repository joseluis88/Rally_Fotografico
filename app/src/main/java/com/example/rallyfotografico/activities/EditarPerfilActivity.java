package com.example.rallyfotografico.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.services.FirebaseService;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Actividad que permite al usuario editar su perfil: nombre, seudónimo y contraseña.
 * El correo electrónico no es editable. Requiere autenticación con contraseña actual.
 */
public class EditarPerfilActivity extends AppCompatActivity {

    // Elementos de UI
    private EditText etNombre, etSeudonimo, etEmail, etPasswordActual, etPasswordNueva, etConfirmarPassword;
    private ImageView ivToggleActual, ivToggleNueva, ivToggleConfirmar;

    // Servicios y datos de usuario
    private final FirebaseService firebaseService = FirebaseService.getInstance();
    private String userId, email;
    private final String adminPrincipal = "moto_castrol@hotmail.com";

    // Estados para mostrar/ocultar contraseñas
    private boolean mostrarActual = false;
    private boolean mostrarNueva = false;
    private boolean mostrarConfirmar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        // Inicializa elementos de UI
        etNombre = findViewById(R.id.etNombre);
        etSeudonimo = findViewById(R.id.etSeudonimo);
        etEmail = findViewById(R.id.etEmail);
        etPasswordActual = findViewById(R.id.etPasswordActual);
        etPasswordNueva = findViewById(R.id.etPasswordNueva);
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
        ivToggleActual = findViewById(R.id.ivToggleActual);
        ivToggleNueva = findViewById(R.id.ivToggleNueva);
        ivToggleConfirmar = findViewById(R.id.ivToggleConfirmar);

        // Carga datos actuales del usuario
        FirebaseUser currentUser = firebaseService.getAuth().getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
            etEmail.setText(email);
            etEmail.setEnabled(false);  // Email no editable
            userId = currentUser.getUid();

            firebaseService.getFirestore().collection("usuarios").document(userId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            etNombre.setText(doc.getString("nombre"));
                            if (!email.equals(adminPrincipal)) {
                                etSeudonimo.setText(doc.getString("seudonimo"));
                            } else {
                                etSeudonimo.setVisibility(View.GONE); // El admin principal no tiene seudónimo
                            }
                        }
                    });
        }

        // Botón de guardar cambios
        findViewById(R.id.btnGuardarCambios).setOnClickListener(v -> guardarCambios());

        // Listeners para mostrar/ocultar contraseñas
        ivToggleActual.setOnClickListener(v -> {
            mostrarActual = !mostrarActual;
            etPasswordActual.setInputType(mostrarActual ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleActual.setImageResource(mostrarActual ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off);
        });

        ivToggleNueva.setOnClickListener(v -> {
            mostrarNueva = !mostrarNueva;
            etPasswordNueva.setInputType(mostrarNueva ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleNueva.setImageResource(mostrarNueva ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off);
        });

        ivToggleConfirmar.setOnClickListener(v -> {
            mostrarConfirmar = !mostrarConfirmar;
            etConfirmarPassword.setInputType(mostrarConfirmar ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleConfirmar.setImageResource(mostrarConfirmar ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off);
        });

        // Validaciones visuales en tiempo real para contraseñas
        setupValidacionesTiempoReal();
    }

    /**
     * Guarda los cambios de perfil después de validar contraseñas y reautenticar al usuario.
     */
    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        String seudonimo = etSeudonimo.getText().toString().trim();
        String actualPass = etPasswordActual.getText().toString();
        String nuevaPass = etPasswordNueva.getText().toString();
        String confirmarPass = etConfirmarPassword.getText().toString();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(actualPass) || TextUtils.isEmpty(nuevaPass) || TextUtils.isEmpty(confirmarPass)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nuevaPass.equals(confirmarPass)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(nuevaPass)) {
            Toast.makeText(this, "Contraseña nueva inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reautenticación del usuario
        AuthCredential credential = EmailAuthProvider.getCredential(email, actualPass);
        FirebaseUser user = firebaseService.getAuth().getCurrentUser();

        if (user != null) {
            user.reauthenticate(credential).addOnSuccessListener(authResult -> {
                // Si se autentica correctamente, actualiza la contraseña
                user.updatePassword(nuevaPass).addOnSuccessListener(aVoid -> {
                    // Actualiza datos en Firestore
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("nombre", nombre);
                    if (!email.equals(adminPrincipal)) updates.put("seudonimo", seudonimo);

                    firebaseService.getFirestore().collection("usuarios").document(userId).update(updates).addOnSuccessListener(unused -> {
                        // Actualiza también el displayName en FirebaseAuth
                        if (!email.equals(adminPrincipal)) {
                            user.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(seudonimo).build());
                        }
                        Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }).addOnFailureListener(e -> Toast.makeText(this, "Error al cambiar contraseña", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Valida contraseñas en tiempo real y muestra feedback visual.
     */
    private void setupValidacionesTiempoReal() {
        // Validación de contraseña nueva
        etPasswordNueva.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (isPasswordValid(s.toString())) {
                    etPasswordNueva.setBackgroundResource(R.drawable.edittext_ok);
                } else {
                    etPasswordNueva.setBackgroundResource(R.drawable.edittext_error);
                }

                // Valida también confirmación
                if (!etConfirmarPassword.getText().toString().isEmpty()) {
                    if (etPasswordNueva.getText().toString().equals(etConfirmarPassword.getText().toString())) {
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
                if (s.toString().equals(etPasswordNueva.getText().toString()) && !s.toString().isEmpty()) {
                    etConfirmarPassword.setBackgroundResource(R.drawable.edittext_ok);
                } else {
                    etConfirmarPassword.setBackgroundResource(R.drawable.edittext_error);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    /**
     * Verifica si la contraseña es válida según los siguientes criterios:
     * - Mínimo 4 caracteres
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un número
     * - Al menos un carácter especial
     * @param password Contraseña a validar
     * @return true si es válida, false si no
     */
    private boolean isPasswordValid(String password) {
        Pattern mayuscula = Pattern.compile("[A-Z]");
        Pattern minuscula = Pattern.compile("[a-z]");
        Pattern numero = Pattern.compile("[0-9]");
        Pattern especial = Pattern.compile("[^a-zA-Z0-9]");
        return password.length() >= 4 &&
                mayuscula.matcher(password).find() &&
                minuscula.matcher(password).find() &&
                numero.matcher(password).find() &&
                especial.matcher(password).find();
    }
}
