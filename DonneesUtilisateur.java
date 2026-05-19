import java.util.ArrayList;

// Classe qui stocke toutes les informations liées à un utilisateur.
// Ces informations seront utilisées dans not outils durant les proces de calcul.
class DonneesUtilisateur {
  String nomAffichage;
  int age;
  // Pourcentage d'argent voulu pour l'outil d'investissement
  int pourcentInvestissement;
  String occupation;
  ArrayList<Objectif> objectifs = new ArrayList<>();

  // Constructeur
  public DonneesUtilisateur(String nomAffichage, int age, int pourcentInvestissement, String occupation) {
    // Initialisation des informations
    this.nomAffichage = nomAffichage;
    this.age = age;
    this.pourcentInvestissement = pourcentInvestissement;
    this.occupation = occupation;
  }
}
