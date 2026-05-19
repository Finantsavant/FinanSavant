import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

/**
 * FORMAT DU FICHIER utilisateurs.txt :
 * nom_utilisateur | mot_de_passe | est_admin(1/0) | nom_affichage | âge | %_investissement | occupation | [objectifs]
 * Objectifs : nom : montant_total : épargne_mensuelle (séparés par ';' entre objectifs)
 */
class GestionAuth {
  private static final String FICHIER_UTILISATEURS = "utilisateurs.txt";
  private static HashMap<String, String> utilisateurs = new HashMap<>();
  private static HashSet<String> administrateurs = new HashSet<>();
  private static HashMap<String, DonneesUtilisateur> profils = new HashMap<>();
  private static final String[][] ADMINS_PAR_DEFAUT = {
    {"barbieri", "1234"}, {"abdeck", "1234"}, {"daniel", "1234"}, {"sebiota", "1234"}, {"kenji", "1234"}
  };
  private static final String DELIMITEUR_REGEX = "\\|";
  private static final String DELIMITEUR = "|";

  static {
    chargerDonnees();
  }

  private static void chargerDonnees() {
    File fichier = new File(FICHIER_UTILISATEURS);
    if (!fichier.exists()) {
      File ancienFichier = new File("users.txt");
      if (ancienFichier.exists()) fichier = ancienFichier;
    }
    if (!fichier.exists()) {
      initialiserParDefaut();
      sauvegarderDonnees();
      return;
    }
    try (Scanner lecteur = new Scanner(fichier)) {
      utilisateurs.clear();
      administrateurs.clear();
      profils.clear();
      while (lecteur.hasNextLine()) {
        String ligne = lecteur.nextLine();
        String[] morceaux = ligne.split(DELIMITEUR_REGEX);
        if (morceaux.length >= 7) {
          String nomUtilisateur = morceaux[0], motDePasse = morceaux[1];
          boolean estAdministrateur = morceaux[2].equals("1");
          String nomAffichage = morceaux[3];
          int age = Integer.parseInt(morceaux[4]), pourcentInvestissement = 50;
          try {
            pourcentInvestissement = Integer.parseInt(morceaux[5]);
            if (pourcentInvestissement < 0) pourcentInvestissement = 0;
            if (pourcentInvestissement > 100) pourcentInvestissement = 100;
          } catch (NumberFormatException ex) {
            // pourcentage invalide : on garde 50
          }
          String occupation = morceaux[6];
          utilisateurs.put(nomUtilisateur, motDePasse);
          if (estAdministrateur) administrateurs.add(nomUtilisateur);
          DonneesUtilisateur donneesProfil = new DonneesUtilisateur(nomAffichage, age, pourcentInvestissement, occupation);
          if (morceaux.length >= 8 && !morceaux[7].isEmpty()) {
            String[] jetonsObjectifs = morceaux[7].split(";");
            for (String jeton : jetonsObjectifs) {
              String[] morceauxObjectif = jeton.split(":");
              if (morceauxObjectif.length == 3) {
                String nomObjectif = morceauxObjectif[0];
                double total = Double.parseDouble(morceauxObjectif[1]);
                double mensuel = Double.parseDouble(morceauxObjectif[2]);
                donneesProfil.objectifs.add(new Objectif(nomObjectif, total, mensuel));
              }
            }
          }
          profils.put(nomUtilisateur, donneesProfil);
        }
      }
      if (initialiserParDefaut()) sauvegarderDonnees();
    } catch (FileNotFoundException e) {
      sauvegarderDonnees();
    } catch (NumberFormatException e) {
      System.err.println("Erreur de lecture : format de nombre incorrect.");
    }
  }

  private static boolean initialiserParDefaut() {
    boolean ajoute = false;
    for (String[] admin : ADMINS_PAR_DEFAUT) {
      if (!utilisateurs.containsKey(admin[0])) {
        utilisateurs.put(admin[0], admin[1]);
        administrateurs.add(admin[0]);
        profils.put(admin[0], new DonneesUtilisateur(admin[0], 0, 50, ""));
        ajoute = true;
      }
    }
    return ajoute;
  }

