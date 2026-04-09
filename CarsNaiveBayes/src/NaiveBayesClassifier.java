import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

// Définition de la classe principale du classifieur Naive Bayes
public class NaiveBayesClassifier {

    // Classe interne représentant une instance d'apprentissage (exemple)
    public static class Instance {
        public String[] attributes; // Valeurs des attributs de l'exemple
        public String classLabel;   // Étiquette de classe associée à l'exemple

        public Instance(String[] attributes, String classLabel) {
            this.attributes = attributes;
            this.classLabel = classLabel;
        }
    }

    // Classe interne stockant les probabilités liées à une classe spécifique
    private static class ClassProbabilities {
        String classLabel; // Nom de la classe
        double priorProbability; // Probabilité a priori P(C)
        double[][] conditionalProbabilities; // Matrice P(Xi=xi | C)
        String[][] attributeValues; // Valeurs possibles pour chaque attribut dans cette classe

        public ClassProbabilities(String classLabel, double priorProbability,
                                  double[][] conditionalProbabilities, String[][] attributeValues) {
            this.classLabel = classLabel;
            this.priorProbability = priorProbability;
            this.conditionalProbabilities = conditionalProbabilities;
            this.attributeValues = attributeValues;
        }
    }

    private Instance[] trainingData; // Tableau des données d'entraînement
    private String[] attributeNames; // Noms des attributs
    private String[] classLabels; // Liste des classes possibles
    private ClassProbabilities[] classProbabilities; // Tableau des probabilités pour chaque classe
    private String[][] globalAttributeValues; // Toutes les valeurs possibles pour chaque attribut

    // Constructeur principal
    public NaiveBayesClassifier(String[] attributeNames, String[] classLabels) {
        this.attributeNames = attributeNames;
        this.classLabels = classLabels;
        this.globalAttributeValues = new String[attributeNames.length][];
    }

    // Méthode d'entraînement du modèle
    public void train(Instance[] trainingData) {
        this.trainingData = trainingData;
        this.classProbabilities = new ClassProbabilities[classLabels.length];

        // 1. Déterminer les valeurs possibles pour chaque attribut
        for (int attr = 0; attr < attributeNames.length; attr++) {
            List<String> values = new ArrayList<>();
            for (Instance instance : trainingData) {
                if (!values.contains(instance.attributes[attr])) {
                    values.add(instance.attributes[attr]);
                }
            }
            globalAttributeValues[attr] = values.toArray(new String[0]);
        }

        // 2. Calculer les probabilités pour chaque classe
        for (int i = 0; i < classLabels.length; i++) {
            String currentClass = classLabels[i];
            int classCount = countClassOccurrences(currentClass); // Nombre d’occurrences de la classe
            double priorProb = (double) classCount / trainingData.length; // P(C)

            double[][] condProbs = new double[attributeNames.length][]; // Pour P(Xi=xi | C)
            String[][] attrValues = new String[attributeNames.length][]; // Valeurs de Xi

            for (int attr = 0; attr < attributeNames.length; attr++) {
                attrValues[attr] = globalAttributeValues[attr];
                condProbs[attr] = new double[attrValues[attr].length];

                for (int val = 0; val < attrValues[attr].length; val++) {
                    String value = attrValues[attr][val];
                    int count = countAttributeValueWithClass(attr, value, currentClass);
                    // Lissage de Laplace appliqué ici
                    condProbs[attr][val] = (count + 1) / (double)(classCount + attrValues[attr].length);
                }
            }

            // Stocker les probabilités calculées pour cette classe
            classProbabilities[i] = new ClassProbabilities(currentClass, priorProb, condProbs, attrValues);
        }
    }

    // Calcul des probabilités pour chaque classe donnée une instance
    public double[] getClassProbabilitiesRaw(String[] instance) {
        double[] probabilities = new double[classLabels.length];

        for (int i = 0; i < classLabels.length; i++) {
            ClassProbabilities cp = classProbabilities[i];
            double probability = cp.priorProbability; // Commencer avec P(C)

            for (int attr = 0; attr < instance.length; attr++) {
                String value = instance[attr];
                int valueIndex = -1;

                // Trouver l'index de la valeur de l'attribut
                for (int j = 0; j < cp.attributeValues[attr].length; j++) {
                    if (cp.attributeValues[attr][j].equals(value)) {
                        valueIndex = j;
                        break;
                    }
                }

                // Si valeur connue : multiplier par P(Xi=xi | C)
                if (valueIndex != -1) {
                    probability *= cp.conditionalProbabilities[attr][valueIndex];
                } else {
                    // Si inconnue : appliquer Laplace pour éviter probabilité nulle
                    probability *= 1.0 / (countClassOccurrences(cp.classLabel) + cp.attributeValues[attr].length);
                }
            }
            probabilities[i] = probability;
        }return probabilities;
    }

