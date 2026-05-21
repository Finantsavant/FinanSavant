import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
// on importe ArrayList pour stocker les objectifs de l'utilisateur de façon dynamique
// https://docs.oracle.com/javase/8/docs/api/java/util/ArrayList.html

// page qui recommande un compte d'épargne canadien à l'utilisateur
class PageEpargne extends JPanel {

  // menus déroulants pour les questions du formulaire
  private JComboBox<String> boiteStatutProprietaire;
  private JComboBox<String> boiteEpargneEtudesEnfants;
  private JComboBox<String> boiteObjectifFinancierPrincipal;

  private JButton boutonGenererPlan;

  // garde en mémoire l'objectif à présélectionner après un refresh de la liste
  private String objectifSelectionnePref = null;

  // référence à la fenêtre principale pour naviguer entre les pages
  private final FenetrePrincipale fenetrePrincipale;


  public PageEpargne(FenetrePrincipale fenetre) {
    this.fenetrePrincipale = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // barre du haut avec bouton retour et titre
    JPanel panneauHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panneauHaut.setOpaque(false);
    JButton boutonRetour = new JButton("< Retour");
    boutonRetour.addActionListener(e -> fenetrePrincipale.retourAccueil());
    panneauHaut.add(boutonRetour);
    JLabel titrePage = new JLabel("Plan d'épargne personnalisé");
    titrePage.setFont(Apparence.SOUS_TITRE);
    titrePage.setForeground(Apparence.PRINCIPALE);
    panneauHaut.add(titrePage);
    add(panneauHaut, BorderLayout.NORTH);

    // formulaire en grille 3 rangées x 2 colonnes (label + champ par rangée)
    // https://docs.oracle.com/javase/tutorial/uiswing/layout/grid.html
    JPanel panneauFormulaire = new JPanel(new GridLayout(3, 2, 8, 8));
    panneauFormulaire.setOpaque(false);
    panneauFormulaire.setBorder(BorderFactory.createTitledBorder(null, "Votre situation",
      0, 0, Apparence.CORPS, Apparence.TEXTE));

    // question 1 : est-ce que l'utilisateur est propriétaire ?
    JLabel labelMaison = new JLabel("Propriétaire d'une maison ?");
    labelMaison.setFont(Apparence.CORPS);
    labelMaison.setForeground(Apparence.TEXTE);
    panneauFormulaire.add(labelMaison);
    boiteStatutProprietaire = new JComboBox<>(new String[]{"Non", "Oui", "Je loue"});
    boiteStatutProprietaire.setFont(Apparence.CORPS);
    panneauFormulaire.add(boiteStatutProprietaire);

    // question 2 : épargne-t-il pour les études d'un enfant ?
    JLabel labelEtudes = new JLabel("Épargne pour études d'enfant ?");
    labelEtudes.setFont(Apparence.CORPS);
    labelEtudes.setForeground(Apparence.TEXTE);
    panneauFormulaire.add(labelEtudes);
    boiteEpargneEtudesEnfants = new JComboBox<>(new String[]{"Non", "Oui, un enfant", "Oui, plusieurs"});
    boiteEpargneEtudesEnfants.setFont(Apparence.CORPS);
    panneauFormulaire.add(boiteEpargneEtudesEnfants);

    // question 3 : objectif financier principal 
    JLabel labelObjectif = new JLabel("Objectif financier principal");
    labelObjectif.setFont(Apparence.CORPS);
    labelObjectif.setForeground(Apparence.TEXTE);
    panneauFormulaire.add(labelObjectif);
    boiteObjectifFinancierPrincipal = new JComboBox<>();
    boiteObjectifFinancierPrincipal.setFont(Apparence.CORPS);
    panneauFormulaire.add(boiteObjectifFinancierPrincipal);

    // charge les objectifs disponibles dès le départ
    actualiserObjectifsDisponibles();

    // on met le formulaire dans un panneau vertical puis dans un JScrollPane
    // BoxLayout.Y_AXIS = empile les éléments de haut en bas
    JPanel panneauCentre = new JPanel();
    panneauCentre.setOpaque(false);
    panneauCentre.setLayout(new BoxLayout(panneauCentre, BoxLayout.Y_AXIS));
    panneauCentre.add(panneauFormulaire);
    add(new JScrollPane(panneauCentre), BorderLayout.CENTER);

    // bouton "générer le plan" en bas de page
    JPanel panneauBas = new JPanel(new BorderLayout(0, 10));
    panneauBas.setOpaque(false);
    boutonGenererPlan = new JButton("Générer le plan");
    boutonGenererPlan.setFont(Apparence.SOUS_TITRE);
    boutonGenererPlan.setBackground(Apparence.SECONDAIRE);
    boutonGenererPlan.setForeground(Color.WHITE);
    boutonGenererPlan.setPreferredSize(new Dimension(200, 44));

    JPanel panneauBouton = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauBouton.setOpaque(false);
    panneauBouton.add(boutonGenererPlan);
    panneauBas.add(panneauBouton, BorderLayout.CENTER);
    add(panneauBas, BorderLayout.SOUTH);

    // déclenche la génération quand on clique
    boutonGenererPlan.addActionListener(e -> genererPlanDetaille());
  }


