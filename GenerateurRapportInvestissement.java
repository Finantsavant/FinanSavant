import java.util.ArrayList;

/** Génère les sections texte du plan d'investissement et la simulation. */
class GenerateurRapportInvestissement {

  static String construirePlan(double montant, DonneesUtilisateur utilisateur, int niveauRisque,
    boolean actionsSelectionnees, boolean fnbSelectionne, boolean cryptoSelectionnee,
    boolean obligationsSelectionnees, boolean matieresSelectionnees, boolean immobilierSelectionne) {
    int pourcentInvestissement = utilisateur.pourcentInvestissement;
    double montantInvesti = montant * pourcentInvestissement / 100.0;
    double montantEpargne = montant - montantInvesti;
    int age = utilisateur.age;
    String occupation = utilisateur.occupation;
    StringBuilder rapport = new StringBuilder();
    rapport.append("=================================================\n");
    rapport.append(" PLAN D'INVESTISSEMENT PERSONNALISÉ\n");
    rapport.append("=================================================\n\n");
    rapport.append(String.format("Montant total analysé : %.2f $\n", montant));
    rapport.append(String.format("Investissement : %.2f $ (%d%%) | Épargne : %.2f $ (%d%%)\n\n",
      montantInvesti, pourcentInvestissement, montantEpargne, 100 - pourcentInvestissement));
    rapport.append(obtenirResumeConseiller(utilisateur, niveauRisque, montant, true));
    rapport.append("\n-------------------------------------------------\n\n");
    if (actionsSelectionnees) rapport.append(construireSectionActions(montantInvesti, niveauRisque, age, occupation));
    if (fnbSelectionne) rapport.append(construireSectionFnb(montantInvesti, niveauRisque, age));
    if (cryptoSelectionnee) rapport.append(construireSectionCrypto(montantInvesti, niveauRisque, age));
    if (obligationsSelectionnees) rapport.append(construireSectionObligations(montantInvesti, niveauRisque, age, occupation));
    if (matieresSelectionnees) rapport.append(construireSectionMatieresPremieres(montantInvesti, niveauRisque));
    if (immobilierSelectionne) rapport.append(construireSectionImmobilierCote(montantInvesti, niveauRisque, age, occupation));
    rapport.append(construireRapportSimulation(montant, utilisateur, niveauRisque,
      actionsSelectionnees, fnbSelectionne, cryptoSelectionnee,
      obligationsSelectionnees, matieresSelectionnees, immobilierSelectionne));
    rapport.append("\n=================================================\n");
    rapport.append("⚠ Ceci est un outil éducatif, pas un conseil\n");
    rapport.append(" financier professionnel certifié.\n");
    rapport.append("=================================================\n");
    return rapport.toString();
  }

  static String obtenirResumeConseiller(DonneesUtilisateur utilisateur, int niveauRisque,
    double montant, boolean auMoinsUnSelectionne) {
    StringBuilder rapport = new StringBuilder();
    rapport.append("Bonjour ").append(utilisateur.nomAffichage).append(", voici votre analyse personnelle :\n\n");
    rapport.append("Profil : âge ").append(utilisateur.age).append(" ans, occupation ")
      .append(utilisateur.occupation).append(", allocation invest/épargne ")
      .append(utilisateur.pourcentInvestissement).append("% / ")
      .append(100 - utilisateur.pourcentInvestissement).append("%\n");
    rapport.append("Tolérance au risque : ");
    if (niveauRisque == 0) rapport.append("Conservatrice\n\n");
    else if (niveauRisque == 1) rapport.append("Équilibrée\n\n");
    else rapport.append("Agressive\n\n");
    if (montant > 0) rapport.append(String.format("Budget analysé : %.2f $\n\n", montant));
    if (!auMoinsUnSelectionne) {
      rapport.append("Sélectionnez des classes d'actifs pour recevoir un plan clair.\n");
      return rapport.toString();
    }
    rapport.append("Recommandations immédiates :\n");
    if (niveauRisque == 0) rapport.append("- Priorisez la stabilité : obligations, FNB défensifs, immobilier coté.\n");
    else if (niveauRisque == 1) rapport.append("- Mélangez actions, FNB diversifiés et obligations.\n");
    else rapport.append("- Exposition croissance : actions, cryptomonnaies, actifs thématiques.\n");
    if (utilisateur.age < 30) rapport.append("- Horizon long terme : réinvestissez vos gains.\n");
    else if (utilisateur.age >= 55) rapport.append("- Préservez le capital avec des revenus fixes.\n");
    rapport.append("- Gardez une réserve de trésorerie.\n\n");
    rapport.append("Ce rapport inclut une projection de croissance simplifiée.\n");
    return rapport.toString();
  }

