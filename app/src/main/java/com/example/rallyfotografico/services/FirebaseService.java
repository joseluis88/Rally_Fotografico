package com.example.rallyfotografico.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Clase singleton que centraliza el acceso a los servicios de Firebase utilizados en la aplicación:
 * - Autenticación (FirebaseAuth)
 * - Base de datos Firestore (FirebaseFirestore)
 * - Almacenamiento de archivos (FirebaseStorage)
 *
 * Esta clase permite que todas las actividades y clases del proyecto puedan acceder a Firebase de forma unificada
 * sin necesidad de crear múltiples instancias en diferentes lugares del código.
 */
public class FirebaseService {

    // Instancia única del servicio (patrón singleton)
    private static FirebaseService instance;

    // Referencias internas a los servicios de Firebase
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    /**
     * Constructor privado para evitar instanciación externa.
     * Inicializa las referencias a los servicios de Firebase.
     */
    private FirebaseService() {
        auth = FirebaseAuth.getInstance();               // Servicio de autenticación
        firestore = FirebaseFirestore.getInstance();     // Base de datos Firestore
        storage = FirebaseStorage.getInstance();         // Almacenamiento en la nube
    }

    /**
     * Obtiene la instancia única de FirebaseService.
     * Si no existe, la crea una sola vez.
     *
     * @return La instancia compartida de FirebaseService.
     */
    public static FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    /**
     * Obtiene la instancia de FirebaseAuth para autenticación de usuarios.
     *
     * @return Objeto FirebaseAuth.
     */
    public FirebaseAuth getAuth() {
        return auth;
    }

    /**
     * Obtiene la instancia de FirebaseFirestore para operaciones con base de datos.
     *
     * @return Objeto FirebaseFirestore.
     */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    /**
     * Obtiene la instancia de FirebaseStorage para subir y descargar archivos.
     *
     * @return Objeto FirebaseStorage.
     */
    public FirebaseStorage getStorage() {
        return storage;
    }
}
