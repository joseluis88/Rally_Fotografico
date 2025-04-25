package com.example.rallyfotografico.adapters;

import android.content.Context;
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
 * Adaptador personalizado para mostrar objetos Foto en un RecyclerView.
 * Puede configurarse para mostrar botón de eliminación o posición en ranking.
 */
public class FotoAdapter extends RecyclerView.Adapter<FotoAdapter.ViewHolder> {

    /**
     * Interfaz para capturar el evento de eliminación de una foto.
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(Foto foto);
    }

    private List<Foto> fotoList;                     // Lista de fotos a mostrar
    private OnDeleteClickListener listener;          // Listener para botón eliminar
    private boolean mostrarBotonEliminar;            // Si se debe mostrar el botón eliminar
    private boolean mostrarPosicion;                 // Si se debe mostrar el ranking

    /**
     * Constructor tradicional (perfil de usuario o validación de fotos).
     * @param fotoList Lista de fotos a mostrar
     * @param listener Listener para eliminar fotos
     * @param mostrarBotonEliminar Si se debe mostrar el botón de eliminar
     */
    public FotoAdapter(List<Foto> fotoList, OnDeleteClickListener listener, boolean mostrarBotonEliminar) {
        this.fotoList = fotoList;
        this.listener = listener;
        this.mostrarBotonEliminar = mostrarBotonEliminar;
        this.mostrarPosicion = false;
    }

    /**
     * Constructor especial para el ranking (sin botón eliminar, con posición).
     * @param fotoList Lista de fotos
     * @param mostrarPosicion True para mostrar la posición en el ranking
     */
    public FotoAdapter(List<Foto> fotoList, boolean mostrarPosicion) {
        this.fotoList = fotoList;
        this.listener = null;
        this.mostrarBotonEliminar = false;
        this.mostrarPosicion = mostrarPosicion;
    }

    /**
     * Infla el layout del ítem de foto.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_foto, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Asocia los datos de una foto a su vista.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Foto foto = fotoList.get(position);
        Context context = holder.itemView.getContext();

        // Cargar la imagen con Glide
        Glide.with(context)
                .load(foto.getUrl())
                .into(holder.ivFotoItem);

        // Mostrar estado o votos según modo
        if (mostrarPosicion) {
            holder.tvEstadoFoto.setText((position + 1) + "º - " + foto.getVotos() + " votos");
        } else {
            holder.tvEstadoFoto.setText("Estado: " + foto.getEstado());
        }

        // Configurar botón eliminar
        holder.btnDelete.setVisibility(mostrarBotonEliminar ? View.VISIBLE : View.GONE);
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(foto);
            }
        });

        // Abrir imagen en pantalla completa al hacer clic
        holder.ivFotoItem.setOnClickListener(v -> {
            Intent intent = new Intent(context, PreviewImageActivity.class);
            intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_URI, foto.getUrl());
            context.startActivity(intent);
        });
    }

    /**
     * Devuelve el número de ítems en la lista.
     */
    @Override
    public int getItemCount() {
        return fotoList.size();
    }

    /**
     * ViewHolder que representa una fila de foto.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFotoItem;
        TextView tvEstadoFoto;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFotoItem = itemView.findViewById(R.id.ivFotoItem);
            tvEstadoFoto = itemView.findViewById(R.id.tvEstadoFoto);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
