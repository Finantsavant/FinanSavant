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
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // --- Rangée du haut : Profil | Titre | Déconnexion ---
    JPanel panneauHaut = new JPanel(new BorderLayout());
    panneauHaut.setOpaque(false);
    panneauHaut.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

    boutonProfil = new JButton("Profil");
    boutonProfil.addActionListener(e -> fenetre.retourProfil());
    panneauHaut.add(boutonProfil, BorderLayout.WEST);

    etiquetteEntete = new JLabel("FinanSavant", SwingConstants.CENTER);
    etiquetteEntete.setFont(Apparence.TITRE);
    etiquetteEntete.setForeground(Apparence.PRINCIPALE);
    panneauHaut.add(etiquetteEntete, BorderLayout.CENTER);

    boutonDeconnexion = new JButton("Déconnexion");
    boutonDeconnexion.setVisible(false);
    boutonDeconnexion.addActionListener(e -> fenetre.deconnecter());
    panneauHaut.add(boutonDeconnexion, BorderLayout.EAST);

    add(panneauHaut);

    // --- Message non connecté ---
    JPanel panneauMessage = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauMessage.setOpaque(false);
    messageNonConnecte = new JLabel(
      "Veuillez vous connecter (bouton Profil) pour utiliser les options.", SwingConstants.CENTER);
    messageNonConnecte.setFont(Apparence.CORPS);
    messageNonConnecte.setForeground(Color.GRAY);
    panneauMessage.add(messageNonConnecte);
    add(panneauMessage);

    // --- Rangée des boutons principaux ---
    JPanel panneauBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
    panneauBoutons.setOpaque(false);

    boutonObjectifs = new JButton("Mes Objectifs");
    boutonObjectifs.setPreferredSize(new Dimension(200, 100));
    boutonObjectifs.setEnabled(false);
    boutonObjectifs.addActionListener(e -> fenetre.retourObjectif());
    panneauBoutons.add(boutonObjectifs);

    boutonInvestissement = new JButton("Investissement");
    boutonInvestissement.setPreferredSize(new Dimension(200, 100));
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
    boutonEpargne.setPreferredSize(new Dimension(200, 100));
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

    add(panneauBoutons);

    // --- Bouton Admin ---
    JPanel panneauAdmin = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauAdmin.setOpaque(false);
    boutonAdmin = new JButton("Gérer les comptes");
    boutonAdmin.setVisible(false);
    boutonAdmin.addActionListener(e -> fenetre.retourAdmin());
    panneauAdmin.add(boutonAdmin);
    add(panneauAdmin);
  }

  public void definirUtilisateurConnecte(boolean connecte, String nomUtilisateur) {
    boutonObjectifs.setEnabled(connecte);
    boutonInvestissement.setEnabled(connecte);
    boutonEpargne.setEnabled(connecte);
    if (connecte && GestionAuth.estAdmin(nomUtilisateur)) {
      etiquetteEntete.setText("Administration FinanSavant");
    } else {
      etiquetteEntete.setText("FinanSavant");
    }
    messageNonConnecte.setVisible(!connecte);
    boutonProfil.setText(connecte ? nomUtilisateur : "Profil");
    boutonDeconnexion.setVisible(connecte);
    boutonAdmin.setVisible(connecte && GestionAuth.estAdmin(nomUtilisateur));
  }
}

