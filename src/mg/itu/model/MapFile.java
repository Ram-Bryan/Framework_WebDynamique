package mg.itu.model;

public class MapFile {
    private String nomFichier;
    private String extension;
    private byte[] contenu;

    public MapFile() {
    }

    public MapFile(String nomFichier, String extension, byte[] contenu) {
        this.nomFichier = nomFichier;
        this.extension = extension;
        this.contenu = contenu;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getContenu() {
        return contenu;
    }

    public void setContenu(byte[] contenu) {
        this.contenu = contenu;
    }
}
