import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class ClassificationResultDialog extends JDialog {
    // Ajout d'un tableau de noms d'attributs plus descriptifs
    private static final String[] DESCRIPTIVE_ATTRIBUTE_NAMES = {
            "Odeur",
            "Couleur des spores",
            "Couleur des lamelles",
            "Meurtrissures",
            "Type d'anneau",
            "Population",
            "Habitat"
    };

    public ClassificationResultDialog(JFrame parent, String predictedClass,
                                      double[] normalizedProbabilities, String[] classLabels,
                                      String[] attributeNames, String[] attributeValues,
                                      NaiveBayesClassifier classifier) {
        super(parent, "Résultats de Classification", true);
        setSize(1200, 800);
        setLocationRelativeTo(parent);
        setModal(true);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Panel de prédiction
        mainPanel.add(createPredictionPanel(predictedClass), BorderLayout.NORTH);
        double[] normalized = classifier.getNormalizedProbabilities(attributeValues);
        String[] rawFormatted = classifier.getClassProbabilitiesRawFormatted(attributeValues);
        // 2. Panel principal avec onglets
        JTabbedPane tabbedPane = new JTabbedPane();
        double[] rawProbabilities = classifier.getClassProbabilitiesRaw(attributeValues);
        tabbedPane.addTab("Résultats", createResultsPanel(normalized, rawFormatted, classifier.getClassLabels()));
        tabbedPane.addTab("Calculs", createCalculationsPanel(classifier, classLabels, attributeNames, attributeValues));
        tabbedPane.addTab("Probabilités", createProbabilitiesPanel(classifier, classLabels, attributeNames, attributeValues));

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createResultsPanel(double[] normalizedProbabilities,
                                      String[] rawProbabilitiesFormatted,
                                      String[] classLabels) {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel();

        model.addColumn("Classe");
        model.addColumn("Probabilité brute (10 décimales)");
        model.addColumn("Probabilité normalisée");

        // Format pour les probabilités normalisées
        DecimalFormat normalizedFormat = new DecimalFormat("0.##########");

        for (int i = 0; i < classLabels.length; i++) {
            model.addRow(new Object[]{
                    classLabels[i],
                    rawProbabilitiesFormatted[i],  // Affiche avec 10 décimales exactes
                    normalizedFormat.format(normalizedProbabilities[i])
            });
        }

        JTable table = new JTable(model);
        styleTable(table);

        // Ajustement de la largeur des colonnes
        table.getColumnModel().getColumn(1).setPreferredWidth(200);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCalculationsPanel(NaiveBayesClassifier classifier,
                                           String[] classLabels,
                                           String[] attributeNames,
                                           String[] attributeValues) {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);

        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.#####");

        for (String classLabel : classLabels) {
            sb.append("=== ").append(classLabel).append(" ===\n");
            sb.append("P(").append(classLabel).append(") = ")
                    .append(df.format(classifier.getPriorProbability(classLabel))).append("\n");

            sb.append("P(X|").append(classLabel).append(") = ");
            for (int i = 0; i < attributeNames.length; i++) {
                String descriptiveName = (i < DESCRIPTIVE_ATTRIBUTE_NAMES.length) ?
                        DESCRIPTIVE_ATTRIBUTE_NAMES[i] : attributeNames[i];
                sb.append("P(").append(descriptiveName).append("=")
                        .append(attributeValues[i]).append("|").append(classLabel).append(")");
                if (i < attributeNames.length - 1) sb.append(" × ");
            }
            sb.append("\n= ");

            for (int i = 0; i < attributeNames.length; i++) {
                sb.append(df.format(classifier.getConditionalProbability(i, attributeValues[i], classLabel)));
                if (i < attributeNames.length - 1) sb.append(" × ");
            }
            sb.append("\n\n");
        }

        textArea.setText(sb.toString());
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProbabilitiesPanel(NaiveBayesClassifier classifier,
                                            String[] classLabels,
                                            String[] attributeNames,
                                            String[] attributeValues) {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel();

        model.addColumn("Attribut");
        model.addColumn("Valeur");
        for (String label : classLabels) {
            model.addColumn("P(attribut|" + label + ")");
        }

        for (int i = 0; i < attributeNames.length; i++) {
            Object[] row = new Object[classLabels.length + 2];
            // Utilisation du nom descriptif si disponible
            String descriptiveName = (i < DESCRIPTIVE_ATTRIBUTE_NAMES.length) ?
                    DESCRIPTIVE_ATTRIBUTE_NAMES[i] : attributeNames[i];
            row[0] = descriptiveName;
            row[1] = attributeValues[i];

            for (int j = 0; j < classLabels.length; j++) {
                row[j + 2] = formatProbability(classifier.getConditionalProbability(i, attributeValues[i], classLabels[j]));
            }
            model.addRow(row);
        }

        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        JButton closeButton = new JButton("Fermer");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);
        return panel;
    }

    private JPanel createPredictionPanel(String predictedClass) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Détails de Classification", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel prediction = new JLabel("Classe prédite: " + predictedClass, JLabel.CENTER);
        prediction.setFont(new Font("SansSerif", Font.BOLD, 24));
        prediction.setForeground(predictedClass.equals("toxique") ? Color.RED : new Color(0, 128, 0));

        panel.add(title, BorderLayout.NORTH);
        panel.add(prediction, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private String formatProbability(double prob) {
        DecimalFormat df = new DecimalFormat(prob < 0.0001 ? "0.##########" : "0.#####");
        return df.format(prob);
    }
}