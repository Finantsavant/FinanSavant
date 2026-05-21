import javax.swing.*;
import java.awt.*;

/** fenêtre principale avec navigation par cartes entre les pages */
class FenetrePrincipale extends JFrame {

  // cardlayout permet de changer de “page” sans ouvrir plusieurs fenêtres
  CardLayout disposition;

  // panneau principal qui contient toutes les pages
  JPanel panneauPrincipal;

  PageAccueil panneauAccueil;
  PageProfil panneauProfil;
  PageAdmin panneauAdmin;
  PageObjectif panneauObjectif;
  PageEpargne panneauEpargne;

  // variable pour stocker un montant (utilisé dans l’application)
  double montantOutil = 0;

  // indique si un utilisateur est connecté ou non
  boolean estConnecte = false;

  // nom de l’utilisateur connecté
  String nomUtilisateurConnecte = null;

  public FenetrePrincipale() {

    setTitle("FinanSavant");
    setSize(900, 500);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    // on initialise le cardlayout pour gérer les différentes pages
    disposition = new CardLayout();
    panneauPrincipal = new JPanel(disposition);

    panneauPrincipal.setBackground(Apparence.FOND);

    // création des différentes pages de l’application
    panneauAccueil = new PageAccueil(this);
    panneauProfil = new PageProfil(this);
    panneauAdmin = new PageAdmin(this);
    panneauObjectif = new PageObjectif(this);

    // on ajoute chaque page au cardlayout avec un nom pour pouvoir y accéder
    panneauPrincipal.add(panneauAccueil, "ACCUEIL");
    panneauPrincipal.add(panneauProfil, "PROFIL");
    panneauPrincipal.add(panneauObjectif, "OBJECTIF");
    panneauPrincipal.add(new PageInvestissement(this), "INVEST");
    panneauEpargne = new PageEpargne(this);
    panneauPrincipal.add(panneauEpargne, "EPARGNE");
    panneauPrincipal.add(panneauAdmin, "ADMIN");

    add(panneauPrincipal);
    setVisible(true);
  }

  // met à jour l’état de connexion de l’utilisateur
  public void definirConnexion(boolean connecte, String nomUtilisateur) {
    this.estConnecte = connecte;
    this.nomUtilisateurConnecte = nomUtilisateur;

    // on informe aussi la page d’accueil du changement
    panneauAccueil.definirUtilisateurConnecte(connecte, nomUtilisateur);
  }

  // déconnecte l’utilisateur et remet l’application à l’état initial
  public void deconnecter() {
    estConnecte = false;
    nomUtilisateurConnecte = null;

    panneauAccueil.definirUtilisateurConnecte(false, null);
    panneauProfil.reinitialiserConnexion();

    // retour automatique à la page d’accueil
    disposition.show(panneauPrincipal, "ACCUEIL");
  }

  // méthodes de navigation entre les pages

  public void retourAccueil() {
    disposition.show(panneauPrincipal, "ACCUEIL");
  }

  public void retourProfil() {
    disposition.show(panneauPrincipal, "PROFIL");
  }

  public void retourObjectif() {
    // on actualise les objectifs avant d’afficher la page
    panneauObjectif.actualiserObjectifs();
    disposition.show(panneauPrincipal, "OBJECTIF");
  }

  public void retourInvest() {
    disposition.show(panneauPrincipal, "INVEST");
  }

  public void retourEpargne() {
    if (panneauEpargne != null) panneauEpargne.actualiserObjectifs();
    disposition.show(panneauPrincipal, "EPARGNE");
  }

  public void allerEpargne(Objectif objectif) {
    if (panneauEpargne != null && objectif != null) {
      panneauEpargne.selectionnerObjectif(objectif.nom);
      disposition.show(panneauPrincipal, "EPARGNE");
    }
  }

  public void retourAdmin() {
    // on met à jour la liste des utilisateurs avant d’afficher l’admin
    panneauAdmin.actualiserListeUtilisateurs();
    disposition.show(panneauPrincipal, "ADMIN");
  }
}
