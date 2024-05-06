package types;

import mpi.MPI;
import mpi.Request;
import shared.Matrix;

public class NonBlocking {
    public static void multiply(int rank, int size, int n, Matrix A, Matrix B, double[] C) {


        int rowsPerProcess = n / size;
        Request[] requests = new Request[size * 2];

        sendOrReceiveMatrix(rank, size, requests, n, A, rowsPerProcess);

        exchangeMatrixBetweenProcesses(rank, size, requests, n, B);

        // Wait for all non-blocking operations to complete before starting the matrix multiplication
        Request.Waitall(requests);

        computeMatrixMultiplication(rank, rowsPerProcess, n, A, B, C);

        if (rank == 0)
            collectResults(size, C, rowsPerProcess, n);

        else
            MPI.COMM_WORLD.Isend(C, rank * rowsPerProcess * n, rowsPerProcess * n, MPI.DOUBLE, 0, 0).Wait();

    }


    private static void sendOrReceiveMatrix(int rank, int size, Request[] requests, int n, Matrix A, int rowsPerProcess) {
        if (rank == 0) {
            for (int dest = 1; dest < size; dest++) {
                requests[dest] = MPI.COMM_WORLD.Isend(A.getMatrix(), dest * rowsPerProcess * n, rowsPerProcess * n, MPI.DOUBLE, dest, 0);
            }
        } else {
            requests[rank] = MPI.COMM_WORLD.Irecv(A.getMatrix(), rank * rowsPerProcess * n, rowsPerProcess * n, MPI.DOUBLE, 0, 0);
        }
    }

    private static void exchangeMatrixBetweenProcesses(int rank, int size, Request[] requests, int n, Matrix B) {
        for (int source = 0; source < size; source++) {
            if (rank == source) {
                requests[source + size] = MPI.COMM_WORLD.Isend(B.getMatrix(), 0, n * n, MPI.DOUBLE, (rank + 1) % size, 0);
            } else if (rank == (source + 1) % size) {
                requests[source + size] = MPI.COMM_WORLD.Irecv(B.getMatrix(), 0, n * n, MPI.DOUBLE, source, 0);
            }
        }
    }

    private static void computeMatrixMultiplication(int rank, int rowsPerProcess, int n, Matrix A, Matrix B, double[] C) {
        for (int i = rank * rowsPerProcess; i < (rank + 1) * rowsPerProcess; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    C[i * n + j] += A.elementAt(i * n + k) * B.elementAt(k * n + j);
                }
            }
        }
    }

    private static void collectResults(int size, double[] C, int rowsPerProcess, int n) {
        for (int source = 1; source < size; source++) {
            MPI.COMM_WORLD.Irecv(C, source * rowsPerProcess * n, rowsPerProcess * n, MPI.DOUBLE, source, 0).Wait();
        }
    }


}