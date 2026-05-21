import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
// on importe tout ce qu'il faut pour faire l'interface graphique en java
// doc officielle : https://docs.oracle.com/javase/8/docs/api/javax/swing/package-summary.html

/** interface de génération du plan d'investissement personnalisé. */
class PageInvestissement extends JPanel {

  // liste déroulante pour choisir le niveau de risque (faible/moyen/élevé)
  JComboBox<String> boiteRisque;

  // cases à cocher pour les différentes classes d'actifs
  // une "classe d'actif" c'est juste un type d'investissement
  JCheckBox caseActions;
  JCheckBox caseEtf;        // fonds négociés en bourse (comme ETF en anglais)
  JCheckBox caseCrypto;
  JCheckBox caseObligations;
  JCheckBox caseMatieresPremieres;
  JCheckBox caseImmobilierCote;

  // référence vers la fenêtre principale pour pouvoir naviguer entre les pages
  private final FenetrePrincipale fenetre;


  // constructeur : construit la page avec tous ses composants
  public PageInvestissement(FenetrePrincipale fenetre) {
    this.fenetre = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(20, 20, 20, 20)); // espace autour du contenu

    // barre du haut avec bouton retour + titre
    JPanel panneauHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panneauHaut.setOpaque(false);
    JButton boutonRetour = new JButton("< Retour");
    boutonRetour.addActionListener(e -> fenetre.retourAccueil());
    panneauHaut.add(boutonRetour);
    JLabel titre = new JLabel("Générateur de plan d'investissement");
    titre.setFont(Apparence.SOUS_TITRE);
    titre.setForeground(Apparence.PRINCIPALE);
    panneauHaut.add(titre);
    add(panneauHaut, BorderLayout.NORTH);

    // contenu principal empilé verticalement
    // BoxLayout.Y_AXIS = les éléments se placent les uns en dessous des autres
    // https://stackoverflow.com/questions/22260434/boxlayout-y-axis-explanation
    JPanel conteneurPrincipal = new JPanel();
    conteneurPrincipal.setOpaque(false);
    conteneurPrincipal.setLayout(new BoxLayout(conteneurPrincipal, BoxLayout.Y_AXIS));

    // panneau pour choisir la tolérance au risque
    JPanel panneauRisque = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauRisque.setOpaque(false);
    JLabel etiquetteRisque = new JLabel("Tolérance au risque :");
    etiquetteRisque.setFont(Apparence.CORPS);
    etiquetteRisque.setForeground(Apparence.TEXTE);
    panneauRisque.add(etiquetteRisque);
    String[] risques = {
      "Faible - Stable et sécuritaire",
      "Moyen - Équilibre croissance/sécurité",
      "Élevé - Croissance agressive"
    };
    boiteRisque = new JComboBox<>(risques);
    boiteRisque.setFont(Apparence.CORPS);
    panneauRisque.add(boiteRisque);
    conteneurPrincipal.add(panneauRisque);

    // grille 2x3 pour les cases à cocher (2 rangées, 3 colonnes, espacement 10px)
    // https://docs.oracle.com/javase/8/docs/api/java/awt/GridLayout.html
    JPanel panneauTypes = new JPanel(new GridLayout(2, 3, 10, 10));
    panneauTypes.setOpaque(false);
    panneauTypes.setBorder(BorderFactory.createTitledBorder("Types d'investissements"));

    // creerCaseConfirmation ajoute automatiquement la boîte de confirmation
    caseActions = creerCaseConfirmation("Actions");
    caseEtf = creerCaseConfirmation("ETF");
    caseCrypto = creerCaseConfirmation("Cryptomonnaies");
    caseObligations = creerCaseConfirmation("Obligations");
    caseMatieresPremieres = creerCaseConfirmation("Matières premières");
    caseImmobilierCote = creerCaseConfirmation("Immobilier coté");

