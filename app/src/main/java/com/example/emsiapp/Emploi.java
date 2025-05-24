package com.example.emsiapp;

public class Emploi {

    private String professeurId;
    private String nomProfesseur;
    private String semestre;
    private String anneeScolaire;
    private String jour;
    private String heureDebut;
    private String heureFin;
    private String matiere;
    private String salle;

    // Constructeur vide (obligatoire pour Firebase)
    public Emploi() {
    }

    // Constructeur complet
    public Emploi(String professeurId, String nomProfesseur, String semestre, String anneeScolaire,
                  String jour, String heureDebut, String heureFin, String matiere, String salle) {
        this.professeurId = professeurId;
        this.nomProfesseur = nomProfesseur;
        this.semestre = semestre;
        this.anneeScolaire = anneeScolaire;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.matiere = matiere;
        this.salle = salle;
    }

    // Getters et Setters
    public String getProfesseurId() {
        return professeurId;
    }

    public void setProfesseurId(String professeurId) {
        this.professeurId = professeurId;
    }

    public String getNomProfesseur() {
        return nomProfesseur;
    }

    public void setNomProfesseur(String nomProfesseur) {
        this.nomProfesseur = nomProfesseur;
    }

    public String getSemestre() {
        return semestre;
    }
    private String niveau;

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public String getAnneeScolaire() {
        return anneeScolaire;
    }

    public void setAnneeScolaire(String anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
    }

    public String getJour() {
        return jour;
    }

    public void setJour(String jour) {
        this.jour = jour;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }
}
