package com.example.emsiapp;

public class Rattrapage {
    public String matiere, groupe, site, salle, heureDebut, heureFin, description;
    public com.google.firebase.Timestamp date;

    public Rattrapage() {} // Nécessaire pour Firebase

    public Rattrapage(String matiere, String groupe, String site, String salle,
                      String heureDebut, String heureFin, String description,
                      com.google.firebase.Timestamp date) {
        this.matiere = matiere;
        this.groupe = groupe;
        this.site = site;
        this.salle = salle;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.description = description;
        this.date = date;
    }
}
