/** Représente un but d'épargne (nom, montant total et épargne mensuelle). */
class Objectif {
  String nom;
  double montantTotal;
  double epargneMensuelle;

  public Objectif(String nom, double montantTotal, double epargneMensuelle) {
    this.nom = nom;
    this.montantTotal = montantTotal;
    this.epargneMensuelle = epargneMensuelle;
  }

  public int obtenirMoisNecessaires() {
    if (epargneMensuelle <= 0) return Integer.MAX_VALUE;
    return (int) Math.ceil(montantTotal / epargneMensuelle);
  }
}
