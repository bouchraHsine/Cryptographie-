import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DatasetPrep {

    // Classe interne pour stocker chaque instance de données
    public static class DataInstance {
        public String[] attributes;  // Tableau pour les attributs
        public String label;         // Étiquette de classe

        public DataInstance(String[] attributes, String label) {
            this.attributes = attributes;
            this.label = label;
        }
    }

    private List<DataInstance> dataset;       // Liste pour stocker toutes les données
    private List<DataInstance> trainingSet;   // Ensemble d'entraînement
    private List<DataInstance> testSet;       // Ensemble de test
    private double splitRatio;                // Ratio pour la séparation train/test
    private int numberOfClasses;              // Nombre de classes différentes dans le dataset
    private int numberOfAttributes;           // Nombre d'attributs par instance

    // Constructeur
    public DatasetPrep(double splitRatio) {
        this.dataset = new ArrayList<>();
        this.splitRatio = splitRatio;
        this.numberOfClasses = 0;
        this.numberOfAttributes = 0;
    }

    //Charge et prépare les données depuis un fichier

    public void prepareDataset(String filePath) {
        //  Lecture du fichier
        List<String> rawLines = readFile(filePath);

        // Nettoyage et traitement
        processRawData(rawLines);
        // Calculer le nombre de classes et d'attributs
        calculateStats();
        //Séparation train/test
        splitData();
        System.out.println("Dataset chargé:");
        System.out.println("- Nombre total d'instances: " + dataset.size());
        System.out.println("- Nombre de classes: " + numberOfClasses);
        System.out.println("- Distribution des classes:");
        Map<String, Integer> classDistribution = new HashMap<>();
        for (DataInstance instance : dataset) {
            classDistribution.merge(instance.label, 1, Integer::sum);
        }
        classDistribution.forEach((k, v) ->
                System.out.printf("  %s: %d (%.1f%%)\n", k, v, 100.0*v/dataset.size()));
    }

    //Lecture manuelle du fichier

    private List<String> readFile(String filePath) {
        List<String> lines = new ArrayList<>();

        try {
            // Ouvrir le fichier avec FileReader et BufferedReader pour une lecture efficace
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Lire ligne par ligne
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                // Ajouter seulement si la ligne n'est pas vide après trim()
                if (!currentLine.trim().isEmpty()) {
                    lines.add(currentLine);
                }
            }
            // Fermer les ressources pour éviter les fuites de mémoire
            bufferedReader.close();
            fileReader.close();

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier: " + e.getMessage());
        }

        return lines;
    }

    //Étape 2: Traitement des données brutes
    private void processRawData(List<String> rawLines) {
        boolean isFirstLine = true; // Pour sauter l'en-tête
        for (String line : rawLines) {
            if (isFirstLine) {
                isFirstLine = false;
                continue; // Saute la première ligne (en-tête)
            }// Supprimer les espaces inutiles au début et à la fin
            line = line.trim();
            // Ignorer les lignes vides après nettoyage
            if (line.isEmpty()) continue;
            // Trouver la dernière virgule qui sépare attributs et classe
            int lastComma = line.lastIndexOf(',');
            // Vérifier que la ligne est valide (a une virgule et pas en dernière position)
            if (lastComma == -1 || lastComma == line.length() - 1) {
                System.err.println("Ligne ignorée - format invalide: " + line);
                continue;
            }
            // Découper la ligne en deux parties
            String attrString = line.substring(0, lastComma);
            String label = line.substring(lastComma + 1).trim();
            // Traitement des attributs
            String[] attributes = processAttributes(attrString);
            // Vérifier que le traitement a réussi
            if (attributes != null && !label.isEmpty()) {
                DataInstance instance = new DataInstance(attributes, label);
                dataset.add(instance);
            }

        }
    }

    //return tableau des attributs traités ou null si invalide
    private String[] processAttributes(String attrString) {
        // Compter le nombre de virgules pour déterminer le nombre d'attributs
        int commaCount = 0;
        for (int i = 0; i < attrString.length(); i++) {
            if (attrString.charAt(i) == ',') commaCount++;
        }
        String[] attributes = new String[commaCount + 1]; // +1 car n virgules → n+1 éléments
        int start = 0;
        int attrIndex = 0;
        for (int i = 0; i <= attrString.length(); i++) {
            if (i == attrString.length() || attrString.charAt(i) == ',') {
                // Extraire la valeur entre deux virgules
                String value = attrString.substring(start, i).trim();
                // Uniformiser le format (tout en minuscules)
                value = value.toLowerCase();
                // Vérifier que la valeur n'est pas vide
                if (value.isEmpty()) {
                    System.err.println("Attribut vide trouvé - ligne ignorée");
                    return null;
                }
                attributes[attrIndex++] = value;
                start = i + 1;
            }
        }

        return attributes;
    }

    /**

    /**
     * Compare deux instances pour détecter les doublons
     * @param a première instance à comparer
     * @param b deuxième instance à comparer
     * @return true si les instances sont identiques, false sinon
     */


    /**
     * Étape 4: Calcule les statistiques du dataset (nombre de classes et d'attributs)
     */
    private void calculateStats() {
        if (dataset.isEmpty()) {
            numberOfClasses = 0;
            numberOfAttributes = 0;
            return;
        }

        // Utilisation d'un Set pour compter les classes uniques
        Set<String> uniqueClasses = new HashSet<>();

        // Le nombre d'attributs est le même pour toutes les instances
        numberOfAttributes = dataset.get(0).attributes.length;

        // Parcourir toutes les instances pour collecter les classes
        for (DataInstance instance : dataset) {
            uniqueClasses.add(instance.label);

            // Vérification de la cohérence du nombre d'attributs
            if (instance.attributes.length != numberOfAttributes) {
                System.err.println("Attention: nombre d'attributs incohérent dans le dataset");
            }
        }

        numberOfClasses = uniqueClasses.size();
    }

    /**
     * Étape 5: Séparation du dataset en ensembles d'entraînement et de test
     */
    private void splitData() {
        // Calculer la taille de l'ensemble d'entraînement
        int trainSize = (int)(dataset.size() * splitRatio);
        // Mélanger le dataset pour éviter tout biais
        manualShuffle();
        // Initialiser les listes
        trainingSet = new ArrayList<>();
        testSet = new ArrayList<>();
        // Répartir les instances
        for (int i = 0; i < dataset.size(); i++) {
            if (i < trainSize) {
                trainingSet.add(dataset.get(i));
            } else {
                testSet.add(dataset.get(i));
            }
        }
    }
    //Mélange aléatoire du dataset (algorithme de Fisher-Yates)
    private void manualShuffle() {
        // Parcourir le tableau de la fin au début
        for (int i = dataset.size() - 1; i > 0; i--) {
            // Choisir un index aléatoire entre 0 et i
            int j = (int)(Math.random() * (i + 1));

            // Échanger les éléments i et j
            DataInstance temp = dataset.get(i);
            dataset.set(i, dataset.get(j));
            dataset.set(j, temp);
        }
    }


    // Méthodes d'accès aux données et statistiques
    public List<DataInstance> getTrainingSet() { return trainingSet; }
    public List<DataInstance> getTestSet() { return testSet; }
    public List<DataInstance> getFullDataset() { return dataset; }
    public int getNumberOfClasses() { return numberOfClasses; }
    public int getNumberOfAttributes() { return numberOfAttributes; }

    // Exemple d'utilisation

}