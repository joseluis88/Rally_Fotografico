package com.example.rallyfotografico.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.models.Usuario;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UsuarioViewHolder> {

    // Interface para manejar acciones sobre cada usuario
    public interface OnUserActionListener {
        void onDeleteClick(Usuario usuario);
        void onToggleAdminClick(Usuario usuario);
    }

    private List<Usuario> usuarioList;
    private OnUserActionListener listener;
    // Definimos el email del administrador principal
    private static final String ROOT_ADMIN_EMAIL = "moto_castrol@hotmail.com";

    public UserAdapter(List<Usuario> usuarioList, OnUserActionListener listener) {
        this.usuarioList = usuarioList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);
        holder.tvNombre.setText(usuario.getNombre());
        holder.tvEmail.setText(usuario.getEmail());
        holder.tvRol.setText("Rol: " + usuario.getRol());

        // Si el usuario es el root (administrador principal), no se permite eliminar ni cambiar rol
        if (usuario.getEmail().equals(ROOT_ADMIN_EMAIL)) {
            holder.btnEliminar.setVisibility(View.GONE);
            holder.btnToggleAdmin.setVisibility(View.GONE);
        } else {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnToggleAdmin.setVisibility(View.VISIBLE);

            // Configurar el texto del botón de cambio de rol según el rol actual
            if ("administrador".equalsIgnoreCase(usuario.getRol())) {
                holder.btnToggleAdmin.setText("Quitar Admin");
            } else {
                holder.btnToggleAdmin.setText("Hacer Admin");
            }

            // Asignar los listeners de clic
            holder.btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(usuario);
                }
            });

            holder.btnToggleAdmin.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleAdminClick(usuario);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEmail, tvRol;
        Button btnEliminar, btnToggleAdmin;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRol = itemView.findViewById(R.id.tvRol);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnToggleAdmin = itemView.findViewById(R.id.btnToggleAdmin);
        }
    }
}
