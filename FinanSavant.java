import javax.swing.*;
import java.awt.*;
import java.io.*;
// LocalDateTime = date + heure sans fuseau (pas dans le cours, on utilise ça au lieu de vieux trucs genre Date)
import java.time.LocalDateTime;
// DateTimeFormatter = pour afficher la date comme on veut (ex: "16 mai 2026 14:30")
import java.time.format.DateTimeFormatter;
// HashMap = genre un dictionnaire: tu donnes une clé (ex: username) et tu récupères une valeur (ex: password) super vite
import java.util.HashMap;
// HashSet = liste où chaque truc ne peut exister qu'une fois (parfait pour la liste des admins sans doublons)
import java.util.HashSet;
import java.util.Random; // Random on l'a vu en cours — nextInt() pour des trucs aléatoires
import java.util.Set; // Set = interface pour des collections, ici on l'utilise juste pour typer une variable temporaire
import java.util.ArrayList; // ArrayList = liste dynamique du cours, elle grandit toute seule quand on add()
import javax.swing.border.EmptyBorder; // EmptyBorder = marge vide autour d'un panneau (padding) pour que ce soit pas collé aux bords
/*
 *Tous les choses qui ne sont pas dans le cours mais qui sont utilisées dans ce projet:
 * HashMap/HashSet, BufferedReader/PrintWriter, try-with-resources, lambdas (->),
 * CardLayout + autres layouts, JPasswordField/JSlider/JCheckBox/JList, JOptionPane,
 * LocalDateTime, StringBuilder, regex split, throw, Math.pow/ceil, AncestorListener, etc.
 */
