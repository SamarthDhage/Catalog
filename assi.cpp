#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <sstream>
#include <iomanip>
#include <cmath>
#include <nlohmann/json.hpp> // Include the nlohmann/json library

using json = nlohmann::json;

// Function to decode the y value from the encoded base
int decode_y_value(int base, const std::string& encoded_value) {
    return std::stoi(encoded_value, nullptr, base);
}

// Function to apply Gauss elimination to find the coefficients of the polynomial
std::vector<double> gauss_elimination(const std::vector<std::pair<int, int>>& points) {
    int n = points.size();
    std::vector<std::vector<double>> A(n, std::vector<double>(n + 1, 0.0));

    // Fill the matrix with x^j and y values
    for (int i = 0; i < n; ++i) {
        int x_i = points[i].first;
        int y_i = points[i].second;
        for (int j = 0; j < n; ++j) {
            A[i][j] = std::pow(x_i, j); // x^j for coefficients
        }
        A[i][n] = y_i; // Set the y value in the last column
    }

    // Apply Gauss Elimination
    for (int i = 0; i < n; ++i) {
        // Make the diagonal contain all 1s
        double diag = A[i][i];
        for (int j = 0; j <= n; ++j) {
            A[i][j] /= diag;
        }
        for (int j = i + 1; j < n; ++j) {
            double factor = A[j][i];
            for (int k = 0; k <= n; ++k) {
                A[j][k] -= A[i][k] * factor;
            }
        }
    }

    // Back substitution to find coefficients
    std::vector<double> coeffs(n, 0.0);
    for (int i = n - 1; i >= 0; --i) {
        coeffs[i] = A[i][n];
        for (int j = i + 1; j < n; ++j) {
            coeffs[i] -= A[i][j] * coeffs[j];
        }
    }

    return coeffs;
}

// Function to read JSON file and extract the secret key using Gauss elimination method
int find_secret_from_json(const std::string& json_file_path) {
    std::ifstream file(json_file_path);
    json data;
    file >> data;

    std::vector<std::pair<int, int>> points;
    int n = data["keys"]["n"];
    int k = data["keys"]["k"];

    // Iterate through JSON to extract points
    for (auto& element : data.items()) {
        const std::string& x_str = element.key();
        if (x_str == "keys") continue;

        int x_val = std::stoi(x_str); // x is the key in the JSON
        int base = element.value()["base"]; // Base in which y is encoded
        std::string encoded_y = element.value()["value"]; // Encoded y value

        // Decode the y value
        int y_val = decode_y_value(base, encoded_y);

        // Add the (x, y) pair to the points list
        points.emplace_back(x_val, y_val);
    }

    // Ensure we only use k points for interpolation
    if (points.size() > k) {
        points.resize(k);
    }

    // Find coefficients using Gauss elimination
    std::vector<double> coefficients = gauss_elimination(points);

    // Calculate the secret key (constant term at P(0))
    int secret = static_cast<int>(coefficients[0]); // P(0) is the constant term

    return secret;
}

// Example usage
int main() {
    std::string file_path = "D:/catalog/samples.json";
    
    try {
        int secret = find_secret_from_json(file_path);
        std::cout << "The secret (constant term) is: " << secret << std::endl;
    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << std::endl;
    }

    return 0;
}