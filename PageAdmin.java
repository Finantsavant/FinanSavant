import javax.swing.*;
import java.awt.*;

/** administration des comptes utilisateurs (réservée aux administrateurs). */
class PageAdmin extends JPanel {

  // DefaultListModel = liste dynamique qu'on peut modifier sans recréer toute la JList
  // https://docs.oracle.com/javase/8/docs/api/javax/swing/DefaultListModel.html
  private DefaultListModel<String> modeleListeUtilisateurs;

  // JList affiche le contenu du modèle et permet de cliquer pour sélectionner
  private JList<String> listeUtilisateurs;

  // référence vers la fenêtre principale pour naviguer ou déconnecter
  private final FenetrePrincipale fenetre;

  // bouton noir secret, visible seulement si "barbieri" est connecté
  private JButton boutonReinitBarbieri;


  public PageAdmin(FenetrePrincipale fenetre) {
    this.fenetre = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());

    // bouton retour "<" en haut à gauche, gros pour être facile à cliquer
    JPanel barreHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
    barreHaut.setBackground(Apparence.FOND);
    JButton boutonRetour = new JButton("<");
    boutonRetour.setFont(new Font("Arial", Font.BOLD, 25));
    boutonRetour.setPreferredSize(new Dimension(60, 40));
    boutonRetour.addActionListener(e -> fenetre.retourAccueil());
    barreHaut.add(boutonRetour);
    add(barreHaut, BorderLayout.NORTH);

    // liste des utilisateurs au centre, avec défilement si trop longue
    modeleListeUtilisateurs = new DefaultListModel<>();
    listeUtilisateurs = new JList<>(modeleListeUtilisateurs);
    add(new JScrollPane(listeUtilisateurs), BorderLayout.CENTER);

    // GridLayout(1, 0) = une rangée, autant de colonnes que de boutons ajoutés
    // https://docs.oracle.com/javase/8/docs/api/java/awt/GridLayout.html
    JPanel panneauBoutons = new JPanel(new GridLayout(1, 0, 10, 10));


    // bouton supprimer un seul compte
    JButton boutonSupprimer = new JButton("Supprimer le compte sélectionné");
    boutonSupprimer.addActionListener(e -> {
      String selection = listeUtilisateurs.getSelectedValue();
      if (selection != null) {
        // le format est "nomUtilisateur (détails...)", donc on coupe avant la parenthèse
        // https://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
        String nomUtilisateur = selection.split(" \\(")[0];

        // on interdit la suppression d'un admin pour pas se retrouver sans accès
        if (GestionAuth.estAdmin(nomUtilisateur)) {
          JOptionPane.showMessageDialog(this, "Impossible de supprimer un administrateur.",
            "Erreur", JOptionPane.ERROR_MESSAGE);
          return;
        }

        // confirmation avant d'agir, car la suppression est permanente
        int confirmation = JOptionPane.showConfirmDialog(this,
          "Voulez-vous vraiment supprimer le compte " + nomUtilisateur + " ?",
          "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
          try {
            GestionAuth.supprimerCompte(nomUtilisateur);

            // si c'est le compte connecté qui vient d'être supprimé, on déconnecte
            if (nomUtilisateur.equals(fenetre.nomUtilisateurConnecte)) fenetre.deconnecter();

            actualiserListeUtilisateurs(); // recharge la liste à jour
          } catch (IllegalArgumentException ex) {
            // GestionAuth peut lancer une exception si la suppression est refusée
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
          }
        }
      } else {
        // personne de sélectionné dans la liste
        JOptionPane.showMessageDialog(this, "Veuillez sélectionner un compte à supprimer.");
      }
    });


