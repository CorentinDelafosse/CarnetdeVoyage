package fr.upjv.carnetdevoyage;

public class Voyage {
    private String nom_voyage;
    private int periodicite;
    private String email; //

    public Voyage() {} // Requis par Firestore

    public Voyage(String nom_voyage, int periodicite, String email) {
        this.nom_voyage = nom_voyage;
        this.periodicite = periodicite;
        this.email = email;
    }

    public String getNom_voyage() {
        return nom_voyage;
    }

    public int getPeriodicite() {
        return periodicite;
    }

    public String getEmail() {
        return email;
    }

    public void setNom_voyage(String nom_voyage) {
        this.nom_voyage = nom_voyage;
    }

    public void setPeriodicite(int periodicite) {
        this.periodicite = periodicite;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