    panneauTypes.add(caseActions);
    panneauTypes.add(caseEtf);
    panneauTypes.add(caseCrypto);
    panneauTypes.add(caseObligations);
    panneauTypes.add(caseMatieresPremieres);
    panneauTypes.add(caseImmobilierCote);

    // wrapper pour centrer la grille horizontalement
    JPanel panneauTypesWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauTypesWrapper.setOpaque(false);
    panneauTypesWrapper.add(panneauTypes);
    conteneurPrincipal.add(panneauTypesWrapper);

    // gros bouton vert pour générer le plan
    JPanel panneauBouton = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    panneauBouton.setOpaque(false);
    JButton boutonGenerer = new JButton("Générer le plan");
    boutonGenerer.setFont(Apparence.SOUS_TITRE);
    boutonGenerer.setBackground(Apparence.SECONDAIRE);
    boutonGenerer.setForeground(Color.WHITE);
    boutonGenerer.setPreferredSize(new Dimension(200, 44));
    panneauBouton.add(boutonGenerer);
    conteneurPrincipal.add(panneauBouton);

    // JScrollPane rend le contenu défilable si la fenêtre est trop petite
    // https://docs.oracle.com/javase/8/docs/api/javax/swing/JScrollPane.html
    add(new JScrollPane(conteneurPrincipal), BorderLayout.CENTER);

    // AncestorListener détecte quand cette page devient visible
    // on s'en sert pour remettre les cases à zéro à chaque visite
    // https://stackoverflow.com/questions/13731303/ancestorlistener-vs-hierarchylistener
    addAncestorListener(new javax.swing.event.AncestorListener() {
      public void ancestorAdded(javax.swing.event.AncestorEvent e) { reinitialiserPage(); }
      public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
      public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
    });


