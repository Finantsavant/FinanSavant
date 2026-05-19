import javax.swing.*;
import java.awt.*;

/** Fenêtre principale avec navigation par cartes entre les pages. */
class FenetrePrincipale extends JFrame {
  CardLayout disposition;
  JPanel panneauPrincipal;
  PageAccueil panneauAccueil;
  PageProfil panneauProfil;
  PageAdmin panneauAdmin;
  PageObjectif panneauObjectif;
  double montantOutil = 0;
  boolean estConnecte = false;
  String nomUtilisateurConnecte = null;

  public FenetrePrincipale() {
    setTitle("FinanSavant");
    setSize(900, 500);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    disposition = new CardLayout();
    panneauPrincipal = new JPanel(disposition);
    panneauPrincipal.setBackground(Apparence.FOND);

    panneauAccueil = new PageAccueil(this);
    panneauProfil = new PageProfil(this);
    panneauAdmin = new PageAdmin(this);
    panneauObjectif = new PageObjectif(this);
    panneauPrincipal.add(panneauAccueil, "ACCUEIL");
    panneauPrincipal.add(panneauProfil, "PROFIL");
    panneauPrincipal.add(panneauObjectif, "OBJECTIF");
    panneauPrincipal.add(new PageInvestissement(this), "INVEST");
    panneauPrincipal.add(new PageEpargne(this), "EPARGNE");
    panneauPrincipal.add(panneauAdmin, "ADMIN");
    add(panneauPrincipal);
    setVisible(true);
  }

  public void definirConnexion(boolean connecte, String nomUtilisateur) {
    this.estConnecte = connecte;
    this.nomUtilisateurConnecte = nomUtilisateur;
    panneauAccueil.definirUtilisateurConnecte(connecte, nomUtilisateur);
  }

  public void deconnecter() {
    estConnecte = false;
    nomUtilisateurConnecte = null;
    panneauAccueil.definirUtilisateurConnecte(false, null);
    panneauProfil.reinitialiserConnexion();
    disposition.show(panneauPrincipal, "ACCUEIL");
  }

  public void retourAccueil() {
    disposition.show(panneauPrincipal, "ACCUEIL");
  }

  public void retourProfil() {
    disposition.show(panneauPrincipal, "PROFIL");
  }

  public void retourObjectif() {
    panneauObjectif.actualiserObjectifs();
    disposition.show(panneauPrincipal, "OBJECTIF");
  }

  public void retourInvest() {
    disposition.show(panneauPrincipal, "INVEST");
  }

  public void retourEpargne() {
    disposition.show(panneauPrincipal, "EPARGNE");
  }

  public void retourAdmin() {
    panneauAdmin.actualiserListeUtilisateurs();
    disposition.show(panneauPrincipal, "ADMIN");
  }
}
