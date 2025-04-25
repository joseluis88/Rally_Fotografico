package com.example.rallyfotografico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.rallyfotografico.R;

/**
 * Esta actividad se utiliza para previsualizar una imagen a pantalla completa.
 * Se espera que reciba una URI de imagen a través del intent con la clave EXTRA_IMAGE_URI.
 */
public class PreviewImageActivity extends AppCompatActivity {

    // Constante utilizada como clave para obtener la URI de la imagen del intent
    public static final String EXTRA_IMAGE_URI = "extra_image_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image); // Carga el layout con la imagen a pantalla completa

        // Referencia al ImageView donde se mostrará la imagen
        ImageView ivPreviewFull = findViewById(R.id.ivPreviewFull);

        // Obtiene el intent que inició esta actividad
        Intent intent = getIntent();

        // Recupera la URI de la imagen enviada desde otra actividad
        String imageUri = intent.getStringExtra(EXTRA_IMAGE_URI);

        // Si se recibió una URI válida, se carga la imagen usando Glide
        if (imageUri != null) {
            Glide.with(this)
                    .load(imageUri)               // Carga la imagen desde la URI
                    .into(ivPreviewFull);        // La muestra en el ImageView
        }
    }
}