    // action du bouton "générer le plan"
    boutonGenerer.addActionListener(e -> {
      double montant = fenetre.montantOutil;

      // on ne peut pas investir 0$ ou un montant négatif
      if (montant <= 0) {
        JOptionPane.showMessageDialog(this, "Le montant doit être positif.");
        return;
      }

      // on récupère le profil de l'utilisateur connecté
      DonneesUtilisateur utilisateur = GestionAuth.obtenirProfilUtilisateur(fenetre.nomUtilisateurConnecte);
      if (utilisateur == null) {
        JOptionPane.showMessageDialog(this, "Profil introuvable.");
        return;
      }

      // il faut au moins une case cochée sinon le plan n'a aucun sens
      boolean auMoinsUnSelectionne = caseActions.isSelected() || caseEtf.isSelected()
        || caseCrypto.isSelected() || caseObligations.isSelected()
        || caseMatieresPremieres.isSelected() || caseImmobilierCote.isSelected();

      if (!auMoinsUnSelectionne) {
        JOptionPane.showMessageDialog(this, "Veuillez sélectionner au moins un type d'investissement.");
        return;
      }

      // convertit le texte du menu en chiffre : 0 = faible, 1 = moyen, 2 = élevé
      int niveauRisque = obtenirNiveauRisque();

      // génère le texte du plan selon tous les paramètres choisis
      String plan = GenerateurRapportInvestissement.construirePlan(montant, utilisateur, niveauRisque,
        caseActions.isSelected(), caseEtf.isSelected(), caseCrypto.isSelected(),
        caseObligations.isSelected(), caseMatieresPremieres.isSelected(), caseImmobilierCote.isSelected());

      // affiche le plan dans une fenêtre pop-up avec défilement
      afficherDialogueTexte("Votre plan d'investissement personnalisé", plan, 580, 520);
    });
  }


  // crée une JCheckBox avec la boîte de confirmation déjà attachée
  private JCheckBox creerCaseConfirmation(String etiquette) {
    JCheckBox caseCocher = new JCheckBox(etiquette);
    caseCocher.setOpaque(false);
    caseCocher.setFont(Apparence.CORPS);
    attacherEcouteurConfirmation(caseCocher, etiquette);
    return caseCocher;
  }


  // quand on coche une case, affiche une description et demande confirmation
  // si on clique "annuler", la case revient à décochée automatiquement
  private void attacherEcouteurConfirmation(JCheckBox caseCocher, String etiquette) {
    caseCocher.addActionListener(e -> {
      if (!caseCocher.isSelected()) return; // rien à faire si on décoche

      // texte explicatif sur le type d'investissement choisi
      String description = DescriptionsInvestissement.obtenirDescription(etiquette);
      JLabel etiquetteDescription = new JLabel(description);
      etiquetteDescription.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      // showOptionDialog retourne l'index du bouton cliqué (0 = confirmer, autre = annuler)
      // https://stackoverflow.com/questions/4344682/double-clicking-button-on-joptionpane
      int choix = JOptionPane.showOptionDialog(this, etiquetteDescription,
        "Type d'investissement : " + etiquette,
        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
        new String[] {"Confirmer", "Annuler"}, "Confirmer");

      if (choix != 0) caseCocher.setSelected(false); // annulation = on décoche
    });
  }


  // réinitialise toutes les cases et le menu risque quand on revient sur la page
  private void reinitialiserPage() {
    for (JCheckBox caseCocher : new JCheckBox[] {
        caseActions, caseEtf, caseCrypto, caseObligations, caseMatieresPremieres, caseImmobilierCote
      }) {
      if (caseCocher == null) continue;

      // on retire tous les anciens ActionListeners pour éviter d'en avoir plusieurs en double
      // sinon la confirmation apparaît plusieurs fois à chaque clic
      // https://stackoverflow.com/questions/5655215/remove-all-action-listeners
      for (java.awt.event.ActionListener ecouteur : caseCocher.getActionListeners())
        caseCocher.removeActionListener(ecouteur);

      caseCocher.setSelected(false); // remet à décoché
    }

    // on réattache UN seul écouteur par case
    attacherEcouteurConfirmation(caseActions, "Actions");
    attacherEcouteurConfirmation(caseEtf, "ETF");
    attacherEcouteurConfirmation(caseCrypto, "Cryptomonnaies");
    attacherEcouteurConfirmation(caseObligations, "Obligations");
    attacherEcouteurConfirmation(caseMatieresPremieres, "Matières premières");
    attacherEcouteurConfirmation(caseImmobilierCote, "Immobilier coté");

    // remet le risque au premier choix ("faible") par défaut
    if (boiteRisque != null) boiteRisque.setSelectedIndex(0);
  }


  // convertit le texte sélectionné en valeur numérique de risque
  // startsWith est plus fiable que equals si le texte change légèrement
  private int obtenirNiveauRisque() {
    String risque = (String) boiteRisque.getSelectedItem();
    return risque.startsWith("Faible") ? 0 : risque.startsWith("Moyen") ? 1 : 2;
  }


  // affiche le rapport dans une pop-up défilante en lecture seule
  private void afficherDialogueTexte(String titre, String contenu, int largeur, int hauteur) {
    JTextArea zoneTexte = new JTextArea(contenu);
    zoneTexte.setEditable(false);      // lecture seulement, pas d'édition
    zoneTexte.setLineWrap(true);       // retour à la ligne automatique
    zoneTexte.setWrapStyleWord(true);  // coupe aux espaces, pas au milieu d'un mot
    zoneTexte.setFont(new Font("Monospaced", Font.PLAIN, 13)); // police fixe pour l'alignement
    zoneTexte.setMargin(new Insets(10, 10, 10, 10)); // padding intérieur

    JScrollPane defilement = new JScrollPane(zoneTexte);
    // pas de barre horizontale, le texte revient à la ligne de toute façon
    defilement.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    defilement.setPreferredSize(new Dimension(largeur, hauteur));

    // PLAIN_MESSAGE = pas d'icône dans la fenêtre pop-up
    // https://docs.oracle.com/javase/8/docs/api/javax/swing/JOptionPane.html
    JOptionPane.showMessageDialog(this, defilement, titre, JOptionPane.PLAIN_MESSAGE);
  }
}
