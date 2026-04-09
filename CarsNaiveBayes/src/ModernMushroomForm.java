import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;

public class ModernMushroomForm {
    private final NaiveBayesClassifier classifier;
    private JComboBox<String>[] comboBoxes;
    private JPanel mainPanel;
    private MushroomGUI parent;

    public ModernMushroomForm(NaiveBayesClassifier classifier, MushroomGUI parent) {
        this.classifier = classifier;
        this.parent = parent;
        initializeUI();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 250, 240));

        // Création des composants
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFormPanel(), BorderLayout.WEST);
        mainPanel.add(createArrowPanel(), BorderLayout.CENTER);
        mainPanel.add(createImagePanel(), BorderLayout.EAST);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 250, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 20, 40)); // Augmentation des marges

        JButton backButton = new JButton("← Retour");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Taille augmentée
        backButton.addActionListener(e -> parent.showHome());

        JLabel title = new JLabel("Caractéristiques du champignon", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Taille augmentée (20 → 24)
        title.setForeground(Color.DARK_GRAY);

        panel.add(backButton, BorderLayout.WEST);
        panel.add(title, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 250, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 20)); // Marges augmentées

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15); // Espacements augmentés
        gbc.fill = GridBagConstraints.HORIZONTAL;

        LinkedHashMap<String, String[]> attributes = new LinkedHashMap<>();
        attributes.put("Odeur", classifier.getAttributeValues(0));
        attributes.put("couleur_spore", classifier.getAttributeValues(1));
        attributes.put("couleur_lamelles", classifier.getAttributeValues(2));
        attributes.put("meurtrissures", classifier.getAttributeValues(3));
        attributes.put("type_anneau", classifier.getAttributeValues(4));
        attributes.put("Population", classifier.getAttributeValues(5));
        attributes.put("Habitat", classifier.getAttributeValues(6));

        comboBoxes = new JComboBox[attributes.size()];
        int row = 0;

        for (String label : attributes.keySet()) {
            gbc.gridx = 0;
            gbc.gridy = row;
            JLabel attributeLabel = new JLabel("  " + label + ":");
            attributeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Taille augmentée
            panel.add(attributeLabel, gbc);

            gbc.gridx = 1;
            comboBoxes[row] = new JComboBox<>(attributes.get(label));
            comboBoxes[row].setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Taille augmentée
            comboBoxes[row].setPreferredSize(new Dimension(200, 35)); // Taille des combobox augmentée
            panel.add(comboBoxes[row], gbc);
            row++;
        }

        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        panel.add(createSubmitButton(), gbc);

        return panel;
    }

    private JButton createSubmitButton() {
        JButton button = new JButton("Analyser");
        button.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Taille augmentée (14 → 16)
        button.setBackground(new Color(33, 150, 243));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(250, 50)); // Taille augmentée (200,40 → 250,50)

        button.addActionListener(event -> handleSubmit());
        return button;
    }

    private void handleSubmit() {
        String[] attributeValues = new String[comboBoxes.length];
        for (int i = 0; i < comboBoxes.length; i++) {
            attributeValues[i] = (String) comboBoxes[i].getSelectedItem();
        }

        String predictedClass = classifier.predict(attributeValues);
        double[] rawProbabilities = classifier.getClassProbabilitiesRaw(attributeValues);
        double[] normalizedProbabilities = classifier.getNormalizedProbabilities(attributeValues);

        JDialog resultDialog = new ClassificationResultDialog(
                null, // Pas de parent car c'est une JDialog
                predictedClass,
                normalizedProbabilities,
                classifier.getClassLabels(),
                classifier.getAttributeNames(),
                attributeValues,
                classifier
        );
        resultDialog.setVisible(true);
    }


    private JPanel createArrowPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 250, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        ImageIcon icon = new ImageIcon("fl.png");
        Image scaledImage = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        panel.add(new JLabel(new ImageIcon(scaledImage)));

        return panel;
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 250, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 30));

        ImageIcon icon = new ImageIcon("mush.png");
        Image scaledImage = icon.getImage().getScaledInstance(450, 500, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(scaledImage));
        label.setHorizontalAlignment(JLabel.CENTER);

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}