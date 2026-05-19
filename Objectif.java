/** représente un but d'épargne (nom, montant total et épargne mensuelle). */
class Objectif {
  // nom du but d'épargne
  String nom;
  // argent total à atteindre
  double montantTotal;
  // argent mis de côté chaque mois
  double epargneMensuelle;

  // constructeur de l'objectif
  public Objectif(String nom, double montantTotal, double epargneMensuelle) {
    this.nom = nom;
    this.montantTotal = montantTotal;
    this.epargneMensuelle = epargneMensuelle;
  }

  // calcule combien de mois il faut pour atteindre le but
  public int obtenirMoisNecessaires() {
    if (epargneMensuelle <= 0) return Integer.MAX_VALUE;
    return (int) Math.ceil(montantTotal / epargneMensuelle);
  }
}