    // Normalisation des probabilités pour qu'elles forment un total de 1
    public double[] getNormalizedProbabilities(String[] instance) {
        double[] rawProbabilities = getClassProbabilitiesRaw(instance);
        double sum = 0.0;

        for (double prob : rawProbabilities) {
            sum += prob;
        }

        double[] normalized = new double[rawProbabilities.length];
        for (int i = 0; i < rawProbabilities.length; i++) {
            normalized[i] = rawProbabilities[i] / sum;
        }

        return normalized;
    }

    // Retourne la classe ayant la plus haute probabilité brute
    public String predict(String[] attributes) {
        double[] probabilities = getClassProbabilitiesRaw(attributes);
        int maxIndex = 0;
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[maxIndex]) {
                maxIndex = i;
            }
        }
        return classLabels[maxIndex];
    }

    // Retourne les probabilités brutes formatées avec 10 décimales fixes
    public String[] getClassProbabilitiesRawFormatted(String[] instance) {
        double[] rawProbabilities = getClassProbabilitiesRaw(instance);
        String[] formatted = new String[rawProbabilities.length];

        for (int i = 0; i < rawProbabilities.length; i++) {
            BigDecimal bd = new BigDecimal(rawProbabilities[i]);
            bd = bd.setScale(10, RoundingMode.DOWN); // Troncature à 10 décimales
            formatted[i] = bd.toPlainString();

            // Complète avec des zéros si nécessaire
            if (!formatted[i].contains(".")) {
                formatted[i] += ".0000000000";
            } else {
                int decimalPlaces = formatted[i].split("\\.")[1].length();
                while (decimalPlaces < 10) {
                    formatted[i] += "0";
                    decimalPlaces++;
                }
            }
        }

        return formatted;
    }

    // Obtenir les valeurs possibles d’un attribut
    public String[] getAttributeValues(int attributeIndex) {
        return globalAttributeValues[attributeIndex];
    }

    // Obtenir les noms des attributs
    public String[] getAttributeNames() {
        return this.attributeNames;
    }

    // Obtenir les étiquettes de classe
    public String[] getClassLabels() {
        return this.classLabels;
    }

    // Retourne la probabilité a priori P(C) pour une classe donnée
    public double getPriorProbability(String classLabel) {
        for (ClassProbabilities cp : classProbabilities) {
            if (cp.classLabel.equals(classLabel)) {
                return cp.priorProbability;
            }
        }
        return 0.0;
    }

    // Retourne P(Xi=xi | C) pour un attribut, une valeur et une classe donnés
    public double getConditionalProbability(int attributeIndex, String attributeValue, String classLabel) {
        for (ClassProbabilities cp : classProbabilities) {
            if (cp.classLabel.equals(classLabel)) {
                for (int i = 0; i < cp.attributeValues[attributeIndex].length; i++) {
                    if (cp.attributeValues[attributeIndex][i].equals(attributeValue)) {
                        return cp.conditionalProbabilities[attributeIndex][i];
                    }
                }
                // Retour par défaut (Laplace) si la valeur est inconnue
                return 1.0 / (countClassOccurrences(classLabel) + cp.attributeValues[attributeIndex].length);
            }
        }
        return 0.0;
    }

    // Compte le nombre d'occurrences d'une classe donnée
    private int countClassOccurrences(String classLabel) {
        int count = 0;
        for (Instance instance : trainingData) {
            if (instance.classLabel.equals(classLabel)) count++;
        }
        return count;
    }

    // Compte combien de fois une valeur d’attribut apparaît avec une classe donnée
    private int countAttributeValueWithClass(int attributeIndex, String attributeValue, String classLabel) {
        int count = 0;
        for (Instance instance : trainingData) {
            if (instance.classLabel.equals(classLabel) &&
                    instance.attributes[attributeIndex].equals(attributeValue)) {
                count++;
            }
        }
        return count;
    }
}
