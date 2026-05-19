import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/** Recommandation de comptes d'épargne canadiens selon la situation. */
class PageEpargne extends JPanel {
  private JComboBox<String> boiteStatutProprietaire;
  private JComboBox<String> boiteEpargneEtudesEnfants;
  private JComboBox<String> boiteObjectifFinancierPrincipal;
  private JButton boutonGenererPlan;
  private JButton boutonCompteHisa;
  private JButton boutonCompteCeli;
  private JButton boutonCompteReer;
  private JButton boutonCompteCeliapp;
  private JButton boutonCompteReee;
  private JButton boutonCompteReei;
  private JLabel etiquetteCompteRecommande;
  private JLabel etiquetteExplication;
  private JTextArea zonePlanDetaille;
  private final Color couleurSurbrillance = new Color(50, 200, 50);
  private final Color couleurBoutonNormal = new Color(240, 240, 240);
  private final FenetrePrincipale fenetrePrincipale;
  private String compteRecommande = null;

  public PageEpargne(FenetrePrincipale fenetre) {
    this.fenetrePrincipale = fenetre;
    setBackground(Apparence.FOND);
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JPanel panneauHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panneauHaut.setOpaque(false);
    JButton boutonRetour = new JButton("< Retour");
    boutonRetour.addActionListener(e -> fenetrePrincipale.retourAccueil());
    panneauHaut.add(boutonRetour);
    JLabel titrePage = new JLabel("Plan d'épargne personnalisé");
    titrePage.setFont(Apparence.SOUS_TITRE);
    panneauHaut.add(titrePage);
    add(panneauHaut, BorderLayout.NORTH);

    JPanel panneauFormulaire = new JPanel(new GridLayout(3, 2, 8, 8));
    panneauFormulaire.setOpaque(false);
    panneauFormulaire.setBorder(BorderFactory.createTitledBorder("Votre situation"));
    panneauFormulaire.add(new JLabel("Propriétaire d'une maison ?"));
    boiteStatutProprietaire = new JComboBox<>(new String[]{"Non", "Oui", "Je loue"});
    panneauFormulaire.add(boiteStatutProprietaire);
    panneauFormulaire.add(new JLabel("Épargne pour études d'enfant ?"));
    boiteEpargneEtudesEnfants = new JComboBox<>(new String[]{"Non", "Oui, un enfant", "Oui, plusieurs"});
    panneauFormulaire.add(boiteEpargneEtudesEnfants);
    panneauFormulaire.add(new JLabel("Objectif financier principal"));
    boiteObjectifFinancierPrincipal = new JComboBox<>(new String[]{
      "Fonds d'urgence", "Acheter maison", "Retraite", "Croître richesse", "Études enfants", "Revenu complémentaire"
    });
    panneauFormulaire.add(boiteObjectifFinancierPrincipal);

    JPanel panneauComptes = new JPanel(new GridLayout(2, 3, 8, 8));
    panneauComptes.setOpaque(false);
    panneauComptes.setBorder(BorderFactory.createTitledBorder("Comptes disponibles"));
    boutonCompteHisa = creerBoutonCompte("HISA", "Intérêt élevé");
    boutonCompteCeli = creerBoutonCompte("CELI", "Libre d'impôt");
    boutonCompteReer = creerBoutonCompte("REER", "Retraite");
    boutonCompteCeliapp = creerBoutonCompte("CELIAPP", "Première maison");
    boutonCompteReee = creerBoutonCompte("REEE", "Études");
    boutonCompteReei = creerBoutonCompte("REEI", "Invalidité");
    panneauComptes.add(boutonCompteHisa);
    panneauComptes.add(boutonCompteCeli);
    panneauComptes.add(boutonCompteReer);
    panneauComptes.add(boutonCompteCeliapp);
    panneauComptes.add(boutonCompteReee);
    panneauComptes.add(boutonCompteReei);

    JPanel panneauCentre = new JPanel();
    panneauCentre.setOpaque(false);
    panneauCentre.setLayout(new BoxLayout(panneauCentre, BoxLayout.Y_AXIS));
    panneauCentre.add(panneauFormulaire);
    panneauCentre.add(Box.createRigidArea(new Dimension(0, 12)));
    panneauCentre.add(panneauComptes);
    panneauCentre.add(Box.createRigidArea(new Dimension(0, 12)));

    JPanel panneauRecommandation = new JPanel(new GridLayout(2, 1));
    panneauRecommandation.setOpaque(false);
    panneauRecommandation.setBorder(BorderFactory.createTitledBorder("Recommandation"));
    etiquetteCompteRecommande = new JLabel("Compte recommandé : choisissez vos réponses");
    etiquetteExplication = new JLabel(" ");
    panneauRecommandation.add(etiquetteCompteRecommande);
    panneauRecommandation.add(etiquetteExplication);
    panneauCentre.add(panneauRecommandation);
    add(panneauCentre, BorderLayout.CENTER);

    JPanel panneauBas = new JPanel(new BorderLayout(0, 10));
    panneauBas.setOpaque(false);
    boutonGenererPlan = new JButton("Générer le plan");
    boutonGenererPlan.setFont(Apparence.SOUS_TITRE);
    boutonGenererPlan.setBackground(Apparence.SECONDAIRE);
    boutonGenererPlan.setForeground(Color.WHITE);
    JPanel panneauBouton = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panneauBouton.setOpaque(false);
    panneauBouton.add(boutonGenererPlan);
    panneauBas.add(panneauBouton, BorderLayout.NORTH);

    zonePlanDetaille = new JTextArea(10, 40);
    zonePlanDetaille.setEditable(false);
    zonePlanDetaille.setLineWrap(true);
    zonePlanDetaille.setWrapStyleWord(true);
    zonePlanDetaille.setFont(new Font("Monospaced", Font.PLAIN, 12));
    zonePlanDetaille.setMargin(new Insets(10, 10, 10, 10));
    zonePlanDetaille.setText("Cliquez sur « Générer le plan » pour afficher votre plan d'épargne.");
    JScrollPane defilementPlan = new JScrollPane(zonePlanDetaille);
    defilementPlan.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    panneauBas.add(defilementPlan, BorderLayout.CENTER);
    add(panneauBas, BorderLayout.SOUTH);

    boiteStatutProprietaire.addActionListener(e -> mettreAJourRecommandation());
    boiteEpargneEtudesEnfants.addActionListener(e -> mettreAJourRecommandation());
    boiteObjectifFinancierPrincipal.addActionListener(e -> mettreAJourRecommandation());
    boutonGenererPlan.addActionListener(e -> genererPlanDetaille());
    mettreAJourRecommandation();
  }