  public static void sauvegarderDonnees() {
    try (PrintWriter ecrivain = new PrintWriter(FICHIER_UTILISATEURS)) {
      for (String nomUtilisateur : utilisateurs.keySet()) {
        String motDePasse = utilisateurs.get(nomUtilisateur);
        DonneesUtilisateur donnees = profils.get(nomUtilisateur);
        if (donnees == null) donnees = new DonneesUtilisateur(nomUtilisateur, 0, 50, "");
        int estAdministrateur = administrateurs.contains(nomUtilisateur) ? 1 : 0;
        StringBuilder chaineObjectifs = new StringBuilder();
        for (int i = 0; i < donnees.objectifs.size(); i++) {
          if (i > 0) chaineObjectifs.append(";");
          Objectif objectif = donnees.objectifs.get(i);
          chaineObjectifs.append(objectif.nom).append(":").append(objectif.montantTotal)
            .append(":").append(objectif.epargneMensuelle);
        }
        ecrivain.println(nomUtilisateur + DELIMITEUR + motDePasse + DELIMITEUR + estAdministrateur + DELIMITEUR
          + donnees.nomAffichage + DELIMITEUR + donnees.age + DELIMITEUR + donnees.pourcentInvestissement
          + DELIMITEUR + donnees.occupation
          + (chaineObjectifs.length() > 0 ? DELIMITEUR + chaineObjectifs.toString() : ""));
      }
    } catch (FileNotFoundException e) {
      System.err.println("Impossible de créer le fichier " + FICHIER_UTILISATEURS);
    }
  }

  public static void effacerDonneesUtilisateurs() {
    Set<String> aSupprimer = new HashSet<>();
    for (String nomUtilisateur : utilisateurs.keySet()) {
      if (!administrateurs.contains(nomUtilisateur)) aSupprimer.add(nomUtilisateur);
    }
    for (String nomUtilisateur : aSupprimer) {
      utilisateurs.remove(nomUtilisateur);
      profils.remove(nomUtilisateur);
    }
    sauvegarderDonnees();
  }

  public static void reinitialisationComplete() {
    utilisateurs.clear();
    administrateurs.clear();
    profils.clear();
    initialiserParDefaut();
    sauvegarderDonnees();
  }

  public static boolean authentifier(String nomUtilisateur, String motDePasse) {
    return utilisateurs.containsKey(nomUtilisateur) && utilisateurs.get(nomUtilisateur).equals(motDePasse);
  }

  public static boolean enregistrer(String nomUtilisateur, String motDePasse) {
    if (utilisateurs.containsKey(nomUtilisateur)) return false;
    utilisateurs.put(nomUtilisateur, motDePasse);
    profils.put(nomUtilisateur, new DonneesUtilisateur(nomUtilisateur, 0, 50, ""));
    sauvegarderDonnees();
    return true;
  }

  public static boolean estAdmin(String nomUtilisateur) {
    return administrateurs.contains(nomUtilisateur);
  }

  public static DonneesUtilisateur obtenirProfilUtilisateur(String nomUtilisateur) {
    return profils.get(nomUtilisateur);
  }

  public static void mettreAJourProfil(String nomUtilisateur, DonneesUtilisateur donnees) {
    profils.put(nomUtilisateur, donnees);
    sauvegarderDonnees();
  }

  public static void supprimerCompte(String nomUtilisateur) {
    if (administrateurs.contains(nomUtilisateur))
      throw new IllegalArgumentException("Impossible de supprimer un compte administrateur.");
    utilisateurs.remove(nomUtilisateur);
    profils.remove(nomUtilisateur);
    sauvegarderDonnees();
  }

  public static Set<String> obtenirTousLesNomsUtilisateurs() {
    return utilisateurs.keySet();
  }

  public static Set<String> obtenirNomsNonAdmin() {
    Set<String> nonAdmins = new HashSet<>(utilisateurs.keySet());
    nonAdmins.removeAll(administrateurs);
    return nonAdmins;
  }
}
