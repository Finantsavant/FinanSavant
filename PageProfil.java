import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
// on importe les outils graphiques de java pour faire l'interface
// source : https://docs.oracle.com/javase/8/docs/api/javax/swing/package-summary.html

/** Connexion, inscription et modification du profil utilisateur. */
class PageProfil extends JPanel {
  // champs de texte pour le nom d'utilisateur et le mot de passe
  // JPasswordField cache les caractères automatiquement
  // https://stackoverflow.com/questions/8881732/jpasswordfield-vs-jtextfield
  JTextField champUtilisateur = new JTextField(15);
  JPasswordField champMotDePasse = new JPasswordField(15);

  // boutons pour se connecter ou créer un compte
  JButton boutonConnexion = new JButton("Connexion");
  JButton boutonInscription = new JButton("Créer un compte");

  // label pour afficher les messages d'erreur ou de succès de connexion
  JLabel messageConnexion = new JLabel("", JLabel.CENTER);

  // champs pour les infos du profil (nom, age, etc.)
  JTextField champNom = new JTextField(15);
  JTextField champAge = new JTextField(5);

  // curseur pour choisir le % d'investissement vs épargne (de 0 à 100)
  // https://docs.oracle.com/javase/8/docs/api/javax/swing/JSlider.html
  JSlider curseurRepartition = new JSlider(0, 100, 50);
  JLabel etiquetteRepartition = new JLabel("Répartition : ", JLabel.RIGHT);
  JLabel etiquetteCurseur = new JLabel("Investissement : 50% | Épargne : 50%", JLabel.CENTER);

  // liste déroulante pour le statut de l'utilisateur
  // https://docs.oracle.com/javase/8/docs/api/javax/swing/JComboBox.html
  String[] occupations = {"Étudiant", "Temps partiel", "Temps plein", "Retraité", "Autre"};
  JComboBox<String> listeOccupation = new JComboBox<>(occupations);

  JLabel etiquetteNom = new JLabel("Nom : ", JLabel.RIGHT);
  JLabel etiquetteAge = new JLabel("Âge : ", JLabel.RIGHT);
  JLabel etiquetteOccupation = new JLabel("Occupation : ", JLabel.RIGHT);
  JLabel messageErreur = new JLabel("", JLabel.CENTER);
  JButton boutonSauvegarder = new JButton("Sauvegarder");

  CardLayout dispositionCartes = new CardLayout();
  JPanel panneauCartes = new JPanel(dispositionCartes);
  JPanel carteConnexion = new JPanel(); // page de login
  JPanel carteProfil = new JPanel();   // page du profil

  private String utilisateurCourant = null; // null = personne n'est connecté
  private final FenetrePrincipale fenetre;  // référence à la fenêtre principale


  public PageProfil(FenetrePrincipale fenetre) {
    this.fenetre = fenetre;

    // couleur de fond définie dans la classe Apparence
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(20, 20, 20, 20)); // marge de 20px de chaque côté

    // bouton retour en haut à gauche
    JPanel barreHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
    barreHaut.setOpaque(false); // transparent pour voir le fond
    JButton boutonRetour = new JButton("< Retour");
    boutonRetour.addActionListener(e -> fenetre.retourAccueil());
    barreHaut.add(boutonRetour);
    add(barreHaut, BorderLayout.NORTH);


    // --- carte connexion : champs empilés verticalement ---
    // BoxLayout.Y_AXIS = les composants s'empilent de haut en bas
    // https://stackoverflow.com/questions/22260434/boxlayout-y-axis-explanation
    carteConnexion.setBackground(Apparence.FOND);
    carteConnexion.setLayout(new BoxLayout(carteConnexion, BoxLayout.Y_AXIS));

    // ligne avec le label + champ du nom d'utilisateur
    JPanel ligneUtilisateur = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    ligneUtilisateur.setOpaque(false);
    ligneUtilisateur.add(new JLabel("Nom d'utilisateur :"));
    ligneUtilisateur.add(champUtilisateur);
    carteConnexion.add(ligneUtilisateur);

    // ligne avec le label + champ du mot de passe
    JPanel ligneMotDePasse = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    ligneMotDePasse.setOpaque(false);
    ligneMotDePasse.add(new JLabel("Mot de passe :"));
    ligneMotDePasse.add(champMotDePasse);
    carteConnexion.add(ligneMotDePasse);

    // ligne avec les deux boutons (connexion + inscription)
    JPanel ligneBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    ligneBoutons.setOpaque(false);
    ligneBoutons.add(boutonConnexion);
    ligneBoutons.add(boutonInscription);
    carteConnexion.add(ligneBoutons);

    // ligne pour afficher le message de retour (ex : "connexion réussie")
    JPanel ligneMessage = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ligneMessage.setOpaque(false);
    ligneMessage.add(messageConnexion);
    carteConnexion.add(ligneMessage);


    // --- carte profil : infos de l'utilisateur connecté ---
    carteProfil.setBackground(Apparence.FOND);
    carteProfil.setLayout(new BoxLayout(carteProfil, BoxLayout.Y_AXIS));

    // champ nom
    JPanel ligneNom = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    ligneNom.setOpaque(false);
    ligneNom.add(etiquetteNom);
    ligneNom.add(champNom);
    carteProfil.add(ligneNom);

    // champ âge
    JPanel ligneAge = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    ligneAge.setOpaque(false);
    ligneAge.add(etiquetteAge);
    ligneAge.add(champAge);
    carteProfil.add(ligneAge);