  // appelée depuis l'extérieur pour forcer un refresh de la liste d'objectifs
  public void actualiserObjectifs() {
    actualiserObjectifsDisponibles();
  }

  // permet de présélectionner un objectif précis après le refresh
  // utile quand on arrive depuis la page "Mes Objectifs"
  public void selectionnerObjectif(String nomObjectif) {
    this.objectifSelectionnePref = nomObjectif;
    actualiserObjectifsDisponibles();
  }

  // reconstruit la liste déroulante des objectifs
  // d'abord les objectifs du profil, ensuite les options par défaut
  private void actualiserObjectifsDisponibles() {
    ArrayList<String> options = new ArrayList<>();
    DonneesUtilisateur utilisateur = GestionAuth.obtenirProfilUtilisateur(fenetrePrincipale.nomUtilisateurConnecte);

    // on ajoute les objectifs personnels de l'utilisateur en premier
    if (utilisateur != null) {
      for (Objectif objectif : utilisateur.objectifs) {
        if (objectif.nom != null && !objectif.nom.isEmpty() && !options.contains(objectif.nom)) {
          options.add(objectif.nom);
        }
      }
    }

    // on complète avec les objectifs génériques si pas déjà présents
    String[] defauts = {
      "Fonds d'urgence", "Acheter maison", "Retraite", "Croître richesse", "Études enfants", "Revenu complémentaire"
    };
    for (String defaut : defauts) {
      if (!options.contains(defaut)) options.add(defaut);
    }

    // on garde la sélection actuelle si possible après le rechargement
    String selectionPrecedente = objectifSelectionnePref != null
      ? objectifSelectionnePref
      : (String) boiteObjectifFinancierPrincipal.getSelectedItem();

    boiteObjectifFinancierPrincipal.removeAllItems();
    for (String option : options) boiteObjectifFinancierPrincipal.addItem(option);

    if (selectionPrecedente != null && options.contains(selectionPrecedente)) {
      boiteObjectifFinancierPrincipal.setSelectedItem(selectionPrecedente);
    } else if (boiteObjectifFinancierPrincipal.getItemCount() > 0) {
      boiteObjectifFinancierPrincipal.setSelectedIndex(0);
    }

    // on remet à null pour éviter de forcer la même sélection au prochain refresh
    objectifSelectionnePref = null;
  }