  private static String construireRapportSimulation(double montant, DonneesUtilisateur utilisateur,
    int niveauRisque, boolean actionsSel, boolean fnbSel, boolean cryptoSel,
    boolean obligationsSel, boolean matieresSel, boolean immobilierSel) {
    StringBuilder rapport = new StringBuilder();
    rapport.append("──── SIMULATION DE CROISSANCE ────\n\n");
    double montantInvesti = montant * utilisateur.pourcentInvestissement / 100.0;
    ArrayList<String> actifs = new ArrayList<>();
    if (actionsSel) actifs.add("Actions");
    if (fnbSel) actifs.add("FNB");
    if (cryptoSel) actifs.add("Cryptomonnaies");
    if (obligationsSel) actifs.add("Obligations");
    if (matieresSel) actifs.add("Matières premières");
    if (immobilierSel) actifs.add("Immobilier coté");
    if (actifs.isEmpty()) {
      rapport.append("Aucune classe d'actif sélectionnée pour la simulation.\n");
      return rapport.toString();
    }
    double allocation = montantInvesti / actifs.size();
    double totalAnnee1 = 0, totalAnnee3 = 0, totalAnnee5 = 0;
    for (String actif : actifs) {
      double taux = obtenirRendementAttendu(actif, niveauRisque);
      double valeur1 = calculerCapitalCompose(allocation, taux, 1);
      double valeur3 = calculerCapitalCompose(allocation, taux, 3);
      double valeur5 = calculerCapitalCompose(allocation, taux, 5);
      totalAnnee1 += valeur1;
      totalAnnee3 += valeur3;
      totalAnnee5 += valeur5;
      rapport.append(String.format("%s : rendement attendu %.1f%%/an\n", actif, taux * 100));
      rapport.append(String.format(" 1 an : %.2f $ | 3 ans : %.2f $ | 5 ans : %.2f $\n\n",
        valeur1, valeur3, valeur5));
    }
    rapport.append(String.format(
      "Projection globale investie :\n 1 an -> %.2f $\n 3 ans -> %.2f $\n 5 ans -> %.2f $\n\n",
      totalAnnee1, totalAnnee3, totalAnnee5));
    rapport.append("Projections indicatives, non garanties.\n");
    return rapport.toString();
  }

  private static double obtenirRendementAttendu(String actif, int niveauRisque) {
    switch (actif) {
      case "Actions": return niveauRisque == 2 ? 0.12 : niveauRisque == 1 ? 0.08 : 0.05;
      case "FNB": return niveauRisque == 2 ? 0.10 : niveauRisque == 1 ? 0.07 : 0.05;
      case "Cryptomonnaies": return niveauRisque == 2 ? 0.20 : niveauRisque == 1 ? 0.10 : 0.04;
      case "Obligations": return niveauRisque == 2 ? 0.05 : niveauRisque == 1 ? 0.04 : 0.03;
      case "Matières premières": return niveauRisque == 2 ? 0.08 : niveauRisque == 1 ? 0.06 : 0.04;
      case "Immobilier coté": return niveauRisque == 2 ? 0.09 : niveauRisque == 1 ? 0.06 : 0.05;
      default: return 0.05;
    }
  }

  private static double calculerCapitalCompose(double capital, double tauxAnnuel, int annees) {
    return capital * Math.pow(1 + tauxAnnuel, annees);
  }

  private static String formaterLigne(String symbole, String nomTitre, String description,
    double allocation, double base) {
    double montantLigne = base * allocation;
    return String.format(" %-10s %s%n %s%n Allocation : %.0f%% | Montant : %.2f $%n%n",
      symbole, nomTitre, description, allocation * 100, montantLigne);
  }

  private static String construireSectionActions(double montantInvesti, int risque, int age, String occupation) {
    StringBuilder rapport = new StringBuilder("──── ACTIONS ────\n\n");
    if (risque == 0 || age >= 55) {
      rapport.append(formaterLigne("JNJ", "Johnson & Johnson",
        "Entreprise pharmaceutique stable, dividendes réguliers.", 0.20, montantInvesti));
      rapport.append(formaterLigne("KO", "Coca-Cola",
        "Valeur refuge, dividendes croissants.", 0.15, montantInvesti));
    } else if (risque == 1) {
      rapport.append(formaterLigne("AAPL", "Apple Inc.", "Leader technologique.", 0.20, montantInvesti));
      rapport.append(formaterLigne("MSFT", "Microsoft", "Cloud et IA.", 0.15, montantInvesti));
    } else {
      rapport.append(formaterLigne("NVDA", "Nvidia", "Semi-conducteurs et IA.", 0.20, montantInvesti));
      rapport.append(formaterLigne("TSLA", "Tesla", "Véhicules électriques.", 0.15, montantInvesti));
    }
    if (occupation.equals("Étudiant") || occupation.equals("Temps partiel")) {
      rapport.append(formaterLigne("VTI", "Marché total US", "Diversification large.", 0.10, montantInvesti));
    }
    return rapport.append("\n").toString();
  }