    // --- bouton rouge pour effacer TOUS les comptes non-admin ---
    JButton boutonEffacerTous = new JButton("Réinitialiser tous les comptes");
    boutonEffacerTous.setBackground(Apparence.DANGER);
    boutonEffacerTous.setForeground(Color.WHITE);
    boutonEffacerTous.addActionListener(e -> {
      // double confirmation parce que c'est irréversible
      int confirmation = JOptionPane.showConfirmDialog(this,
        "Êtes-vous absolument certain de vouloir supprimer TOUS les comptes non-administrateurs ?\n"
          + "Cette action est irréversible.",
        "Réinitialisation totale", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

      if (confirmation == JOptionPane.YES_OPTION) {
        GestionAuth.effacerDonneesUtilisateurs();

        // si l'utilisateur connecté a été effacé, on le déconnecte automatiquement
        if (fenetre.nomUtilisateurConnecte != null
          && !GestionAuth.obtenirTousLesNomsUtilisateurs().contains(fenetre.nomUtilisateurConnecte)) {
          fenetre.deconnecter();
        }

        actualiserListeUtilisateurs();
        JOptionPane.showMessageDialog(this, "Tous les comptes non-administrateurs ont été supprimés.");
      }
    });


    // --- bouton noir ultra-sensible, réservé à "barbieri" ---
    // setVisible(false) le cache par défaut, actualiserListeUtilisateurs() le montre si besoin
    boutonReinitBarbieri = new JButton("Réinitialisation Totale (Barbieri)");
    boutonReinitBarbieri.setBackground(Color.BLACK);
    boutonReinitBarbieri.setForeground(Color.WHITE);
    boutonReinitBarbieri.setVisible(false);
    boutonReinitBarbieri.addActionListener(e -> {
      // ERROR_MESSAGE donne une icône d'erreur rouge pour bien montrer que c'est critique
      int confirmation = JOptionPane.showConfirmDialog(this,
        "ACTION CRITIQUE : Voulez-vous supprimer TOUS les comptes et réinitialiser les profils administrateurs ?\n"
          + "Cette action est irréversible.",
        "Réinitialisation totale", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

      if (confirmation == JOptionPane.YES_OPTION) {
        GestionAuth.reinitialisationComplete(); // remet tout à l'état d'usine
        fenetre.deconnecter();
        // null comme parent = la boîte apparaît au centre de l'écran
        JOptionPane.showMessageDialog(null, "Le système a été réinitialisé à son état d'usine.");
      }
    });

    // on ajoute les trois boutons dans la barre du bas
    panneauBoutons.add(boutonSupprimer);
    panneauBoutons.add(boutonEffacerTous);
    panneauBoutons.add(boutonReinitBarbieri);
    add(panneauBoutons, BorderLayout.SOUTH);

    // on charge la liste tout de suite à l'ouverture
    actualiserListeUtilisateurs();
  }


  // rafraîchit la liste et gère la visibilité du bouton barbieri
  public void actualiserListeUtilisateurs() {
    // le bouton noir n'apparaît que si "barbieri" est connecté
    if (boutonReinitBarbieri != null && fenetre.nomUtilisateurConnecte != null) {
      boutonReinitBarbieri.setVisible(fenetre.nomUtilisateurConnecte.equals("barbieri"));
    }

    modeleListeUtilisateurs.clear(); // vide l'ancienne liste

    // on reconstruit la liste avec les comptes non-admin et leurs infos
    for (String nom : GestionAuth.obtenirNomsNonAdmin()) {
      DonneesUtilisateur donnees = GestionAuth.obtenirProfilUtilisateur(nom);
      String entree = nom;

      if (donnees != null) {
        // format : "nomUtilisateur (nomAffichage, X ans, Y% investissement / Z% épargne, occupation)"
        entree += " (" + donnees.nomAffichage + ", " + donnees.age + " ans, "
          + donnees.pourcentInvestissement + "% investissement / "
          + (100 - donnees.pourcentInvestissement) + "% épargne, " + donnees.occupation + ")";
      }

      modeleListeUtilisateurs.addElement(entree);
    }
  }
}
