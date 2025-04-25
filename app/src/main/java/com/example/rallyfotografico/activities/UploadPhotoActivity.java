package com.example.rallyfotografico.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.services.FirebaseService;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Actividad para que los participantes seleccionen, validen y suban una fotografía al sistema.
 * Se permite elegir una imagen de la galería o tomar una foto con la cámara.
 */
public class UploadPhotoActivity extends AppCompatActivity {

    // Constantes
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    // Parámetros de validación configurables
    private long maxFileSizeBytes = 5 * 1024 * 1024; // 5 MB por defecto
    private int maxWidth = 1920;
    private int maxHeight = 1080;
    private List<String> allowedFormats = new ArrayList<>();
    private int maxPhotosPerUser = 5;

    // Elementos de la UI
    private ImageView ivPreview;
    private TextView tvPreviewMessage;
    private Button btnSeleccionarFoto, btnSubirFoto, btnQuitarFoto;

    // Firebase y otros
    private Uri imageUri;
    private final FirebaseService firebase = FirebaseService.getInstance();
    private String fechaLimiteStr = null;
    private String seudonimoUsuario = null;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Lanzador para tomar fotos desde la cámara
    private final ActivityResultLauncher<Intent> tomarFotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && imageUri != null) {
                    ivPreview.setImageURI(imageUri);
                    tvPreviewMessage.setVisibility(TextView.GONE);
                    btnQuitarFoto.setVisibility(Button.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        // Inicialización de vistas
        ivPreview = findViewById(R.id.ivPreview);
        tvPreviewMessage = findViewById(R.id.tvPreviewMessage);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnSubirFoto = findViewById(R.id.btnSubirFoto);
        btnQuitarFoto = findViewById(R.id.btnQuitarFoto);

        loadConfiguration();         // Carga parámetros configurables desde Firestore
        loadSeudonimoUsuario();      // Carga el seudónimo del usuario actual

        // Selector entre cámara o galería
        btnSeleccionarFoto.setOnClickListener(v -> mostrarOpcionesSeleccion());

        // Validar y subir la foto
        btnSubirFoto.setOnClickListener(v -> {
            if (imageUri != null) {
                if (fechaLimiteStr != null) {
                    validarFechaLimite(); // Verifica si está dentro del plazo
                } else {
                    Toast.makeText(this, "No se ha cargado la fecha límite.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show();
            }
        });

        // Quitar la imagen seleccionada
        btnQuitarFoto.setOnClickListener(v -> quitarImagenSeleccionada());

        // Previsualizar la imagen seleccionada
        ivPreview.setOnClickListener(v -> {
            if (imageUri != null) {
                Intent intent = new Intent(this, PreviewImageActivity.class);
                intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_URI, imageUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Selecciona una imagen para previsualizarla", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Muestra un diálogo para elegir entre galería o cámara */
    private void mostrarOpcionesSeleccion() {
        String[] opciones = {"Elegir de la galería", "Tomar una foto"};
        new AlertDialog.Builder(this)
                .setTitle("Selecciona una opción")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) openFileChooser();
                    else verificarPermisoCamara();
                }).show();
    }

    /** Verifica el permiso para usar la cámara */
    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            tomarFotoConCamara();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    /** Manejo de resultado del permiso de cámara */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            tomarFotoConCamara();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }

    /** Abre la cámara para tomar una foto */
    private void tomarFotoConCamara() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Nueva Foto");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Desde la cámara");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        tomarFotoLauncher.launch(intent);
    }

    /** Quita la imagen seleccionada y limpia la vista */
    private void quitarImagenSeleccionada() {
        imageUri = null;
        ivPreview.setImageDrawable(null);
        tvPreviewMessage.setVisibility(TextView.VISIBLE);
        btnQuitarFoto.setVisibility(Button.GONE);
    }

    /** Carga parámetros del rally desde Firestore */
    private void loadConfiguration() {
        firebase.getFirestore().collection("configuracion").document("rally").get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Tamaño máximo permitido
                Double tamañoMaximoMB = documentSnapshot.getDouble("tamañoMaximoMB");
                if (tamañoMaximoMB != null) {
                    maxFileSizeBytes = (long) (tamañoMaximoMB * 1024 * 1024);
                }

                // Resolución máxima permitida
                String resolucion = documentSnapshot.getString("resolucion");
                if (!TextUtils.isEmpty(resolucion) && resolucion.contains("x")) {
                    String[] parts = resolucion.split("x");
                    try {
                        maxWidth = Integer.parseInt(parts[0].trim());
                        maxHeight = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) { e.printStackTrace(); }
                }

                // Formatos permitidos
                String tipoImagen = documentSnapshot.getString("tipoImagen");
                allowedFormats.clear();
                if (!TextUtils.isEmpty(tipoImagen)) {
                    for (String f : tipoImagen.split(",")) allowedFormats.add(f.trim().toLowerCase());
                } else {
                    allowedFormats.addAll(Arrays.asList("jpg", "jpeg", "png"));
                }

                // Límite de fotos
                Long limiteFotos = documentSnapshot.getLong("limiteFotos");
                if (limiteFotos != null) maxPhotosPerUser = limiteFotos.intValue();

                // Fecha límite de envío
                fechaLimiteStr = documentSnapshot.getString("fechaLimite");
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error al cargar configuración", Toast.LENGTH_SHORT).show());
    }

    /** Obtiene el seudónimo del usuario actual */
    private void loadSeudonimoUsuario() {
        String userId = firebase.getAuth().getCurrentUser().getUid();
        firebase.getFirestore().collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        seudonimoUsuario = documentSnapshot.getString("seudonimo");
                    }
                });
    }

    /** Valida que se esté dentro del plazo de subida */
    private void validarFechaLimite() {
        try {
            Date fechaActual = new Date();
            Date fechaLimite = sdf.parse(fechaLimiteStr);
            if (fechaLimite != null && fechaActual.after(fechaLimite)) {
                Toast.makeText(this, "El plazo para subir fotos ha finalizado.", Toast.LENGTH_SHORT).show();
            } else {
                verificarLimiteFotos();
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Error al validar la fecha límite.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Lanza selector de imagen desde la galería */
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /** Recibe el resultado del selector de imagen */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivPreview.setImageURI(imageUri);
            tvPreviewMessage.setVisibility(TextView.GONE);
            btnQuitarFoto.setVisibility(Button.VISIBLE);
        }
    }

    /** Verifica que el usuario no haya superado su límite de fotos */
    private void verificarLimiteFotos() {
        String userId = firebase.getAuth().getCurrentUser().getUid();
        firebase.getFirestore().collection("fotos").whereEqualTo("idUsuario", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() >= maxPhotosPerUser) {
                        Toast.makeText(this, "Has alcanzado el límite de " + maxPhotosPerUser + " fotos.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, PerfilActivity.class));
                    } else {
                        if (validateImage(imageUri)) {
                            uploadFile();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al verificar tus fotos", Toast.LENGTH_SHORT).show());
    }

    /** Valida el archivo seleccionado (tamaño, formato, resolución) */
    private boolean validateImage(Uri uri) {
        if (getFileSize(uri) > maxFileSizeBytes) {
            Toast.makeText(this, "El tamaño de la imagen supera el límite permitido.", Toast.LENGTH_SHORT).show();
            return false;
        }

        String extension = getFileExtension(uri);
        if (extension == null || !allowedFormats.contains(extension.toLowerCase())) {
            Toast.makeText(this, "Formato no permitido. Usa: " + allowedFormats, Toast.LENGTH_SHORT).show();
            return false;
        }

        int[] resolution = getImageResolution(uri);
        if (resolution[0] > maxWidth || resolution[1] > maxHeight) {
            Toast.makeText(this, "La resolución de la imagen excede el límite.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private int getFileSize(Uri uri) {
        int size = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                size = (int) cursor.getLong(sizeIndex);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return size;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        String type = cr.getType(uri);
        return (type != null) ? type.substring(type.lastIndexOf("/") + 1) : null;
    }

    private int[] getImageResolution(Uri uri) {
        int[] resolution = new int[]{0, 0};
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, options);
            resolution[0] = options.outWidth;
            resolution[1] = options.outHeight;
        } catch (Exception e) { e.printStackTrace(); }
        return resolution;
    }

    /** Sube la imagen a Firebase Storage y guarda la metadata en Firestore */
    private void uploadFile() {
        String userId = firebase.getAuth().getCurrentUser().getUid();
        StorageReference fileRef = firebase.getStorage().getReference("fotos").child(userId + "_" + System.currentTimeMillis() + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Map<String, Object> photoMap = new HashMap<>();
                    photoMap.put("url", uri.toString());
                    photoMap.put("idUsuario", userId);
                    photoMap.put("seudonimo", seudonimoUsuario);
                    photoMap.put("estado", "pendiente");
                    photoMap.put("timestamp", System.currentTimeMillis());
                    photoMap.put("votos", 0);
                    photoMap.put("fileSizeBytes", getFileSize(imageUri));
                    int[] res = getImageResolution(imageUri);
                    photoMap.put("imageWidth", res[0]);
                    photoMap.put("imageHeight", res[1]);
                    photoMap.put("format", getFileExtension(imageUri));
                    photoMap.put("votantes", new ArrayList<>());

                    firebase.getFirestore().collection("fotos").add(photoMap)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(this, "Foto subida correctamente", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, PerfilActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la foto", Toast.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Fallo en la carga", Toast.LENGTH_SHORT).show());
    }
}