  // génère et affiche le plan d'épargne complet dans une pop-up
  private void genererPlanDetaille() {
    String nomUtilisateur = fenetrePrincipale.nomUtilisateurConnecte;
    if (nomUtilisateur == null) {
      JOptionPane.showMessageDialog(this, "Veuillez vous connecter pour générer un plan d'épargne.",
        "Erreur", JOptionPane.WARNING_MESSAGE);
      return;
    }

    DonneesUtilisateur donneesUtilisateur = GestionAuth.obtenirProfilUtilisateur(nomUtilisateur);
    if (donneesUtilisateur == null) {
      JOptionPane.showMessageDialog(this, "Profil utilisateur introuvable.",
        "Erreur", JOptionPane.WARNING_MESSAGE);
      return;
    }

    ArrayList<Objectif> listeObjectifs = donneesUtilisateur.objectifs;
    String objectifPrincipal = (String) boiteObjectifFinancierPrincipal.getSelectedItem();

    // StringBuilder est plus efficace que + pour construire un long texte en boucle
    // https://stackoverflow.com/questions/1532461/stringbuilder-vs-string-concatenation-in-tostring-in-java
    StringBuilder plan = new StringBuilder();

    plan.append("========== PLAN D'ÉPARGNE PERSONNALISÉ ==========\n\n");
    plan.append("PROFIL UTILISATEUR\n");
    plan.append("Âge : ").append(donneesUtilisateur.age).append(" ans\n");
    plan.append("Occupation : ").append(donneesUtilisateur.occupation).append("\n");
    plan.append("Objectif : ").append(objectifPrincipal).append("\n");
    plan.append("Propriétaire : ").append(boiteStatutProprietaire.getSelectedItem()).append("\n\n");

    if (!listeObjectifs.isEmpty()) {
      plan.append("VOS OBJECTIFS\n");
      for (Objectif objectif : listeObjectifs) {
        // convertit les mois en "X ans et Y mois" pour que ce soit lisible
        int moisRequis = objectif.obtenirMoisNecessaires();
        int anneesRequis = moisRequis / 12;
        int moisRestants = moisRequis % 12;
        String temps = "";
        if (anneesRequis > 0) temps += anneesRequis + " an" + (anneesRequis > 1 ? "s" : "");
        if (anneesRequis > 0 && moisRestants > 0) temps += " et ";
        if (moisRestants > 0 || anneesRequis == 0) temps += moisRestants + " mois";

        // "%.0f" formate un double en entier sans virgule
        // https://docs.oracle.com/javase/tutorial/java/data/numberformat.html
        plan.append("- ").append(objectif.nom).append(": ");
        plan.append(String.format("%.0f", objectif.montantTotal)).append("$ (");
        plan.append(String.format("%.0f", objectif.epargneMensuelle)).append("$/mois) - ");
        plan.append(temps).append("\n");
      }
      plan.append("\n");
    } else {
      // aucun objectif créé encore, on suggère d'aller en créer un
      plan.append("ASTUCE : Allez dans « Mes Objectifs » pour créer votre premier objectif !\n\n");
    }

    // conseils personnalisés selon l'âge de l'utilisateur
    // CELI, REER, REEE = comptes d'épargne canadiens avec avantages fiscaux
    plan.append("CONSEILS\n");
    if (donneesUtilisateur.age <= 0) {
      // âge non renseigné, conseils généraux
      plan.append("- Mettez à jour votre âge dans la page Profil pour des conseils plus personnalisés.\n");
      plan.append("- En attendant, conservez un équilibre entre épargne de précaution et investissements raisonnés.\n");
      plan.append("- Construisez un fonds d'urgence équivalent à 3-6 mois de dépenses.\n");
      plan.append("- Révisez régulièrement vos objectifs pour rester aligné sur vos priorités.\n");
    } else if (donneesUtilisateur.age < 30) {
      // jeune adulte : profiter du temps pour les intérêts composés
      plan.append("- Commencez tôt pour profiter des intérêts composés\n");
      plan.append("- Priorisez le CELI pour la flexibilité\n");
      plan.append("- Épargnez une partie de chaque revenu pour créer une habitude durable\n");
      plan.append("- Utilisez ce temps pour apprendre et construire des bases financières solides\n");
    } else if (donneesUtilisateur.age < 50) {
      // âge moyen : équilibre croissance / sécurité
      plan.append("- Équilibre entre croissance et sécurité\n");
      plan.append("- Considérez le REER pour réduire votre revenu imposable\n");
      plan.append("- Augmentez graduellement votre épargne si vos revenus augmentent\n");
      plan.append("- Ne sacrifiez pas votre fonds d'urgence en faveur d'investissements risqués\n");
    } else {
      // proche de la retraite : préserver le capital
      plan.append("- Privilégiez la préservation du capital\n");
      plan.append("- Le CELI est avantageux pour les retraits libres d'impôt\n");
      plan.append("- Recherchez des produits sûrs avec un rendement régulier\n");
      plan.append("- Pensez à la planification de retraite et à la protection de votre épargne\n");
    }

    plan.append("\n==================================================\n");
    plan.append("Outil éducatif — Consultez un conseiller financier\n");
    plan.append("==================================================");

    afficherDialogueTexte("Votre plan d'épargne personnalisé", plan.toString(), 580, 520);
  }


  // affiche le rapport dans une pop-up défilante en lecture seule
  private void afficherDialogueTexte(String titre, String contenu, int largeur, int hauteur) {
    JTextArea zoneTexte = new JTextArea(contenu);
    zoneTexte.setEditable(false);      // lecture seulement, pas d'édition
    zoneTexte.setLineWrap(true);       // retour à la ligne automatique
    zoneTexte.setWrapStyleWord(true);  // coupe aux espaces, pas au milieu d'un mot
    zoneTexte.setFont(new Font("Monospaced", Font.PLAIN, 13)); // police fixe pour l'alignement
    zoneTexte.setMargin(new Insets(10, 10, 10, 10)); // padding intérieur

    JScrollPane defilement = new JScrollPane(zoneTexte);
    defilement.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    defilement.setPreferredSize(new Dimension(largeur, hauteur));

    // PLAIN_MESSAGE = aucune icône dans la pop-up
    JOptionPane.showMessageDialog(this, defilement, titre, JOptionPane.PLAIN_MESSAGE);
  }
}
