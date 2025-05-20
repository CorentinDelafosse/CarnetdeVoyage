package fr.upjv.carnetdevoyage;

public class Voyage {
    private String nom;
    private String dateDebut;
    private String dateFin;

    public Voyage(String nom, String dateDebut, String dateFin) {
        this.nom = nom;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }
}
