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

public class FotoAdapter extends RecyclerView.Adapter<FotoAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(Foto foto);
    }

    private List<Foto> fotoList;
    private OnDeleteClickListener listener;
    private boolean mostrarBotonEliminar;

    // Constructor: pasar la lista de fotos, un listener para eliminar y un flag para mostrar o no el botón
    public FotoAdapter(List<Foto> fotoList, OnDeleteClickListener listener, boolean mostrarBotonEliminar) {
        this.fotoList = fotoList;
        this.listener = listener;
        this.mostrarBotonEliminar = mostrarBotonEliminar;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_foto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Foto foto = fotoList.get(position);
        Context context = holder.itemView.getContext();

        // Cargar la imagen con Glide
        Glide.with(context)
                .load(foto.getUrl())
                .into(holder.ivFotoItem);

        // Mostrar el estado de la foto (ej. "pendiente", "admitida" o "rechazada")
        holder.tvEstadoFoto.setText("Estado: " + foto.getEstado());

        // Controlar la visibilidad del botón de eliminar
        holder.btnDelete.setVisibility(mostrarBotonEliminar ? View.VISIBLE : View.GONE);

        // Acción del botón eliminar
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(foto);
            }
        });

        // Al pulsar sobre la imagen se abre la vista previa en pantalla completa
        holder.ivFotoItem.setOnClickListener(v -> {
            Intent intent = new Intent(context, PreviewImageActivity.class);
            intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_URI, foto.getUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return fotoList.size();
    }

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
