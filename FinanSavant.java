import javax.swing.*;

/** Point d'entrée de l'application FinanSavant. */
public class FinanSavant {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new FenetrePrincipale());
  }
}
