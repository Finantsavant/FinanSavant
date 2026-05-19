import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

/** Connexion, inscription et modification du profil utilisateur. */
class PageProfil extends JPanel {
  JTextField champUtilisateur = new JTextField(15);
  JPasswordField champMotDePasse = new JPasswordField(15);
  JButton boutonConnexion = new JButton("Connexion");
  JButton boutonInscription = new JButton("Créer un compte");
  JLabel messageConnexion = new JLabel("", JLabel.CENTER);

  JTextField champNom = new JTextField(15);
  JTextField champAge = new JTextField(5);
  JSlider curseurRepartition = new JSlider(0, 100, 50);
  JLabel etiquetteRepartition = new JLabel("Répartition : ", JLabel.RIGHT);
  JLabel etiquetteCurseur = new JLabel("Investissement : 50% | Épargne : 50%", JLabel.CENTER);
  String[] occupations = {"Étudiant", "Temps partiel", "Temps plein", "Retraité", "Autre"};
  JComboBox<String> listeOccupation = new JComboBox<>(occupations);
  JLabel etiquetteNom = new JLabel("Nom : ", JLabel.RIGHT);
  JLabel etiquetteAge = new JLabel("Âge : ", JLabel.RIGHT);
  JLabel etiquetteOccupation = new JLabel("Occupation : ", JLabel.RIGHT);
  JLabel messageErreur = new JLabel("", JLabel.CENTER);
  JButton boutonSauvegarder = new JButton("Sauvegarder");
  CardLayout dispositionCartes = new CardLayout();
  JPanel panneauCartes = new JPanel(dispositionCartes);
  JPanel carteConnexion = new JPanel();
  JPanel carteProfil = new JPanel();
  private String utilisateurCourant = null;
  private final FenetrePrincipale fenetre;

  public PageProfil(FenetrePrincipale fenetre) {
    this.fenetre = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(20, 20, 20, 20));

    JPanel barreHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
    barreHaut.setOpaque(false);
    JButton boutonRetour = new JButton("< Retour");
    boutonRetour.addActionListener(e -> fenetre.retourAccueil());
    barreHaut.add(boutonRetour);
    add(barreHaut, BorderLayout.NORTH);

    // --- Carte Connexion : rows of label + field stacked vertically ---
    carteConnexion.setBackground(Apparence.FOND);
    carteConnexion.setLayout(new BoxLayout(carteConnexion, BoxLayout.Y_AXIS));

    JPanel ligneUtilisateur = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    ligneUtilisateur.setOpaque(false);
    ligneUtilisateur.add(new JLabel("Nom d'utilisateur :"));
    ligneUtilisateur.add(champUtilisateur);
    carteConnexion.add(ligneUtilisateur);

    JPanel ligneMotDePasse = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    ligneMotDePasse.setOpaque(false);
    ligneMotDePasse.add(new JLabel("Mot de passe :"));
    ligneMotDePasse.add(champMotDePasse);
    carteConnexion.add(ligneMotDePasse);

    JPanel ligneBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    ligneBoutons.setOpaque(false);
    ligneBoutons.add(boutonConnexion);
    ligneBoutons.add(boutonInscription);
    carteConnexion.add(ligneBoutons);

    JPanel ligneMessage = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ligneMessage.setOpaque(false);
    ligneMessage.add(messageConnexion);
    carteConnexion.add(ligneMessage);

    // --- Carte Profil : rows stacked vertically ---
    carteProfil.setBackground(Apparence.FOND);
    carteProfil.setLayout(new BoxLayout(carteProfil, BoxLayout.Y_AXIS));

    JPanel ligneNom = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    ligneNom.setOpaque(false);
    ligneNom.add(etiquetteNom);
    ligneNom.add(champNom);
    carteProfil.add(ligneNom);

    JPanel ligneAge = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    ligneAge.setOpaque(false);
    ligneAge.add(etiquetteAge);
    ligneAge.add(champAge);
    carteProfil.add(ligneAge);

    JPanel ligneRepartition = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    ligneRepartition.setOpaque(false);
    curseurRepartition.setBackground(Apparence.FOND);
    curseurRepartition.setMajorTickSpacing(25);
    curseurRepartition.setMinorTickSpacing(5);
    curseurRepartition.setPaintTicks(true);
    curseurRepartition.setPaintLabels(true);
    curseurRepartition.setPreferredSize(new Dimension(400, 50));
    curseurRepartition.addChangeListener(e -> mettreAJourEtiquetteCurseur());
    ligneRepartition.add(etiquetteRepartition);
    ligneRepartition.add(curseurRepartition);
    carteProfil.add(ligneRepartition);

    JPanel ligneCurseurLabel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ligneCurseurLabel.setOpaque(false);
    ligneCurseurLabel.add(etiquetteCurseur);
    carteProfil.add(ligneCurseurLabel);

    JPanel ligneOccupation = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    ligneOccupation.setOpaque(false);
    ligneOccupation.add(etiquetteOccupation);
    ligneOccupation.add(listeOccupation);
    carteProfil.add(ligneOccupation);

