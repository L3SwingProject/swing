package main;

import exceptions.BadFormatMessageException;

public class Capteur implements Comparable<Capteur> {
    private String nom;
    private String batiment;
    private int etage;
    private String lieu;
    private TypeFluide type;
    private float valeurCourante = 0.f;
    private float seuilMin;
    private float seuilMax;
    private boolean connecte = true;
    private String localisation;

    public Capteur(String nom, String batiment, int etage, String lieu, TypeFluide type, float valeurCourante, float seuilMin, float seuilMax, boolean connecte){
        this.nom = nom;
        this.batiment = batiment;
        this.etage = etage;
        this.lieu = lieu;
        this.type = type;
        this.valeurCourante = valeurCourante;
        this.seuilMin = seuilMin;
        this.seuilMax = seuilMax;
        this.connecte = connecte;
        localisation = batiment+"-"+etage+"-"+lieu;
    }

    public Capteur(String nom, String description) throws BadFormatMessageException {
        this.nom=nom;
        String des[] = description.split(":");
        this.batiment=des[1];
        this.lieu = des[3];
        if (batiment.length() > 5)  throw new BadFormatMessageException("Batiment invalide");
        this.localisation=des[1]+"-"+des[2]+"-"+des[3];
        try{
            this.etage= Integer.valueOf(des[2]);
            this.type = TypeFluide.valueOf(des[0]);
        }catch(NumberFormatException e){
            throw new BadFormatMessageException("Numero etage invalide");
        }catch(IllegalArgumentException e){
            throw new BadFormatMessageException("Type invalide");
        }
        initSeuil();
    }

    public void initSeuil() {
        if(type==TypeFluide.AIRCOMPRIME) {
            seuilMin = 0;
            seuilMax = 5;
        }
        if(type==TypeFluide.EAU) {
            seuilMin = 0;
            seuilMax = 10;
        }
        if(type==TypeFluide.ELECTRICITE) {
            seuilMin = 10;
            seuilMax = 500;
        }
        if(type==TypeFluide.TEMPERATURE) {
            seuilMin = 17;
            seuilMax = 22;
        }
    }

    public void update(float newValue){
        this.valeurCourante = newValue;
    }

    public void deconnexion(){
        this.connecte = false;
    }

    @Override
    public int compareTo(Capteur arg){
        return nom.compareTo(arg.nom);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capteur capteur = (Capteur) o;
        return nom.equals(capteur.nom);
    }

    public String getEtageString() {
        return batiment+":"+etage;
    }

    @Override
    public String toString(){
        return nom;
    }

    public String toStringBis() {
        return "Capteur{" +
                "nom='" + nom + '\'' +
                ", batiment='" + batiment + '\'' +
                ", etage=" + etage +
                ", lieu='" + lieu + '\'' +
                ", type=" + type +
                ", valeurCourante=" + valeurCourante +
                ", seuilMin=" + seuilMin +
                ", seuilMax=" + seuilMax +
                ", connecte=" + connecte +
                '}';
    }

    public String getNom() {
        return nom;
    }

    public String getBatiment() {
        return batiment;
    }

    public int getEtage() {
        return etage;
    }

    public String getLieu() {
        return lieu;
    }

    public float getValeurCourante() {
        return valeurCourante;
    }

    public float getSeuilMin() {
        return seuilMin;
    }

    public float getSeuilMax() {
        return seuilMax;
    }

    public TypeFluide getType() {
        return type;
    }

    public String getLocalisation() {
        return localisation;
    }

    public boolean estConnect(){  return connecte;  }

    public void setSeuilMin(float seuilMin){
        this.seuilMin = seuilMin;
    }

    public void setSeuilMax(float seuilMax){
        this.seuilMax = seuilMax;
    }

    public boolean valeurExtSeuil() {
        return (valeurCourante<seuilMin || valeurCourante>seuilMax);
    }
}
