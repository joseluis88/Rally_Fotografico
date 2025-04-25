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
 * Adaptador personalizado para mostrar las fotos en el sistema de votación.
 * Permite a los usuarios votar por las fotos admitidas.
 */
public class VotingAdapter extends RecyclerView.Adapter<VotingAdapter.ViewHolder> {

    /**
     * Interfaz para manejar el evento de voto sobre una foto.
     */
    public interface OnVoteClickListener {
        void onVoteClick(Foto foto);
    }

    private List<Foto> fotoList;                    // Lista de fotos a mostrar
    private OnVoteClickListener listener;           // Listener para manejar la acción de votar

    /**
     * Constructor del adaptador
     * @param fotoList Lista de objetos Foto
     * @param listener Listener que gestiona el evento de voto
     */
    public VotingAdapter(List<Foto> fotoList, OnVoteClickListener listener) {
        this.fotoList = fotoList;
        this.listener = listener;
    }

    /**
     * Infla el layout del ítem que representa cada foto en la votación.
     */
    @NonNull
    @Override
    public VotingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_voting_photo, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Asocia los datos de la foto a los elementos del ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull VotingAdapter.ViewHolder holder, int position) {
        Foto foto = fotoList.get(position);
        Context context = holder.itemView.getContext();

        // Cargar imagen usando Glide
        Glide.with(context)
                .load(foto.getUrl())
                .into(holder.ivPhoto);

        // Mostrar cantidad de votos
        holder.tvVotes.setText("Votos: " + foto.getVotos());

        // Mostrar estado de la foto (normalmente será "admitida")
        holder.tvEstado.setText("Estado: " + foto.getEstado());

        // Al pulsar sobre la imagen, se muestra en pantalla completa
        holder.ivPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(context, PreviewImageActivity.class);
            intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_URI, foto.getUrl());
            context.startActivity(intent);
        });

        // Al pulsar el botón "Votar", se notifica al listener
        holder.btnVote.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVoteClick(foto);
            }
        });
    }

    /**
     * Devuelve la cantidad de elementos (fotos) en la lista.
     */
    @Override
    public int getItemCount() {
        return fotoList.size();
    }

    /**
     * Clase ViewHolder que contiene las referencias a los elementos de la UI
     * para cada ítem de la lista de fotos.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;        // Imagen de la foto
        TextView tvVotes;         // Texto que muestra cantidad de votos
        TextView tvEstado;        // Texto que muestra el estado de la foto
        Button btnVote;           // Botón para emitir el voto

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvVotes = itemView.findViewById(R.id.tvVotes);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnVote = itemView.findViewById(R.id.btnVote);
        }
    }
}
