import javax.swing.*; 
import java.awt.*; 
import java.util.ArrayList; 

// Page qui recommande un compte d'épargne canadien à l'utilisateur
class PageEpargne extends JPanel {
  private JComboBox<String> boiteStatutProprietaire; 
  private JComboBox<String> boiteEpargneEtudesEnfants; 
  private JComboBox<String> boiteObjectifFinancierPrincipal; 
  private JButton boutonGenererPlan;
  private JTextArea zonePlanDetaille; // Grande zone de texte pour le plan généré
  private final FenetrePrincipale fenetrePrincipale; // Référence à la fenêtre principale pour naviguer

  public PageEpargne(FenetrePrincipale fenetre) {
    this.fenetrePrincipale = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout()); 
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); 

    // Barre du haut avec le bouton retour et le titre
    JPanel panneauHaut = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
    panneauHaut.setOpaque(false); 
    JButton boutonRetour = new JButton("< Retour");
    boutonRetour.addActionListener(e -> fenetrePrincipale.retourAccueil()); // Action événement
    panneauHaut.add(boutonRetour);
    JLabel titrePage = new JLabel("Plan d'épargne personnalisé");
    titrePage.setFont(Apparence.SOUS_TITRE);
    titrePage.setForeground(Apparence.PRINCIPALE);
    panneauHaut.add(titrePage);
    add(panneauHaut, BorderLayout.NORTH);

    // Mini formulaire 
    // GridLayout pour placer les composants dans une grille de 3 rangées et 2 colonnes
    // Source: https://docs.oracle.com/javase/tutorial/uiswing/layout/grid.html
    JPanel panneauFormulaire = new JPanel(new GridLayout(3, 2, 8, 8));
    panneauFormulaire.setOpaque(false);
    panneauFormulaire.setBorder(BorderFactory.createTitledBorder("Votre situation")); // Cadre avec titre
    panneauFormulaire.add(new JLabel("Propriétaire d'une maison ?"));
    boiteStatutProprietaire = new JComboBox<>(new String[]{"Non", "Oui", "Je loue"});
    panneauFormulaire.add(boiteStatutProprietaire);
    panneauFormulaire.add(new JLabel("Épargne pour études d'enfant ?"));
    boiteEpargneEtudesEnfants = new JComboBox<>(new String[]{"Non", "Oui, un enfant", "Oui, plusieurs"});
    panneauFormulaire.add(boiteEpargneEtudesEnfants);
    panneauFormulaire.add(new JLabel("Objectif financier principal"));
    boiteObjectifFinancierPrincipal = new JComboBox<>(new String[]{
      "Fonds d'urgence", "Acheter maison", "Retraite", "Croître richesse", "Études enfants", "Revenu complémentaire"
    });
    panneauFormulaire.add(boiteObjectifFinancierPrincipal);

    // BoxLayout.Y_AXIS empile les éléments de haut en bas
    // Source: https://docs.oracle.com/javase/tutorial/uiswing/layout/border.html
    JPanel panneauCentre = new JPanel();
    panneauCentre.setOpaque(false);
    panneauCentre.setLayout(new BoxLayout(panneauCentre, BoxLayout.Y_AXIS));
    panneauCentre.add(panneauFormulaire);
    add(panneauCentre, BorderLayout.CENTER);

    // Partie basse: bouton et zone de texte avec scroll
    JPanel panneauBas = new JPanel(new BorderLayout(0, 10));
    panneauBas.setOpaque(false);
    boutonGenererPlan = new JButton("Générer le plan");
    boutonGenererPlan.setFont(Apparence.SOUS_TITRE);
    boutonGenererPlan.setBackground(Apparence.SECONDAIRE);
    boutonGenererPlan.setForeground(Color.WHITE);

    JPanel panneauBouton = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauBouton.setOpaque(false);
    panneauBouton.add(boutonGenererPlan);
    panneauBas.add(panneauBouton, BorderLayout.NORTH);

    // Zone de texte pour afficher le plan généré
    zonePlanDetaille = new JTextArea(10, 40);
    zonePlanDetaille.setEditable(false); // L'utilisateur ne peut pas modifier le texte
    zonePlanDetaille.setLineWrap(true); 
    zonePlanDetaille.setWrapStyleWord(true); 
    zonePlanDetaille.setFont(new Font("Monospaced", Font.PLAIN, 12));
    zonePlanDetaille.setMargin(new Insets(10, 10, 10, 10)); // Espace intérieur (haut, gauche, bas, droite)
    zonePlanDetaille.setText("Cliquez sur « Générer le plan » pour afficher votre plan d'épargne.");

    // JScrollPane ajoute des barres de défilement si le texte dépasse la zone
    // Source: https://docs.oracle.com/javase/tutorial/uiswing/components/scrollpane.html
    JScrollPane defilementPlan = new JScrollPane(zonePlanDetaille);
    defilementPlan.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Pas de scroll horizontal
    panneauBas.add(defilementPlan, BorderLayout.CENTER);

    add(panneauBas, BorderLayout.SOUTH);

    boutonGenererPlan.addActionListener(e -> genererPlanDetaille());
  }



  private void genererPlanDetaille() {
    String nomUtilisateur = fenetrePrincipale.nomUtilisateurConnecte;
    if (nomUtilisateur == null) {
      zonePlanDetaille.setText("Veuillez vous connecter pour générer un plan d'épargne.");
      return;
    }

    DonneesUtilisateur donneesUtilisateur = GestionAuth.obtenirProfilUtilisateur(nomUtilisateur);
    if (donneesUtilisateur == null) {
      zonePlanDetaille.setText("Profil utilisateur introuvable.");
      return;
    }

    ArrayList<Objectif> listeObjectifs = donneesUtilisateur.objectifs;
    String objectifPrincipal = (String) boiteObjectifFinancierPrincipal.getSelectedItem();

    // StringBuilder est plus efficace que de concaténer des String avec + en boucle
    // https://stackoverflow.com/questions/1532461/stringbuilder-vs-string-concatenation-in-tostring-in-java
    StringBuilder plan = new StringBuilder();

    plan.append("========== PLAN D'ÉPARGNE PERSONNALISÉ ==========\n\n");
    plan.append("PROFIL UTILISATEUR\n");
    plan.append("Âge : ").append(donneesUtilisateur.age).append(" ans\n");
    plan.append("Occupation : ").append(donneesUtilisateur.occupation).append("\n");
    plan.append("Objectif : ").append(objectifPrincipal).append("\n");
    plan.append("Propriétaire : ").append(boiteStatutProprietaire.getSelectedItem()).append("\n\n");

    if (!listeObjectifs.isEmpty()) {
      plan.append("VOS OBJECTIFS\n");
      for (Objectif objectif : listeObjectifs) {
        // Convertit les mois en années + mois restants pour afficher "1 an et 3 mois"
        int moisRequis = objectif.obtenirMoisNecessaires();
        int anneesRequis = moisRequis / 12;
        int moisRestants = moisRequis % 12;
        String temps = "";
        if (anneesRequis > 0) temps += anneesRequis + " an" + (anneesRequis > 1 ? "s" : "");
        if (anneesRequis > 0 && moisRestants > 0) temps += " et ";
        if (moisRestants > 0 || anneesRequis == 0) temps += moisRestants + " mois";

        // String.format("%.0f") formate un double en entier sans décimales
        // https://docs.oracle.com/javase/tutorial/java/data/numberformat.html
        plan.append("- ").append(objectif.nom).append(": ");
        plan.append(String.format("%.0f", objectif.montantTotal)).append("$ (");
        plan.append(String.format("%.0f", objectif.epargneMensuelle)).append("$/mois) - ");
        plan.append(temps).append("\n");
      }
      plan.append("\n");
    } else {
      plan.append("ASTUCE : Allez dans « Mes Objectifs » pour créer votre premier objectif !\n\n");
    }

    // Conseils personnalisés selon l'âge
    plan.append("CONSEILS\n");
    if (donneesUtilisateur.age < 30) {
      plan.append("- Commencez tôt pour profiter des intérêts composés\n");
      plan.append("- Priorisez le CELI pour la flexibilité\n");
    } else if (donneesUtilisateur.age < 50) {
      plan.append("- Équilibre entre croissance et sécurité\n");
      plan.append("- Considérez le REER pour réduire votre revenu imposable\n");
    } else {
      plan.append("- Privilégiez la préservation du capital\n");
      plan.append("- Le CELI est avantageux pour les retraits libres d'impôt\n");
    }

    plan.append("\n==================================================\n");
    plan.append("Outil éducatif — Consultez un conseiller financier\n");
    plan.append("==================================================");

    zonePlanDetaille.setText(plan.toString());
    zonePlanDetaille.setCaretPosition(0); // Remet le scroll en haut du texte
  }
}
