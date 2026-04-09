import javax.swing.*;
import java.awt.*;

public class MushroomGUI extends JFrame {
    private NaiveBayesClassifier classifier;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public MushroomGUI(NaiveBayesClassifier classifier) {
        this.classifier = classifier;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Mushroom Classifier");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Création du CardLayout pour gérer les différentes vues
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 1. Page d'accueil
        JPanel homePanel = createHomePanel();
        mainPanel.add(homePanel, "home");

        // 2. Formulaire de classification
        JPanel formPanel = new ModernMushroomForm(classifier, this).getMainPanel();
        mainPanel.add(formPanel, "form");

        add(mainPanel);
    }

    private JPanel createHomePanel() {
        BackgroundPanel homePanel = new BackgroundPanel("bg.png");
        homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Edible or Poisonous? Know before you pick!");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Mushroom Classification System");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new JButton("Start Classification");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        startButton.setBackground(new Color(50, 50, 50));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(250, 50));
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        startButton.addActionListener(e -> cardLayout.show(mainPanel, "form"));

        homePanel.add(Box.createVerticalGlue());
        homePanel.add(titleLabel);
        homePanel.add(Box.createVerticalStrut(10));
        homePanel.add(subtitleLabel);
        homePanel.add(Box.createVerticalStrut(20));
        homePanel.add(startButton);
        homePanel.add(Box.createVerticalGlue());

        return homePanel;
    }

    public void showHome() {
        cardLayout.show(mainPanel, "home");
    }

    static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String fileName) {
            try {
                backgroundImage = new ImageIcon(fileName).getImage();
            } catch (Exception e) {
                System.err.println("Background image not found.");
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null)
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}