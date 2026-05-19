import javax.swing.*;
import java.awt.*;

/** Page d'accueil avec accès aux outils (objectifs, investissement, épargne). */
class PageAccueil extends JPanel {
  JLabel etiquetteEntete;
  JLabel messageNonConnecte;
  JButton boutonObjectifs, boutonInvestissement, boutonEpargne;
  JButton boutonProfil;
  JButton boutonDeconnexion;
  JButton boutonAdmin;

  public PageAccueil(FenetrePrincipale fenetre) {
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());

    // --- Rangée du haut : Profil | Titre | Déconnexion ---
    JPanel panneauHaut = new JPanel(new GridLayout(1, 3));
    panneauHaut.setOpaque(false);
    panneauHaut.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

    JPanel panneauGauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    panneauGauche.setOpaque(false);
    boutonProfil = new JButton("Profil");
    boutonProfil.addActionListener(e -> fenetre.retourProfil());
    panneauGauche.add(boutonProfil);

    JPanel panneauCentre = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    panneauCentre.setOpaque(false);
    etiquetteEntete = new JLabel("FinanSavant", SwingConstants.CENTER);
    etiquetteEntete.setFont(Apparence.TITRE);
    etiquetteEntete.setForeground(Apparence.PRINCIPALE);
    panneauCentre.add(etiquetteEntete);

    JPanel panneauDroite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    panneauDroite.setOpaque(false);
    boutonDeconnexion = new JButton("Déconnexion");
    boutonDeconnexion.setVisible(false);
    boutonDeconnexion.addActionListener(e -> fenetre.deconnecter());
    panneauDroite.add(boutonDeconnexion);

    panneauHaut.add(panneauGauche);
    panneauHaut.add(panneauCentre);
    panneauHaut.add(panneauDroite);
    add(panneauHaut, BorderLayout.NORTH);

    // --- Message non connecté ---
    JPanel panneauMessage = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauMessage.setOpaque(false);
    messageNonConnecte = new JLabel(
      "Veuillez vous connecter (bouton Profil) pour utiliser les options.", SwingConstants.CENTER);
    messageNonConnecte.setFont(Apparence.CORPS);
    messageNonConnecte.setForeground(Color.GRAY);
    panneauMessage.add(messageNonConnecte);

    // --- Rangée des boutons principaux ---
    JPanel panneauBoutons = new JPanel(new GridLayout(1, 3, 25, 0));
    panneauBoutons.setOpaque(false);

    boutonObjectifs = new JButton("Mes Objectifs");
    boutonObjectifs.setPreferredSize(new Dimension(200, 300));
    boutonObjectifs.setEnabled(false);
    boutonObjectifs.addActionListener(e -> fenetre.retourObjectif());
    panneauBoutons.add(boutonObjectifs);

    boutonInvestissement = new JButton("Investissement");
    boutonInvestissement.setPreferredSize(new Dimension(200,300));
    boutonInvestissement.setEnabled(false);
    boutonInvestissement.addActionListener(e -> {
      String saisie = JOptionPane.showInputDialog(this, "Somme initiale pour vos investissements ($) :");
      if (saisie == null) return;
      try {
        double montant = Double.parseDouble(saisie);
        if (montant <= 0) {
          JOptionPane.showMessageDialog(this, "Veuillez entrer un montant valide.");
          return;
        }
        fenetre.montantOutil = montant;
        fenetre.retourInvest();
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Veuillez entrer un nombre valide.");
      }
    });
    panneauBoutons.add(boutonInvestissement);

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
        fenetre.retourEpargne();
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Veuillez entrer un nombre valide.");
      }
    });
    panneauBoutons.add(boutonEpargne);

    JPanel panneauCentreWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
    panneauCentreWrapper.setOpaque(false);
    panneauCentreWrapper.add(panneauBoutons);
    add(panneauCentreWrapper, BorderLayout.CENTER);

    // --- Bas de page : message et bouton Admin ---
    JPanel panneauAdmin = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauAdmin.setOpaque(false);
    boutonAdmin = new JButton("Gérer les comptes");
    boutonAdmin.setVisible(false);
    boutonAdmin.addActionListener(e -> fenetre.retourAdmin());
    panneauAdmin.add(boutonAdmin);

    JPanel panneauBas = new JPanel();
    panneauBas.setOpaque(false);
    panneauBas.setLayout(new BoxLayout(panneauBas, BoxLayout.Y_AXIS));
    panneauMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
    panneauAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
    panneauBas.add(panneauMessage);
    panneauBas.add(panneauAdmin);
    add(panneauBas, BorderLayout.SOUTH);
  }

  public void definirUtilisateurConnecte(boolean connecte, String nomUtilisateur) {
    boutonObjectifs.setEnabled(connecte);
    boutonInvestissement.setEnabled(connecte);
    boutonEpargne.setEnabled(connecte);
    if (connecte && GestionAuth.estAdmin(nomUtilisateur)) {
      etiquetteEntete.setText("FinanSavant Admin");
    } else {
      etiquetteEntete.setText("FinanSavant");
    }
    messageNonConnecte.setVisible(!connecte);
    boutonProfil.setText(connecte ? nomUtilisateur : "Profil");
    boutonDeconnexion.setVisible(connecte);
    boutonAdmin.setVisible(connecte && GestionAuth.estAdmin(nomUtilisateur));
  }
}

