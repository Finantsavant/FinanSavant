import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;

/** Gestion des objectifs financiers personnels. */
class PageObjectif extends JPanel {
  private final FenetrePrincipale fenetre;
  private JPanel panneauGrille;
  private JButton boutonAjouter;
  private JLabel titrePage;

  public PageObjectif(FenetrePrincipale fenetre) {
    this.fenetre = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(20, 20, 20, 20));

    // ===== PANNEAU DU HAUT comme les autres pages =====
    JPanel barreHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
    barreHaut.setOpaque(false);

    // Bouton retour
    JButton boutonRetour = new JButton("< Retour");
    boutonRetour.setFont(Apparence.CORPS);
    boutonRetour.addActionListener(e -> fenetre.retourAccueil());
    barreHaut.add(boutonRetour);

    // Titre "Objectifs" juste à côté du bouton (pas centré)
    titrePage = new JLabel("Objectifs");
    titrePage.setFont(Apparence.SOUS_TITRE);
    titrePage.setForeground(Apparence.PRINCIPALE);
    barreHaut.add(titrePage);

    add(barreHaut, BorderLayout.NORTH);

    panneauGrille = new JPanel();
    panneauGrille.setOpaque(false);
    panneauGrille.setBackground(Apparence.FOND);
    panneauGrille.setBorder(new EmptyBorder(10, 10, 10, 10));
    add(new JScrollPane(panneauGrille), BorderLayout.CENTER);
  }

  public void actualiserObjectifs() {
    panneauGrille.removeAll();
    String nomUtilisateur = fenetre.nomUtilisateurConnecte;
    if (nomUtilisateur == null) {
      panneauGrille.setLayout(new FlowLayout());
      JLabel messageNonConnecte = new JLabel("Connectez-vous pour voir vos objectifs.");
      messageNonConnecte.setFont(Apparence.CORPS);
      messageNonConnecte.setForeground(Apparence.TEXTE);
      panneauGrille.add(messageNonConnecte);
      panneauGrille.revalidate();
      panneauGrille.repaint();
      return;
    }
    DonneesUtilisateur donnees = GestionAuth.obtenirProfilUtilisateur(nomUtilisateur);
    if (donnees == null) {
      panneauGrille.setLayout(new FlowLayout());
      JLabel messageErreur = new JLabel("Profil introuvable.");
      messageErreur.setFont(Apparence.CORPS);
      messageErreur.setForeground(Apparence.TEXTE);
      panneauGrille.add(messageErreur);
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
    bouton.setFont(Apparence.TITRE);
    bouton.setBackground(Apparence.SECONDAIRE);
    bouton.setForeground(Color.WHITE);
    bouton.setOpaque(true);
    bouton.setBorder(BorderFactory.createLineBorder(Apparence.PRINCIPALE));
    bouton.setPreferredSize(new Dimension(60, 60));
    bouton.addActionListener(e -> afficherDialogueObjectif(null));
    return bouton;
  }

  private JButton creerBoutonObjectif(Objectif objectif) {
    int mois = objectif.obtenirMoisNecessaires();
    String texteTemps = (mois == Integer.MAX_VALUE) ? "∞ mois" : mois + " mois";

    // Met la première lettre du nom en majuscule
    String nomObjectif = objectif.nom;
    if (nomObjectif != null && nomObjectif.length() > 0) {
      char premiereLettre = nomObjectif.charAt(0);
      if (!Character.isUpperCase(premiereLettre)) {
        nomObjectif = Character.toUpperCase(premiereLettre) + nomObjectif.substring(1);
      }
    }

    // <b> = gras en HTML
    String texte = "<html><center><b>" + nomObjectif + "</b><br>"
      + "Total : " + String.format("%.2f", objectif.montantTotal) + " $"
      + "<br>Estimé : " + texteTemps + "</center></html>";

    JButton bouton = new JButton(texte);
    bouton.setFont(Apparence.CORPS);
    bouton.setBackground(Apparence.SECONDAIRE);
    bouton.setForeground(Color.WHITE);
    bouton.setOpaque(true);
    bouton.setBorder(BorderFactory.createLineBorder(Apparence.PRINCIPALE));
    bouton.setVerticalTextPosition(SwingConstants.TOP);
    bouton.setHorizontalTextPosition(SwingConstants.CENTER);
    bouton.addActionListener(e -> afficherDialogueObjectif(objectif));
    return bouton;
  }

  private void afficherDialogueObjectif(Objectif objectifExistant) {
    boolean estNouveau = (objectifExistant == null);
    JTextField champNomObjectif = new JTextField(estNouveau ? "" : objectifExistant.nom, 20);
    champNomObjectif.setFont(Apparence.CORPS);
    JTextField champMontantTotal = new JTextField(
      estNouveau ? "" : String.valueOf(objectifExistant.montantTotal), 10);
    champMontantTotal.setFont(Apparence.CORPS);
    JTextField champEpargneMensuelle = new JTextField(
      estNouveau ? "" : String.valueOf(objectifExistant.epargneMensuelle), 10);
    champEpargneMensuelle.setFont(Apparence.CORPS);
    JPanel panneau = new JPanel(new GridLayout(4, 2, 8, 8));
    panneau.setBackground(Apparence.FOND);
    JLabel labelNom = new JLabel("Nom de l'objectif :");
    labelNom.setFont(Apparence.CORPS);
    labelNom.setForeground(Apparence.TEXTE);
    panneau.add(labelNom);
    panneau.add(champNomObjectif);
    JLabel labelTotal = new JLabel("Montant total ($) :");
    labelTotal.setFont(Apparence.CORPS);
    labelTotal.setForeground(Apparence.TEXTE);
    panneau.add(labelTotal);
    panneau.add(champMontantTotal);
    JLabel labelEpargne = new JLabel("Épargne mensuelle ($) :");
    labelEpargne.setFont(Apparence.CORPS);
    labelEpargne.setForeground(Apparence.TEXTE);
    panneau.add(labelEpargne);
    panneau.add(champEpargneMensuelle);
    panneau.add(new JLabel(""));

    String titre = estNouveau ? "Nouvel objectif" : "Modifier l'objectif";
    String[] options = estNouveau
      ? new String[] {"Créer", "Annuler"}
      : new String[] {"Modifier", "Épargner", "Supprimer", "Annuler"};
    int resultat = JOptionPane.showOptionDialog(this, panneau, titre,
      JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    if (resultat < 0) return;

    if (!estNouveau && resultat == 1) {
      fenetre.allerEpargne(objectifExistant);
      return;
    }

    if (!estNouveau && resultat == 2) {
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
    
    // Met la première lettre en majuscule (optionnel)
    if (nom.length() > 0) {
      char premiereLettre = nom.charAt(0);
      if (!Character.isUpperCase(premiereLettre)) {
        nom = Character.toUpperCase(premiereLettre) + nom.substring(1);
      }
    }
    
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
