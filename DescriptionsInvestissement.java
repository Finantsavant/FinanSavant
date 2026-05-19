import java.util.LinkedHashMap;
import java.util.Map;

/** Textes explicatifs pour chaque type d'investissement. */
class DescriptionsInvestissement {
  private static final Map<String, String> DESCRIPTIONS = new LinkedHashMap<>();

  static {
    DESCRIPTIONS.put("Actions",
      "<html><b>Actions</b><br><br>"
        + "Une action représente une part de propriété dans une entreprise cotée en bourse.<br><br>"
        + "• Potentiel de rendement élevé à long terme<br>"
        + "• Volatilité plus importante que d'autres produits<br>"
        + "• Idéal pour un horizon d'investissement de 5 ans et plus<br><br>"
        + "Risque : Moyen à Élevé</html>");
    DESCRIPTIONS.put("FNB",
      "<html><b>Fonds négociés en bourse (FNB)</b><br><br>"
        + "Un FNB regroupe plusieurs titres (actions, obligations) en un seul produit.<br><br>"
        + "• Diversification automatique<br>"
        + "• Frais de gestion très bas<br>"
        + "• Idéal pour les investisseurs débutants ou passifs<br><br>"
        + "Risque : Faible à Moyen</html>");
    DESCRIPTIONS.put("Cryptomonnaies",
      "<html><b>Cryptomonnaies</b><br><br>"
        + "Monnaies numériques décentralisées (ex. : Bitcoin, Ethereum).<br><br>"
        + "• Potentiel de gains très élevés<br>"
        + "• Volatilité extrêmement forte<br>"
        + "• Convient aux profils tolérant un risque élevé<br><br>"
        + "Risque : Très Élevé</html>");
    DESCRIPTIONS.put("Obligations",
      "<html><b>Obligations</b><br><br>"
        + "Prêts à des gouvernements ou entreprises en échange d'intérêts.<br><br>"
        + "• Revenus fixes et prévisibles<br>"
        + "• Capital généralement protégé<br>"
        + "• Idéal pour les profils conservateurs<br><br>"
        + "Risque : Faible</html>");
    DESCRIPTIONS.put("Matières premières",
      "<html><b>Matières premières</b><br><br>"
        + "Ressources physiques : or, pétrole, blé, etc.<br><br>"
        + "• Protection contre l'inflation<br>"
        + "• Faible corrélation avec les marchés boursiers<br><br>"
        + "Risque : Moyen</html>");
    DESCRIPTIONS.put("Immobilier coté",
      "<html><b>Immobilier coté</b><br><br>"
        + "Investir dans l'immobilier sans acheter un bien directement.<br><br>"
        + "• Dividendes réguliers<br>"
        + "• Exposition au marché immobilier<br>"
        + "• Plus liquide que l'immobilier physique<br><br>"
        + "Risque : Moyen</html>");
  }

  static String obtenirDescription(String etiquette) {
    return DESCRIPTIONS.getOrDefault(etiquette, "<html>Aucune description disponible.</html>");
  }
}
