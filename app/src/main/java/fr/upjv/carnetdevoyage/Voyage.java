package fr.upjv.carnetdevoyage;

public class Voyage {
    private String nom_voyage;
    private int periodicite;

    public Voyage() {} // Nécessaire pour Firestore

    public Voyage(String nom_voyage, int periodicite) {
        this.nom_voyage = nom_voyage;
        this.periodicite = periodicite;
    }

    public String getNom_voyage() { return nom_voyage; }
    public int getPeriodicite() { return periodicite; }
}