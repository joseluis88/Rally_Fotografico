package com.example.rallyfotografico.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.rallyfotografico.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Actividad que permite al administrador configurar los parámetros del rally fotográfico.
 * Incluye validación de fechas, restricciones y guardado en Firestore.
 */
public class ConfigRallyActivity extends AppCompatActivity {

    // Campos de entrada de datos
    private EditText etMensajeParticipante, etTamanoMaximo, etResolucion, etTipoImagen,
            etFechaLimite, etLimiteFotos, etMensajePublico, etFechaInicioVotacion, etFechaFinVotacion, etTituloInicio;
    private Button btnGuardarConfig, btnLimpiarCampos;

    // Formateador de fecha para entrada/salida
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Firebase
    private FirebaseFirestore db;
    private DocumentReference configRef;

    /**
     * Se ejecuta al crear la actividad.
     * Inicializa vistas, conecta con Firestore y carga la configuración existente.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_rally);

        // Referencias a los campos del layout
        etTituloInicio = findViewById(R.id.etTituloInicio);
        etMensajeParticipante = findViewById(R.id.etMensajeParticipante);
        etTamanoMaximo = findViewById(R.id.etTamanoMaximo);
        etResolucion = findViewById(R.id.etResolucion);
        etTipoImagen = findViewById(R.id.etTipoImagen);
        etFechaLimite = findViewById(R.id.etFechaLimite);
        etLimiteFotos = findViewById(R.id.etLimiteFotos);
        etMensajePublico = findViewById(R.id.etMensajePublico);
        etFechaInicioVotacion = findViewById(R.id.etFechaInicioVotacion);
        etFechaFinVotacion = findViewById(R.id.etFechaFinVotacion);
        btnGuardarConfig = findViewById(R.id.btnGuardarConfig);
        btnLimpiarCampos = findViewById(R.id.btnLimpiarCampos);

        // Inicializa Firestore
        db = FirebaseFirestore.getInstance();
        configRef = db.collection("configuracion").document("rally");

        // Selección de fechas
        etFechaLimite.setOnClickListener(v -> showDatePicker(etFechaLimite));
        etFechaInicioVotacion.setOnClickListener(v -> showDatePicker(etFechaInicioVotacion));
        etFechaFinVotacion.setOnClickListener(v -> showDatePicker(etFechaFinVotacion));

        // Guardar configuración
        btnGuardarConfig.setOnClickListener(v -> guardarConfiguracion());

        // Limpiar campos
        btnLimpiarCampos.setOnClickListener(v -> limpiarCampos());
        btnLimpiarCampos.setVisibility(View.GONE);

        // Cargar configuración si ya existe
        cargarConfiguracionExistente();
    }

    /**
     * Muestra un selector de fecha y coloca la selección en el campo correspondiente.
     * @param campo Campo de texto donde se insertará la fecha elegida.
     */
    private void showDatePicker(EditText campo) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            campo.setText(dateFormat.format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Guarda la configuración introducida en Firestore tras validar los campos.
     * Valida fechas y formatos numéricos.
     */
    private void guardarConfiguracion() {
        String tituloInicio = etTituloInicio.getText().toString().trim();
        String mensajePart = etMensajeParticipante.getText().toString().trim();
        String tamano = etTamanoMaximo.getText().toString().trim();
        String resolucion = etResolucion.getText().toString().trim();
        String tipo = etTipoImagen.getText().toString().trim();
        String fechaLimiteStr = etFechaLimite.getText().toString().trim();
        String limiteFotosStr = etLimiteFotos.getText().toString().trim();
        String mensajePub = etMensajePublico.getText().toString().trim();
        String fechaInicioStr = etFechaInicioVotacion.getText().toString().trim();
        String fechaFinStr = etFechaFinVotacion.getText().toString().trim();

        // Validación de campos vacíos
        if (TextUtils.isEmpty(tituloInicio) || TextUtils.isEmpty(mensajePart) || TextUtils.isEmpty(tamano) || TextUtils.isEmpty(resolucion) ||
                TextUtils.isEmpty(tipo) || TextUtils.isEmpty(fechaLimiteStr) || TextUtils.isEmpty(limiteFotosStr) ||
                TextUtils.isEmpty(mensajePub) || TextUtils.isEmpty(fechaInicioStr) || TextUtils.isEmpty(fechaFinStr)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Validación de fechas (en orden correcto)
            long fechaLimite = dateFormat.parse(fechaLimiteStr).getTime();
            long fechaInicio = dateFormat.parse(fechaInicioStr).getTime();
            long fechaFin = dateFormat.parse(fechaFinStr).getTime();

            if (fechaInicio < fechaLimite) {
                Toast.makeText(this, "La fecha de inicio de votaciones no puede ser antes de la fecha límite de fotos", Toast.LENGTH_LONG).show();
                return;
            }

            if (fechaFin < fechaInicio) {
                Toast.makeText(this, "La fecha fin de votación debe ser posterior a la de inicio", Toast.LENGTH_LONG).show();
                return;
            }

            // Conversión numérica
            double tamanoMB = Double.parseDouble(tamano);
            int limiteFotos = Integer.parseInt(limiteFotosStr);

            // Creación de objeto con datos a guardar
            Map<String, Object> datos = new HashMap<>();
            datos.put("tituloInicio", tituloInicio);
            datos.put("mensajeParticipantes", mensajePart);
            datos.put("tamañoMaximoMB", tamanoMB);
            datos.put("resolucion", resolucion);
            datos.put("tipoImagen", tipo);
            datos.put("fechaLimite", fechaLimiteStr);
            datos.put("limiteFotos", limiteFotos);
            datos.put("mensajePublico", mensajePub);
            datos.put("fechaInicioVotacion", fechaInicioStr);
            datos.put("fechaFinVotacion", fechaFinStr);

            // Guardar en Firestore
            configRef.set(datos).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ConfigRallyActivity.this, PerfilActivity.class));
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Error al guardar la configuración", Toast.LENGTH_SHORT).show()
            );

        } catch (ParseException | NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al validar fechas o números", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Carga la configuración existente desde Firestore y la muestra en los campos correspondientes.
     */
    private void cargarConfiguracionExistente() {
        configRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                setTextoSiExiste(snapshot.getString("tituloInicio"), etTituloInicio);
                setTextoSiExiste(snapshot.getString("mensajeParticipantes"), etMensajeParticipante);

                Double tamanoMB = snapshot.getDouble("tamañoMaximoMB");
                if (tamanoMB != null) etTamanoMaximo.setText(String.valueOf(tamanoMB));

                setTextoSiExiste(snapshot.getString("resolucion"), etResolucion);
                setTextoSiExiste(snapshot.getString("tipoImagen"), etTipoImagen);
                setTextoSiExiste(snapshot.getString("fechaLimite"), etFechaLimite);

                Long limiteFotos = snapshot.getLong("limiteFotos");
                if (limiteFotos != null) etLimiteFotos.setText(String.valueOf(limiteFotos));

                setTextoSiExiste(snapshot.getString("mensajePublico"), etMensajePublico);
                setTextoSiExiste(snapshot.getString("fechaInicioVotacion"), etFechaInicioVotacion);
                setTextoSiExiste(snapshot.getString("fechaFinVotacion"), etFechaFinVotacion);

                verificarCamposParaMostrarBotonLimpiar();
            }
        });
    }

    /**
     * Establece texto en un campo solo si el valor no es nulo ni vacío.
     */
    private void setTextoSiExiste(String valor, EditText campo) {
        if (valor != null && !valor.isEmpty()) {
            campo.setText(valor);
        }
    }

    /**
     * Muestra el botón de limpiar solo si hay algún campo con contenido.
     */
    private void verificarCamposParaMostrarBotonLimpiar() {
        if (!etTituloInicio.getText().toString().isEmpty() ||
                !etMensajeParticipante.getText().toString().isEmpty() ||
                !etTamanoMaximo.getText().toString().isEmpty() ||
                !etResolucion.getText().toString().isEmpty() ||
                !etTipoImagen.getText().toString().isEmpty() ||
                !etFechaLimite.getText().toString().isEmpty() ||
                !etLimiteFotos.getText().toString().isEmpty() ||
                !etMensajePublico.getText().toString().isEmpty() ||
                !etFechaInicioVotacion.getText().toString().isEmpty() ||
                !etFechaFinVotacion.getText().toString().isEmpty()) {
            btnLimpiarCampos.setVisibility(View.VISIBLE);
        } else {
            btnLimpiarCampos.setVisibility(View.GONE);
        }
    }

    /**
     * Limpia todos los campos del formulario y oculta el botón de limpiar.
     */
    private void limpiarCampos() {
        etTituloInicio.setText("");
        etMensajeParticipante.setText("");
        etTamanoMaximo.setText("");
        etResolucion.setText("");
        etTipoImagen.setText("");
        etFechaLimite.setText("");
        etLimiteFotos.setText("");
        etMensajePublico.setText("");
        etFechaInicioVotacion.setText("");
        etFechaFinVotacion.setText("");
        btnLimpiarCampos.setVisibility(View.GONE);
    }
}
