import java.util.Random;

public class Matrix {
    double[] matrix;
    int numRows;
    int numCols;

    public Matrix(double[] matrix, int numRows, int numCols) {
        this.matrix = matrix;
    }

    static Matrix generateRandom(int numRows, int numCols) {
        double[] matrix = new double[numRows * numCols];
        Random rand = new Random();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                matrix[i * numCols + j] = (double) rand.nextInt(10) + 1;
            }
        }
        return new Matrix(matrix, numRows, numCols);
    }

    void print() {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                System.out.print(matrix[i * numCols + j] + " ");
            }
            System.out.println();
        }
    }

    double elementAt(int i) {
        return matrix[i];
    }

    public double[] getMatrix() {
        return matrix;
    }


    public double[][] to2D(int rows, int cols) {
        if (matrix.length != rows * cols) {
            throw new IllegalArgumentException("Invalid array size");
        }
        double[][] mat = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(matrix, i * cols, mat[i], 0, cols);
        }
        return mat;
    }

    void print2D(int rows, int cols) {
        final double[][] mat = to2D(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(mat[i][j] + " ");
            }
            System.out.println();
        }
    }


    Matrix multiply(Matrix B) {
        if (numCols != B.numRows) {
            throw new IllegalArgumentException("Matrix dimensions are not compatible for multiplication");
        }
        double[] result = new double[numRows * B.numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < B.numCols; j++) {
                for (int k = 0; k < numCols; k++) {
                    result[i * B.numCols + j] += matrix[i * numCols + k] * B.matrix[k * B.numCols + j];
                }
            }
        }
        return new Matrix(result, numRows, B.numCols);
    }
}