import org.json.JSONObject;  // Make sure to include org.json library in your project
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SecretFinder {

    // Function to decode y value from the given base
    private static long decodeYValue(int base, String encodedValue) {
        return Long.parseLong(encodedValue, base);
    }

    // Function to apply Gauss elimination to find polynomial coefficients
    private static double[] gaussElimination(List<Point> points) {
        int n = points.size();
        double[][] A = new double[n][n + 1];

        // Fill the augmented matrix
        for (int i = 0; i < n; i++) {
            int x_i = points.get(i).x;
            long y_i = points.get(i).y;
            for (int j = 0; j < n; j++) {
                A[i][j] = Math.pow(x_i, j);  // x^j for coefficients
            }
            A[i][n] = (double) y_i;  // Set the y value in the last column
        }

        // Apply Gauss elimination
        for (int i = 0; i < n; i++) {
            // Make the diagonal contain all 1s
            double diagVal = A[i][i];
            for (int j = 0; j < n + 1; j++) {
                A[i][j] /= diagVal;
            }
            for (int j = i + 1; j < n; j++) {
                double factor = A[j][i];
                for (int k = 0; k < n + 1; k++) {
                    A[j][k] -= A[i][k] * factor;
                }
            }
        }

        // Back substitution to find coefficients
        double[] coeffs = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            coeffs[i] = A[i][n];
            for (int j = i + 1; j < n; j++) {
                coeffs[i] -= A[i][j] * coeffs[j];
            }
        }

        return coeffs;
    }

    // Function to find the secret key from JSON
    private static long findSecretFromJson(String jsonFilePath) {
        StringBuilder jsonData = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonData.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject data = new JSONObject(jsonData.toString());

        List<Point> points = new ArrayList<>();
        int n = data.getJSONObject("keys").getInt("n");
        int k = data.getJSONObject("keys").getInt("k");

        for (String key : data.keySet()) {
            if (key.equals("keys")) {
                continue;
            }

            int xVal = Integer.parseInt(key);  // x is the key in the JSON
            int base = data.getJSONObject(key).getInt("base");  // Base in which y is encoded
            String encodedY = data.getJSONObject(key).getString("value");  // Encoded y value

            // Decode the y value
            long yVal = decodeYValue(base, encodedY);

            // Add the (x, y) pair to the points list
            points.add(new Point(xVal, yVal));
        }

        // Ensure we only use k points for interpolation
        if (points.size() > k) {
            points = points.subList(0, k);
        }

        // Find coefficients using Gauss elimination
        double[] coefficients = gaussElimination(points);

        // The secret key (constant term at P(0))
        long secret = (long) coefficients[0];
        return secret;
    }

    // Point class to hold x and y values
    private static class Point {
        int x;
        long y;

        Point(int x, long y) {
            this.x = x;
            this.y = y;
        }
    }

    // Main function
    public static void main(String[] args) {
        String jsonData = "{ \"keys\": { \"n\": 9, \"k\": 6 }, " +
                          "\"1\": { \"base\": \"10\", \"value\": \"28735619723837\" }, " +
                          "\"2\": { \"base\": \"16\", \"value\": \"1A228867F0CA\" }, " +
                          "\"3\": { \"base\": \"12\", \"value\": \"32811A4AA0B7B\" }, " +
                          "\"4\": { \"base\": \"11\", \"value\": \"917978721331A\" }, " +
                          "\"5\": { \"base\": \"16\", \"value\": \"1A22886782E1\" }, " +
                          "\"6\": { \"base\": \"10\", \"value\": \"28735619654702\" }, " +
                          "\"7\": { \"base\": \"14\", \"value\": \"71AB5070CC4B\" }, " +
                          "\"8\": { \"base\": \"9\", \"value\": \"122662581541670\" }, " +
                          "\"9\": { \"base\": \"8\", \"value\": \"642121030037605\" } }";

        // Save the JSON to a file for demonstration purposes
        try (FileWriter fileWriter = new FileWriter("data.json")) {
            fileWriter.write(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Find the secret key
        long secretKey = findSecretFromJson("data.json");
        System.out.println("The secret key is: " + secretKey);
    }
}
