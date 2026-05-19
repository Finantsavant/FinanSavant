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
    setLayout(new GridBagLayout());
    GridBagConstraints contraintes = new GridBagConstraints();
    contraintes.insets = new Insets(15, 15, 15, 15);

    boutonProfil = new JButton("Profil");
    boutonProfil.addActionListener(e -> fenetre.retourProfil());
    contraintes.gridx = 0;
    contraintes.gridy = 0;
    contraintes.weightx = 0;
    contraintes.weighty = 0;
    contraintes.anchor = GridBagConstraints.NORTHWEST;
    add(boutonProfil, contraintes);

    etiquetteEntete = new JLabel("FinanSavant", SwingConstants.CENTER);
    etiquetteEntete.setFont(Apparence.TITRE);
    etiquetteEntete.setForeground(Apparence.PRINCIPALE);
    contraintes.gridx = 1;
    contraintes.gridy = 0;
    contraintes.weightx = 1.0;
    contraintes.anchor = GridBagConstraints.CENTER;
    add(etiquetteEntete, contraintes);

    boutonDeconnexion = new JButton("Déconnexion");
    boutonDeconnexion.setVisible(false);
    boutonDeconnexion.addActionListener(e -> fenetre.deconnecter());
    contraintes.gridx = 2;
    contraintes.gridy = 0;
    contraintes.weightx = 0;
    contraintes.anchor = GridBagConstraints.NORTHEAST;
    add(boutonDeconnexion, contraintes);

    messageNonConnecte = new JLabel(
      "Veuillez vous connecter (bouton Profil) pour utiliser les options.", SwingConstants.CENTER);
    messageNonConnecte.setFont(Apparence.CORPS);
    messageNonConnecte.setForeground(Color.GRAY);
    contraintes.gridx = 0;
    contraintes.gridy = 1;
    contraintes.gridwidth = 3;
    contraintes.weighty = 0.1;
    contraintes.fill = GridBagConstraints.HORIZONTAL;
    add(messageNonConnecte, contraintes);

    contraintes.gridwidth = 1;
    contraintes.weighty = 0.5;
    contraintes.gridy = 2;
    contraintes.fill = GridBagConstraints.BOTH;

    boutonObjectifs = new JButton("Mes Objectifs");
    boutonObjectifs.setPreferredSize(new Dimension(200, 100));
    boutonObjectifs.setEnabled(false);
    boutonObjectifs.addActionListener(e -> fenetre.retourObjectif());
    contraintes.gridx = 0;
    add(boutonObjectifs, contraintes);

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
    contraintes.gridx = 1;
    add(boutonInvestissement, contraintes);

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
    contraintes.gridx = 2;
    add(boutonEpargne, contraintes);

    boutonAdmin = new JButton("Gérer les comptes");
    boutonAdmin.setVisible(false);
    boutonAdmin.addActionListener(e -> fenetre.retourAdmin());
    contraintes.gridx = 1;
    contraintes.gridy = 3;
    contraintes.weighty = 0;
    add(boutonAdmin, contraintes);
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
