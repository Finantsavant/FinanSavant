import javax.swing.*;
import java.awt.*;

/** Administration des comptes utilisateurs (réservée aux administrateurs). */
class PageAdmin extends JPanel {
  private DefaultListModel<String> modeleListeUtilisateurs;
  private JList<String> listeUtilisateurs;
  private final FenetrePrincipale fenetre;
  private JButton boutonReinitBarbieri;

  public PageAdmin(FenetrePrincipale fenetre) {
    this.fenetre = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());
    JPanel barreHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
    barreHaut.setBackground(Apparence.FOND);
    JButton boutonRetour = new JButton("<");
    boutonRetour.setFont(new Font("Arial", Font.BOLD, 25));
    boutonRetour.setPreferredSize(new Dimension(60, 40));
    boutonRetour.addActionListener(e -> fenetre.retourAccueil());
    barreHaut.add(boutonRetour);
    add(barreHaut, BorderLayout.NORTH);

    modeleListeUtilisateurs = new DefaultListModel<>();
    listeUtilisateurs = new JList<>(modeleListeUtilisateurs);
    add(new JScrollPane(listeUtilisateurs), BorderLayout.CENTER);

    JPanel panneauBoutons = new JPanel(new GridLayout(1, 0, 10, 10));
    JButton boutonSupprimer = new JButton("Supprimer le compte sélectionné");
    boutonSupprimer.addActionListener(e -> {
      String selection = listeUtilisateurs.getSelectedValue();
      if (selection != null) {
        String nomUtilisateur = selection.split(" \\(")[0];
        if (GestionAuth.estAdmin(nomUtilisateur)) {
          JOptionPane.showMessageDialog(this, "Impossible de supprimer un administrateur.",
            "Erreur", JOptionPane.ERROR_MESSAGE);
          return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
          "Voulez-vous vraiment supprimer le compte " + nomUtilisateur + " ?",
          "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
          try {
            GestionAuth.supprimerCompte(nomUtilisateur);
            if (nomUtilisateur.equals(fenetre.nomUtilisateurConnecte)) fenetre.deconnecter();
            actualiserListeUtilisateurs();
          } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
          }
        }
      } else {
        JOptionPane.showMessageDialog(this, "Veuillez sélectionner un compte à supprimer.");
      }
    });

    JButton boutonEffacerTous = new JButton("Réinitialiser tous les comptes");
    boutonEffacerTous.setBackground(Apparence.DANGER);
    boutonEffacerTous.setForeground(Color.WHITE);
    boutonEffacerTous.addActionListener(e -> {
      int confirmation = JOptionPane.showConfirmDialog(this,
        "Êtes-vous absolument certain de vouloir supprimer TOUS les comptes non-administrateurs ?\n"
          + "Cette action est irréversible.",
        "Réinitialisation totale", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (confirmation == JOptionPane.YES_OPTION) {
        GestionAuth.effacerDonneesUtilisateurs();
        if (fenetre.nomUtilisateurConnecte != null
          && !GestionAuth.obtenirTousLesNomsUtilisateurs().contains(fenetre.nomUtilisateurConnecte)) {
          fenetre.deconnecter();
        }
        actualiserListeUtilisateurs();
        JOptionPane.showMessageDialog(this, "Tous les comptes non-administrateurs ont été supprimés.");
      }
    });

    boutonReinitBarbieri = new JButton("Réinitialisation Totale (Barbieri)");
    boutonReinitBarbieri.setBackground(Color.BLACK);
    boutonReinitBarbieri.setForeground(Color.WHITE);
    boutonReinitBarbieri.setVisible(false);
    boutonReinitBarbieri.addActionListener(e -> {
      int confirmation = JOptionPane.showConfirmDialog(this,
        "ACTION CRITIQUE : Voulez-vous supprimer TOUS les comptes et réinitialiser les profils administrateurs ?\n"
          + "Cette action est irréversible.",
        "Réinitialisation totale", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
      if (confirmation == JOptionPane.YES_OPTION) {
        GestionAuth.reinitialisationComplete();
        fenetre.deconnecter();
        JOptionPane.showMessageDialog(null, "Le système a été réinitialisé à son état d'usine.");
      }
    });

    panneauBoutons.add(boutonSupprimer);
    panneauBoutons.add(boutonEffacerTous);
    panneauBoutons.add(boutonReinitBarbieri);
    add(panneauBoutons, BorderLayout.SOUTH);
    actualiserListeUtilisateurs();
  }

  public void actualiserListeUtilisateurs() {
    if (boutonReinitBarbieri != null && fenetre.nomUtilisateurConnecte != null) {
      boutonReinitBarbieri.setVisible(fenetre.nomUtilisateurConnecte.equals("barbieri"));
    }
    modeleListeUtilisateurs.clear();
    for (String nom : GestionAuth.obtenirNomsNonAdmin()) {
      DonneesUtilisateur donnees = GestionAuth.obtenirProfilUtilisateur(nom);
      String entree = nom;
      if (donnees != null) {
        entree += " (" + donnees.nomAffichage + ", " + donnees.age + " ans, "
          + donnees.pourcentInvestissement + "% investissement / "
          + (100 - donnees.pourcentInvestissement) + "% épargne, " + donnees.occupation + ")";
      }
      modeleListeUtilisateurs.addElement(entree);
    }
  }
}
