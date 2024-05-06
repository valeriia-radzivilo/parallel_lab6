import mpi.MPI;

public class Blocking {

    public static void multiply(int rank, int numberOfProcessors, int n, Matrix A, Matrix B, double[] C) {


        int rowsPerProcess = n / numberOfProcessors;


        if (rank == 0) {

            for (int dest = 1; dest < numberOfProcessors; dest++) {
                MPI.COMM_WORLD.Send(A.getMatrix(), dest * rowsPerProcess * n, rowsPerProcess * n, MPI.DOUBLE, dest, 0);
            }

        } else {
            MPI.COMM_WORLD.Recv(A.getMatrix(), rank * rowsPerProcess * n, rowsPerProcess * n, MPI.DOUBLE, 0, 0);
        }


        for (int source = 0; source < numberOfProcessors; source++) {
            if (rank == source) {
                MPI.COMM_WORLD.Send(B.getMatrix(), 0, n * n, MPI.DOUBLE, (rank + 1) % numberOfProcessors, 0);
            } else if (rank == (source + 1) % numberOfProcessors) {
                MPI.COMM_WORLD.Recv(B.getMatrix(), 0, n * n, MPI.DOUBLE, source, 0);
            }
        }


        for (int i = rank * rowsPerProcess; i < (rank + 1) * rowsPerProcess; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    C[i * n + j] += A.elementAt(i * n + k) * B.elementAt(k * n + j);
                }
            }
        }


        if (rank == 0) {

            for (int source = 1; source < numberOfProcessors; source++) {
                MPI.COMM_WORLD.Recv(C, source * rowsPerProcess * n, rowsPerProcess * n, MPI.DOUBLE, source, 0);
            }

        } else {
            MPI.COMM_WORLD.Send(C, rank * rowsPerProcess * n, rowsPerProcess * n, MPI.DOUBLE, 0, 0);
        }
    }
}