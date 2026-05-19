import javax.swing.*;
import java.awt.*; 

// Page d'accueil avec accès aux outils (objectifs, investissement, épargne).
class PageAccueil extends JPanel { 
  JLabel etiquetteEntete; 
  JLabel messageNonConnecte; // Message affiché quand l'utilisateur n'est pas connecté
  JButton boutonObjectifs, boutonInvestissement, boutonEpargne; 
  JButton boutonProfil; 
  JButton boutonDeconnexion; 
  JButton boutonAdmin; // Seulement apparait pour les comptes admins

  public PageAccueil(FenetrePrincipale fenetre) {
    setBackground(Apparence.FOND); 
    setLayout(new BorderLayout()); 

    JPanel panneauHaut = new JPanel(new GridLayout(1, 3));

    panneauHaut.setOpaque(false); // false = le fond du panneau est transparent
    // Source: https://stackoverflow.com/questions/2452775/how-to-make-a-jpanel-transparent
    panneauHaut.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
    // createEmptyBorder = ajoute un espace vide autour (haut, gauche, bas, droite)
    // Source: https://stackoverflow.com/questions/1783793/java-difference-between-border-and-padding-in-swing

    JPanel panneauGauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    panneauGauche.setOpaque(false);
    boutonProfil = new JButton("Profil");
    boutonProfil.addActionListener(e -> fenetre.retourProfil()); // Mène au page profil
    panneauGauche.add(boutonProfil);

    // Titre de l'application
    JPanel panneauCentre = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    panneauCentre.setOpaque(false);
    etiquetteEntete = new JLabel("FinanSavant", SwingConstants.CENTER);
    // SwingConstants.CENTER = aligne le texte au centre du JLabel.
    // Source: https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/SwingConstants.html 
    etiquetteEntete.setFont(Apparence.TITRE); 
    etiquetteEntete.setForeground(Apparence.PRINCIPALE); 
    panneauCentre.add(etiquetteEntete);

    // Bouton Déconnexion aligné à droite.
    JPanel panneauDroite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    panneauDroite.setOpaque(false);
    boutonDeconnexion = new JButton("Déconnexion");
    boutonDeconnexion.setVisible(false); // Caché par défaut, affiché seulement après connexion à une compte.
    boutonDeconnexion.addActionListener(e -> fenetre.deconnecter());
    panneauDroite.add(boutonDeconnexion);

    // Aout des 3 colonnes dans le panneau du haut
    panneauHaut.add(panneauGauche);
    panneauHaut.add(panneauCentre);
    panneauHaut.add(panneauDroite);
    add(panneauHaut, BorderLayout.NORTH); 

    // Message affiché si l'utilisateur n'est pas connecté
    JPanel panneauMessage = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauMessage.setOpaque(false);
    messageNonConnecte = new JLabel(
      "Veuillez vous connecter (bouton Profil) pour utiliser les options.", SwingConstants.CENTER);
    messageNonConnecte.setFont(Apparence.CORPS);
    messageNonConnecte.setForeground(Color.GRAY);
    panneauMessage.add(messageNonConnecte);

    // Les 3 gros boutons du menu principal 
    JPanel panneauBoutons = new JPanel(new GridLayout(1, 3, 25, 0));
    panneauBoutons.setOpaque(false);

    // Bouton 1: Objectifs
    boutonObjectifs = new JButton("Mes Objectifs");
    boutonObjectifs.setPreferredSize(new Dimension(200, 300));
    // setPreferredSize suggère une taille, mais le layout peut l'ignorer selon les paramètres
    // Source: https://stackoverflow.com/questions/7229226/setpreferredsize-not-working
    boutonObjectifs.setEnabled(false); // Désactivé tant que l'utilisateur n'est pas connecté
    boutonObjectifs.addActionListener(e -> fenetre.retourObjectif()); // Mène au page objectif
    panneauBoutons.add(boutonObjectifs);

    // Bouton 2 : Investissement
    boutonInvestissement = new JButton("Investissement");
    boutonInvestissement.setPreferredSize(new Dimension(200, 300));
    boutonInvestissement.setEnabled(false);
    boutonInvestissement.addActionListener(e -> {
      // showInputDialog ouvre une petite fenêtre popup pour demander une valeur à l'utilisateur qui serait utiliser pour la calcule et qui serait lié au jSlider du PageProfil
      // Source: https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
      String saisie = JOptionPane.showInputDialog(this, "Somme initiale pour vos investissements ($) :");

      if (saisie == null) return; // Si l'utilisateur clique sur Annulé, la page retourne

      try {
        double montant = Double.parseDouble(saisie);
        // Gestion d'erreur
        if (montant <= 0) {
          JOptionPane.showMessageDialog(this, "Veuillez entrer un montant valide.");
          return; 
        }

        fenetre.montantOutil = montant; // Sauvegarder le montant dans la fenêtre principale
        fenetre.retourInvest(); // Mène au page investissement
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Veuillez entrer un nombre valide.");
      }
    });
    panneauBoutons.add(boutonInvestissement);

    // Bouton 3: Épargne 
    // Même interface initiale que la page investissement
    boutonEpargne = new JButton("Épargne");
    boutonEpargne.setPreferredSize(new Dimension(200, 300));
    boutonEpargne.setEnabled(false);
    boutonEpargne.addActionListener(e -> {
      String saisie = JOptionPane.showInputDialog(this, "Montant à répartir pour vos projets ($) :");

      if (saisie == null) return;

      try {
        double montant = Double.parseDouble(saisie);

        if (montant <= 0) {
          JOptionPane.showMessageDialog(this, "Veuillez entrer un montant valide.");
          return;
        }

        fenetre.montantOutil = montant;
        fenetre.retourEpargne(); // Mène au page epargne
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Veuillez entrer un nombre valide.");
      }
    });
    panneauBoutons.add(boutonEpargne);

    JPanel panneauCentreWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
    panneauCentreWrapper.setOpaque(false);
    panneauCentreWrapper.add(panneauBoutons);
    add(panneauCentreWrapper, BorderLayout.CENTER); 

    // Bas de page: message d'info et bouton admin
    JPanel panneauAdmin = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauAdmin.setOpaque(false);
    boutonAdmin = new JButton("Gérer les comptes");
    boutonAdmin.setVisible(false); // Invisible pour tout les utilisateurs normales
    boutonAdmin.addActionListener(e -> fenetre.retourAdmin()); // Mène au page admin
    panneauAdmin.add(boutonAdmin);

    JPanel panneauBas = new JPanel();
    panneauBas.setOpaque(false);
    panneauBas.setLayout(new BoxLayout(panneauBas, BoxLayout.Y_AXIS));
    // BoxLayout avec Y_AXIS empile les éléments de haut en ba
    // Source: https://docs.oracle.com/javase/tutorial/uiswing/layout/box.html

    // CENTER_ALIGNMENT centre les éléments horizontalement dans un BoxLayout vertical
    // Source: https://stackoverflow.com/questions/22735311/boxlayout-center-alignment
    panneauMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
    panneauAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);

