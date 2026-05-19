import javax.swing.*;
import java.awt.*;

/** administration des comptes utilisateurs (réservée aux administrateurs). */
class PageAdmin extends JPanel {
  // modèle qui garde les comptes à afficher dans la liste
  private DefaultListModel<String> modeleListeUtilisateurs;
  // liste visible à l'écran pour sélectionner un compte
  private JList<String> listeUtilisateurs;
  // fenêtre principale de l'application, utilisée pour revenir ou se déconnecter
  private final FenetrePrincipale fenetre;
  // bouton caché sauf pour l'utilisateur spécial barbieri
  private JButton boutonReinitBarbieri;

  // crée la page admin avec la fenêtre principale en paramètre
  public PageAdmin(FenetrePrincipale fenetre) {
    this.fenetre = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());

    // barre du haut avec le bouton retour
    JPanel barreHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
    barreHaut.setBackground(Apparence.FOND);
    JButton boutonRetour = new JButton("<");
    boutonRetour.setFont(new Font("Arial", Font.BOLD, 25));
    boutonRetour.setPreferredSize(new Dimension(60, 40));
    boutonRetour.addActionListener(e -> fenetre.retourAccueil());
    barreHaut.add(boutonRetour);
    add(barreHaut, BorderLayout.NORTH);

    // initialise la liste des utilisateurs
    modeleListeUtilisateurs = new DefaultListModel<>();
    listeUtilisateurs = new JList<>(modeleListeUtilisateurs);
    add(new JScrollPane(listeUtilisateurs), BorderLayout.CENTER);

    // zone du bas avec les actions d'administration
    JPanel panneauBoutons = new JPanel(new GridLayout(1, 0, 10, 10));

    // bouton pour supprimer seulement le compte choisi
    JButton boutonSupprimer = new JButton("Supprimer le compte sélectionné");
    boutonSupprimer.addActionListener(e -> {
      String selection = listeUtilisateurs.getSelectedValue();
      if (selection != null) {
        // on prend juste le nom d'utilisateur avant les détails
        String nomUtilisateur = selection.split(" \\(")[0];

        // on empêche de supprimer un admin pour éviter de casser le système
        if (GestionAuth.estAdmin(nomUtilisateur)) {
          JOptionPane.showMessageDialog(this, "Impossible de supprimer un administrateur.",
            "Erreur", JOptionPane.ERROR_MESSAGE);
          return;
        }

        // confirmation avant la suppression, pour éviter une erreur de clic
        int confirmation = JOptionPane.showConfirmDialog(this,
          "Voulez-vous vraiment supprimer le compte " + nomUtilisateur + " ?",
          "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
          try {
            // suppression réelle du compte
            GestionAuth.supprimerCompte(nomUtilisateur);

            // si on a supprimé le compte connecté, on déconnecte la personne
            if (nomUtilisateur.equals(fenetre.nomUtilisateurConnecte)) fenetre.deconnecter();

            // on recharge la liste pour enlever le compte supprimé
            actualiserListeUtilisateurs();
          } catch (IllegalArgumentException ex) {
            // message d'erreur si la suppression est refusée
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
          }
        }
      } else {
        // aucun compte n'a été choisi
        JOptionPane.showMessageDialog(this, "Veuillez sélectionner un compte à supprimer.");
      }
    });

    // bouton dangereux pour effacer tous les comptes non-admin
    JButton boutonEffacerTous = new JButton("Réinitialiser tous les comptes");
    boutonEffacerTous.setBackground(Apparence.DANGER);
    boutonEffacerTous.setForeground(Color.WHITE);
    boutonEffacerTous.addActionListener(e -> {
      // on demande une confirmation forte car l'action est irréversible
      int confirmation = JOptionPane.showConfirmDialog(this,
        "Êtes-vous absolument certain de vouloir supprimer TOUS les comptes non-administrateurs ?\n"
          + "Cette action est irréversible.",
        "Réinitialisation totale", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

      if (confirmation == JOptionPane.YES_OPTION) {
        // supprime toutes les données des utilisateurs normaux
        GestionAuth.effacerDonneesUtilisateurs();

        // si l'utilisateur connecté n'existe plus, on le sort du compte
        if (fenetre.nomUtilisateurConnecte != null
          && !GestionAuth.obtenirTousLesNomsUtilisateurs().contains(fenetre.nomUtilisateurConnecte)) {
          fenetre.deconnecter();
        }

        // mise à jour de l'affichage après la suppression
        actualiserListeUtilisateurs();
        JOptionPane.showMessageDialog(this, "Tous les comptes non-administrateurs ont été supprimés.");
      }
    });

    // bouton spécial réservé à barbieri pour réinitialiser complètement le système
    boutonReinitBarbieri = new JButton("Réinitialisation Totale (Barbieri)");
    boutonReinitBarbieri.setBackground(Color.BLACK);
    boutonReinitBarbieri.setForeground(Color.WHITE);
    boutonReinitBarbieri.setVisible(false);
    boutonReinitBarbieri.addActionListener(e -> {
      // action très sensible, donc on redemande encore une confirmation
      int confirmation = JOptionPane.showConfirmDialog(this,
        "ACTION CRITIQUE : Voulez-vous supprimer TOUS les comptes et réinitialiser les profils administrateurs ?\n"
          + "Cette action est irréversible.",
        "Réinitialisation totale", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

      if (confirmation == JOptionPane.YES_OPTION) {
        // remise à zéro complète du système
        GestionAuth.reinitialisationComplete();
        fenetre.deconnecter();
        JOptionPane.showMessageDialog(null, "Le système a été réinitialisé à son état d'usine.");
      }
    });

    // ajoute les boutons à la barre du bas
    panneauBoutons.add(boutonSupprimer);
    panneauBoutons.add(boutonEffacerTous);
    panneauBoutons.add(boutonReinitBarbieri);
    add(panneauBoutons, BorderLayout.SOUTH);

    // charge les comptes dès l'ouverture de la page
    actualiserListeUtilisateurs();
  }

  // recharge la liste avec les comptes non administrateurs
  public void actualiserListeUtilisateurs() {
    // le bouton spécial apparaît seulement si barbieri est connecté
    if (boutonReinitBarbieri != null && fenetre.nomUtilisateurConnecte != null) {
      boutonReinitBarbieri.setVisible(fenetre.nomUtilisateurConnecte.equals("barbieri"));
    }

    // vide l'ancienne liste avant de la reconstruire
    modeleListeUtilisateurs.clear();

    // ajoute chaque compte non-admin avec quelques détails utiles
    for (String nom : GestionAuth.obtenirNomsNonAdmin()) {
      DonneesUtilisateur donnees = GestionAuth.obtenirProfilUtilisateur(nom);
      String entree = nom;

      if (donnees != null) {
        // on affiche aussi le nom complet, l'âge, le ratio investissement/épargne et l'occupation
        entree += " (" + donnees.nomAffichage + ", " + donnees.age + " ans, "
          + donnees.pourcentInvestissement + "% investissement / "
          + (100 - donnees.pourcentInvestissement) + "% épargne, " + donnees.occupation + ")";
      }

      modeleListeUtilisateurs.addElement(entree);
    }
  }
}
