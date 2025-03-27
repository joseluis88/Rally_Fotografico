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

public class ValidarFotosAdapter extends RecyclerView.Adapter<ValidarFotosAdapter.FotoViewHolder> {

    public interface OnFotoValidationListener {
        void onAdmitirClick(Foto foto);
        void onRechazarClick(Foto foto);
    }

    private List<Foto> fotoList;
    private OnFotoValidationListener listener;

    public ValidarFotosAdapter(List<Foto> fotoList, OnFotoValidationListener listener) {
        this.fotoList = fotoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_validar_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        Foto foto = fotoList.get(position);

        // Cargar la imagen con Glide (se puede usar placeholder si lo deseas)
        String imageUrl = foto.getUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_placeholder);
        }

        // Asignar listeners a los botones de validación
        holder.btnAdmitir.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAdmitirClick(foto);
            }
        });
        holder.btnRechazar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRechazarClick(foto);
            }
        });

        // Al pulsar en el item se abre la vista previa a pantalla completa
        holder.itemView.setOnClickListener(v -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Intent intent = new Intent(v.getContext(), PreviewImageActivity.class);
                intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_URI, imageUrl);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fotoList.size();
    }

    public static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        Button btnAdmitir, btnRechazar;
        TextView tvEstado;

        public FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            btnAdmitir = itemView.findViewById(R.id.btnAdmitir);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
            tvEstado = itemView.findViewById(R.id.tvEstadoFoto);
        }
    }
}
