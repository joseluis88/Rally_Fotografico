package com.example.rallyfotografico.models;

/**
 * Modelo que representa una fotografía subida por un usuario participante.
 * Esta clase se utiliza para mapear los datos entre Firestore y la aplicación.
 */
public class Foto {

    // ID de la foto (se asigna manualmente desde el documento de Firestore, no se guarda automáticamente)
    private String id;

    // URL pública de la imagen almacenada en Firebase Storage
    private String url;

    // UID del usuario que subió la foto
    private String idUsuario;

    // Seudónimo del usuario que subió la foto (mostrado en el ranking y la galería)
    private String seudonimo;

    // Estado de la foto: puede ser "pendiente", "admitida" o "rechazada"
    private String estado;

    // Timestamp de la subida (System.currentTimeMillis)
    private long timestamp;

    // Cantidad de votos que ha recibido esta foto
    private int votos;

    // Ancho de la imagen en píxeles
    private int imageWidth;

    // Alto de la imagen en píxeles
    private int imageHeight;

    // Tamaño del archivo en bytes
    private int fileSizeBytes;

    // Formato del archivo (jpg, png, etc.)
    private String format;

    // Correo del participante (puede usarse para control interno)
    private String participantEmail;

    /**
     * Constructor vacío requerido por Firestore para deserialización automática.
     */
    public Foto() {}

    // ------------------- GETTERS Y SETTERS -------------------

    public String getId() {
        return id;
    }

    /**
     * Se establece desde el adaptador después de obtener el ID del documento en Firestore.
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getSeudonimo() {
        return seudonimo;
    }

    public void setSeudonimo(String seudonimo) {
        this.seudonimo = seudonimo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getVotos() {
        return votos;
    }

    public void setVotos(int votos) {
        this.votos = votos;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(int fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }
}
