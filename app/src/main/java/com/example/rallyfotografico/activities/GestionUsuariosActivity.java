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

/**
 * Actividad para la gestión de usuarios desde el panel de administración.
 * Permite visualizar la lista de usuarios, cambiar su rol (participante ↔ administrador)
 * y eliminar usuarios registrados.
 */
public class GestionUsuariosActivity extends AppCompatActivity {

    // Elementos de UI y Firestore
    private RecyclerView rvUsuarios;
    private FirebaseFirestore db;
    private List<Usuario> usuarioList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        // Inicializa componentes de UI
        rvUsuarios = findViewById(R.id.rvUsuarios);
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this)); // Disposición vertical

        // Inicializa Firestore
        db = FirebaseFirestore.getInstance();

        // Lista vacía de usuarios
        usuarioList = new ArrayList<>();

        // Inicializa el adaptador con funciones de acción para cada usuario
        userAdapter = new UserAdapter(usuarioList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onDeleteClick(Usuario usuario) {
                deleteUsuario(usuario); // Acción de eliminar usuario
            }

            @Override
            public void onToggleAdminClick(Usuario usuario) {
                toggleAdmin(usuario); // Acción de cambiar rol
            }
        });

        // Asocia el adaptador al RecyclerView
        rvUsuarios.setAdapter(userAdapter);

        // Carga los usuarios desde Firestore
        loadUsuarios();
    }

    /**
     * Consulta la colección "usuarios" de Firestore y actualiza la lista y el RecyclerView.
     */
    private void loadUsuarios() {
        db.collection("usuarios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    usuarioList.clear(); // Limpia la lista anterior
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Usuario usuario = doc.toObject(Usuario.class);
                        if (usuario != null) {
                            usuario.setId(doc.getId()); // Guarda el ID del documento
                            usuarioList.add(usuario);
                        }
                    }
                    userAdapter.notifyDataSetChanged(); // Notifica que hubo cambios
                })
                .addOnFailureListener(e ->
                        Toast.makeText(GestionUsuariosActivity.this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show());
    }

    /**
     * Elimina un usuario específico de Firestore.
     *
     * @param usuario Objeto Usuario a eliminar.
     */
    private void deleteUsuario(Usuario usuario) {
        db.collection("usuarios").document(usuario.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                    loadUsuarios(); // Recargar la lista tras eliminar
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show());
    }

    /**
     * Cambia el rol de un usuario entre "participante" y "administrador".
     *
     * @param usuario Usuario cuyo rol se desea alternar.
     */
    private void toggleAdmin(Usuario usuario) {
        String nuevoRol = "administrador".equalsIgnoreCase(usuario.getRol()) ? "participante" : "administrador";

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("rol", nuevoRol);

        db.collection("usuarios").document(usuario.getId())
                .update(updateMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Rol actualizado a " + nuevoRol, Toast.LENGTH_SHORT).show();
                    loadUsuarios(); // Recarga la lista actualizada
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar rol", Toast.LENGTH_SHORT).show());
    }
}
