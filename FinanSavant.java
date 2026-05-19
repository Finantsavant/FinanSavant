import javax.swing.*;

// Classe principale qui démarre le programme
public class FinanSavant {
  public static void main(String[] args) {

    // Exécute la fenêtre dans le thread graphique de Swing (EDT) afin d’éviter des problèmes d’affichage et de synchronisation.
    // Source : https://www.reddit.com/r/learnprogramming/comments/29ik8n/java_can_someone_explain/
    SwingUtilities.invokeLater(() -> new FenetrePrincipale());
  }
}
