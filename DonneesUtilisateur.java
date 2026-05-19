import java.util.ArrayList;

/** Informations personnelles et objectifs d'un utilisateur. */
class DonneesUtilisateur {
  String nomAffichage;
  int age;
  int pourcentInvestissement;
  String occupation;
  ArrayList<Objectif> objectifs = new ArrayList<>();

  public DonneesUtilisateur(String nomAffichage, int age, int pourcentInvestissement, String occupation) {
    this.nomAffichage = nomAffichage;
    this.age = age;
    this.pourcentInvestissement = pourcentInvestissement;
    this.occupation = occupation;
  }
}
