package com.example.rallyfotografico.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rallyfotografico.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class ConfigRallyActivity extends AppCompatActivity {

    private EditText etTamañoMaximo, etResolucion, etTipoImagen, etFechaLimite, etLimiteFotos, etFechaInicioVotacion, etFechaFinVotacion;
    private Button btnGuardarConfig;
    private FirebaseFirestore db;
    private DocumentReference configRef;
    private Calendar calendar;

    // Formato de fecha para almacenar en Firestore, por ejemplo "dd/MM/yyyy"
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_rally);

        // Vincular los elementos del layout
        etTamañoMaximo = findViewById(R.id.etTamañoMaximo);
        etResolucion = findViewById(R.id.etResolucion);
        etTipoImagen = findViewById(R.id.etTipoImagen);
        etFechaLimite = findViewById(R.id.etFechaLimite);
        etLimiteFotos = findViewById(R.id.etLimiteFotos);
        etFechaInicioVotacion = findViewById(R.id.etFechaInicioVotacion);
        etFechaFinVotacion = findViewById(R.id.etFechaFinVotacion);
        btnGuardarConfig = findViewById(R.id.btnGuardarConfig);

        db = FirebaseFirestore.getInstance();
        // Se asume que la configuración está en la colección "configuracion" y el documento "rally"
        configRef = db.collection("configuracion").document("rally");

        calendar = Calendar.getInstance();

        // Configurar DatePicker para los campos de fecha
        etFechaLimite.setOnClickListener(v -> showDatePickerDialog(etFechaLimite));
        etFechaInicioVotacion.setOnClickListener(v -> showDatePickerDialog(etFechaInicioVotacion));
        etFechaFinVotacion.setOnClickListener(v -> showDatePickerDialog(etFechaFinVotacion));

        // Cargar la configuración actual si existe
        loadConfig();

        btnGuardarConfig.setOnClickListener(v -> {
            String tamañoMaximoStr = etTamañoMaximo.getText().toString().trim();
            String resolucion = etResolucion.getText().toString().trim();
            String tipoImagen = etTipoImagen.getText().toString().trim();
            String fechaLimite = etFechaLimite.getText().toString().trim();
            String limiteFotosStr = etLimiteFotos.getText().toString().trim();
            String fechaInicioVotacion = etFechaInicioVotacion.getText().toString().trim();
            String fechaFinVotacion = etFechaFinVotacion.getText().toString().trim();

            if (TextUtils.isEmpty(tamañoMaximoStr) || TextUtils.isEmpty(resolucion) || TextUtils.isEmpty(tipoImagen)
                    || TextUtils.isEmpty(fechaLimite) || TextUtils.isEmpty(limiteFotosStr)
                    || TextUtils.isEmpty(fechaInicioVotacion) || TextUtils.isEmpty(fechaFinVotacion)) {
                Toast.makeText(ConfigRallyActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double tamañoMaximoMB = Double.parseDouble(tamañoMaximoStr);
            int limiteFotos = Integer.parseInt(limiteFotosStr);

            Map<String, Object> configMap = new HashMap<>();
            configMap.put("tamañoMaximoMB", tamañoMaximoMB);
            configMap.put("resolucion", resolucion);
            configMap.put("tipoImagen", tipoImagen); // Ej: "jpg,png,jpeg"
            configMap.put("fechaLimite", fechaLimite);
            configMap.put("limiteFotos", limiteFotos);
            configMap.put("fechaInicioVotacion", fechaInicioVotacion);
            configMap.put("fechaFinVotacion", fechaFinVotacion);

            configRef.set(configMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ConfigRallyActivity.this, "Configuración guardada", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ConfigRallyActivity.this, AdminDashboardActivity.class));
                } else {
                    Toast.makeText(ConfigRallyActivity.this, "Error al guardar configuración", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showDatePickerDialog(EditText targetEditText) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ConfigRallyActivity.this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    targetEditText.setText(dateFormat.format(cal.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Cargar la configuración actual desde Firestore (si existe)
    private void loadConfig() {
        configRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if(documentSnapshot.contains("tamañoMaximoMB")){
                    double tamañoMaximo = documentSnapshot.getDouble("tamañoMaximoMB");
                    etTamañoMaximo.setText(String.valueOf(tamañoMaximo));
                }
                if(documentSnapshot.contains("resolucion")){
                    etResolucion.setText(documentSnapshot.getString("resolucion"));
                }
                if(documentSnapshot.contains("tipoImagen")){
                    etTipoImagen.setText(documentSnapshot.getString("tipoImagen"));
                }
                if(documentSnapshot.contains("fechaLimite")){
                    etFechaLimite.setText(documentSnapshot.getString("fechaLimite"));
                }
                if(documentSnapshot.contains("limiteFotos")){
                    etLimiteFotos.setText(String.valueOf(documentSnapshot.getLong("limiteFotos")));
                }
                if(documentSnapshot.contains("fechaInicioVotacion")){
                    etFechaInicioVotacion.setText(documentSnapshot.getString("fechaInicioVotacion"));
                }
                if(documentSnapshot.contains("fechaFinVotacion")){
                    etFechaFinVotacion.setText(documentSnapshot.getString("fechaFinVotacion"));
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ConfigRallyActivity.this, "Error al cargar la configuración", Toast.LENGTH_SHORT).show();
        });
    }
}