  private JButton creerBoutonCompte(String nomCourt, String description) {
    JButton bouton = new JButton("<html><center>" + nomCourt + "<br><font size='1'>"
      + description + "</font></center></html>");
    bouton.setFont(new Font("Arial", Font.PLAIN, 11));
    bouton.setFocusPainted(false);
    bouton.setBackground(couleurBoutonNormal);
    return bouton;
  }

  private void mettreCompteEnSurbrillance(String compte) {
    JButton[] comptes = {boutonCompteHisa, boutonCompteCeli, boutonCompteReer,
      boutonCompteCeliapp, boutonCompteReee, boutonCompteReei};
    for (JButton bouton : comptes) bouton.setBackground(couleurBoutonNormal);
    switch (compte) {
      case "HISA": boutonCompteHisa.setBackground(couleurSurbrillance); break;
      case "CELI": boutonCompteCeli.setBackground(couleurSurbrillance); break;
      case "REER": boutonCompteReer.setBackground(couleurSurbrillance); break;
      case "CELIAPP": boutonCompteCeliapp.setBackground(couleurSurbrillance); break;
      case "REEE": boutonCompteReee.setBackground(couleurSurbrillance); break;
      case "REEI": boutonCompteReei.setBackground(couleurSurbrillance); break;
    }
  }

