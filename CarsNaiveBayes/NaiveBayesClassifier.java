import java.io.*;
import java.util.*;

public class NaiveBayesClassifier {
    static class DataRow {
        int[] features;
        String label;

        DataRow(int[] features, String label) {
            this.features = features;
            this.label = label;
        }
    }

    public static void main(String[] args) throws IOException {
        String filePath = "src/car_encoded.csv";
        List<DataRow> dataset = readCSV(filePath);

        // Séparer dataset en deux parties (80% train, 20% test)
        int trainSize = (int) (dataset.size() * 0.8);
        List<DataRow> trainSet = dataset.subList(0, trainSize);
        List<DataRow> testSet = dataset.subList(trainSize, dataset.size());

        // Entraîner le classifieur
        NaiveBayesModel model = train(trainSet);

        // Tester le classifieur
        int correct = 0;
        for (DataRow row : testSet) {
            String predicted = predict(row.features, model);
            if (predicted.equals(row.label)) {
                correct++;
            }
        }

        System.out.println("Précision sur les données test : " + (100.0 * correct / testSet.size()) + "%");

        // Prédiction manuelle (exemple)
        int[] nouvelleVoiture = {3, 1, 2, 3, 1, 2}; // modifiable
        String resultat = predict(nouvelleVoiture, model);
        System.out.println("Classe prédite pour la voiture : " + resultat);
    }

    static List<DataRow> readCSV(String filePath) throws IOException {
        List<DataRow> data = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            int[] features = new int[parts.length - 1];
            for (int i = 0; i < parts.length - 1; i++) {
                features[i] = Integer.parseInt(parts[i]);
            }
            String label = parts[parts.length - 1];
            data.add(new DataRow(features, label));
        }
        reader.close();
        return data;
    }

    static class NaiveBayesModel {
        Map<String, Integer> classCounts = new HashMap<>();
        Map<String, List<Map<Integer, Integer>>> featureCounts = new HashMap<>();
        int totalSamples = 0;
    }

    static NaiveBayesModel train(List<DataRow> dataset) {
        NaiveBayesModel model = new NaiveBayesModel();
        model.totalSamples = dataset.size();

        for (DataRow row : dataset) {
            model.classCounts.put(row.label, model.classCounts.getOrDefault(row.label, 0) + 1);
            model.featureCounts.putIfAbsent(row.label, new ArrayList<>());

            List<Map<Integer, Integer>> featureList = model.featureCounts.get(row.label);
            if (featureList.isEmpty()) {
                for (int i = 0; i < row.features.length; i++) {
                    featureList.add(new HashMap<>());
                }
            }

            for (int i = 0; i < row.features.length; i++) {
                Map<Integer, Integer> countMap = featureList.get(i);
                countMap.put(row.features[i], countMap.getOrDefault(row.features[i], 0) + 1);
            }
        }

        return model;
    }

    static String predict(int[] features, NaiveBayesModel model) {
        double bestProb = -1;
        String bestClass = null;

        for (String label : model.classCounts.keySet()) {
            double prob = Math.log(model.classCounts.get(label) * 1.0 / model.totalSamples);
            List<Map<Integer, Integer>> featureList = model.featureCounts.get(label);

            for (int i = 0; i < features.length; i++) {
                Map<Integer, Integer> countMap = featureList.get(i);
                int count = countMap.getOrDefault(features[i], 0);
                int total = model.classCounts.get(label);

                // Laplace smoothing
                prob += Math.log((count + 1.0) / (total + countMap.size()));
            }

            if (bestClass == null || prob > bestProb) {
                bestProb = prob;
                bestClass = label;
            }
        }

        return bestClass;
    }
}