    panneauBas.add(panneauMessage);
    panneauBas.add(panneauAdmin);
    add(panneauBas, BorderLayout.SOUTH);
  }

  // Méthode appelée après connexion/déconnexion pour mettre à jour l'interface
  public void definirUtilisateurConnecte(boolean connecte, String nomUtilisateur) {
    // Active ou désactive les 3 boutons selon si l'utilisateur est connecté
    boutonObjectifs.setEnabled(connecte);
    boutonInvestissement.setEnabled(connecte);
    boutonEpargne.setEnabled(connecte);

    // Si c'est un admin connecté, on change le titre pour l'indiquer
    if (connecte && GestionAuth.estAdmin(nomUtilisateur)) {
      etiquetteEntete.setText("FinanSavant Admin");
    } else {
      etiquetteEntete.setText("FinanSavant");
    }

    messageNonConnecte.setVisible(!connecte); // Cacher le message si l'utilisateur est connecté
    boutonProfil.setText(connecte ? nomUtilisateur : "Profil"); // Affiche le nom si connecté, sinon "Profil"
    boutonDeconnexion.setVisible(connecte); // Le bouton déconnexion apparait seulement si connecté
    boutonAdmin.setVisible(connecte && GestionAuth.estAdmin(nomUtilisateur)); // Visible seulement si admin
  }
}
