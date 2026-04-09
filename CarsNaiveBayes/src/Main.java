import javax.swing.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // 1. Préparation des données
        DatasetPrep dataPrep = new DatasetPrep(0.8);
        dataPrep.prepareDataset("mushrooms_dst.csv");
        // 2. Détection automatique des attributs et classes
        String[] attributeNames = detectAttributeNames(dataPrep);
        String[] classLabels = detectClassLabels(dataPrep);
        System.out.println("Attributs détectés (" + attributeNames.length + "):");
        for (String attr : attributeNames) {
            System.out.println("- " + attr);
        }
        System.out.println("\nClasses détectées (" + classLabels.length + "):");
        for (String label : classLabels) {
            System.out.println("- " + label);
        }
        // 3. Conversion des données
        List<NaiveBayesClassifier.Instance> trainingInstances = convertToInstances(dataPrep.getTrainingSet());
        // 4. Initialisation du classifieur
        NaiveBayesClassifier classifier = new NaiveBayesClassifier(attributeNames, classLabels);
        classifier.train(trainingInstances.toArray(new NaiveBayesClassifier.Instance[0]));
        // 5. Évaluation
        evaluateClassifier(dataPrep, classifier, classLabels);
        // 6. Interface graphique
        SwingUtilities.invokeLater(() -> {
            new MushroomGUI(classifier).setVisible(true);
        });
        // 7. Prédiction manuelle
        manualPrediction(classifier, attributeNames);
    }
    private static String[] detectAttributeNames(DatasetPrep dataPrep) {
        if (!dataPrep.getTrainingSet().isEmpty()) {
            int numAttributes = dataPrep.getTrainingSet().get(0).attributes.length;
            String[] attributeNames = new String[numAttributes];

            for (int i = 0; i < numAttributes; i++) {
                attributeNames[i] = "attr_" + (i + 1);
            }
            return attributeNames;
        }
        return new String[0];
    }

    private static String[] detectClassLabels(DatasetPrep dataPrep) {
        List<String> uniqueLabels = new ArrayList<>();

        for (DatasetPrep.DataInstance instance : dataPrep.getFullDataset()) {
            String label = instance.label;
            if (!contains(uniqueLabels, label)) {
                uniqueLabels.add(label);
            }
        }

        return uniqueLabels.toArray(new String[0]);
    }

    private static List<NaiveBayesClassifier.Instance> convertToInstances(List<DatasetPrep.DataInstance> dataInstances) {
        List<NaiveBayesClassifier.Instance> instances = new ArrayList<>();
        for (DatasetPrep.DataInstance di : dataInstances) {
            instances.add(new NaiveBayesClassifier.Instance(di.attributes, di.label));
        }
        return instances;
    }

    private static void evaluateClassifier(DatasetPrep dataPrep, NaiveBayesClassifier classifier, String[] classLabels) {
        int correct = 0;
        int[][] confusionMatrix = new int[classLabels.length][classLabels.length];

        for (DatasetPrep.DataInstance testInstance : dataPrep.getTestSet()) {
            String predicted = classifier.predict(testInstance.attributes);
            if (predicted.equals(testInstance.label)) correct++;

            int actualIndex = findIndex(classLabels, testInstance.label);
            int predictedIndex = findIndex(classLabels, predicted);
            confusionMatrix[actualIndex][predictedIndex]++;
        }

        double accuracy = (double) correct / dataPrep.getTestSet().size();
        System.out.printf("\nPrécision: %.2f%%\n", accuracy * 100);

        System.out.println("\nMatrice de confusion:");
        System.out.print("Actual\\Predicted\t");
        for (String label : classLabels) System.out.print(label + "\t");
        System.out.println();

        for (int i = 0; i < classLabels.length; i++) {
            System.out.print(classLabels[i] + "\t\t\t");
            for (int j = 0; j < classLabels.length; j++) {
                System.out.print(confusionMatrix[i][j] + "\t");
            }
            System.out.println();
        }
    }

    private static void manualPrediction(NaiveBayesClassifier classifier, String[] attributeNames) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== Nouvelle prédiction ===");

        String[] newInstance = new String[attributeNames.length];
        for (int i = 0; i < attributeNames.length; i++) {
            String[] validValues = classifier.getAttributeValues(i);
            System.out.print(attributeNames[i] + " (" + String.join(", ", validValues) + "): ");
            newInstance[i] = scanner.nextLine().trim().toLowerCase();

            while (!contains(validValues, newInstance[i])) {
                System.out.println("Valeur invalide. Valeurs acceptées: " + String.join(", ", validValues));
                System.out.print(attributeNames[i] + ": ");
                newInstance[i] = scanner.nextLine().trim().toLowerCase();
            }
        }

        String prediction = classifier.predict(newInstance);
        double[] normalizedProbs = classifier.getNormalizedProbabilities(newInstance);

        System.out.println("\nProbabilités normalisées:");
        for (int i = 0; i < classifier.getClassLabels().length; i++) {
            System.out.printf("- %s: %.6f (%.2f%%)\n",
                    classifier.getClassLabels()[i],
                    normalizedProbs[i],
                    normalizedProbs[i] * 100);
        }

        System.out.println("\nClasse prédite: " + prediction);
        scanner.close();
    }

    private static int findIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return -1;
    }

    private static boolean contains(List<String> list, String value) {
        for (String s : list) {
            if (s.equals(value)) return true;
        }
        return false;
    }

    private static boolean contains(String[] array, String value) {
        for (String s : array) {
            if (s.equals(value)) return true;
        }
        return false;
    }
}