    // curseur pour la répartition investissement / épargne
    JPanel ligneRepartition = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    ligneRepartition.setOpaque(false);
    curseurRepartition.setBackground(Apparence.FOND);
    curseurRepartition.setMajorTickSpacing(25); // graduation principale tous les 25
    curseurRepartition.setMinorTickSpacing(5);  // petite graduation tous les 5
    curseurRepartition.setPaintTicks(true);
    curseurRepartition.setPaintLabels(true);
    curseurRepartition.setPreferredSize(new Dimension(400, 50));
    // met à jour le label quand on bouge le curseur
    curseurRepartition.addChangeListener(e -> mettreAJourEtiquetteCurseur());
    ligneRepartition.add(etiquetteRepartition);
    ligneRepartition.add(curseurRepartition);
    carteProfil.add(ligneRepartition);

    // label qui affiche le % en temps réel
    JPanel ligneCurseurLabel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ligneCurseurLabel.setOpaque(false);
    ligneCurseurLabel.add(etiquetteCurseur);
    carteProfil.add(ligneCurseurLabel);

    // liste déroulante pour l'occupation
    JPanel ligneOccupation = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    ligneOccupation.setOpaque(false);
    ligneOccupation.add(etiquetteOccupation);
    ligneOccupation.add(listeOccupation);
    carteProfil.add(ligneOccupation);

    // bouton sauvegarder
    JPanel ligneSauvegarder = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ligneSauvegarder.setOpaque(false);
    ligneSauvegarder.add(boutonSauvegarder);
    carteProfil.add(ligneSauvegarder);

    // message d'erreur du profil (ex : âge invalide)
    JPanel ligneErreur = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ligneErreur.setOpaque(false);
    ligneErreur.add(messageErreur);
    carteProfil.add(ligneErreur);

    // on ajoute les deux cartes au panneau principal avec leurs noms
    panneauCartes.add(carteConnexion, "CONNEXION");
    panneauCartes.add(carteProfil, "PROFIL");
    add(panneauCartes, BorderLayout.CENTER);


    // action du bouton connexion
    boutonConnexion.addActionListener(e -> {
      String utilisateur = champUtilisateur.getText().trim();
      String motDePasse = new String(champMotDePasse.getPassword());
      // vérifie que les champs ne sont pas vides
      if (utilisateur.isEmpty() || motDePasse.isEmpty()) {
        messageConnexion.setText("Veuillez remplir tous les champs.");
        return;
      }
      // appelle la méthode d'authentification et change de carte si ok
      if (GestionAuth.authentifier(utilisateur, motDePasse)) {
        fenetre.definirConnexion(true, utilisateur);
        messageConnexion.setText("Connexion réussie !");
        utilisateurCourant = utilisateur;
        remplirProfil(); // charge les données sauvegardées
        dispositionCartes.show(panneauCartes, "PROFIL");
        champNom.setText(utilisateur);
      } else {
        messageConnexion.setText("Identifiants incorrects.");
      }
    });


    // action du bouton inscription - pareil que connexion mais crée un nouveau compte
    boutonInscription.addActionListener(e -> {
      String utilisateur = champUtilisateur.getText().trim();
      String motDePasse = new String(champMotDePasse.getPassword());
      if (utilisateur.isEmpty() || motDePasse.isEmpty()) {
        messageConnexion.setText("Veuillez remplir tous les champs.");
        return;
      }
      // enregistrer() retourne false si le nom d'utilisateur est déjà pris
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


    // action du bouton sauvegarder le profil
    boutonSauvegarder.addActionListener(e -> {
      String nom = champNom.getText();
      String ageTexte = champAge.getText();

      if (nom.isEmpty()) {
        messageErreur.setText("Erreur : veuillez entrer un nom.");
        return;
      }

      // on vérifie que l'âge est un nombre entre 1 et 120
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

      // on garde les objectifs de l'ancien profil pour pas les perdre lors de la sauvegarde
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


  // met à jour le texte sous le curseur quand on le bouge
  private void mettreAJourEtiquetteCurseur() {
    int investissement = curseurRepartition.getValue();
    int epargne = 100 - investissement; // l'épargne = ce qui reste après l'investissement
    etiquetteCurseur.setText("Investissement : " + investissement + "% | Épargne : " + epargne + "%");
  }


  // remplit les champs du profil avec les données déjà sauvegardées
  private void remplirProfil() {
    DonneesUtilisateur donnees = GestionAuth.obtenirProfilUtilisateur(utilisateurCourant);
    if (donnees != null) {
      champNom.setText(donnees.nomAffichage);
      champAge.setText(String.valueOf(donnees.age));
      curseurRepartition.setValue(donnees.pourcentInvestissement);
      mettreAJourEtiquetteCurseur();
      listeOccupation.setSelectedItem(donnees.occupation);
    }
    // seul l'admin peut changer le nom d'affichage
    champNom.setEditable(GestionAuth.estAdmin(utilisateurCourant));
  }


  // remet tout à zéro quand l'utilisateur se déconnecte
  public void reinitialiserConnexion() {
    dispositionCartes.show(panneauCartes, "CONNEXION");
    champUtilisateur.setText("");
    champMotDePasse.setText("");
    messageConnexion.setText("");
    champNom.setText("");
    champAge.setText("");
    curseurRepartition.setValue(50); // remet le curseur au milieu
    mettreAJourEtiquetteCurseur();
    listeOccupation.setSelectedIndex(0); // remet à "étudiant" par défaut
    messageErreur.setText("");
    utilisateurCourant = null; // plus personne de connecté
    champNom.setEditable(true);
  }
}