    JPanel ligneSauvegarder = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ligneSauvegarder.setOpaque(false);
    ligneSauvegarder.add(boutonSauvegarder);
    carteProfil.add(ligneSauvegarder);

    JPanel ligneErreur = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ligneErreur.setOpaque(false);
    ligneErreur.add(messageErreur);
    carteProfil.add(ligneErreur);

    panneauCartes.add(carteConnexion, "CONNEXION");
    panneauCartes.add(carteProfil, "PROFIL");
    add(panneauCartes, BorderLayout.CENTER);

    boutonConnexion.addActionListener(e -> {
      String utilisateur = champUtilisateur.getText().trim();
      String motDePasse = new String(champMotDePasse.getPassword());
      if (utilisateur.isEmpty() || motDePasse.isEmpty()) {
        messageConnexion.setText("Veuillez remplir tous les champs.");
        return;
      }
      if (GestionAuth.authentifier(utilisateur, motDePasse)) {
        fenetre.definirConnexion(true, utilisateur);
        messageConnexion.setText("Connexion réussie !");
        utilisateurCourant = utilisateur;
        remplirProfil();
        dispositionCartes.show(panneauCartes, "PROFIL");
        champNom.setText(utilisateur);
      } else {
        messageConnexion.setText("Identifiants incorrects.");
      }
    });

    boutonInscription.addActionListener(e -> {
      String utilisateur = champUtilisateur.getText().trim();
      String motDePasse = new String(champMotDePasse.getPassword());
      if (utilisateur.isEmpty() || motDePasse.isEmpty()) {
        messageConnexion.setText("Veuillez remplir tous les champs.");
        return;
      }
      if (GestionAuth.enregistrer(utilisateur, motDePasse)) {
        fenetre.definirConnexion(true, utilisateur);
        messageConnexion.setText("Compte créé et connecté !");
        utilisateurCourant = utilisateur;
        remplirProfil();
        dispositionCartes.show(panneauCartes, "PROFIL");
        champNom.setText(utilisateur);
      } else {
        messageConnexion.setText("Ce nom d'utilisateur existe déjà.");
      }
    });

    boutonSauvegarder.addActionListener(e -> {
      String nom = champNom.getText();
      String ageTexte = champAge.getText();
      if (nom.isEmpty()) {
        messageErreur.setText("Erreur : veuillez entrer un nom.");
        return;
      }
      try {
        int age = Integer.parseInt(ageTexte);
        if (age < 1 || age > 120) {
          messageErreur.setText("Erreur : âge invalide (1-120).");
          return;
        }
      } catch (NumberFormatException ex) {
        messageErreur.setText("Erreur : l'âge doit être un nombre.");
        return;
      }
      int pourcentInvestissement = curseurRepartition.getValue();
      String occupation = (String) listeOccupation.getSelectedItem();
      DonneesUtilisateur anciennesDonnees = GestionAuth.obtenirProfilUtilisateur(utilisateurCourant);
      DonneesUtilisateur nouvellesDonnees = new DonneesUtilisateur(
        nom, Integer.parseInt(ageTexte), pourcentInvestissement, occupation);
      if (anciennesDonnees != null) nouvellesDonnees.objectifs = anciennesDonnees.objectifs;
      GestionAuth.mettreAJourProfil(utilisateurCourant, nouvellesDonnees);
      messageErreur.setText("Profil sauvegardé : " + nom + ", " + ageTexte + " ans, "
        + pourcentInvestissement + "% investissement / " + (100 - pourcentInvestissement)
        + "% épargne, " + occupation);
    });
  }

  private void mettreAJourEtiquetteCurseur() {
    int investissement = curseurRepartition.getValue();
    int epargne = 100 - investissement;
    etiquetteCurseur.setText("Investissement : " + investissement + "% | Épargne : " + epargne + "%");
  }

  private void remplirProfil() {
    DonneesUtilisateur donnees = GestionAuth.obtenirProfilUtilisateur(utilisateurCourant);
    if (donnees != null) {
      champNom.setText(donnees.nomAffichage);
      champAge.setText(String.valueOf(donnees.age));
      curseurRepartition.setValue(donnees.pourcentInvestissement);
      mettreAJourEtiquetteCurseur();
      listeOccupation.setSelectedItem(donnees.occupation);
    }
    champNom.setEditable(GestionAuth.estAdmin(utilisateurCourant));
  }

  public void reinitialiserConnexion() {
    dispositionCartes.show(panneauCartes, "CONNEXION");
    champUtilisateur.setText("");
    champMotDePasse.setText("");
    messageConnexion.setText("");
    champNom.setText("");
    champAge.setText("");
    curseurRepartition.setValue(50);
    mettreAJourEtiquetteCurseur();
    listeOccupation.setSelectedIndex(0);
    messageErreur.setText("");
    utilisateurCourant = null;
    champNom.setEditable(true);
  }
}

