
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

/** interface de génération du plan d'investissement personnalisé. */
class PageInvestissement extends JPanel {
  // choix du niveau de risque
  JComboBox<String> boiteRisque;
  // cases à cocher pour les classes d'actifs
  JCheckBox caseActions;
  JCheckBox caseFnb;
  JCheckBox caseCrypto;
  JCheckBox caseObligations;
  JCheckBox caseMatieresPremieres;
  JCheckBox caseImmobilierCote;
  // référence vers la fenêtre principale
  private final FenetrePrincipale fenetre;

  // construit la page avec tous les éléments de l'interface
  public PageInvestissement(FenetrePrincipale fenetre) {
    this.fenetre = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(20, 20, 20, 20));

    // zone du haut avec retour et titre
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

    // conteneur principal en colonne
    JPanel conteneurPrincipal = new JPanel();
    conteneurPrincipal.setOpaque(false);
    conteneurPrincipal.setLayout(new BoxLayout(conteneurPrincipal, BoxLayout.Y_AXIS));

    // sélection de la tolérance au risque
    JPanel panneauRisque = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauRisque.setOpaque(false);
    panneauRisque.add(new JLabel("Tolérance au risque :"));
    String[] risques = {
      "Faible - Stable et sécuritaire",
      "Moyen - Équilibre croissance/sécurité",
      "Élevé - Croissance agressive"
    };
    boiteRisque = new JComboBox<>(risques);
    panneauRisque.add(boiteRisque);
    conteneurPrincipal.add(panneauRisque);

    // section des types d'investissements
    JPanel panneauTypes = new JPanel(new GridLayout(2, 3, 10, 10));
    panneauTypes.setOpaque(false);
    panneauTypes.setBorder(BorderFactory.createTitledBorder("Types d'investissements"));

    // chaque case demande une confirmation avant d'être gardée
    caseActions = creerCaseConfirmation("Actions");
    caseFnb = creerCaseConfirmation("FNB");
    caseCrypto = creerCaseConfirmation("Cryptomonnaies");
    caseObligations = creerCaseConfirmation("Obligations");
    caseMatieresPremieres = creerCaseConfirmation("Matières premières");
    caseImmobilierCote = creerCaseConfirmation("Immobilier coté");

    panneauTypes.add(caseActions);
    panneauTypes.add(caseFnb);
    panneauTypes.add(caseCrypto);
    panneauTypes.add(caseObligations);
    panneauTypes.add(caseMatieresPremieres);
    panneauTypes.add(caseImmobilierCote);

    JPanel panneauTypesWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauTypesWrapper.setOpaque(false);
    panneauTypesWrapper.add(panneauTypes);
    conteneurPrincipal.add(panneauTypesWrapper);

    // bouton pour lancer la génération du plan
    JPanel panneauBouton = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    panneauBouton.setOpaque(false);
    JButton boutonGenerer = new JButton("Générer le plan");
    boutonGenerer.setFont(Apparence.SOUS_TITRE);
    boutonGenerer.setBackground(Apparence.SECONDAIRE);
    boutonGenerer.setForeground(Color.WHITE);
    boutonGenerer.setPreferredSize(new Dimension(240, 50));
    panneauBouton.add(boutonGenerer);
    conteneurPrincipal.add(panneauBouton);

    add(new JScrollPane(conteneurPrincipal), BorderLayout.CENTER);

    // remet la page à zéro quand elle est affichée
    addAncestorListener(new javax.swing.event.AncestorListener() {
      public void ancestorAdded(javax.swing.event.AncestorEvent e) { reinitialiserPage(); }
      public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
      public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
    });