  private void mettreAJourRecommandation() {
    String statutProprietaire = (String) boiteStatutProprietaire.getSelectedItem();
    String epargneEtudes = (String) boiteEpargneEtudesEnfants.getSelectedItem();
    String objectifPrincipal = (String) boiteObjectifFinancierPrincipal.getSelectedItem();
    String nomUtilisateur = fenetrePrincipale.nomUtilisateurConnecte;
    DonneesUtilisateur donneesUtilisateur = GestionAuth.obtenirProfilUtilisateur(nomUtilisateur);
    int ageUtilisateur = (donneesUtilisateur != null) ? donneesUtilisateur.age : 30;
    String occupationUtilisateur = (donneesUtilisateur != null) ? donneesUtilisateur.occupation : "Temps plein";
    if (objectifPrincipal.equals("Acheter maison") && !statutProprietaire.equals("Oui")) {
      compteRecommande = "CELIAPP";
      etiquetteExplication.setText("Le CELIAPP est idéal pour constituer votre apport initial.");
    } else if (!epargneEtudes.equals("Non")) {
      compteRecommande = "REEE";
      etiquetteExplication.setText("Le REEE aide à construire une réserve pour les études.");
    } else if (objectifPrincipal.equals("Retraite")
      && (ageUtilisateur >= 45 || occupationUtilisateur.equals("Temps plein"))) {
      compteRecommande = "REER";
      etiquetteExplication.setText("Le REER réduit votre impôt et soutient votre retraite.");
    } else if (ageUtilisateur < 40
      && (objectifPrincipal.equals("Croître richesse") || objectifPrincipal.equals("Fonds d'urgence"))) {
      compteRecommande = "CELI";
      etiquetteExplication.setText("Le CELI permet d'épargner sans impôt sur les gains.");
    } else if (objectifPrincipal.equals("Revenu complémentaire") && ageUtilisateur < 50) {
      compteRecommande = "REEI";
      etiquetteExplication.setText("Le REEI aide à soutenir un revenu complémentaire à long terme.");
    } else {
      compteRecommande = "HISA";
      etiquetteExplication.setText("Le HISA apporte stabilité et liquidité à votre épargne.");
    }
    etiquetteCompteRecommande.setText("Compte recommandé : " + compteRecommande);
    mettreCompteEnSurbrillance(compteRecommande);
  }

  private void genererPlanDetaille() {
    String nomUtilisateur = fenetrePrincipale.nomUtilisateurConnecte;
    if (nomUtilisateur == null) {
      zonePlanDetaille.setText("Veuillez vous connecter pour générer un plan d'épargne.");
      return;
    }
    DonneesUtilisateur donneesUtilisateur = GestionAuth.obtenirProfilUtilisateur(nomUtilisateur);
    if (donneesUtilisateur == null) {
      zonePlanDetaille.setText("Profil utilisateur introuvable.");
      return;
    }
    ArrayList<Objectif> listeObjectifs = donneesUtilisateur.objectifs;
    String objectifPrincipal = (String) boiteObjectifFinancierPrincipal.getSelectedItem();
    StringBuilder plan = new StringBuilder();
    plan.append("========== PLAN D'ÉPARGNE PERSONNALISÉ ==========\n\n");
    plan.append("PROFIL UTILISATEUR\n");
    plan.append("Âge : ").append(donneesUtilisateur.age).append(" ans\n");
    plan.append("Occupation : ").append(donneesUtilisateur.occupation).append("\n");
    plan.append("Objectif : ").append(objectifPrincipal).append("\n");
    plan.append("Propriétaire : ").append(boiteStatutProprietaire.getSelectedItem()).append("\n\n");
    plan.append(">>> COMPTE RECOMMANDÉ : ").append(compteRecommande).append(" <<<\n\n");

    if (!listeObjectifs.isEmpty()) {
      plan.append("VOS OBJECTIFS\n");
      for (Objectif objectif : listeObjectifs) {
        int moisRequis = objectif.obtenirMoisNecessaires();
        int anneesRequis = moisRequis / 12;
        int moisRestants = moisRequis % 12;
        String temps = "";
        if (anneesRequis > 0) temps += anneesRequis + " an" + (anneesRequis > 1 ? "s" : "");
        if (anneesRequis > 0 && moisRestants > 0) temps += " et ";
        if (moisRestants > 0 || anneesRequis == 0) temps += moisRestants + " mois";
        plan.append("- ").append(objectif.nom).append(": ");
        plan.append(String.format("%.0f", objectif.montantTotal)).append("$ (");
        plan.append(String.format("%.0f", objectif.epargneMensuelle)).append("$/mois) - ");
        plan.append(temps).append("\n");
      }
      plan.append("\n");
    } else {
      plan.append("ASTUCE : Allez dans « Mes Objectifs » pour créer votre premier objectif !\n\n");
    }
    plan.append("CONSEILS\n");
    if (donneesUtilisateur.age < 30) {
      plan.append("- Commencez tôt pour profiter des intérêts composés\n");
      plan.append("- Priorisez le CELI pour la flexibilité\n");
    } else if (donneesUtilisateur.age < 50) {
      plan.append("- Équilibre entre croissance et sécurité\n");
      plan.append("- Considérez le REER pour réduire votre revenu imposable\n");
    } else {
      plan.append("- Privilégiez la préservation du capital\n");
      plan.append("- Le CELI est avantageux pour les retraits libres d'impôt\n");
    }
    plan.append("\n==================================================\n");
    plan.append("Outil éducatif — Consultez un conseiller financier\n");
    plan.append("==================================================");
    zonePlanDetaille.setText(plan.toString());
    zonePlanDetaille.setCaretPosition(0);
  }
}
