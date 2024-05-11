package types;

import mpi.MPI;
import mpi.MPIException;
import shared.Matrix;

public class MatrixMultiplication {

    public static void main(String[] args) throws MPIException {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();


        int n = Integer.parseInt(args[0]); // assuming that the matrix size is passed as a command-line argument

        Matrix A = Matrix.generateRandom(n, n);
        Matrix B = Matrix.generateRandom(n, n);

        double[] C = multiply(A, B, n, rank, size);

        MPI.Finalize();
    }

    public static double[] multiply(Matrix a, Matrix b, int n, int rank, int size) throws MPIException {
        final double[][] a2D = a.to2D(n, n);
        final double[][] b2D = b.to2D(n, n);

        double[] c = new double[n * n];
        int rowsPerProcess = n / size;

        for (int i = rank * rowsPerProcess; i < (rank + 1) * rowsPerProcess; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    c[i * n + j] += a2D[i][k] * b2D[k][j];
                }
            }
        }

        if (rank != 0) {
            MPI.COMM_WORLD.Send(c, 0, c.length, MPI.DOUBLE, 0, 0);
        } else {
            for (int i = 1; i < size; i++) {
                double[] temp = new double[n * n];
                MPI.COMM_WORLD.Recv(temp, 0, temp.length, MPI.DOUBLE, i, 0);
                for (int j = 0; j < temp.length; j++) {
                    c[j] += temp[j];
                }
            }
        }

        return c;
    }
}