  private static String construireSectionFnb(double montantInvesti, int risque, int age) {
    StringBuilder rapport = new StringBuilder("──── FNB ────\n\n");
    rapport.append(formaterLigne("VOO", "FNB S&P 500",
      "Les 500 plus grandes entreprises américaines.", risque == 2 ? 0.20 : 0.30, montantInvesti));
    if (risque >= 1) {
      rapport.append(formaterLigne("QQQ", "FNB Nasdaq-100", "Grandes entreprises technologiques.", 0.15, montantInvesti));
    }
    if (age >= 40 || risque == 0) {
      rapport.append(formaterLigne("XBB.TO", "FNB obligations canadiennes",
        "Marché obligataire canadien.", 0.15, montantInvesti));
    }
    return rapport.append("\n").toString();
  }

  private static String construireSectionCrypto(double montantInvesti, int risque, int age) {
    StringBuilder rapport = new StringBuilder("──── CRYPTOMONNAIES ────\n\n");
    if (risque == 0 || age >= 55) {
      rapport.append("⚠ Déconseillé pour un profil conservateur.\n");
      rapport.append(formaterLigne("BTC", "Bitcoin", "Crypto la plus établie.", 0.05, montantInvesti));
    } else if (risque == 1) {
      rapport.append(formaterLigne("BTC", "Bitcoin", "Liquidité maximale.", 0.10, montantInvesti));
      rapport.append(formaterLigne("ETH", "Ethereum", "Contrats intelligents.", 0.07, montantInvesti));
    } else {
      rapport.append(formaterLigne("BTC", "Bitcoin", "Référence du marché.", 0.10, montantInvesti));
      rapport.append(formaterLigne("ETH", "Ethereum", "Écosystème DeFi.", 0.08, montantInvesti));
    }
    return rapport.append("\n").toString();
  }

  private static String construireSectionObligations(double montantInvesti, int risque, int age, String occupation) {
    StringBuilder rapport = new StringBuilder("──── OBLIGATIONS ────\n\n");
    double allocGouv = (age >= 55 || occupation.equals("Retraité")) ? 0.25 : 0.15;
    rapport.append(formaterLigne("CAN GOV", "Obligations du Canada",
      "Faible risque, revenus prévisibles.", allocGouv, montantInvesti));
    if (risque >= 1) {
      rapport.append(formaterLigne("ZAG.TO", "FNB obligations agrégées",
        "Mix gouvernemental et corporatif.", 0.10, montantInvesti));
    }
    return rapport.append("\n").toString();
  }

  private static String construireSectionMatieresPremieres(double montantInvesti, int risque) {
    StringBuilder rapport = new StringBuilder("──── MATIÈRES PREMIÈRES ────\n\n");
    rapport.append(formaterLigne("GLD", "FNB Or",
      "Protection contre l'inflation.", risque == 0 ? 0.15 : 0.10, montantInvesti));
    if (risque >= 1) {
      rapport.append(formaterLigne("SLV", "FNB Argent", "Plus volatil que l'or.", 0.07, montantInvesti));
    }
    return rapport.append("\n").toString();
  }

  private static String construireSectionImmobilierCote(double montantInvesti, int risque, int age, String occupation) {
    StringBuilder rapport = new StringBuilder("──── IMMOBILIER COTÉ ────\n\n");
    rapport.append(formaterLigne("VNQ", "FNB immobilier diversifié",
      "Bureaux, résidentiel, entrepôts.", 0.15, montantInvesti));
    if (occupation.equals("Retraité") || age >= 50) {
      rapport.append(formaterLigne("O", "Realty Income",
        "Dividendes mensuels fiables.", 0.10, montantInvesti));
    }
    if (risque >= 1) {
      rapport.append(formaterLigne("STAG", "STAG Industrial",
        "Immobilier industriel.", 0.08, montantInvesti));
    }
    return rapport.append("\n").toString();
  }
}
