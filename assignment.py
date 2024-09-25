import json
import numpy as np

def decode_y_value(base, encoded_value):
    """Decode the y value from the encoded base."""
    return int(encoded_value, base)

def gauss_elimination(points):
    """Apply Gauss elimination to find the coefficients of the polynomial."""
    n = len(points)  # Number of points
    # Create the augmented matrix
    A = np.zeros((n, n + 1))  # n equations and n+1 columns (for constants)

    # Fill the matrix with x^j and y values
    for i in range(n):
        x_i, y_i = points[i]
        for j in range(n):
            A[i][j] = x_i ** j  # x^j for coefficients
        A[i][n] = y_i  # Set the y value in the last column
    
    
    # Apply Gauss Elimination
    for i in range(n):
        # Make the diagonal contain all 1s
        A[i] = A[i] / A[i][i]
        for j in range(i + 1, n):
            A[j] = A[j] - A[i] * A[j][i]

    # Back substitution to find coefficients
    coeffs = np.zeros(n)
    for i in range(n - 1, -1, -1):
        coeffs[i] = A[i][n] - np.sum(A[i][j] * coeffs[j] for j in range(i + 1, n))

    return coeffs

def find_secret_from_json(json_file_path):
    """Read JSON file and extract the secret key using the Gauss elimination method."""
    with open(json_file_path, 'r') as f:
        data = json.load(f)
    
    points = []
    n = data['keys']['n']
    k = data['keys']['k']
    
    for x, info in data.items():
        # Skip the 'keys' section
        if x == "keys":
            continue
        
        x_val = int(x)  # x is the key in the JSON
        base = int(info['base'])  # Base in which y is encoded
        encoded_y = info['value']  # Encoded y value
        
        # Decode the y value
        y_val = decode_y_value(base, encoded_y)
        
        # Add the (x, y) pair to the points list
        points.append((x_val, y_val))
    
    # Ensure we only use k points for interpolation
    points = points[:k]
    
    # Find coefficients using Gauss elimination
    coefficients = gauss_elimination(points)
    
    # Calculate the secret key (constant term at P(0))
    secret = coefficients[0]  # P(0) is the constant term
    
    return secret

# Example usage
file_path = r"D:/catalog/samples.json"
secret = find_secret_from_json(file_path)
print("The secret (constant term) is:", secret)
