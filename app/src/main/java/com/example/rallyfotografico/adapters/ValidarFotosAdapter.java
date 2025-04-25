package com.example.rallyfotografico.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rallyfotografico.R;
import com.example.rallyfotografico.activities.PreviewImageActivity;
import com.example.rallyfotografico.models.Foto;

import java.util.List;

/**
 * Adaptador personalizado para mostrar una lista de fotos pendientes
 * en el RecyclerView dentro de ValidarFotosActivity.
 * Permite al administrador admitir o rechazar cada foto.
 */
public class ValidarFotosAdapter extends RecyclerView.Adapter<ValidarFotosAdapter.FotoViewHolder> {

    /**
     * Interfaz para manejar las acciones sobre una foto:
     * admitir o rechazar.
     */
    public interface OnFotoValidationListener {
        void onAdmitirClick(Foto foto);
        void onRechazarClick(Foto foto);
    }

    private List<Foto> fotoList;                    // Lista de fotos a mostrar
    private OnFotoValidationListener listener;      // Listener para gestionar las acciones

    /**
     * Constructor del adaptador
     * @param fotoList Lista de fotos pendientes
     * @param listener Listener para gestionar admisión/rechazo
     */
    public ValidarFotosAdapter(List<Foto> fotoList, OnFotoValidationListener listener) {
        this.fotoList = fotoList;
        this.listener = listener;
    }

    /**
     * Infla el layout XML de cada ítem de la lista.
     */
    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_validar_foto, parent, false);
        return new FotoViewHolder(view);
    }

    /**
     * Asocia los datos de la foto con la vista del ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        Foto foto = fotoList.get(position);
        String imageUrl = foto.getUrl();

        // Cargar la imagen con Glide o usar un placeholder si la URL está vacía
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Mostrar el estado actual de la foto
        holder.tvEstadoFoto.setText("Estado: " + foto.getEstado());

        // Botón para admitir la foto
        holder.btnAdmitir.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAdmitirClick(foto);
            }
        });

        // Botón para rechazar la foto
        holder.btnRechazar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRechazarClick(foto);
            }
        });

        // Al pulsar sobre la imagen se abre en pantalla completa
        holder.itemView.setOnClickListener(v -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Intent intent = new Intent(v.getContext(), PreviewImageActivity.class);
                intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_URI, imageUrl);
                v.getContext().startActivity(intent);
            }
        });
    }

    /**
     * Devuelve la cantidad de fotos en la lista.
     */
    @Override
    public int getItemCount() {
        return fotoList.size();
    }

    /**
     * ViewHolder que representa cada ítem individual de la lista.
     * Contiene la imagen, estado y botones de acción.
     */
    public static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvEstadoFoto;
        Button btnAdmitir, btnRechazar;

        public FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            tvEstadoFoto = itemView.findViewById(R.id.tvEstadoFoto);
            btnAdmitir = itemView.findViewById(R.id.btnAdmitir);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
        }
    }
}
