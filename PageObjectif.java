import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;

/** Gestion des objectifs financiers personnels. */
class PageObjectif extends JPanel {
  private final FenetrePrincipale fenetre;
  private JPanel panneauGrille;
  private JButton boutonAjouter;

  public PageObjectif(FenetrePrincipale fenetre) {
    this.fenetre = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(10, 10, 10, 10));
    JPanel barreHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
    barreHaut.setOpaque(false);
    JButton boutonRetour = new JButton("< Retour");
    boutonRetour.addActionListener(e -> fenetre.retourAccueil());
    barreHaut.add(boutonRetour);
    add(barreHaut, BorderLayout.NORTH);
    panneauGrille = new JPanel();
    panneauGrille.setBackground(Apparence.FOND);
    add(new JScrollPane(panneauGrille), BorderLayout.CENTER);
  }

  public void actualiserObjectifs() {
    panneauGrille.removeAll();
    String nomUtilisateur = fenetre.nomUtilisateurConnecte;
    if (nomUtilisateur == null) {
      panneauGrille.setLayout(new FlowLayout());
      panneauGrille.add(new JLabel("Connectez-vous pour voir vos objectifs."));
      panneauGrille.revalidate();
      panneauGrille.repaint();
      return;
    }
    DonneesUtilisateur donnees = GestionAuth.obtenirProfilUtilisateur(nomUtilisateur);
    if (donnees == null) {
      panneauGrille.setLayout(new FlowLayout());
      panneauGrille.add(new JLabel("Profil introuvable."));
      panneauGrille.revalidate();
      panneauGrille.repaint();
      return;
    }
    ArrayList<Objectif> objectifs = donnees.objectifs;
    if (objectifs.isEmpty()) {
      panneauGrille.setLayout(new GridBagLayout());
      boutonAjouter = creerBoutonAjout();
      panneauGrille.add(boutonAjouter);
    } else {
      int colonnes = 3;
      int lignes = (int) Math.ceil((objectifs.size() + 1) / (double) colonnes);
      panneauGrille.setLayout(new GridLayout(lignes, colonnes, 10, 10));
      for (Objectif objectif : objectifs) panneauGrille.add(creerBoutonObjectif(objectif));
      boutonAjouter = creerBoutonAjout();
      panneauGrille.add(boutonAjouter);
    }
    panneauGrille.revalidate();
    panneauGrille.repaint();
  }

  private JButton creerBoutonAjout() {
    JButton bouton = new JButton("+");
    bouton.setFont(new Font("Arial", Font.BOLD, 30));
    bouton.addActionListener(e -> afficherDialogueObjectif(null));
    return bouton;
  }

  private JButton creerBoutonObjectif(Objectif objectif) {
    int mois = objectif.obtenirMoisNecessaires();
    String texteTemps = (mois == Integer.MAX_VALUE) ? "∞ mois" : mois + " mois";
    String texte = "<html><center>" + objectif.nom + "<br>"
      + "Total : " + String.format("%.2f", objectif.montantTotal) + " $"
      + "<br>Estimé : " + texteTemps + "</center></html>";
    JButton bouton = new JButton(texte);
    bouton.setFont(new Font("Arial", Font.PLAIN, 12));
    bouton.addActionListener(e -> afficherDialogueObjectif(objectif));
    return bouton;
  }

  private void afficherDialogueObjectif(Objectif objectifExistant) {
    boolean estNouveau = (objectifExistant == null);
    JTextField champNomObjectif = new JTextField(estNouveau ? "" : objectifExistant.nom, 20);
    JTextField champMontantTotal = new JTextField(
      estNouveau ? "" : String.valueOf(objectifExistant.montantTotal), 10);
    JTextField champEpargneMensuelle = new JTextField(
      estNouveau ? "" : String.valueOf(objectifExistant.epargneMensuelle), 10);
    JPanel panneau = new JPanel(new GridLayout(4, 2, 5, 5));
    panneau.add(new JLabel("Nom de l'objectif :"));
    panneau.add(champNomObjectif);
    panneau.add(new JLabel("Montant total ($) :"));
    panneau.add(champMontantTotal);
    panneau.add(new JLabel("Épargne mensuelle ($) :"));
    panneau.add(champEpargneMensuelle);
    panneau.add(new JLabel(""));

    String titre = estNouveau ? "Nouvel objectif" : "Modifier l'objectif";
    String[] options = estNouveau
      ? new String[] {"Créer", "Annuler"}
      : new String[] {"Modifier", "Supprimer", "Annuler"};
    int resultat = JOptionPane.showOptionDialog(this, panneau, titre,
      JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    if (resultat < 0) return;

    if (!estNouveau && resultat == 1) {
      int confirmation = JOptionPane.showConfirmDialog(this,
        "Voulez-vous vraiment supprimer l'objectif \"" + objectifExistant.nom + "\" ?",
        "Confirmation", JOptionPane.YES_NO_OPTION);
      if (confirmation == JOptionPane.YES_OPTION) {
        DonneesUtilisateur donnees = GestionAuth.obtenirProfilUtilisateur(fenetre.nomUtilisateurConnecte);
        if (donnees != null) {
          donnees.objectifs.remove(objectifExistant);
          GestionAuth.sauvegarderDonnees();
          actualiserObjectifs();
        }
      }
      return;
    }

    String nom = champNomObjectif.getText().trim();
    String totalTexte = champMontantTotal.getText().trim();
    String epargneTexte = champEpargneMensuelle.getText().trim();
    if (nom.isEmpty() || totalTexte.isEmpty() || epargneTexte.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Tous les champs doivent être remplis.",
        "Erreur", JOptionPane.ERROR_MESSAGE);
      return;
    }
    double montantTotal, epargneMensuelle;
    try {
      montantTotal = Double.parseDouble(totalTexte);
      epargneMensuelle = Double.parseDouble(epargneTexte);
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this, "Les montants doivent être des nombres valides.",
        "Erreur", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (montantTotal <= 0 || epargneMensuelle <= 0) {
      JOptionPane.showMessageDialog(this, "Les montants doivent être des nombres positifs.",
        "Erreur", JOptionPane.ERROR_MESSAGE);
      return;
    }
    DonneesUtilisateur donnees = GestionAuth.obtenirProfilUtilisateur(fenetre.nomUtilisateurConnecte);
    if (donnees != null) {
      if (estNouveau) {
        donnees.objectifs.add(new Objectif(nom, montantTotal, epargneMensuelle));
      } else {
        objectifExistant.nom = nom;
        objectifExistant.montantTotal = montantTotal;
        objectifExistant.epargneMensuelle = epargneMensuelle;
      }
      GestionAuth.sauvegarderDonnees();
      actualiserObjectifs();
    }
  }
}
