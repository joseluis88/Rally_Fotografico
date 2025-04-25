package com.example.rallyfotografico.models;

/**
 * Modelo que representa un usuario registrado en el sistema del rally fotográfico.
 * Esta clase se utiliza para mapear los datos de la colección "usuarios" en Firestore.
 */
public class Usuario {

    // ID del usuario (coincide con el UID de Firebase Authentication)
    private String id;

    // Nombre completo del usuario
    private String nombre;

    // Correo electrónico del usuario
    private String email;

    // Rol del usuario: puede ser "participante" o "administrador"
    private String rol;

    /**
     * Constructor vacío requerido por Firestore para poder deserializar automáticamente.
     */
    public Usuario() { }

    // ------------------- GETTERS Y SETTERS -------------------

    /**
     * Devuelve el ID del usuario (UID de Firebase Auth).
     */
    public String getId() {
        return id;
    }

    /**
     * Establece el ID del usuario.
     * @param id UID generado por Firebase Auth.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Devuelve el nombre completo del usuario.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre completo del usuario.
     * @param nombre Nombre introducido en el registro.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve el correo electrónico del usuario.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electrónico del usuario.
     * @param email Correo introducido en el registro.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Devuelve el rol asignado al usuario.
     */
    public String getRol() {
        return rol;
    }

    /**
     * Establece el rol del usuario.
     * @param rol Puede ser "participante" o "administrador".
     */
    public void setRol(String rol) {
        this.rol = rol;
    }
}
