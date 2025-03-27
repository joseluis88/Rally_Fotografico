package com.example.rallyfotografico.models;

public class Foto {
    private String id;
    private String url;
    private String idUsuario;
    private String estado;
    private long timestamp;
    private int votos;

    // Nuevos campos
    private int imageWidth;
    private int imageHeight;
    private int fileSizeBytes;
    private String format;
    private String participantEmail;

    // Constructor vacío necesario para Firestore
    public Foto() { }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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

    // Getters y setters para los nuevos campos
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
