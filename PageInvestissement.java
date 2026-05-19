import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

/** Interface de génération du plan d'investissement personnalisé. */
class PageInvestissement extends JPanel {
  JComboBox<String> boiteRisque;
  JCheckBox caseActions;
  JCheckBox caseFnb;
  JCheckBox caseCrypto;
  JCheckBox caseObligations;
  JCheckBox caseMatieresPremieres;
  JCheckBox caseImmobilierCote;
  private final FenetrePrincipale fenetre;

  public PageInvestissement(FenetrePrincipale fenetre) {
    this.fenetre = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(20, 20, 20, 20));

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

    JPanel conteneurPrincipal = new JPanel(new GridBagLayout());
    conteneurPrincipal.setOpaque(false);
    GridBagConstraints contraintes = new GridBagConstraints();
    contraintes.insets = new Insets(10, 10, 10, 10);
    contraintes.fill = GridBagConstraints.HORIZONTAL;

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
    contraintes.gridx = 0;
    contraintes.gridy = 0;
    contraintes.gridwidth = 2;
    conteneurPrincipal.add(panneauRisque, contraintes);

    contraintes.gridy++;
    contraintes.gridwidth = 1;
    contraintes.weightx = 0.6;
    JPanel panneauTypes = new JPanel(new GridLayout(2, 3, 10, 10));
    panneauTypes.setOpaque(false);
    panneauTypes.setBorder(BorderFactory.createTitledBorder("Types d'investissements"));
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
    conteneurPrincipal.add(panneauTypes, contraintes);

    contraintes.gridy++;
    contraintes.gridwidth = 2;
    contraintes.weightx = 0;
    JPanel panneauBouton = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    panneauBouton.setOpaque(false);
    JButton boutonGenerer = new JButton("Générer le plan");
    boutonGenerer.setFont(Apparence.SOUS_TITRE);
    boutonGenerer.setBackground(Apparence.SECONDAIRE);
    boutonGenerer.setForeground(Color.WHITE);
    boutonGenerer.setPreferredSize(new Dimension(240, 50));
    panneauBouton.add(boutonGenerer);
    conteneurPrincipal.add(panneauBouton, contraintes);
    add(new JScrollPane(conteneurPrincipal), BorderLayout.CENTER);

    addAncestorListener(new javax.swing.event.AncestorListener() {
      public void ancestorAdded(javax.swing.event.AncestorEvent e) { reinitialiserPage(); }
      public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
      public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
    });

    boutonGenerer.addActionListener(e -> {
      double montant = fenetre.montantOutil;
      if (montant <= 0) {
        JOptionPane.showMessageDialog(this, "Le montant doit être positif.");
        return;
      }
      DonneesUtilisateur utilisateur = GestionAuth.obtenirProfilUtilisateur(fenetre.nomUtilisateurConnecte);
      if (utilisateur == null) {
        JOptionPane.showMessageDialog(this, "Profil introuvable.");
        return;
      }
      boolean auMoinsUnSelectionne = caseActions.isSelected() || caseFnb.isSelected()
        || caseCrypto.isSelected() || caseObligations.isSelected()
        || caseMatieresPremieres.isSelected() || caseImmobilierCote.isSelected();
      if (!auMoinsUnSelectionne) {
        JOptionPane.showMessageDialog(this, "Veuillez sélectionner au moins un type d'investissement.");
        return;
      }
      int niveauRisque = obtenirNiveauRisque();
      String plan = GenerateurRapportInvestissement.construirePlan(montant, utilisateur, niveauRisque,
        caseActions.isSelected(), caseFnb.isSelected(), caseCrypto.isSelected(),
        caseObligations.isSelected(), caseMatieresPremieres.isSelected(), caseImmobilierCote.isSelected());
      afficherDialogueTexte("Votre plan d'investissement personnalisé", plan, 580, 520);
    });
  }

  private JCheckBox creerCaseConfirmation(String etiquette) {
    JCheckBox caseCocher = new JCheckBox(etiquette);
    caseCocher.setOpaque(false);
    attacherEcouteurConfirmation(caseCocher, etiquette);
    return caseCocher;
  }

  private void attacherEcouteurConfirmation(JCheckBox caseCocher, String etiquette) {
    caseCocher.addActionListener(e -> {
      if (!caseCocher.isSelected()) return;
      String description = DescriptionsInvestissement.obtenirDescription(etiquette);
      JLabel etiquetteDescription = new JLabel(description);
      etiquetteDescription.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      int choix = JOptionPane.showOptionDialog(this, etiquetteDescription,
        "Type d'investissement : " + etiquette,
        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
        new String[] {"Confirmer", "Annuler"}, "Confirmer");
      if (choix != 0) caseCocher.setSelected(false);
    });
  }

  private void reinitialiserPage() {
    for (JCheckBox caseCocher : new JCheckBox[] {
        caseActions, caseFnb, caseCrypto, caseObligations, caseMatieresPremieres, caseImmobilierCote
      }) {
      if (caseCocher == null) continue;
      for (java.awt.event.ActionListener ecouteur : caseCocher.getActionListeners())
        caseCocher.removeActionListener(ecouteur);
      caseCocher.setSelected(false);
    }
    attacherEcouteurConfirmation(caseActions, "Actions");
    attacherEcouteurConfirmation(caseFnb, "FNB");
    attacherEcouteurConfirmation(caseCrypto, "Cryptomonnaies");
    attacherEcouteurConfirmation(caseObligations, "Obligations");
    attacherEcouteurConfirmation(caseMatieresPremieres, "Matières premières");
    attacherEcouteurConfirmation(caseImmobilierCote, "Immobilier coté");
    if (boiteRisque != null) boiteRisque.setSelectedIndex(0);
  }

  private int obtenirNiveauRisque() {
    String risque = (String) boiteRisque.getSelectedItem();
    return risque.startsWith("Faible") ? 0 : risque.startsWith("Moyen") ? 1 : 2;
  }

  private void afficherDialogueTexte(String titre, String contenu, int largeur, int hauteur) {
    JTextArea zoneTexte = new JTextArea(contenu);
    zoneTexte.setEditable(false);
    zoneTexte.setLineWrap(true);
    zoneTexte.setWrapStyleWord(true);
    zoneTexte.setFont(new Font("Monospaced", Font.PLAIN, 13));
    zoneTexte.setMargin(new Insets(10, 10, 10, 10));
    JScrollPane defilement = new JScrollPane(zoneTexte);
    defilement.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    defilement.setPreferredSize(new Dimension(largeur, hauteur));
    JOptionPane.showMessageDialog(this, defilement, titre, JOptionPane.PLAIN_MESSAGE);
  }
}
