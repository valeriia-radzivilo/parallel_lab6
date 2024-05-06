import mpi.MPI;

import java.util.Arrays;
import java.util.List;

public class MatrixMultiplication {

    final static boolean IS_BLOCKING = true;

    public static void main(String[] args) {
        MPI.Init(args);

        runOneSize(IS_BLOCKING);
        //runForDifferentSizes(rank, size);


        MPI.Finalize();
    }

    private static void runOneSize(boolean isBlocking) {
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int n = 3;
        Matrix A = Matrix.generateRandom(n, n);
        Matrix B = Matrix.generateRandom(n, n);
        double[] C = new double[n * n];

        if (isBlocking)
            Blocking.multiply(rank, size, n, A, B, C);
        else
            NonBlocking.multiply(rank, size, n, A, B, C);


        if (rank == 0) {
            System.out.println("Matrix A:");
            A.print2D(n, n);
            System.out.println("Matrix B:");
            B.print2D(n, n);
            System.out.println("Result:");
            final Matrix result = new Matrix(C, n * n, 1);
            result.print2D(n, n);

        }
    }


    private static void runForDifferentSizes(int rank, int size) {
        if (rank == 0)
            System.out.println("Results for " + size + "processors");
        int experC = 2;
        List<Integer> arraySizes = Arrays.asList(500, 1000, 1500, 1750);

        for (int n : arraySizes) {

            Matrix A = Matrix.generateRandom(n, n);
            Matrix B = Matrix.generateRandom(n, n);
            double[] C = new double[n * n];

            final long start_time = System.currentTimeMillis();
            for (int i = 0; i < experC; i++) {
                Blocking.multiply(rank, size, n, A, B, C);
            }
            MPI.COMM_WORLD.Barrier();
            if (rank == 0) {
                final long time = System.currentTimeMillis() - start_time;
                System.out.println(time + " ms taken for " + n + " elements in an array");
            }
        }
    }
}