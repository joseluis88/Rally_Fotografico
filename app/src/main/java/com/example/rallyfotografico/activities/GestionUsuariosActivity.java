package com.example.rallyfotografico.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.adapters.UserAdapter;
import com.example.rallyfotografico.models.Usuario;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GestionUsuariosActivity extends AppCompatActivity {

    private RecyclerView rvUsuarios;
    private FirebaseFirestore db;
    private List<Usuario> usuarioList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        rvUsuarios = findViewById(R.id.rvUsuarios);
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        usuarioList = new ArrayList<>();
        userAdapter = new UserAdapter(usuarioList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onDeleteClick(Usuario usuario) {
                deleteUsuario(usuario);
            }

            @Override
            public void onToggleAdminClick(Usuario usuario) {
                toggleAdmin(usuario);
            }
        });
        rvUsuarios.setAdapter(userAdapter);

        loadUsuarios();
    }

    // Consulta la colección "usuarios" y actualiza el RecyclerView
    private void loadUsuarios() {
        db.collection("usuarios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    usuarioList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Usuario usuario = doc.toObject(Usuario.class);
                        usuario.setId(doc.getId());
                        usuarioList.add(usuario);
                    }
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(GestionUsuariosActivity.this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show());
    }

    // Elimina un usuario de la base de datos
    private void deleteUsuario(Usuario usuario) {
        db.collection("usuarios").document(usuario.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(GestionUsuariosActivity.this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                    loadUsuarios();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(GestionUsuariosActivity.this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show());
    }

    // Cambia el rol del usuario: si es "administrador" lo pone como "participante" y viceversa.
    private void toggleAdmin(Usuario usuario) {
        // Determinar el nuevo rol: si es administrador, lo quitamos, sino lo promovemos
        String nuevoRol;
        if ("administrador".equalsIgnoreCase(usuario.getRol())) {
            nuevoRol = "participante";
        } else {
            nuevoRol = "administrador";
        }
        final String finalNuevoRol = nuevoRol; // variable final para usar en la lambda
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("rol", finalNuevoRol);
        db.collection("usuarios").document(usuario.getId())
                .update(updateMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(GestionUsuariosActivity.this, "Rol actualizado a " + finalNuevoRol, Toast.LENGTH_SHORT).show();
                    loadUsuarios();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(GestionUsuariosActivity.this, "Error al actualizar rol", Toast.LENGTH_SHORT).show());
    }

}
