import java.util.LinkedHashMap;
import java.util.Map;

/** textes explicatifs pour chaque type d'investissement */
class DescriptionsInvestissement {

  // linkedhashmap garde l’ordre d’insertion (les éléments restent dans l’ordre où on les ajoute)
  private static final Map<String, String> DESCRIPTIONS = new LinkedHashMap<>();

  static {

    // on associe chaque type d’investissement à une description en html
    // html ici sert à formater le texte (gras, sauts de ligne, listes) dans une interface graphique
    // https://docs.oracle.com/javase/tutorial/uiswing/components/html.html
    DESCRIPTIONS.put("Actions",
      "<html><b>Actions</b><br><br>"
        + "Une action représente une part de propriété dans une entreprise cotée en bourse.<br><br>"
        + "• Potentiel de rendement élevé à long terme<br>"
        + "• Volatilité plus importante que d'autres produits<br>"
        + "• Idéal pour un horizon d'investissement de 5 ans et plus<br><br>"
        + "Risque : Moyen à Élevé</html>");

    DESCRIPTIONS.put("ETF",
      "<html><b>Fonds négociés en bourse </b><br><br>"
        + "Un ETF regroupe plusieurs titres (actions, obligations) en un seul produit.<br><br>"
        + "• diversification automatique<br>"
        + "• frais de gestion très bas<br>"
        + "• idéal pour les investisseurs débutants ou passifs<br><br>"
        + "Risque : faible à moyen</html>");

    DESCRIPTIONS.put("Cryptomonnaies",
      "<html><b>Cryptomonnaies</b><br><br>"
        + "monnaies numériques décentralisées (ex. bitcoin, ethereum).<br><br>"
        + "• potentiel de gains très élevés<br>"
        + "• volatilité extrêmement forte<br>"
        + "• convient aux profils tolérant un risque élevé<br><br>"
        + "Risque : très élevé</html>");

    DESCRIPTIONS.put("Obligations",
      "<html><b>Obligations</b><br><br>"
        + "prêts à des gouvernements ou entreprises en échange d'intérêts.<br><br>"
        + "• revenus fixes et prévisibles<br>"
        + "• capital généralement protégé<br>"
        + "• idéal pour les profils conservateurs<br><br>"
        + "Risque : faible</html>");

    DESCRIPTIONS.put("Matières premières",
      "<html><b>Matières premières</b><br><br>"
        + "ressources physiques : or, pétrole, blé, etc.<br><br>"
        + "• protection contre l'inflation<br>"
        + "• faible corrélation avec les marchés boursiers<br><br>"
        + "Risque : moyen</html>");

    DESCRIPTIONS.put("Immobilier coté",
      "<html><b>Immobilier coté</b><br><br>"
        + "investir dans l'immobilier sans acheter un bien directement.<br><br>"
        + "• dividendes réguliers<br>"
        + "• exposition au marché immobilier<br>"
        + "• plus liquide que l'immobilier physique<br><br>"
        + "Risque : moyen</html>");
  }

  // méthode qui retourne la description selon le type choisi
  static String obtenirDescription(String etiquette) {
    // si la clé existe pas, on affiche un message par défaut
    return DESCRIPTIONS.getOrDefault(etiquette, "<html>aucune description disponible.</html>");
  }
}