/**
* FORMAT DU FICHIER users.txt :
* Chaque ligne représente un utilisateur avec les champs séparés par '|' :
* nom_utilisateur | mot_de_passe | est_admin(1/0) | nom_affichage | âge | %_investissement | occupation | [objectifs]
*
* Format des objectifs (séparés par ';') :
* nom_objectif : montant_total : épargne_mensuelle
*/
// Theme = classe utilitaire avec des constantes partagées (couleurs/fonts) pour que toute l'app ait le même look
class Theme {
 // static final = une seule copie pour toute l'app, jamais réassignée (constantes de design)
 public static final Color PRIMARY = new Color(41, 128, 185); // Color(R,G,B) chaque nombre 0-255
 public static final Color SECONDARY = new Color(46, 204, 113); // Vert succès
 public static final Color DANGER = new Color(231, 76, 60); // Rouge erreur
 public static final Color BG = new Color(245, 247, 250); // Gris très clair
 public static final Color TEXT = new Color(44, 62, 80); // Texte sombre
 public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 26);
 public static final Font SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
 public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
}
// Classe d'entrée du programme (le point de départ quand tu run le .java)
public class FinanSavant {
 public static void main(String[] args) {
   // SwingUtilities.invokeLater = PAS dans le cours mais important:
   // Swing doit tourner sur un thread spécial (EDT). Si tu crées la fenêtre dans main() direct,
   // parfois ça glitch ou freeze. invokeLater dit "fais ça sur le bon thread quand t'es prêt".
   // Le "()-> new MainFrame()" c'est une LAMBDA = fonction courte sans écrire une classe entière.
   // Le "e ->" qu'on voit partout c'est pareil: "quand l'événement e arrive, fais ce bloc".
   SwingUtilities.invokeLater(() -> new MainFrame());
 }
}
// AuthManager = classe statique qui gère login/register/fichier users.txt (pas un objet qu'on "new", tout est static)
class AuthManager {
 // HashMap<String,String> = clé = nom d'utilisateur, valeur = mot de passe. .put() pour ajouter, .get() pour lire.
 private static HashMap < String, String > users = new HashMap < > ();
 // HashSet = juste des noms d'admins. .add() et .contains() — si le nom est déjà dedans, add() ne fait rien.
 private static HashSet < String > admins = new HashSet < > ();
 // Deuxième HashMap mais la valeur c'est un objet UserData (profil complet, pas juste le password)
 private static HashMap < String, UserData > profiles = new HashMap < > ();
  // Liste des administrateurs par défaut.
 private static final String[][] DEFAULT_ADMINS = { {"barbieri", "1234"}, {"abdeck", "1234"}, {"daniel", "1234"}, {"sebiota", "1234"}, {"kenji", "1234"} };
 // Un REGEX est un modèle qui permet de découper du texte selon un symbole précis (ici la barre verticale |).
 // Regex (Regular Expressions) : Syntaxe pour rechercher et manipuler des chaînes de caractères.
 // Source : https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
 // DELIMITER_REGEX = "\\|" parce que en regex le | veut dire "OU", donc on l'échappe avec \\ pour couper SUR le |
 private static final String DELIMITER_REGEX = "\\|";
 private static final String DELIMITER = "|";
 // Bloc static { } = s'exécute UNE FOIS quand la classe AuthManager est chargée (avant le main, en gros)
 // C'est comme un constructeur mais pour une classe full-static
 static {
   loadData(); // Charge users.txt dans les HashMap dès que le programme démarre
 }
 private static void loadData() {
   File file = new File("users.txt");
   // Si le fichier n'existe pas, on crée les comptes de base et on sauvegarde.
   if (!file.exists()) {
     initDefaults();
     saveData();
     return;
   }
   // try-with-resources (pas Scanner comme au cours — on utilise BufferedReader + FileReader):
   // Le cours dit Scanner + File, nous on lit ligne par ligne avec readLine() c'est plus clean pour des gros fichiers.
   // BufferedReader = lit par paquets (buffer) au lieu de caractère par caractère = plus rapide.
   // try ( ... ) = Java ferme br tout seul à la fin, même si ça crash (pas besoin de finally { br.close() })
   try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
     users.clear();
     admins.clear();
     profiles.clear();
     String line;
     while ((line = br.readLine()) != null) {
       // Découpe la ligne en morceaux pour extraire chaque information.
       String[] parts = line.split(DELIMITER_REGEX);
       if (parts.length >= 7) {
         String username = parts[0], password = parts[1];
         boolean isAdmin = parts[2].equals("1");
         String displayName = parts[3];
         int age = Integer.parseInt(parts[4]), investPercent = 50;
        
         try {
           investPercent = Integer.parseInt(parts[5]);
           // On s'assure que le pourcentage reste entre 0 et 100 pour éviter les erreurs de calcul.
           if (investPercent < 0) investPercent = 0;
           if (investPercent > 100) investPercent = 100;
         } catch (NumberFormatException ex) {
           // catch vide = si le % investissement est pourri dans le fichier, on garde 50 par défaut et on continue
         }
         String occupation = parts[6];
         users.put(username, password);
         if (isAdmin) admins.add(username);
        
         // Crée l'objet profil en mémoire.
         UserData userData = new UserData(displayName, age, investPercent, occupation);
        
         // Lecture des objectifs financiers s'ils existent (séparés par des points-virgules).
         if (parts.length >= 8 && !parts[7].isEmpty()) {
           String[] goalTokens = parts[7].split(";");
           for (String token: goalTokens) {
             String[] goalParts = token.split(":");
             if (goalParts.length == 3) {
               String goalName = goalParts[0];
               double total = Double.parseDouble(goalParts[1]);
               double monthly = Double.parseDouble(goalParts[2]);
               userData.goals.add(new Goal(goalName, total, monthly));
             }
           }
         }
         profiles.put(username, userData);
       }
     }
     // Si des admins par défaut manquent dans le fichier, on les rajoute.
     if (initDefaults()) saveData();
   } catch (FileNotFoundException e) {
     // fichier disparu entre le exists() et l'ouverture — rare mais on recrée le fichier
     saveData();
   } catch (NumberFormatException e) {
     System.err.println("Erreur de lecture : format de nombre incorrect.");
   } catch (IOException e) {
     e.printStackTrace(); // printStackTrace = affiche l'erreur dans la console (debug), pas user-friendly mais utile
   }
 }
 // initDefaults = si barbieri/kenji/etc. manquent dans le fichier, on les recrée
 private static boolean initDefaults() {
   boolean added = false;
   for (String[] admin : DEFAULT_ADMINS) {
     // On vérifie si le nom d'utilisateur existe déjà dans le dictionnaire.
     if (!users.containsKey(admin[0])) {
       users.put(admin[0], admin[1]); admins.add(admin[0]);
       profiles.put(admin[0], new UserData(admin[0], 0, 50, ""));
       added = true;
     }
   }
   return added;
 }
 // saveData = écrit tout ce qu'il y a dans les HashMap vers users.txt (persistance = données survivent après fermeture)
 public static void saveData() {
   // PrintWriter + FileWriter = pour ÉCRIRE dans un fichier (le cours parle surtout de lire avec Scanner)
   try (PrintWriter pw = new PrintWriter(new FileWriter("users.txt"))) {
     // keySet() = toutes les clés du HashMap (tous les usernames). Boucle for-each du cours mais sur un Set de clés
     for (String username: users.keySet()) {
       String pass = users.get(username);
       UserData ud = profiles.get(username);
       // Valeurs par défaut si le profil est vide.
       if (ud == null) ud = new UserData(username, 0, 50, "");
      
       // Opérateur ternaire ? : = mini if sur une ligne (pas toujours au cours mais super commun)
       int isAdmin = admins.contains(username) ? 1 : 0;
       // StringBuilder = comme concaténer des String mais BEAUCOUP plus rapide quand tu fais plein de .append()
       // (concaténer avec + dans une boucle recrée un nouveau String à chaque fois = lag)
       StringBuilder goalStr = new StringBuilder();
       for (int i = 0; i < ud.goals.size(); i++) {
         if (i > 0) goalStr.append(";"); // ; entre chaque objectif dans le même champ du fichier
         Goal g = ud.goals.get(i);
         goalStr.append(g.name).append(":").append(g.totalAmount).append(":").append(g.monthlySavings);
       }
       pw.println(username + DELIMITER + pass + DELIMITER + isAdmin + DELIMITER +
         ud.displayName + DELIMITER + ud.age + DELIMITER + ud.investPercent + DELIMITER + ud.occupation +
         (goalStr.length() > 0 ? DELIMITER + goalStr.toString() : ""));
     }
   } catch (IOException e) {
     e.printStackTrace();
   }
 }
 // Supprime tous les utilisateurs sauf les administrateurs.
 public static void wipeAllUserData() {
   // TRICK: tu peux PAS enlever des trucs d'un HashMap pendant que tu fais for (x : users.keySet())
   // ça throw ConcurrentModificationException. Donc on met les noms à supprimer dans un HashSet temporaire d'abord.
   Set < String > toRemove = new HashSet < > ();
   for (String username: users.keySet()) {
     if (!admins.contains(username)) toRemove.add(username);
   }
   for (String username: toRemove) {
     users.remove(username);
     profiles.remove(username);
   }
   saveData();
 }
 public static void hardReset() {
   users.clear();
   admins.clear(); profiles.clear();
   initDefaults();
   saveData();
 }
 // Vérifie si le couple utilisateur/mot de passe est correct.
 public static boolean authenticate(String username, String password) {
   return users.containsKey(username) && users.get(username).equals(password);
 }
 // Crée un nouveau compte si le nom n'est pas déjà pris.
 public static boolean register(String username, String password) {
   if (users.containsKey(username)) return false;
   users.put(username, password);
   // Crée un profil vide par défaut.
   profiles.put(username, new UserData(username, 0, 50, ""));
   saveData();
   return true;
 }
 public static boolean isAdmin(String username) {
   return admins.contains(username);
 }
 public static UserData getUserProfile(String username) {
   return profiles.get(username);
 }
 public static void updateUserProfile(String username, UserData data) {
   profiles.put(username, data);
   saveData();
 }
 // Retrait sécurisé d'un utilisateur de la base de données.
 public static void deleteAccount(String username) {
   // throw = on LANCE une exception volontairement (le cours parle surtout de catch, pas de throw)
   // IllegalArgumentException = "tu m'as donné un mauvais argument" — ici supprimer un admin est interdit
   if (admins.contains(username))
     throw new IllegalArgumentException("Impossible de supprimer un compte administrateur.");
   users.remove(username);
   profiles.remove(username);
   saveData();
 }
 // Renvoie la liste de tous les noms d'utilisateurs (admins et normaux).
 public static Set < String > getAllUsernames() {
   return users.keySet();
 }
 // Renvoie uniquement les noms des utilisateurs normaux (utilisé par l'admin).
 public static Set < String > getNonAdminUsernames() {
   // new HashSet<>(users.keySet()) = copie toutes les clés dans un nouveau HashSet qu'on peut modifier
   Set < String > nonAdmins = new HashSet < > (users.keySet());
   nonAdmins.removeAll(admins); // removeAll = enlève tout ce qui est aussi dans admins (garde que les users normaux)
   return nonAdmins;
 }
}
// Stocke les informations personnelles. investPercent définit le ratio Investissement / Épargne.
class UserData {
 String displayName;
 int age;
 int investPercent; // Ratio : % pour investir vs % pour épargner.
 String occupation;
 ArrayList < Goal > goals = new ArrayList < > ();
 public UserData(String displayName, int age, int investPercent, String occupation) {
   // "this" permet de différencier les variables de la classe des paramètres reçus.
   this.displayName = displayName;
   this.age = age;
   this.investPercent = investPercent;
   this.occupation = occupation;
 }
}
// Représente un but d'épargne (nom, montant total et montant mensuel).
class Goal {
 String name;
 double totalAmount;
 double monthlySavings;
 public Goal(String name, double totalAmount, double monthlySavings) {
   this.name = name;
   this.totalAmount = totalAmount;
   this.monthlySavings = monthlySavings;
 }
 // Combien de mois pour atteindre le montant si tu épargnes X par mois
 public int getMonthsNeeded() {
   if (monthlySavings <= 0) return Integer.MAX_VALUE; // MAX_VALUE = plus gros int possible, on l'utilise comme "infini"
   // Math.ceil = arrondi vers le HAUT (cours a floor et random, pas ceil). Ex: 10.1 mois -> 11 mois.
   // (int) devant = cast: coupe les décimales après le ceil
   return (int) Math.ceil(totalAmount / monthlySavings);
 }
}
// MainFrame = JFrame principal (fenêtre) — le cours montre JFrame + JPanel, nous on empile des "pages"
class MainFrame extends JFrame {
 // CardLayout = layout manager PAS au cours: imagine un paquet de cartes, une seule visible à la fois.
 // layout.show(panel, "ACCUEIL") = flip vers la carte nommée "ACCUEIL"
 CardLayout layout;
 JPanel mainPanel;
 PageAccueil accueilPanel;
 PageProfil profilPanel;
 PageAdmin adminPanel;
 PageObjectif objectifPanel;
 double montantOutil = 0;
 boolean isLoggedIn = false;
 String loggedInUsername = null;
  public MainFrame() {
   setTitle("FinanSavant");
   setSize(900, 500);
   setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   setLocationRelativeTo(null); // null = centre la fenêtre sur l'écran (sinon elle spawn en haut à gauche lol)
  
   layout = new CardLayout();
   mainPanel = new JPanel(layout);
   mainPanel.setBackground(Theme.BG);
  
   // Création des différentes pages.
   accueilPanel = new PageAccueil(this);
   profilPanel = new PageProfil(this);
   adminPanel = new PageAdmin(this);
   objectifPanel = new PageObjectif(this);
   mainPanel.add(accueilPanel, "ACCUEIL");
   mainPanel.add(profilPanel, "PROFIL");
   mainPanel.add(objectifPanel, "OBJECTIF");
   mainPanel.add(new PageInvest(this), "INVEST");
   mainPanel.add(new PageEpargne(this), "EPARGNE");
   mainPanel.add(adminPanel, "ADMIN");
   add(mainPanel);
   setVisible(true);
 }
 // Gère l'état global une fois que l'utilisateur est identifié.
 public void setLoggedIn(boolean loggedIn, String username) {
   this.isLoggedIn = loggedIn;
   this.loggedInUsername = username;
   accueilPanel.setLoggedInUser(loggedIn, username);
 }
 // Déconnecte l'utilisateur et remet les formulaires à zéro.
 public void logout() {
   isLoggedIn = false;
   loggedInUsername = null;
   accueilPanel.setLoggedInUser(false, null);
   profilPanel.resetToLogin();
   layout.show(mainPanel, "ACCUEIL");
 }
 // Fonctions de navigation pour changer de "carte" (page).
 public void retourAccueil() {
   layout.show(mainPanel, "ACCUEIL");
 }
 public void retourProfil() {
   layout.show(mainPanel, "PROFIL");
 }
 public void retourObjectif() {
   objectifPanel.refreshGoals();
   layout.show(mainPanel, "OBJECTIF");
 }
 public void retourInvest() {
   layout.show(mainPanel, "INVEST");
 }
 public void retourEpargne() {
   layout.show(mainPanel, "EPARGNE");
 }
 public void retourAdmin() {
   adminPanel.refreshUserList();
   layout.show(mainPanel, "ADMIN");
 }
}
// Premier écran contenant les accès aux différents outils (Objectifs, Investissement, Épargne).
class PageAccueil extends JPanel {
 JLabel header;
 JLabel notLoggedMessage;
 JButton objectifBtn, investBtn, epargneBtn;
 JButton profilBtn;
 JButton deconnexionBtn;
 JButton adminBtn;
 public PageAccueil(MainFrame frame) {
   setBackground(Theme.BG);
   // GridBagLayout = le layout le plus flexible (pas au cours). Chaque widget a des "contraintes" (gbc).
   // gridx/gridy = case dans la grille, weightx/weighty = qui prend l'espace en extra, anchor = où coller le widget
   setLayout(new GridBagLayout());
   GridBagConstraints gbc = new GridBagConstraints();
   gbc.insets = new Insets(15, 15, 15, 15); // Insets = marge haut/gauche/bas/droite autour du composant
  
   profilBtn = new JButton("Profil");
   profilBtn.addActionListener(e -> frame.retourProfil()); // lambda: au clic, va page profil (pas besoin de ActionListener class)
  
   gbc.gridx = 0; // Placement dans la grille (colonne 0, ligne 0).
   gbc.gridy = 0;
   gbc.weightx = 0;
   gbc.weighty = 0;
   gbc.anchor = GridBagConstraints.NORTHWEST;
   add(profilBtn, gbc);
   header = new JLabel("FinanSavant", SwingConstants.CENTER);
   header.setFont(Theme.TITLE);
   header.setForeground(Theme.PRIMARY);
   gbc.gridx = 1;
   gbc.gridy = 0;
   gbc.weightx = 1.0;
   gbc.anchor = GridBagConstraints.CENTER;
   add(header, gbc);
  
   deconnexionBtn = new JButton("Déconnexion");
   deconnexionBtn.setVisible(false);
   deconnexionBtn.addActionListener(e -> frame.logout());
   gbc.gridx = 2;
   gbc.gridy = 0;
   gbc.weightx = 0;
   gbc.anchor = GridBagConstraints.NORTHEAST;
   add(deconnexionBtn, gbc);
  
   // Message qui s'affiche si l'utilisateur n'est pas encore connecté.
   notLoggedMessage = new JLabel("Veuillez vous connecter (bouton Profil) pour utiliser les options.", SwingConstants.CENTER);
   notLoggedMessage.setFont(Theme.BODY);
   notLoggedMessage.setForeground(Color.GRAY);
   gbc.gridx = 0;
   gbc.gridy = 1;
   gbc.gridwidth = 3;
   gbc.weighty = 0.1;
   gbc.fill = GridBagConstraints.HORIZONTAL;
   add(notLoggedMessage, gbc);
  
   gbc.gridwidth = 1;
   gbc.weighty = 0.5; gbc.gridy = 2; gbc.fill = GridBagConstraints.BOTH;
  
   objectifBtn = new JButton("Mes Objectifs");
   objectifBtn.setPreferredSize(new Dimension(200, 100)); // Dimension = largeur x hauteur en pixels
   objectifBtn.setEnabled(false); // désactivé tant que pas connecté
   objectifBtn.addActionListener(e -> frame.retourObjectif());
   gbc.gridx = 0;
   add(objectifBtn, gbc);
  
   investBtn = new JButton("Investissement");
   investBtn.setPreferredSize(new Dimension(200, 100));
   investBtn.setEnabled(false);
   investBtn.addActionListener(e -> {
     // JOptionPane = popups toutes faites (input, message, oui/non) — pas dans la liste du cours mais super pratique
     String input = JOptionPane.showInputDialog(this, "Somme initiale pour vos investissements ($) :");
     if (input == null) return; // null = user a cliqué Annuler sur le popup
     try {
       double montant = Double.parseDouble(input);
       if (montant <= 0) {
         JOptionPane.showMessageDialog(this, "Veuillez entrer un montant valide."); return;
       }
       frame.montantOutil = montant;
       frame.retourInvest();
     } catch (NumberFormatException ex) {
       JOptionPane.showMessageDialog(this, "Veuillez entrer un nombre valide.");
     }
   });
   gbc.gridx = 1;
   add(investBtn, gbc);
  
   epargneBtn = new JButton("Épargne");
   epargneBtn.setPreferredSize(new Dimension(200, 100));
   epargneBtn.setEnabled(false);
   epargneBtn.addActionListener(e -> {
     String input = JOptionPane.showInputDialog(
       this, "Montant à répartir pour vos projets ($) :");
     if (input == null) return;
     try {
       double montant = Double.parseDouble(input);
       if (montant <= 0) {
         JOptionPane.showMessageDialog(this, "Veuillez entrer un montant valide."); return;
       }
       frame.montantOutil = montant;
       frame.retourEpargne();
     } catch (NumberFormatException ex) {
       JOptionPane.showMessageDialog(this,
         "Veuillez entrer un nombre valide.");
     }
   });
   gbc.gridx = 2;
   add(epargneBtn, gbc);
  
   // Bouton visible uniquement pour les administrateurs.
   adminBtn = new JButton("Gérer les comptes");
   adminBtn.setVisible(false);
   adminBtn.addActionListener(e -> frame.retourAdmin());
   gbc.gridx = 1;
   gbc.gridy = 3;
   gbc.weighty = 0;
   add(adminBtn, gbc);
 }
 // Met à jour les éléments de l'accueil selon l'utilisateur connecté.
 public void setLoggedInUser(boolean loggedIn, String username) {
   objectifBtn.setEnabled(loggedIn);
   investBtn.setEnabled(loggedIn);
   epargneBtn.setEnabled(loggedIn);
   if (loggedIn && AuthManager.isAdmin(username)) {
     header.setText("FinanSavant Admin");
   } else {
     header.setText("FinanSavant");
   }
   notLoggedMessage.setVisible(!loggedIn);
   profilBtn.setText(loggedIn ? username : "Profil"); // ternaire: si connecté affiche le username sur le bouton
   deconnexionBtn.setVisible(loggedIn);
   adminBtn.setVisible(loggedIn && AuthManager.isAdmin(username));
 }
}
// Gère l'identification et la modification des données du profil (Age, Répartition invest/épargne).
class PageProfil extends JPanel {
 // Composants LOGIN
 JTextField champUser = new JTextField(15);
 // JPasswordField = comme JTextField mais cache les caractères (****). getPassword() retourne char[] pas String
 JPasswordField champPass = new JPasswordField(15);
 JButton btnConnexion = new JButton("Connexion");
 JButton btnInscription = new JButton("Créer un compte");
 JLabel messageLogin = new JLabel("", JLabel.CENTER);
  // Composants PROFILE
 JTextField champNom = new JTextField(15);
 JTextField champAge = new JTextField(5);
  // JSlider(min, max, valeurInitiale) = curseur pour choisir un % entre 0 et 100 (composant pas au cours)
 JSlider sliderRepartition = new JSlider(0, 100, 50);
 JLabel labelRepartition = new JLabel("Répartition : ", JLabel.RIGHT);
 JLabel labelSliderDisplay = new JLabel("Investissement : 50% | Épargne : 50%", JLabel.CENTER);
 String[] occupations = {
   "Étudiant",
   "Temps partiel",
   "Temps plein",
   "Retraité",
   "Autre"
 };
 // JComboBox = menu déroulant (dropdown). getSelectedItem() donne ce que l'user a choisi
 JComboBox < String > champOccupation = new JComboBox < > (occupations);
 JLabel labelNom = new JLabel("Nom : ", JLabel.RIGHT);
 JLabel labelAge = new JLabel("Âge : ", JLabel.RIGHT);
 JLabel labelOccupation = new JLabel("Occupation : ", JLabel.RIGHT);
 JLabel messageErreur = new JLabel("", JLabel.CENTER);
 JButton sauvegarder = new JButton("Sauvegarder");
 CardLayout cardLayout = new CardLayout();
 JPanel cards = new JPanel(cardLayout); // Zone qui change entre Connexion et Formulaire Profil.
  JPanel loginCard = new JPanel(new GridBagLayout());
 JPanel profileCard = new JPanel(new GridBagLayout());
 private String currentUser = null;
  public PageProfil(MainFrame frame) {
   setBackground(Theme.BG);
   // BorderLayout = divise en NORTH/SOUTH/EAST/WEST/CENTER (pas au cours, on l'utilise partout ici)
   setLayout(new BorderLayout());
   setBorder(new EmptyBorder(20, 20, 20, 20)); // padding 20px de chaque côté
  
   // FlowLayout = aligne les trucs en ligne comme du texte qui wrap (bouton retour à gauche)
   JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
   topBar.setOpaque(false); // opaque false = panneau transparent, on voit le BG du parent
   JButton backBtn = new JButton("< Retour");
   backBtn.addActionListener(e -> frame.retourAccueil());
   topBar.add(backBtn);
   add(topBar, BorderLayout.NORTH);
   // Carte LOGIN
   loginCard.setBackground(Theme.BG);
   GridBagConstraints lgbc = new GridBagConstraints();
   lgbc.insets = new Insets(10, 10, 10, 10);
   lgbc.gridx = 0;
   lgbc.gridy = 0;
   lgbc.anchor = GridBagConstraints.EAST;
   loginCard.add(new JLabel("Nom d'utilisateur :"), lgbc);
   lgbc.gridx = 1;
   lgbc.anchor = GridBagConstraints.WEST;
   loginCard.add(champUser, lgbc);
   lgbc.gridx = 0;
   lgbc.gridy++; // gridy++ = passe à la ligne suivante dans la grille (comme descendre d'une rangée)
   lgbc.anchor = GridBagConstraints.EAST;
   loginCard.add(new JLabel("Mot de passe :"), lgbc);
   lgbc.gridx = 1;
   lgbc.anchor = GridBagConstraints.WEST;
   loginCard.add(champPass, lgbc);
   lgbc.gridx = 0;
   lgbc.gridy++;
   lgbc.gridwidth = 2;
   lgbc.anchor = GridBagConstraints.CENTER;
   JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
   btnRow.setOpaque(false);
   btnRow.add(btnConnexion);
   btnRow.add(btnInscription);
   loginCard.add(btnRow, lgbc);
   lgbc.gridy++;
   loginCard.add(messageLogin, lgbc);
  
   // Carte PROFILE
   profileCard.setBackground(Theme.BG);
   GridBagConstraints pgbc = new GridBagConstraints();
   pgbc.insets = new Insets(5, 5, 5, 5);
   pgbc.fill = GridBagConstraints.HORIZONTAL;
   pgbc.gridx = 0;
   pgbc.gridy = 0;
   pgbc.anchor = GridBagConstraints.EAST;
   pgbc.fill = GridBagConstraints.NONE;
   profileCard.add(labelNom, pgbc);
   pgbc.gridx = 1;
   pgbc.anchor = GridBagConstraints.WEST;
   profileCard.add(champNom, pgbc);
   pgbc.gridx = 0;
   pgbc.gridy++;
   pgbc.anchor = GridBagConstraints.EAST;
   profileCard.add(labelAge, pgbc);
   pgbc.gridx = 1;
   pgbc.anchor = GridBagConstraints.WEST;
   profileCard.add(champAge, pgbc);
   pgbc.gridx = 0;
   pgbc.gridy++;
   pgbc.anchor = GridBagConstraints.EAST;
   profileCard.add(labelRepartition, pgbc);
   pgbc.gridx = 1;
   pgbc.anchor = GridBagConstraints.WEST;
   pgbc.fill = GridBagConstraints.HORIZONTAL;
   profileCard.add(sliderRepartition, pgbc);
   pgbc.gridy++;
   sliderRepartition.setBackground(Theme.BG);
   sliderRepartition.setMajorTickSpacing(25);
   sliderRepartition.setMinorTickSpacing(5);
   sliderRepartition.setPaintTicks(true);
   sliderRepartition.setPaintLabels(true);
   sliderRepartition.setPreferredSize(new Dimension(400, 50));
   // ChangeListener = comme ActionListener mais pour le SLIDER (déclenché quand tu bouges le curseur, pas au clic)
   sliderRepartition.addChangeListener(e -> updateSliderLabel());
   pgbc.gridx = 0;
   pgbc.gridy++;
   pgbc.gridwidth = 2;
   pgbc.anchor = GridBagConstraints.CENTER;
   profileCard.add(labelSliderDisplay, pgbc);
   pgbc.gridwidth = 1;
   pgbc.gridy++;
   pgbc.gridx = 0;
   pgbc.anchor = GridBagConstraints.EAST;
   pgbc.fill = GridBagConstraints.NONE;
   profileCard.add(labelOccupation, pgbc);
   pgbc.gridx = 1;
   pgbc.anchor = GridBagConstraints.WEST;
   profileCard.add(champOccupation, pgbc);
   pgbc.gridx = 0;
   pgbc.gridy++;
   pgbc.gridwidth = 2;
   pgbc.anchor = GridBagConstraints.CENTER;
   profileCard.add(sauvegarder, pgbc);
   pgbc.gridy++;
   profileCard.add(messageErreur, pgbc);
   cards.add(loginCard, "LOGIN");
   cards.add(profileCard, "PROFILE");
   add(cards, BorderLayout.CENTER);
   // --- Événements ---
   btnConnexion.addActionListener(e -> { // Action lors du clic sur Connexion.
     String user = champUser.getText().trim(); // trim() = enlève espaces au début/fin (évite " kenji " qui marche pas)
     String pass = new String(champPass.getPassword()); // char[] -> String parce que le reste du code compare des String
     if (user.isEmpty() || pass.isEmpty()) {
       messageLogin.setText("Veuillez remplir tous les champs.");
       return;
     }
     if (AuthManager.authenticate(user, pass)) {
       frame.setLoggedIn(true, user);
       messageLogin.setText("Connexion réussie !");
       currentUser = user;
       populateProfile();
       cardLayout.show(cards, "PROFILE");
       champNom.setText(user);
     } else {
       messageLogin.setText("Identifiants incorrects.");
     }
   });
   btnInscription.addActionListener(e -> { // Action lors de la création d'un compte.
     String user = champUser.getText().trim(); // trim() = enlève espaces au début/fin (évite " kenji " qui marche pas)
     String pass = new String(champPass.getPassword()); // char[] -> String parce que le reste du code compare des String
     if (user.isEmpty() || pass.isEmpty()) {
       messageLogin.setText("Veuillez remplir tous les champs.");
       return;
     }
     if (AuthManager.register(user, pass)) {
       frame.setLoggedIn(true, user);
       messageLogin.setText("Compte créé et connecté !");
       currentUser = user;
       populateProfile();
       cardLayout.show(cards, "PROFILE");
       champNom.setText(user);
     } else {
       messageLogin.setText("Ce nom d'utilisateur existe déjà.");
     }
   });
   sauvegarder.addActionListener(e -> { // Enregistre les changements d'âge ou d'occupation.
     String nom = champNom.getText();
     String ageStr = champAge.getText();
     if (nom.isEmpty()) {
       messageErreur.setText("Erreur : veuillez entrer un nom.");
       return;
     }
     // Validation pour s'assurer que l'âge est un nombre cohérent.
     try {
       int age = Integer.parseInt(ageStr);
       if (age < 1 || age > 120) {
         messageErreur.setText("Erreur : âge invalide (1-120).");
         return;
       }
     } catch (NumberFormatException ex) {
       messageErreur.setText("Erreur : l'âge doit être un nombre.");
       return;
     }
     int investPercent = sliderRepartition.getValue();
     String occupation = (String) champOccupation.getSelectedItem();
     UserData oldData = AuthManager.getUserProfile(currentUser);
     UserData newData = new UserData(nom, Integer.parseInt(ageStr), investPercent, occupation);
     if (oldData != null) newData.goals = oldData.goals;
     AuthManager.updateUserProfile(currentUser, newData);
     messageErreur.setText("Profil sauvegardé : " + nom + ", " + ageStr + " ans, " +
       investPercent + "% invest / " + (100 - investPercent) + "% épargne, " + occupation);
   });
 }
 // Met à jour l'affichage texte du pourcentage choisi sur le curseur (slider).
 private void updateSliderLabel() {
   int invest = sliderRepartition.getValue();
   int epargne = 100 - invest;
   labelSliderDisplay.setText("Investissement : " + invest + "% | Épargne : " + epargne + "%");
 }
 // Remplit les champs du formulaire avec les données déjà sauvegardées de l'utilisateur.
 private void populateProfile() {
   UserData data = AuthManager.getUserProfile(currentUser);
   if (data != null) {
     champNom.setText(data.displayName);
     champAge.setText(String.valueOf(data.age));
     sliderRepartition.setValue(data.investPercent);
     updateSliderLabel();
     champOccupation.setSelectedItem(data.occupation);
   }
   champNom.setEditable(AuthManager.isAdmin(currentUser));
 }
 // Vide les champs quand on se déconnecte.
 public void resetToLogin() {
   cardLayout.show(cards, "LOGIN");
   champUser.setText("");
   champPass.setText("");
   messageLogin.setText("");
   champNom.setText("");
   champAge.setText("");
   sliderRepartition.setValue(50);
   updateSliderLabel();
   champOccupation.setSelectedIndex(0);
   messageErreur.setText("");
   currentUser = null;
   champNom.setEditable(true);
 }
}
// Permet de lister, d'ajouter ou de modifier ses projets financiers personnels.
class PageObjectif extends JPanel {
 private MainFrame frame;
 private JPanel gridPanel;
 private JButton addButton;
 public PageObjectif(MainFrame frame) {
   this.frame = frame;
   setBackground(Theme.BG);
   setLayout(new BorderLayout());
   setBorder(new EmptyBorder(10, 10, 10, 10));
   JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
   topBar.setOpaque(false);
   JButton backBtn = new JButton("< Retour");
   backBtn.addActionListener(e -> frame.retourAccueil());
   topBar.add(backBtn);
   add(topBar, BorderLayout.NORTH);
   gridPanel = new JPanel();
   gridPanel.setBackground(Theme.BG);
   // JScrollPane = met des barres de scroll si le contenu est trop gros pour la fenêtre
   add(new JScrollPane(gridPanel), BorderLayout.CENTER);
 }
 public void refreshGoals() {
   gridPanel.removeAll(); // enlève tous les boutons vieux avant de redessiner
   String username = frame.loggedInUsername;
   if (username == null) {
     gridPanel.setLayout(new FlowLayout());
     gridPanel.add(new JLabel("Connectez-vous pour voir vos objectifs."));
     // revalidate + repaint = OBLIGATOIRE après removeAll/add sinon Swing affiche rien ou un écran vide
     gridPanel.revalidate();
     gridPanel.repaint();
     return;
   }
   UserData data = AuthManager.getUserProfile(username);
   if (data == null) {
     gridPanel.setLayout(new FlowLayout());
     gridPanel.add(new JLabel("Profil introuvable."));
     gridPanel.revalidate();
     gridPanel.repaint();
     return;
   }
   ArrayList < Goal > goals = data.goals;
   if (goals.isEmpty()) {
     gridPanel.setLayout(new GridBagLayout());
     addButton = createAddButton();
     gridPanel.add(addButton);
   } else {
     // GridLayout(rows, cols) = grille fixe de boutons (3 colonnes, rows calculé pour tout fit)
     int cols = 3;
     int rows = (int) Math.ceil((goals.size() + 1) / (double) cols); // +1 pour le bouton "+" d'ajout
     gridPanel.setLayout(new GridLayout(rows, cols, 10, 10));
     for (Goal g: goals) gridPanel.add(createGoalButton(g));
     addButton = createAddButton();
     gridPanel.add(addButton);
   }
   gridPanel.revalidate();
   gridPanel.repaint();
 }
 private JButton createAddButton() {
   JButton btn = new JButton("+");
   btn.setFont(new Font("Arial", Font.BOLD, 30));
   btn.addActionListener(e -> showGoalDialog(null));
   return btn;
 }
 // Crée un bouton visuel pour un objectif existant.
 private JButton createGoalButton(Goal goal) {
   int months = goal.getMonthsNeeded();
   String timeText = (months == Integer.MAX_VALUE) ? "∞ mois" : months + " mois";
   // HTML dans un JLabel/JButton = trick Swing: <br> pour saut de ligne, <center> pour centrer (pas du vrai HTML web)
   String text = "<html><center>" + goal.name + "<br>" +
     "Total : " + String.format("%.2f", goal.totalAmount) + " $" + // String.format = nombre avec 2 décimales
     "<br>Estimé : " + timeText + "</center></html>";
   JButton btn = new JButton(text);
   btn.setFont(new Font("Arial", Font.PLAIN, 12));
   btn.addActionListener(e -> showGoalDialog(goal));
   return btn;
 }
 // Fenêtre contextuelle pour ajouter ou modifier un objectif.
 private void showGoalDialog(Goal existingGoal) {
   boolean isNew = (existingGoal == null);
   JTextField nameField = new JTextField(isNew ? "" : existingGoal.name, 20);
   JTextField totalField = new JTextField(isNew ? "" : String.valueOf(existingGoal.totalAmount), 10);
   JTextField savingField = new JTextField(isNew ? "" : String.valueOf(existingGoal.monthlySavings), 10);
   JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
   panel.add(new JLabel("Nom de l'objectif :"));
   panel.add(nameField);
   panel.add(new JLabel("Montant total ($) :"));
   panel.add(totalField);
   panel.add(new JLabel("Épargne mensuelle ($) :"));
   panel.add(savingField);
   panel.add(new JLabel(""));
  
   String title = isNew ? "Nouvel objectif" : "Modifier l'objectif";
   String[] options = isNew ?
     new String[] {
       "Créer",
       "Annuler"
     } :
     new String[] {
       "Modifier",
       "Supprimer",
       "Annuler"
     };
   // showOptionDialog = popup avec plusieurs boutons custom (Créer/Modifier/Supprimer/Annuler) au lieu de juste OK
   int result = JOptionPane.showOptionDialog(this, panel, title,
     JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
   if (result < 0) return;
  
   if (!isNew && result == 1) {
     int confirm = JOptionPane.showConfirmDialog(this,
       "Voulez-vous vraiment supprimer l'objectif \"" + existingGoal.name + "\" ?",
       "Confirmation", JOptionPane.YES_NO_OPTION);
     if (confirm == JOptionPane.YES_OPTION) {
       UserData data = AuthManager.getUserProfile(frame.loggedInUsername);
       if (data != null) {
         data.goals.remove(existingGoal);
         AuthManager.saveData();
         refreshGoals();
       }
     }
     return;
   }
   String name = nameField.getText().trim();
   String totalStr = totalField.getText().trim();
   String savingStr = savingField.getText().trim();
   if (name.isEmpty() || totalStr.isEmpty() || savingStr.isEmpty()) {
     JOptionPane.showMessageDialog(this, "Tous les champs doivent être remplis.", "Erreur", JOptionPane.ERROR_MESSAGE);
     return;
   }
   double totalAmount, monthlySavings;
   try {
     totalAmount = Double.parseDouble(totalStr);
     monthlySavings = Double.parseDouble(savingStr);
   } catch (NumberFormatException ex) {
     JOptionPane.showMessageDialog(this, "Les montants doivent être des nombres valides.", "Erreur",
       JOptionPane.ERROR_MESSAGE);
     return;
   }
   if (totalAmount <= 0 || monthlySavings <= 0) {
     JOptionPane.showMessageDialog(this, "Les montants doivent être des nombres positifs.", "Erreur",
       JOptionPane.ERROR_MESSAGE);
     return;
   }
   UserData data = AuthManager.getUserProfile(frame.loggedInUsername);
   if (data != null) {
     if (isNew) data.goals.add(new Goal(name, totalAmount, monthlySavings));
     else {
       existingGoal.name = name;
       existingGoal.totalAmount = totalAmount;
       existingGoal.monthlySavings = monthlySavings;
     }
     AuthManager.saveData();
     refreshGoals();
   }
 }
}
// Propose des conseils d'investissement et simule la croissance du capital sur 1, 3 et 5 ans.
class PageInvest extends JPanel {
 JComboBox < String > risqueBox;
 // JCheckBox = case à cocher (checkbox) — isSelected() dit si c'est coché ou pas
 JCheckBox actionsBox;
 JCheckBox etfBox;
 JCheckBox cryptoBox;
 JCheckBox obligationsBox;
 JCheckBox commoditesBox;
 JCheckBox reitsBox;
 private JLabel marketPulseLabel;
 private JLabel lastUpdateLabel;
 private JTextArea advisorArea;
 private Random random = new Random();
 // LinkedHashMap = comme HashMap MAIS garde l'ordre où t'as ajouté les trucs (utile pour afficher dans un ordre fixe)
 private static final java.util.Map < String, String > DESCRIPTIONS = new java.util.LinkedHashMap < > ();
 static { // même idée que le static block dans AuthManager — remplit DESCRIPTIONS au chargement de la classe
   DESCRIPTIONS.put("Actions",
     "<html><b>Actions (Stocks)</b><br><br>" +
     "Une action représente une part de propriété dans une entreprise cotée en bourse.<br><br>" +
     "• Potentiel de rendement élevé à long terme<br>" +
     "• Volatilité plus importante que d'autres produits<br>" +
     "• Idéal pour un horizon d'investissement de 5 ans et plus<br><br>" +
     "Risque : Moyen à Élevé</html>");
   DESCRIPTIONS.put("ETF",
     "<html><b>Fonds négociés en bourse (ETF)</b><br><br>" +
     "Un ETF regroupe plusieurs titres (actions, obligations) en un seul produit.<br><br>" +
     "• Diversification automatique<br>" +
     "• Frais de gestion très bas<br>" +
     "• Idéal pour les investisseurs débutants ou passifs<br><br>" +
     "Risque : Faible à Moyen</html>");
   DESCRIPTIONS.put("Crypto",
     "<html><b>Cryptomonnaies</b><br><br>" +
     "Les cryptomonnaies sont des monnaies numériques décentralisées (ex : Bitcoin, Ethereum).<br><br>" +
     "• Potentiel de gains très élevés<br>" +
     "• Volatilité extrêmement forte<br>" +
     "• Convient uniquement aux investisseurs tolérant un risque élevé<br><br>" +
     "Risque : Très Élevé</html>");
   DESCRIPTIONS.put("Obligations",
     "<html><b>Obligations (Bonds)</b><br><br>" +
     "Les obligations sont des prêts faits à des gouvernements ou entreprises en échange d'intérêts.<br><br>" +
     "• Revenus fixes et prévisibles<br>" +
     "• Capital généralement protégé<br>" +
     "• Idéal pour les profils conservateurs ou proches de la retraite<br><br>" +
     "Risque : Faible</html>");
   DESCRIPTIONS.put("Commodités",
     "<html><b>Commodités (Matières premières)</b><br><br>" +
     "Investissement dans des ressources physiques : or, pétrole, blé, etc.<br><br>" +
     "• Protection naturelle contre l'inflation<br>" +
     "• Faible corrélation avec les marchés boursiers<br>" +
     "• Bonne diversification pour un portefeuille mixte<br><br>" +
     "Risque : Moyen</html>");
   DESCRIPTIONS.put("REITs",
     "<html><b>REITs (Immobilier coté)</b><br><br>" +
     "Les REITs permettent d'investir dans l'immobilier sans acheter un bien directement.<br><br>" +
     "• Versement de dividendes réguliers<br>" +
     "• Exposition au marché immobilier commercial ou résidentiel<br>" +
     "• Liquide, contrairement à l'immobilier physique<br><br>" +
     "Risque : Moyen</html>");
 }
 public PageInvest(MainFrame frame) {
   // Mise en page de la fenêtre d'investissement.
   setBackground(Theme.BG);
   setLayout(new BorderLayout());
   setBorder(new EmptyBorder(20, 20, 20, 20));
   JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
   topPanel.setOpaque(false);
   JButton backBtn = new JButton("< Retour");
   backBtn.addActionListener(e -> frame.retourAccueil());
   topPanel.add(backBtn);
   JLabel titre = new JLabel("Générateur de plan d'investissement");
   titre.setFont(Theme.SUBTITLE);
   titre.setForeground(Theme.PRIMARY);
   topPanel.add(titre);
   add(topPanel, BorderLayout.NORTH);
   JPanel mainContainer = new JPanel(new GridBagLayout());
   mainContainer.setOpaque(false);
   GridBagConstraints gbc = new GridBagConstraints();
   gbc.insets = new Insets(10, 10, 10, 10);
   gbc.fill = GridBagConstraints.HORIZONTAL;
   gbc.gridx = 0;
   gbc.gridy = 0;
   JPanel risquePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
   risquePanel.setOpaque(false);
   risquePanel.add(new JLabel("Tolérance au risque :"));
   String[] risques = {
     "Faible - Stable et sécuritaire",
     "Moyen - Équilibre croissance/sécurité",
     "Élevé - Croissance agressive"
   };
   risqueBox = new JComboBox < > (risques);
   risquePanel.add(risqueBox);
   gbc.gridx = 0;
   gbc.gridy = 0;
   gbc.gridwidth = 2;
   mainContainer.add(risquePanel, gbc);
   gbc.gridy++;
   gbc.gridx = 0;
   gbc.gridwidth = 1;
   gbc.weightx = 0.6;
   JPanel typePanel = new JPanel(new GridLayout(2, 3, 10, 10));
   typePanel.setOpaque(false);
   // BorderFactory.createTitledBorder = met un titre autour du panneau (genre un encadré avec label)
   typePanel.setBorder(BorderFactory.createTitledBorder("Types d'investissements"));
   actionsBox = makeConfirmCheckBox("Actions", frame);
   etfBox = makeConfirmCheckBox("ETF", frame);
   cryptoBox = makeConfirmCheckBox("Crypto", frame);
   obligationsBox = makeConfirmCheckBox("Obligations", frame);
   commoditesBox = makeConfirmCheckBox("Commodités", frame);
   reitsBox = makeConfirmCheckBox("REITs", frame);
   typePanel.add(actionsBox);
   typePanel.add(etfBox);
   typePanel.add(cryptoBox);
   typePanel.add(obligationsBox);
   typePanel.add(commoditesBox);
   typePanel.add(reitsBox);
   mainContainer.add(typePanel, gbc);
   gbc.gridx = 1;
   gbc.weightx = 0.4;
   JPanel advisorPanel = new JPanel(new BorderLayout(8, 8));
   advisorPanel.setOpaque(false);
   advisorPanel.setBorder(BorderFactory.createTitledBorder("Conseiller financier"));
   marketPulseLabel = new JLabel("Marché : sélectionnez votre profil pour générer des conseils.");
   marketPulseLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
   advisorArea = new JTextArea(12, 30);
   advisorArea.setEditable(false);
   advisorArea.setLineWrap(true);
   advisorArea.setWrapStyleWord(true);
   advisorArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
   advisorArea.setText("Votre conseiller personnel attend vos choix. Sélectionnez des options pour voir une analyse personnalisée et une simulation de croissance.");
   advisorArea.setBackground(new Color(250, 250, 250));
   // JScrollPane : Conteneur qui ajoute des barres de défilement à un composant.
   // Source : https://docs.oracle.com/javase/8/docs/api/javax/swing/JScrollPane.html
   JScrollPane advisorScroll = new JScrollPane(advisorArea);
   advisorScroll.setBorder(BorderFactory.createEmptyBorder());
   lastUpdateLabel = new JLabel("Dernière mise à jour : " + getCurrentTimestamp());
   advisorPanel.add(marketPulseLabel, BorderLayout.NORTH);
   advisorPanel.add(advisorScroll, BorderLayout.CENTER);
   advisorPanel.add(lastUpdateLabel, BorderLayout.SOUTH);
   mainContainer.add(advisorPanel, gbc);
  
   gbc.gridy++;
   gbc.gridx = 0;
   gbc.gridwidth = 2;
   gbc.weightx = 0;
   JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
   btnPanel.setOpaque(false);
   JButton genererBtn = new JButton("Générer le plan");
   genererBtn.setFont(Theme.SUBTITLE);
   genererBtn.setBackground(Theme.SECONDARY);
   genererBtn.setForeground(Color.WHITE);
   genererBtn.setPreferredSize(new Dimension(220, 50));
   JButton simulateBtn = new JButton("Simuler la croissance");
   simulateBtn.setFont(Theme.SUBTITLE);
   simulateBtn.setBackground(new Color(69, 179, 157));
   simulateBtn.setForeground(Color.WHITE);
   simulateBtn.setPreferredSize(new Dimension(220, 50));
   btnPanel.add(genererBtn);
   btnPanel.add(simulateBtn);
   mainContainer.add(btnPanel, gbc);
   add(mainContainer, BorderLayout.CENTER);
  
   // AncestorListener = PAS ActionListener — se déclenche quand le panneau devient visible dans la fenêtre
   // (genre quand CardLayout montre la page INVEST). On reset les checkboxes à chaque fois qu'on arrive sur la page.
   addAncestorListener(new javax.swing.event.AncestorListener() {
     public void ancestorAdded(javax.swing.event.AncestorEvent e) {
       resetPage();
     }
     public void ancestorRemoved(javax.swing.event.AncestorEvent e) {} // méthodes vides obligatoires (interface)
     public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
   });
  
   // Mise à jour de l'aperçu du conseiller dès qu'une case est cochée ou changée.
   risqueBox.addActionListener(e -> refreshAdvisorPreview(frame));
   actionsBox.addActionListener(e -> refreshAdvisorPreview(frame));
   etfBox.addActionListener(e -> refreshAdvisorPreview(frame));
   cryptoBox.addActionListener(e -> refreshAdvisorPreview(frame));
   obligationsBox.addActionListener(e -> refreshAdvisorPreview(frame));
   commoditesBox.addActionListener(e -> refreshAdvisorPreview(frame));
   reitsBox.addActionListener(e -> refreshAdvisorPreview(frame));
  
   genererBtn.addActionListener(e -> {
     double montant = frame.montantOutil;
     if (montant <= 0) {
       JOptionPane.showMessageDialog(this, "Le montant doit être positif.");
       return;
     }
     UserData user = AuthManager.getUserProfile(frame.loggedInUsername);
     if (user == null) {
       JOptionPane.showMessageDialog(this, "Profil introuvable.");
       return;
     }
     boolean anySelected = actionsBox.isSelected() || etfBox.isSelected() ||
     cryptoBox.isSelected() || obligationsBox.isSelected() ||
     commoditesBox.isSelected() || reitsBox.isSelected();
     if (!anySelected) {
       JOptionPane.showMessageDialog(this,
         "Veuillez sélectionner au moins un type d'investissement.");
       return;
     }
     int risqueLevel = getRiskLevel();
     refreshAdvisorPreview(frame);
     String plan = buildPlan(montant, user, risqueLevel);
     showTextDialog("Votre plan d'investissement personnalisé", plan, 580, 520);
   });
  
   simulateBtn.addActionListener(e -> {
     double montant = frame.montantOutil;
     if (montant <= 0) {
       JOptionPane.showMessageDialog(this, "Le montant doit être positif.");
       return;
     }
     UserData user = AuthManager.getUserProfile(frame.loggedInUsername);
     if (user == null) {
       JOptionPane.showMessageDialog(this, "Profil introuvable.");
       return;
     }
     boolean anySelected = actionsBox.isSelected() || etfBox.isSelected() ||
     cryptoBox.isSelected() || obligationsBox.isSelected() ||
     commoditesBox.isSelected() || reitsBox.isSelected();
     if (!anySelected) {
       JOptionPane.showMessageDialog(this,
         "Veuillez sélectionner au moins un type d'investissement.");
       return;
     }
     int risqueLevel = getRiskLevel();
     refreshAdvisorPreview(frame);
     String simulation = buildSimulationReport(montant, user, risqueLevel,
       actionsBox.isSelected(), etfBox.isSelected(), cryptoBox.isSelected(),
       obligationsBox.isSelected(), commoditesBox.isSelected(), reitsBox.isSelected());
     showTextDialog("Simulation de croissance", simulation, 580, 520);
   });
 }
 // Crée une case à cocher qui affiche une description détaillée du type d'investissement avant sélection.
 private JCheckBox makeConfirmCheckBox(String label, MainFrame frame) {
   JCheckBox box = new JCheckBox(label);
   box.setOpaque(false);
   attachConfirmListener(box, label);
   return box;
 }
 // Associe l'affichage d'une boîte de dialogue descriptive quand on coche une option.
 private void attachConfirmListener(JCheckBox box, String label) {
   box.addActionListener(e -> {
     if (!box.isSelected()) return;
     // getOrDefault = si la clé existe dans la Map, retourne la valeur, sinon retourne le 2e argument (backup)
     String desc = DESCRIPTIONS.getOrDefault(label,
       "<html>Aucune description disponible.</html>");
     JLabel descLabel = new JLabel(desc);
     descLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
     int choice = JOptionPane.showOptionDialog(
       this, descLabel,
       "Type d'investissement : " + label,
       JOptionPane.DEFAULT_OPTION,
       JOptionPane.INFORMATION_MESSAGE,
       null,
       new String[] {
         "Confirmer",
         "Annuler"
       },
       "Confirmer"
     );
     if (choice != 0) {
       box.setSelected(false);
     }
   });
 }
 // Réinitialise les choix de l'utilisateur quand il quitte ou revient sur la page.
 private void resetPage() {
   for (JCheckBox box: new JCheckBox[] {
       actionsBox,
       etfBox,
       cryptoBox,
       obligationsBox,
       commoditesBox,
       reitsBox
     }) {
     if (box == null) continue;
     // On enlève les vieux listeners avant d'en remettre — sinon à chaque visite tu stack des doublons et ça pop 5 fois
     for (java.awt.event.ActionListener al: box.getActionListeners())
       box.removeActionListener(al);
     box.setSelected(false);
   }
   attachConfirmListener(actionsBox, "Actions");
   attachConfirmListener(etfBox, "ETF");
   attachConfirmListener(cryptoBox, "Crypto");
   attachConfirmListener(obligationsBox, "Obligations");
   attachConfirmListener(commoditesBox, "Commodités");
   attachConfirmListener(reitsBox, "REITs");
   if (risqueBox != null) risqueBox.setSelectedIndex(0);
 }
 // Méthodes pour construire les sections du rapport selon le profil de risque et l'âge.
 private String buildActions(double montantInvesti, int risque, int age, String occupation) {
   StringBuilder sb = new StringBuilder();
   sb.append("──── ACTIONS ────\n\n");
   if (risque == 0 || age >= 55) {
     double alloc = 0.20;
     sb.append(formatLine("JNJ", "Johnson & Johnson",
       "Entreprise pharmaceutique très stable, dividendes réguliers.",
       alloc, montantInvesti));
     sb.append(formatLine("KO", "Coca-Cola",
       "Valeur refuge, dividendes croissants depuis plus de 50 ans.",
       0.15, montantInvesti));
   } else if (risque == 1) {
     sb.append(formatLine("AAPL", "Apple Inc.",
       "Leader technologique, flux de trésorerie solides.",
       0.20, montantInvesti));
     sb.append(formatLine("MSFT", "Microsoft",
       "Cloud & IA en forte croissance, bilan robuste.",
       0.15, montantInvesti));
     sb.append(formatLine("BRK.B", "Berkshire Hathaway",
       "Holding diversifié de Warren Buffett, très défensif.",
       0.10, montantInvesti));
   } else {
     sb.append(formatLine("NVDA", "Nvidia",
       "Leader IA et semi-conducteurs, croissance explosive.",
       0.20, montantInvesti));
     sb.append(formatLine("TSLA", "Tesla",
       "Véhicules électriques et énergie, forte volatilité/potentiel.",
       0.15, montantInvesti));
     sb.append(formatLine("AMZN", "Amazon",
       "E-commerce et cloud AWS, moteur de croissance majeur.",
       0.10, montantInvesti));
     if (age < 30) {
       sb.append(formatLine("META", "Meta Platforms",
         "Réseaux sociaux et métavers, profil risque/rendement élevé.",
         0.10, montantInvesti));
     }
   }
   if (occupation.equals("Étudiant") || occupation.equals("Temps partiel")) {
     sb.append(formatLine("VTI", "Vanguard Total Market (Action)",
       "Exposition maximale au marché US avec un seul titre.",
       0.10, montantInvesti));
   }
   return sb.append("\n").toString();
 }
 private String buildETF(double montantInvesti, int risque, int age) {
   StringBuilder sb = new StringBuilder();
   sb.append("──── ETF ────\n\n");
   sb.append(formatLine("VOO", "ETF S&P 500 (Vanguard)",
     "Réplique les 500 plus grandes entreprises américaines.",
     risque == 2 ? 0.20 : 0.30, montantInvesti));
   if (risque >= 1) {
     sb.append(formatLine("QQQ", "ETF Nasdaq-100 (Invesco)",
       "Exposition aux 100 plus grandes tech américaines.",
       0.15, montantInvesti));
   }
   if (age >= 40 || risque == 0) {
     sb.append(formatLine("XBB.TO", "ETF Obligations canadiennes (iShares)",
       "Diversification défensive sur le marché obligataire canadien.",
       0.15, montantInvesti));
   }
   if (risque == 2 && age < 40) {
     sb.append(formatLine("ARKK", "ARK Innovation ETF",
       "ETF thématique axé sur l'innovation disruptive. Très volatile.",
       0.10, montantInvesti));
   }
   sb.append(formatLine("XEF.TO", "ETF Marchés développés (iShares)",
     "Diversification internationale hors États-Unis.",
     0.10, montantInvesti));
   return sb.append("\n").toString();
 }
 private String buildCrypto(double montantInvesti, int risque, int age) {
   StringBuilder sb = new StringBuilder();
   sb.append("──── CRYPTOMONNAIES ────\n\n");
   if (risque == 0 || age >= 55) {
     sb.append("⚠ Avertissement : la crypto est déconseillée pour un profil conservateur ou en fin de carrière.\n");
     sb.append(formatLine("BTC", "Bitcoin",
       "Seule crypto recommandée pour profil prudent -- la plus établie.",
       0.05, montantInvesti));
   } else if (risque == 1) {
     sb.append(formatLine("BTC", "Bitcoin",
       "Crypto principale, liquidité maximale, volatilité modérée.",
       0.10, montantInvesti));
     sb.append(formatLine("ETH", "Ethereum",
       "Plateforme de contrats intelligents, très utilisée.",
       0.07, montantInvesti));
   } else {
     sb.append(formatLine("BTC", "Bitcoin",
       "Référence du marché crypto.",
       0.10, montantInvesti));
     sb.append(formatLine("ETH", "Ethereum",
       "Deuxième plus grande crypto, écosystème DeFi dominant.",
       0.08, montantInvesti));
     sb.append(formatLine("SOL", "Solana",
       "Blockchain rapide et scalable, forte croissance en 2024-25.",
       0.05, montantInvesti));
     if (age < 30) {
       sb.append(formatLine("DOT", "Polkadot",
         "Protocole d'interopérabilité entre blockchains. Spéculatif.",
         0.03, montantInvesti));
     }
   }
   return sb.append("\n").toString();
 }
 private String buildObligations(double montantInvesti, int risque, int age, String occupation) {
   StringBuilder sb = new StringBuilder();
   sb.append("──── OBLIGATIONS ────\n\n");
   double allocGouv = (age >= 55 || occupation.equals("Retraité")) ? 0.25 : 0.15;
   sb.append(formatLine("CAN GOV", "Obligations du gouvernement canadien",
     "Faible risque, revenus prévisibles, idéales comme filet de sécurité.",
     allocGouv, montantInvesti));
   if (risque >= 1) {
     sb.append(formatLine("ZAG.TO", "ETF Obligations agrégées (BMO)",
       "Mix d'obligations gouvernementales et corporatives canadiennes.",
       0.10, montantInvesti));
   }
   if (risque == 2 && age < 45) {
     sb.append(formatLine("HYG", "ETF Obligations à haut rendement (iShares)",
       "Obligations corporatives à rendement élevé, risque plus important.",
       0.08, montantInvesti));
   }
   if (occupation.equals("Retraité") || age >= 60) {
     sb.append(formatLine("TIP", "ETF TIPS (iShares)",
       "Obligations protégées contre l'inflation -- idéales pour retraités.",
       0.12, montantInvesti));
   }
   return sb.append("\n").toString();
 }
 private String buildCommodites(double montantInvesti, int risque) {
   StringBuilder sb = new StringBuilder();
   sb.append("──── COMMODITÉS ────\n\n");
   sb.append(formatLine("GLD", "ETF Or (SPDR Gold Shares)",
     "Protection classique contre l'inflation et les crises.",
     risque == 0 ? 0.15 : 0.10, montantInvesti));
   if (risque >= 1) {
     sb.append(formatLine("SLV", "ETF Argent (iShares Silver Trust)",
       "Argent métal -- plus volatil que l'or, potentiel industriel.",
       0.07, montantInvesti));
     sb.append(formatLine("USO", "ETF Pétrole brut (US Oil Fund)",
       "Exposition au marché pétrolier. Très sensible aux événements géopolitiques.",
       0.06, montantInvesti));
   }
   if (risque == 2) {
     sb.append(formatLine("CORN", "ETF Maïs (Teucrium)",
       "Matière première agricole, diversification non-corrélée.",
       0.05, montantInvesti));
   }
   return sb.append("\n").toString();
 }
 private String buildREITs(double montantInvesti, int risque, int age, String occupation) {
   StringBuilder sb = new StringBuilder();
   sb.append("──── REITS (Immobilier) ────\n\n");
   sb.append(formatLine("VNQ", "Vanguard Real Estate ETF",
     "ETF immobilier diversifié : bureaux, résidentiel, entrepôts.",
     0.15, montantInvesti));
   if (occupation.equals("Retraité") || age >= 50) {
     sb.append(formatLine("O", "Realty Income Corp.",
       "Surnommé « The Monthly Dividend Company » -- dividendes mensuels très fiables.",
       0.10, montantInvesti));
   }
   if (risque >= 1) {
     sb.append(formatLine("STAG", "STAG Industrial REIT",
       "Immobilier industriel et logistique -- secteur en forte demande.",
       0.08, montantInvesti));
   }
   if (risque == 2 && age < 45) {
     sb.append(formatLine("IRM", "Iron Mountain Inc.",
       "Centres de données et stockage -- croissance numérique.",
       0.07, montantInvesti));
   }
   return sb.append("\n").toString();
 }
 // Convertit le texte du combo en nombre 0/1/2 pour simplifier les if dans le reste du code
 private int getRiskLevel() {
   String risque = (String) risqueBox.getSelectedItem();
   return risque.startsWith("Faible") ? 0 : risque.startsWith("Moyen") ? 1 : 2; // ternaire chaîné
 }
 // Met à jour la boîte texte du conseiller en temps réel.
 private void refreshAdvisorPreview(MainFrame frame) {
   UserData user = AuthManager.getUserProfile(frame.loggedInUsername);
   if (user == null) return;
   int riskLevel = getRiskLevel();
   boolean anySelected = actionsBox.isSelected() || etfBox.isSelected() ||
     cryptoBox.isSelected() || obligationsBox.isSelected() ||
     commoditesBox.isSelected() || reitsBox.isSelected();
   marketPulseLabel.setText(getMarketPulse(riskLevel));
   lastUpdateLabel.setText("Dernière mise à jour : " + getCurrentTimestamp());
   advisorArea.setText(getAdvisorSummary(user, riskLevel, frame.montantOutil, anySelected));
 }
 // Génère un résumé textuel basé sur les données de l'utilisateur.
 private String getAdvisorSummary(UserData user, int riskLevel, double montant, boolean anySelected) {
   StringBuilder sb = new StringBuilder();
   sb.append("Bonjour " + user.displayName + ", voici votre analyse personnelle :\n\n");
   sb.append("Profil : âge " + user.age + " ans, occupation " + user.occupation + ", ");
   sb.append("allocation invest/épargne " + user.investPercent + "% / " + (100 - user.investPercent) + "%\n");
   sb.append("Tolérance au risque : ");
   if (riskLevel == 0) sb.append("Conservatrice\n\n");
   else if (riskLevel == 1) sb.append("Équilibrée\n\n");
   else sb.append("Agressive\n\n");
   if (montant > 0) {
     sb.append(String.format("Budget analysé : %.2f $\n\n", montant));
   }
   if (!anySelected) {
     sb.append("Sélectionnez des classes d'actifs pour recevoir un plan clair, des allocations et une simulation de croissance.\n");
     sb.append("Conseil : diversifier vous aidera à lisser les variations de marché et à faire croître votre patrimoine de manière plus stable.");
     return sb.toString();
   }
   sb.append("Recommandations immédiates :\n");
   if (riskLevel == 0) {
     sb.append("- Priorisez la stabilité et privilégiez les obligations, ETF défensifs et REITs de qualité.\n");
   } else if (riskLevel == 1) {
     sb.append("- Mélangez actions de qualité, ETF diversifiés et une touche d'obligations.\n");
   } else {
     sb.append("- Conservez une exposition aux actions de croissance, crypto et actifs thématiques.\n");
   }
   if (user.age < 30) {
     sb.append("- Vous avez un horizon long terme. Laissez la majeure partie de vos gains se réinvestir.\n");
   } else if (user.age >= 55) {
     sb.append("- Préservez votre capital, avec une portion plus élevée en revenus fixes.\n");
   }
   sb.append("- Toujours garder une réserve de trésorerie pour profiter des opportunités sans paniquer.\n\n");
   sb.append("Ce conseiller simule l'évolution de votre portefeuille et vous aide à conserver une stratégie claire.\n");
   return sb.toString();
 }
 private String getMarketPulse(int riskLevel) {
   int seed = random.nextInt(3);
   if (riskLevel == 0) {
     return seed == 0 ?
       "Marché actuel : préférence pour les actifs défensifs et les flux de trésorerie stables." :
       seed == 1 ?
       "Marché actuel : volatilité modérée, privilégiez la sécurité." :
       "Marché actuel : conditions favorables pour renforcer la réserve de capital.";
   }
   if (riskLevel == 1) {
     return seed == 0 ?
       "Marché actuel : bonnes opportunités de diversification." :
       seed == 1 ?
       "Marché actuel : équilibrez croissance et protection." :
       "Marché actuel : restez attentif aux tendances sectorielles.";
   }
   return seed == 0 ?
     "Marché actuel : tolérance élevée recommandée, mais surveillez la volatilité." :
     seed == 1 ?
     "Marché actuel : opportunités de croissance agressive présentes." :
     "Marché actuel : le marché est volatile, ciblez des positions long terme.";
 }
 private String getCurrentTimestamp() {
   // ofPattern = tu définis le format de date toi-même (MMM = mois abrégé genre "mai")
   DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
   return LocalDateTime.now().format(formatter); // now() = date/heure actuelle de l'ordi
 }
 private void showTextDialog(String title, String content, int width, int height) {
   JTextArea area = new JTextArea(content);
   area.setEditable(false);
   area.setFont(new Font("Monospaced", Font.PLAIN, 13));
   area.setMargin(new Insets(10, 10, 10, 10));
   JScrollPane scroll = new JScrollPane(area);
   scroll.setPreferredSize(new Dimension(width, height));
   JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.PLAIN_MESSAGE);
 }
 private String buildPlan(double montant, UserData user, int risqueLevel) {
   int investPercent = user.investPercent;
   double montantInvesti = montant * investPercent / 100.0;
   double montantEpargne = montant - montantInvesti;
   int age = user.age;
   String occupation = user.occupation;
   StringBuilder sb = new StringBuilder();
   sb.append("=================================================\n");
   sb.append(" PLAN D'INVESTISSEMENT PERSONNALISÉ\n");
   sb.append("=================================================\n\n");
   sb.append(String.format("Montant total analysé : %.2f $\n", montant));
   sb.append(String.format("Investissement : %.2f $ (%d%%) | Épargne : %.2f $ (%d%%)\n\n",
     montantInvesti, investPercent, montantEpargne, 100 - investPercent));
   sb.append(getAdvisorSummary(user, risqueLevel, montant, true));
   sb.append("\n-------------------------------------------------\n\n");
   if (actionsBox.isSelected()) sb.append(buildActions(montantInvesti, risqueLevel, age, occupation));
   if (etfBox.isSelected()) sb.append(buildETF(montantInvesti, risqueLevel, age));
   if (cryptoBox.isSelected()) sb.append(buildCrypto(montantInvesti, risqueLevel, age));
   if (obligationsBox.isSelected()) sb.append(buildObligations(montantInvesti, risqueLevel, age, occupation));
   if (commoditesBox.isSelected()) sb.append(buildCommodites(montantInvesti, risqueLevel));
   if (reitsBox.isSelected()) sb.append(buildREITs(montantInvesti, risqueLevel, age, occupation));
   sb.append(buildSimulationReport(montant, user, risqueLevel,
     actionsBox.isSelected(), etfBox.isSelected(), cryptoBox.isSelected(),
     obligationsBox.isSelected(), commoditesBox.isSelected(), reitsBox.isSelected()));
   sb.append("\n=================================================\n");
   sb.append("⚠ Ceci est un outil éducatif, pas un conseil\n");
   sb.append(" financier professionnel certifié.\n");
   sb.append("=================================================\n");
   return sb.toString();
 }
 private String buildSimulationReport(double montant, UserData user, int risqueLevel,
   boolean actionsSel, boolean etfSel, boolean cryptoSel,
   boolean obligationsSel, boolean commoditesSel, boolean reitsSel) {
   StringBuilder sb = new StringBuilder();
   sb.append("──── SIMULATION DE CROISSANCE ────\n\n");
   double montantInvesti = montant * user.investPercent / 100.0;
   ArrayList < String > assets = new ArrayList < > ();
   if (actionsSel) assets.add("Actions");
   if (etfSel) assets.add("ETF");
   if (cryptoSel) assets.add("Crypto");
   if (obligationsSel) assets.add("Obligations");
   if (commoditesSel) assets.add("Commodités");
   if (reitsSel) assets.add("REITs");
   if (assets.isEmpty()) {
     sb.append("Aucune classe d'actif sélectionnée pour la simulation.\n");
     return sb.toString();
   }
   double allocation = montantInvesti / assets.size();
   double totalYear1 = 0;
   double totalYear3 = 0;
   double totalYear5 = 0;
   for (String asset: assets) {
     double rate = getExpectedReturn(asset, risqueLevel);
     double value1 = compound(allocation, rate, 1);
     double value3 = compound(allocation, rate, 3);
     double value5 = compound(allocation, rate, 5);
     totalYear1 += value1;
     totalYear3 += value3;
     totalYear5 += value5;
     sb.append(String.format("%s : rendement attendu %.1f%%/an\n", asset, rate * 100));
     sb.append(String.format(" 1 an : %.2f $ | 3 ans : %.2f $ | 5 ans : %.2f $\n\n",
       value1, value3, value5));
   }
   sb.append(String.format("Projection globale investie :\n 1 an -> %.2f $\n 3 ans -> %.2f $\n 5 ans -> %.2f $\n\n",
     totalYear1, totalYear3, totalYear5));
   sb.append("Le simulateur utilise une approche réaliste basée sur le risque sélectionné et la diversification.\n");
   sb.append("Gardez à l'esprit que les chiffres sont des projections et non des garanties.\n");
   return sb.toString();
 }
 // switch sur String = choisit le taux selon le type d'actif (on l'a vu avec int au cours, String marche pareil)
 private double getExpectedReturn(String asset, int risqueLevel) {
   switch (asset) {
   case "Actions":
     return risqueLevel == 2 ? 0.12 : risqueLevel == 1 ? 0.08 : 0.05;
   case "ETF":
     return risqueLevel == 2 ? 0.10 : risqueLevel == 1 ? 0.07 : 0.05;
   case "Crypto":
     return risqueLevel == 2 ? 0.20 : risqueLevel == 1 ? 0.10 : 0.04;
   case "Obligations":
     return risqueLevel == 2 ? 0.05 : risqueLevel == 1 ? 0.04 : 0.03;
   case "Commodités":
     return risqueLevel == 2 ? 0.08 : risqueLevel == 1 ? 0.06 : 0.04;
   case "REITs":
     return risqueLevel == 2 ? 0.09 : risqueLevel == 1 ? 0.06 : 0.05;
   default:
     return 0.05;
   }
 }
 // Intérêts composés: argent * (1 + taux)^années — Math.pow fait l'exposant (pas dans le cours, on a floor/random)
 private double compound(double principal, double annualRate, int years) {
   return principal * Math.pow(1 + annualRate, years);
 }
 // Prépare l'affichage du rapport d'investissement (nom, description, allocation suggérée).
 private String formatLine(String ticker, String name, String description,
   double allocation, double base) {
   double amount = base * allocation;
   return String.format(
     " %-10s %s%n" +
     " %s%n" +
     " Allocation : %.0f%% | Montant : %.2f $%n%n",
     ticker, name, description, allocation * 100, amount);
 }
}
// Recommande le meilleur type de compte d'épargne canadien (CELI, REER, CELIAPP, etc.) selon la situation.
class PageEpargne extends JPanel {
 private JComboBox < String > boiteStatutProprietaire;
 private JComboBox < String > boiteEpargneEtudesEnfants;
 private JComboBox < String > boiteObjectifFinancierPrincipal;
 private JButton boutonCompteHisa;
 private JButton boutonCompteTfsa;
 private JButton boutonCompteRrsp;
 private JButton boutonCompteFhsa;
 private JButton boutonCompteResp;
 private JButton boutonCompteRdsp;
 private JButton boutonGenererPlan;
 private JLabel etiquetteCompteRecommande;
 private JLabel etiquetteExplication;
 private JTextArea zonePlanDetaille;
 private final Color COULEUR_SURBRILLANCE = new Color(50, 200, 50);
 private final Color COULEUR_BOUTON_NORMAL = new Color(240, 240, 240); // Gris clair par défaut.
 private MainFrame fenetrePrincipale;
 private String compteRecommande = null;
 public PageEpargne(MainFrame frame) {
   this.fenetrePrincipale = frame;
   setLayout(new BorderLayout());
   JPanel panneauHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
   panneauHaut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
   JButton boutonRetourAccueil = new JButton("←");
   boutonRetourAccueil.setFont(new Font("Arial", Font.BOLD, 20));
   boutonRetourAccueil.setPreferredSize(new Dimension(50, 35));
   boutonRetourAccueil.setToolTipText("Retour à l'accueil");
   boutonRetourAccueil.addActionListener(e -> fenetrePrincipale.retourAccueil());
   panneauHaut.add(boutonRetourAccueil);
   JLabel titrePage = new JLabel("Plan d'épargne personnalisé");
   titrePage.setFont(new Font("Arial", Font.BOLD, 16));
   panneauHaut.add(titrePage);
   add(panneauHaut, BorderLayout.NORTH);
   // ==================== PANNEAU PRINCIPAL ====================
   JPanel panneauPrincipal = new JPanel();
   // BoxLayout Y_AXIS = empile les panneaux verticalement un sous l'autre (comme une colonne flex)
   panneauPrincipal.setLayout(new BoxLayout(panneauPrincipal, BoxLayout.Y_AXIS));
   panneauPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
   // ==================== SECTION QUESTIONS (plus compacte) ====================
   JPanel panneauQuestions = new JPanel();
   panneauQuestions.setLayout(new GridLayout(3, 2, 8, 8));
   panneauQuestions.setBorder(BorderFactory.createTitledBorder("Votre situation"));
   // Integer.MAX_VALUE = largeur "infinie" mais hauteur max 110 — trick pour que BoxLayout respecte la taille
   panneauQuestions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
   // Question 1
   JLabel etiquetteProprietaire = new JLabel("Propriétaire d'une maison ? :");
   etiquetteProprietaire.setFont(new Font("Arial", Font.PLAIN, 11));
   String[] optionsProprietaire = {
     "Non",
     "Oui",
     "Je loue"
   };
   boiteStatutProprietaire = new JComboBox < > (optionsProprietaire);
   boiteStatutProprietaire.setFont(new Font("Arial", Font.PLAIN, 11));
   panneauQuestions.add(etiquetteProprietaire);
   panneauQuestions.add(boiteStatutProprietaire);
   JLabel etiquetteEtudesEnfants = new JLabel("Épargne pour études d'enfant ? :"); etiquetteEtudesEnfants.setFont(new Font("Arial", Font.PLAIN, 11));
   String[] optionsEtudes = {
     "Non",
     "Oui, un enfant",
     "Oui, plusieurs"
   };
   boiteEpargneEtudesEnfants = new JComboBox < > (optionsEtudes);
   boiteEpargneEtudesEnfants.setFont(new Font("Arial", Font.PLAIN, 11));
   panneauQuestions.add(etiquetteEtudesEnfants);
   panneauQuestions.add(boiteEpargneEtudesEnfants);
   JLabel etiquetteObjectif = new JLabel("Objectif financier principal :");
   etiquetteObjectif.setFont(new Font("Arial", Font.PLAIN, 11));
   String[] optionsObjectif = {
     "Fonds d'urgence",
     "Acheter maison",
     "Retraite",
     "Croître richesse",
     "Études enfants",
     "Revenu complémentaire"
   };
   boiteObjectifFinancierPrincipal = new JComboBox < > (optionsObjectif);
   boiteObjectifFinancierPrincipal.setFont(new Font("Arial", Font.PLAIN, 11));
   panneauQuestions.add(etiquetteObjectif);
   panneauQuestions.add(boiteObjectifFinancierPrincipal);
   panneauPrincipal.add(panneauQuestions);
   panneauPrincipal.add(Box.createRigidArea(new Dimension(0, 8))); // espace vide fixe de 8px entre sections
   JPanel panneauComptes = new JPanel();
   panneauComptes.setLayout(new GridLayout(2, 3, 8, 8));
   panneauComptes.setBorder(BorderFactory.createTitledBorder("Comptes disponibles"));
   panneauComptes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
   boutonCompteHisa = creerBoutonCompte("HISA", "Intérêt élevé");
   boutonCompteTfsa = creerBoutonCompte("TFSA", "Libre d'impôt");
   boutonCompteRrsp = creerBoutonCompte("RRSP", "Retraite");
   boutonCompteFhsa = creerBoutonCompte("FHSA", "Première maison");
   boutonCompteResp = creerBoutonCompte("RESP", "Études");
   boutonCompteRdsp = creerBoutonCompte("RDSP", "Invalidité");
  
   panneauComptes.add(boutonCompteHisa);
   panneauComptes.add(boutonCompteTfsa);
   panneauComptes.add(boutonCompteRrsp);
   panneauComptes.add(boutonCompteFhsa);
   panneauComptes.add(boutonCompteResp);
   panneauComptes.add(boutonCompteRdsp);
   panneauPrincipal.add(panneauComptes);
   panneauPrincipal.add(Box.createRigidArea(new Dimension(0, 8))); // espace vide fixe de 8px entre sections
   JPanel panneauRecommandation = new JPanel();
   panneauRecommandation.setLayout(new BoxLayout(panneauRecommandation, BoxLayout.Y_AXIS));
   panneauRecommandation.setBorder(BorderFactory.createTitledBorder("Recommandation"));
   panneauRecommandation.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
   etiquetteCompteRecommande = new JLabel("Sélectionnez vos options");
   etiquetteCompteRecommande.setFont(new Font("Arial", Font.BOLD, 12));
   etiquetteCompteRecommande.setAlignmentX(Component.CENTER_ALIGNMENT); // centre le label dans un BoxLayout
   etiquetteExplication = new JLabel(" ");
   etiquetteExplication.setFont(new Font("Arial", Font.PLAIN, 10));
   etiquetteExplication.setAlignmentX(Component.CENTER_ALIGNMENT);
   panneauRecommandation.add(etiquetteCompteRecommande);
   panneauRecommandation.add(Box.createRigidArea(new Dimension(0, 5)));
   panneauRecommandation.add(etiquetteExplication);
   panneauPrincipal.add(panneauRecommandation);
   panneauPrincipal.add(Box.createRigidArea(new Dimension(0, 8))); // espace vide fixe de 8px entre sections
   JPanel panneauBoutonGenerer = new JPanel(new FlowLayout(FlowLayout.CENTER));
   panneauBoutonGenerer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
   boutonGenererPlan = new JButton("Générer le plan");
   boutonGenererPlan.setFont(new Font("Arial", Font.BOLD, 12));
   boutonGenererPlan.setBackground(new Color(50, 100, 200));
   boutonGenererPlan.setForeground(Color.BLACK);
   boutonGenererPlan.setPreferredSize(new Dimension(180, 32));
   boutonGenererPlan.setFocusPainted(false);
   panneauBoutonGenerer.add(boutonGenererPlan);
   panneauPrincipal.add(panneauBoutonGenerer);
   panneauPrincipal.add(Box.createRigidArea(new Dimension(0, 8))); // espace vide fixe de 8px entre sections
  
   JPanel panneauPlanDetaille = new JPanel(); // Zone texte finale du plan.
   panneauPlanDetaille.setLayout(new BorderLayout());
   panneauPlanDetaille.setBorder(BorderFactory.createTitledBorder("Votre plan"));
   zonePlanDetaille = new JTextArea(8, 40);
   zonePlanDetaille.setEditable(false);
   zonePlanDetaille.setFont(new Font("Monospaced", Font.PLAIN, 10));
   zonePlanDetaille.setMargin(new Insets(8, 8, 8, 8));
   zonePlanDetaille.setText("Cliquez sur 'Générer le plan' pour voir votre plan d'épargne...");
   zonePlanDetaille.setLineWrap(true);
   zonePlanDetaille.setWrapStyleWord(true);
   JScrollPane defilementPlan = new JScrollPane(zonePlanDetaille);
   defilementPlan.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
   defilementPlan.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
   JScrollBar barreVerticale = defilementPlan.getVerticalScrollBar();
   barreVerticale.setUnitIncrement(20);
   panneauPlanDetaille.add(defilementPlan, BorderLayout.CENTER);
   panneauPlanDetaille.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
   panneauPrincipal.add(panneauPlanDetaille);
  
   JScrollPane defilementPrincipal = new JScrollPane(panneauPrincipal);
   defilementPrincipal.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
   defilementPrincipal.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
   defilementPrincipal.getVerticalScrollBar().setUnitIncrement(20);
   add(defilementPrincipal, BorderLayout.CENTER);
   boiteStatutProprietaire.addActionListener(e -> mettreAJourRecommandation());
   boiteEpargneEtudesEnfants.addActionListener(e -> mettreAJourRecommandation());
   boiteObjectifFinancierPrincipal.addActionListener(e -> mettreAJourRecommandation());
   boutonGenererPlan.addActionListener(e -> genererPlanDetaille());
   mettreAJourRecommandation();
 }
 private JButton creerBoutonCompte(String nomCourt, String description) {
   JButton bouton = new JButton("<html><center>" + nomCourt + "<br><font size='1'>" + description + "</font></center></html>");
   bouton.setFont(new Font("Arial", Font.PLAIN, 10));
   bouton.setPreferredSize(new Dimension(100, 50)); bouton.setBackground(COULEUR_BOUTON_NORMAL);
   bouton.setToolTipText(description); bouton.setFocusPainted(false);
   return bouton;
 }
 // Colore le bouton du compte suggéré en vert pour attirer l'oeil.
 private void mettreCompteEnSurbrillance(String compte) {
   JButton[] bts = {boutonCompteHisa, boutonCompteTfsa, boutonCompteRrsp, boutonCompteFhsa, boutonCompteResp, boutonCompteRdsp};
   for (JButton b : bts) b.setBackground(COULEUR_BOUTON_NORMAL);
   if (compte != null) {
     // switch pour colorer le bon bouton en vert selon le compte recommandé
   switch (compte) {
     case "HISA": boutonCompteHisa.setBackground(COULEUR_SURBRILLANCE); break;
     case "TFSA": boutonCompteTfsa.setBackground(COULEUR_SURBRILLANCE); break;
     case "RRSP": boutonCompteRrsp.setBackground(COULEUR_SURBRILLANCE); break;
     case "FHSA": boutonCompteFhsa.setBackground(COULEUR_SURBRILLANCE); break;
     case "RESP": boutonCompteResp.setBackground(COULEUR_SURBRILLANCE); break;
     case "RDSP": boutonCompteRdsp.setBackground(COULEUR_SURBRILLANCE); break;
     }
   }
 }
 // Logique de décision pour recommander le meilleur compte canadien selon les besoins de l'utilisateur.
 private void mettreAJourRecommandation() {
   String statutProprietaire = (String) boiteStatutProprietaire.getSelectedItem();
   String epargneEtudes = (String) boiteEpargneEtudesEnfants.getSelectedItem();
   String objectifPrincipal = (String) boiteObjectifFinancierPrincipal.getSelectedItem();
   String nomUtilisateur = fenetrePrincipale.loggedInUsername;
   UserData donneesUtilisateur = AuthManager.getUserProfile(nomUtilisateur);
   int ageUtilisateur = (donneesUtilisateur != null) ? donneesUtilisateur.age : 30;
   String occupationUtilisateur = (donneesUtilisateur != null) ? donneesUtilisateur.occupation : "Temps plein";
   String compteRecommandeTemp = null;
   String explicationTemp = "";
   if (objectifPrincipal.equals("Acheter maison") && !statutProprietaire.equals("Oui")) {
     compteRecommandeTemp = "FHSA";
     explicationTemp = "Le FHSA est parfait pour l'achat d'une première maison. Cotisation max: 8 000$/an.";
   } else if (!epargneEtudes.equals("Non")) {
     compteRecommandeTemp = "RESP";
     explicationTemp = "Le RESP offre 20% de subvention gouvernementale pour les études.";
   } else if (objectifPrincipal.equals("Retraite") && (ageUtilisateur >= 45 || occupationUtilisateur.equals("Temps plein"))) {
     compteRecommandeTemp = "RRSP";
     explicationTemp = "Le RRSP réduit votre revenu imposable pour la retraite.";
   } else if (ageUtilisateur < 40 && (objectifPrincipal.equals("Croître richesse") || objectifPrincipal.equals("Fonds d'urgence"))) {
     compteRecommandeTemp = "TFSA";
     explicationTemp = "Le TFSA permet une croissance totalement libre d'impôt.";
   } else if (objectifPrincipal.equals("Revenu complémentaire") && ageUtilisateur < 50) {
     compteRecommandeTemp = "RDSP";
     explicationTemp = "Le RDSP offre des subventions généreuses (jusqu'à 300%).";
   } else {
     compteRecommandeTemp = "HISA";
     explicationTemp = "Le HISA est sécuritaire et accessible en tout temps.";
   }
   compteRecommande = compteRecommandeTemp;
   etiquetteCompteRecommande.setText("Compte recommandé : " + compteRecommande);
   etiquetteExplication.setText("<html>" + explicationTemp + "</html>");
   mettreCompteEnSurbrillance(compteRecommande);
 }
 private void genererPlanDetaille() {
   String nomUtilisateur = fenetrePrincipale.loggedInUsername;
   if (nomUtilisateur == null) {
     zonePlanDetaille.setText("Veuillez vous connecter pour générer un plan d'épargne.");
     return;
   }
   UserData donneesUtilisateur = AuthManager.getUserProfile(nomUtilisateur);
   if (donneesUtilisateur == null) {
     zonePlanDetaille.setText("Profil utilisateur introuvable.");
     return;
   }
   ArrayList < Goal > listeObjectifs = donneesUtilisateur.goals;
   String objectifPrincipal = (String) boiteObjectifFinancierPrincipal.getSelectedItem();
   StringBuilder plan = new StringBuilder();
   plan.append("========== PLAN D'EPARGNE PERSONNALISE ==========\n\n");
   plan.append("PROFIL UTILISATEUR\n");
   plan.append("Age: ").append(donneesUtilisateur.age).append(" ans\n");
   plan.append("Occupation: ").append(donneesUtilisateur.occupation).append("\n");
   plan.append("Objectif: ").append(objectifPrincipal).append("\n");
   plan.append("Proprietaire: ").append(boiteStatutProprietaire.getSelectedItem()).append("\n\n");
   plan.append(">>> COMPTE RECOMMANDE: ").append(compteRecommande).append(" <<<\n\n");
  
   if (!listeObjectifs.isEmpty()) {
     plan.append("VOS OBJECTIFS\n");
     for (Goal objectif: listeObjectifs) {
       int moisRequis = objectif.getMonthsNeeded();
       int anneesRequis = moisRequis / 12;
       int moisRestants = moisRequis % 12; // % = modulo (reste de la division) — ex: 14 mois = 1 an et 2 mois
       String temps = "";
       if (anneesRequis > 0) {
         temps += anneesRequis + " an" + (anneesRequis > 1 ? "s" : "");
       }
       if (anneesRequis > 0 && moisRestants > 0) {
         temps += " et ";
       }
       if (moisRestants > 0 || anneesRequis == 0) {
         temps += moisRestants + " mois";
       }
       plan.append("- ").append(objectif.name).append(": ");
       plan.append(String.format("%.0f", objectif.totalAmount)).append("$ (");
       plan.append(String.format("%.0f", objectif.monthlySavings)).append("$/mois) - ");
       plan.append(temps).append("\n");
     }
     plan.append("\n");
   } else {
     plan.append("ASTUCE: Allez dans 'Mes Objectifs' pour creer votre premier objectif!\n\n");
   }
   plan.append("CONSEILS\n");
   if (donneesUtilisateur.age < 30) {
     plan.append("- Commencez tot pour profiter des interets composes\n");
     plan.append("- Priorisez le TFSA pour la flexibilite\n");
   } else if (donneesUtilisateur.age < 50) {
     plan.append("- Equilibre entre croissance et securite\n");
     plan.append("- Considerez le RRSP pour reduire votre revenu imposable\n");
   } else {
     plan.append("- Privilegiez la preservation du capital\n");
     plan.append("- Le CELI est avantageux pour les retraits libres d'impot\n");
   }
   plan.append("\n==================================================\n");
   plan.append("Outil educatif - Consultez un conseiller financier\n");
   plan.append("==================================================");
   zonePlanDetaille.setText(plan.toString());
   zonePlanDetaille.setCaretPosition(0); // scroll le texte tout en haut quand on génère un nouveau plan
 }
}
// Page admin — liste des users + suppression (réservé aux comptes isAdmin)
class PageAdmin extends JPanel {
 private DefaultListModel < String > userListModel;
 private JList < String > userList;
 private MainFrame frame;
 private JButton barbieriResetBtn;
 public PageAdmin(MainFrame frame) {
   this.frame = frame;
   setBackground(Theme.BG);
   setLayout(new BorderLayout());
   JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
   topBar.setBackground(Theme.BG);
   JButton back = new JButton("<");
   back.setFont(new Font("Arial", Font.BOLD, 25));
   back.setPreferredSize(new Dimension(60, 40));
   back.addActionListener(e -> frame.retourAccueil());
   topBar.add(back);
   add(topBar, BorderLayout.NORTH);
  
   // JList = liste cliquable à gauche (comme un explorateur de fichiers). DefaultListModel = les données derrière
   // addElement/clear sur le model = la liste à l'écran se met à jour automatiquement
   userListModel = new DefaultListModel < > ();
   userList = new JList < > (userListModel);
   add(new JScrollPane(userList), BorderLayout.CENTER);
   JPanel btnPanel = new JPanel(new GridLayout(1, 0, 10, 10));
   JButton deleteBtn = new JButton("Supprimer le compte sélectionné");
   deleteBtn.addActionListener(e -> {
     String selected = userList.getSelectedValue();
     if (selected != null) {
       // split(" \\(") = coupe la string au premier " (" pour avoir juste le username (avant les infos entre parenthèses)
       String username = selected.split(" \\(")[0];
       // Sécurité pour éviter que l'admin ne se supprime lui-même.
       if (AuthManager.isAdmin(username)) {
         JOptionPane.showMessageDialog(this, "Impossible de supprimer un administrateur.", "Erreur",
           JOptionPane.ERROR_MESSAGE);
         return;
       }
       int confirm = JOptionPane.showConfirmDialog(this,
         "Voulez-vous vraiment supprimer le compte " + username + " ?",
         "Confirmation", JOptionPane.YES_NO_OPTION);
       if (confirm == JOptionPane.YES_OPTION) {
         try {
           AuthManager.deleteAccount(username);
           if (username.equals(frame.loggedInUsername)) frame.logout();
           refreshUserList();
         } catch (IllegalArgumentException ex) {
           // ex.getMessage() = le texte qu'on a mis dans le throw dans deleteAccount()
           JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
         }
       }
     } else {
       JOptionPane.showMessageDialog(this, "Veuillez sélectionner un compte à supprimer.");
     }
   });
   JButton wipeBtn = new JButton("Réinitialiser tous les comptes");
   wipeBtn.setBackground(Theme.DANGER);
   wipeBtn.setForeground(Color.WHITE);
   wipeBtn.addActionListener(e -> {
     int confirm = JOptionPane.showConfirmDialog(this,
       "Êtes-vous absolument certain de vouloir supprimer TOUS les comptes non-administrateurs ?\nCette action est irréversible.",
       "Réinitialisation totale", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
     if (confirm == JOptionPane.YES_OPTION) {
       AuthManager.wipeAllUserData();
       if (frame.loggedInUsername != null && !AuthManager.getAllUsernames().contains(frame.loggedInUsername))
         frame.logout();
       refreshUserList();
       JOptionPane.showMessageDialog(this, "Tous les comptes non-administrateurs ont été supprimés.");
     }
   });
   barbieriResetBtn = new JButton("Réinitialisation Totale (Barbieri)");
   barbieriResetBtn.setBackground(Color.BLACK);
   barbieriResetBtn.setForeground(Color.WHITE);
   barbieriResetBtn.setVisible(false);
   barbieriResetBtn.addActionListener(e -> {
     int confirm = JOptionPane.showConfirmDialog(this,
       "ACTION CRITIQUE : Voulez-vous supprimer TOUS les comptes et réinitialiser les profils administrateurs ?\n" +
       "Cette action est irréversible.",
       "Hard Reset", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
     if (confirm == JOptionPane.YES_OPTION) {
       AuthManager.hardReset();
       frame.logout();
       JOptionPane.showMessageDialog(null, "Le système a été réinitialisé à son état d'usine.");
     }
   });
   btnPanel.add(deleteBtn);
   btnPanel.add(wipeBtn);
   btnPanel.add(barbieriResetBtn);
   add(btnPanel, BorderLayout.SOUTH);
   refreshUserList();
 }
 // Recharge la liste des utilisateurs depuis le fichier/mémoire.
 public void refreshUserList() {
   // Le bouton Barbieri n'apparaît que pour l'admin "barbieri".
   if (barbieriResetBtn != null && frame.loggedInUsername != null) {
     barbieriResetBtn.setVisible(frame.loggedInUsername.equals("barbieri"));
   }
   userListModel.clear();
   for (String u: AuthManager.getNonAdminUsernames()) {
     UserData data = AuthManager.getUserProfile(u);
     String entry = u;
     if (data != null) {
       entry += " (" + data.displayName + ", " + data.age + " ans, " +
         data.investPercent + "% invest / " + (100 - data.investPercent) + "% épargne, " +
         data.occupation + ")";
     }
     userListModel.addElement(entry);
   }
 }
}

