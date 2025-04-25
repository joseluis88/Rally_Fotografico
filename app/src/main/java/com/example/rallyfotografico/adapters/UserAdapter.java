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

/**
 * Adaptador personalizado para mostrar y gestionar una lista de usuarios en un RecyclerView.
 * Se usa principalmente en la sección de administración de usuarios.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UsuarioViewHolder> {

    /**
     * Interfaz que define las acciones disponibles para cada usuario:
     * eliminar o cambiar el rol (administrador/participante).
     */
    public interface OnUserActionListener {
        void onDeleteClick(Usuario usuario);
        void onToggleAdminClick(Usuario usuario);
    }

    private List<Usuario> usuarioList;               // Lista de usuarios a mostrar
    private OnUserActionListener listener;           // Listener para acciones sobre usuarios
    private static final String ROOT_ADMIN_EMAIL = "moto_castrol@hotmail.com";  // Email del admin principal

    /**
     * Constructor del adaptador.
     * @param usuarioList Lista de usuarios
     * @param listener Listener para manejar acciones sobre usuarios
     */
    public UserAdapter(List<Usuario> usuarioList, OnUserActionListener listener) {
        this.usuarioList = usuarioList;
        this.listener = listener;
    }

    /**
     * Infla el layout para cada elemento de la lista.
     */
    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    /**
     * Asigna los datos de cada usuario a su respectivo ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);
        holder.tvNombre.setText(usuario.getNombre());
        holder.tvEmail.setText(usuario.getEmail());
        holder.tvRol.setText("Rol: " + usuario.getRol());

        // El administrador principal no puede ser eliminado ni cambiar su rol
        if (usuario.getEmail().equals(ROOT_ADMIN_EMAIL)) {
            holder.btnEliminar.setVisibility(View.GONE);
            holder.btnToggleAdmin.setVisibility(View.GONE);
        } else {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnToggleAdmin.setVisibility(View.VISIBLE);

            // Mostrar el texto adecuado en el botón según el rol actual
            if ("administrador".equalsIgnoreCase(usuario.getRol())) {
                holder.btnToggleAdmin.setText("Quitar Admin");
            } else {
                holder.btnToggleAdmin.setText("Hacer Admin");
            }

            // Asignar eventos a los botones
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

    /**
     * Devuelve la cantidad de usuarios en la lista.
     */
    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    /**
     * ViewHolder para cada ítem de usuario.
     */
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