    // bouton principal qui valide les entrées puis construit le plan
    boutonGenerer.addActionListener(e -> {
      double montant = fenetre.montantOutil;

      // un montant nul ou négatif ne peut pas marcher
      if (montant <= 0) {
        JOptionPane.showMessageDialog(this, "Le montant doit être positif.");
        return;
      }

      // on récupère le profil du joueur/utilisateur connecté
      DonneesUtilisateur utilisateur = GestionAuth.obtenirProfilUtilisateur(fenetre.nomUtilisateurConnecte);
      if (utilisateur == null) {
        JOptionPane.showMessageDialog(this, "Profil introuvable.");
        return;
      }

      // au moins une classe d'actif doit être choisie
      boolean auMoinsUnSelectionne = caseActions.isSelected() || caseFnb.isSelected()
        || caseCrypto.isSelected() || caseObligations.isSelected()
        || caseMatieresPremieres.isSelected() || caseImmobilierCote.isSelected();

      if (!auMoinsUnSelectionne) {
        JOptionPane.showMessageDialog(this, "Veuillez sélectionner au moins un type d'investissement.");
        return;
      }

      // on traduit le choix affiché en niveau numérique
      int niveauRisque = obtenirNiveauRisque();

      // on crée le texte du rapport à partir des choix
      String plan = GenerateurRapportInvestissement.construirePlan(montant, utilisateur, niveauRisque,
        caseActions.isSelected(), caseFnb.isSelected(), caseCrypto.isSelected(),
        caseObligations.isSelected(), caseMatieresPremieres.isSelected(), caseImmobilierCote.isSelected());

      // le plan est montré dans une fenêtre texte
      afficherDialogueTexte("Votre plan d'investissement personnalisé", plan, 580, 520);
    });
  }

  // crée une case à cocher avec la logique de confirmation intégrée
  private JCheckBox creerCaseConfirmation(String etiquette) {
    JCheckBox caseCocher = new JCheckBox(etiquette);
    caseCocher.setOpaque(false);
    attacherEcouteurConfirmation(caseCocher, etiquette);
    return caseCocher;
  }

  // affiche une petite description avant de valider le choix
  private void attacherEcouteurConfirmation(JCheckBox caseCocher, String etiquette) {
    caseCocher.addActionListener(e -> {
      if (!caseCocher.isSelected()) return;

      // on récupère une explication simple du type choisi
      String description = DescriptionsInvestissement.obtenirDescription(etiquette);
      JLabel etiquetteDescription = new JLabel(description);
      etiquetteDescription.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      // confirmation pour éviter de cliquer par erreur
      int choix = JOptionPane.showOptionDialog(this, etiquetteDescription,
        "Type d'investissement : " + etiquette,
        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
        new String[] {"Confirmer", "Annuler"}, "Confirmer");

      // si on annule, la case revient décochée
      if (choix != 0) caseCocher.setSelected(false);
    });
  }

  // réinitialise les choix quand on revient sur la page
  private void reinitialiserPage() {
    for (JCheckBox caseCocher : new JCheckBox[] {
        caseActions, caseFnb, caseCrypto, caseObligations, caseMatieresPremieres, caseImmobilierCote
      }) {
      if (caseCocher == null) continue;

      // on enlève les anciens écouteurs pour éviter les doublons
      for (java.awt.event.ActionListener ecouteur : caseCocher.getActionListeners())
        caseCocher.removeActionListener(ecouteur);

      // on repart de zéro
      caseCocher.setSelected(false);
    }

    // on remet les confirmations sur chaque case
    attacherEcouteurConfirmation(caseActions, "Actions");
    attacherEcouteurConfirmation(caseFnb, "FNB");
    attacherEcouteurConfirmation(caseCrypto, "Cryptomonnaies");
    attacherEcouteurConfirmation(caseObligations, "Obligations");
    attacherEcouteurConfirmation(caseMatieresPremieres, "Matières premières");
    attacherEcouteurConfirmation(caseImmobilierCote, "Immobilier coté");

    // le risque recommence au premier choix par défaut
    if (boiteRisque != null) boiteRisque.setSelectedIndex(0);
  }

  // transforme le texte du menu en valeur de risque
  private int obtenirNiveauRisque() {
    String risque = (String) boiteRisque.getSelectedItem();
    return risque.startsWith("Faible") ? 0 : risque.startsWith("Moyen") ? 1 : 2;
  }

  // affiche le rapport dans une boîte de dialogue défilante

  // affiche le rapport dans une boîte de dialogue défilante
  private void afficherDialogueTexte(String titre, String contenu, int largeur, int hauteur) {
    // zone de texte pour afficher le rapport
    JTextArea zoneTexte = new JTextArea(contenu);
    // permet seulement la lecture du texte
    zoneTexte.setEditable(false);
    // fait revenir le texte à la ligne automatiquement
    zoneTexte.setLineWrap(true);
    // coupe les mots de façon plus propre quand ça dépasse
    zoneTexte.setWrapStyleWord(true);
    // style simple pour que le texte reste lisible
    zoneTexte.setFont(new Font("Monospaced", Font.PLAIN, 13));
    // ajoute un peu d'espace autour du texte
    zoneTexte.setMargin(new Insets(10, 10, 10, 10));
    // met la zone de texte dans un panneau défilable
    JScrollPane defilement = new JScrollPane(zoneTexte);
    // enlève la barre horizontale parce que le texte revient déjà à la ligne
    defilement.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    // définit la taille de la fenêtre de texte
    defilement.setPreferredSize(new Dimension(largeur, hauteur));
    // affiche le rapport dans une fenêtre pop-up
    JOptionPane.showMessageDialog(this, defilement, titre, JOptionPane.PLAIN_MESSAGE);
  }
}  // <-- This closes the class - no extra parenthesis needed
