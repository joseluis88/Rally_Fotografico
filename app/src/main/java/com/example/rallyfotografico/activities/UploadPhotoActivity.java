package com.example.rallyfotografico.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rallyfotografico.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

public class UploadPhotoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private long maxFileSizeBytes = 5 * 1024 * 1024; // Tamaño por defecto: 5 MB
    private int maxWidth = 1920; // Ancho máximo por defecto
    private int maxHeight = 1080; // Alto máximo por defecto
    private List<String> allowedFormats = new ArrayList<>();
    private int maxPhotosPerUser = 5; // Número máximo por defecto

    private ImageView ivPreview;
    private Button btnSeleccionarFoto, btnSubirFoto;
    private Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference configRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        ivPreview = findViewById(R.id.ivPreview);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnSubirFoto = findViewById(R.id.btnSubirFoto);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("fotos");
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        configRef = db.collection("configuracion").document("rally");
        loadConfiguration();

        btnSeleccionarFoto.setOnClickListener(v -> openFileChooser());
        btnSubirFoto.setOnClickListener(v -> {
            if (imageUri != null) {
                verificarLimiteFotos();
            } else {
                Toast.makeText(UploadPhotoActivity.this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show();
            }
        });

        // Si se pulsa sobre la imagen previsualizada, se abre en pantalla completa (PreviewImageActivity)
        ivPreview.setOnClickListener(v -> {
            if (imageUri != null) {
                Intent intent = new Intent(UploadPhotoActivity.this, PreviewImageActivity.class);
                intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_URI, imageUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(UploadPhotoActivity.this, "Selecciona una imagen para previsualizarla", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadConfiguration() {
        configRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Double tamañoMaximoMB = documentSnapshot.getDouble("tamañoMaximoMB");
                if (tamañoMaximoMB != null) {
                    maxFileSizeBytes = (long) (tamañoMaximoMB * 1024 * 1024);
                }

                String resolucion = documentSnapshot.getString("resolucion");
                if (!TextUtils.isEmpty(resolucion) && resolucion.contains("x")) {
                    String[] parts = resolucion.split("x");
                    try {
                        maxWidth = Integer.parseInt(parts[0].trim());
                        maxHeight = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                String tipoImagen = documentSnapshot.getString("tipoImagen");
                if (!TextUtils.isEmpty(tipoImagen)) {
                    String[] formatos = tipoImagen.split(",");
                    allowedFormats.clear();
                    for (String f : formatos) {
                        allowedFormats.add(f.trim().toLowerCase());
                    }
                } else {
                    allowedFormats.add("jpg");
                    allowedFormats.add("jpeg");
                    allowedFormats.add("png");
                }

                Long limiteFotos = documentSnapshot.getLong("limiteFotos");
                if (limiteFotos != null) {
                    maxPhotosPerUser = limiteFotos.intValue();
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(UploadPhotoActivity.this, "Error al cargar configuración", Toast.LENGTH_SHORT).show());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivPreview.setImageURI(imageUri);
        }
    }

    private void verificarLimiteFotos() {
        String userId = mAuth.getCurrentUser().getUid();
        // Contar todas las fotos subidas por el usuario (se pueden filtrar solo las aceptadas si se desea)
        db.collection("fotos").whereEqualTo("idUsuario", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int fotosSubidas = queryDocumentSnapshots.size();
                    if (fotosSubidas >= maxPhotosPerUser) {
                        Toast.makeText(UploadPhotoActivity.this, "Has alcanzado el límite de " + maxPhotosPerUser + " fotos.", Toast.LENGTH_SHORT).show();
                        // Redirigir automáticamente al perfil para ver el ítem de foto
                        startActivity(new Intent(UploadPhotoActivity.this, PerfilActivity.class));
                    } else {
                        if (validateImage(imageUri)) {
                            uploadFile();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(UploadPhotoActivity.this, "Error al verificar tus fotos. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show());
    }

    private boolean validateImage(Uri uri) {
        int fileSize = getFileSize(uri);
        if (fileSize > maxFileSizeBytes) {
            Toast.makeText(UploadPhotoActivity.this, "El tamaño de la imagen supera el límite (" + (maxFileSizeBytes / (1024 * 1024)) + " MB).", Toast.LENGTH_SHORT).show();
            return false;
        }

        String extension = getFileExtension(uri);
        if (extension == null || !allowedFormats.contains(extension.toLowerCase())) {
            Toast.makeText(UploadPhotoActivity.this, "Formato de imagen no permitido. Usa: " + allowedFormats.toString(), Toast.LENGTH_SHORT).show();
            return false;
        }

        int[] resolution = getImageResolution(uri);
        if (resolution[0] > maxWidth || resolution[1] > maxHeight) {
            Toast.makeText(UploadPhotoActivity.this, "La resolución de la imagen excede el límite permitido.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private int getFileSize(Uri uri) {
        int size = 0;
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                cursor.moveToFirst();
                size = (int) cursor.getLong(sizeIndex);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        String type = cr.getType(uri);
        if (type != null) {
            return type.substring(type.lastIndexOf("/") + 1);
        }
        return null;
    }

    private int[] getImageResolution(Uri uri) {
        int[] resolution = new int[]{0, 0};
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, options);
            resolution[0] = options.outWidth;
            resolution[1] = options.outHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resolution;
    }

    private void uploadFile() {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference fileRef = storageReference.child(userId + "_" + System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String photoUrl = uri.toString();
                            int fileSize = getFileSize(imageUri);
                            String extension = getFileExtension(imageUri);
                            int[] resolution = getImageResolution(imageUri);

                            Map<String, Object> photoMap = new HashMap<>();
                            photoMap.put("url", photoUrl);
                            photoMap.put("idUsuario", userId);
                            // Al subir, el estado inicial es "pendiente"
                            photoMap.put("estado", "pendiente");
                            photoMap.put("timestamp", System.currentTimeMillis());
                            photoMap.put("votos", 0);
                            photoMap.put("fileSizeBytes", fileSize);
                            photoMap.put("imageWidth", resolution[0]);
                            photoMap.put("imageHeight", resolution[1]);
                            photoMap.put("format", extension);

                            db.collection("fotos").add(photoMap)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(UploadPhotoActivity.this, "Foto subida correctamente.", Toast.LENGTH_SHORT).show();
                                        // Redirigir automáticamente al perfil para ver el ítem de foto
                                        startActivity(new Intent(UploadPhotoActivity.this, PerfilActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(UploadPhotoActivity.this, "Error al guardar la foto.", Toast.LENGTH_SHORT).show());
                        })
                )
                .addOnFailureListener(e -> Toast.makeText(UploadPhotoActivity.this, "Fallo en la carga.", Toast.LENGTH_SHORT).show());
    }
}
