import mpi.MPI;
import shared.Matrix;
import types.Blocking;
import types.NonBlocking;

import java.util.Arrays;
import java.util.List;

public class Main {

    final static boolean IS_BLOCKING = false;

    public static void main(String[] args) {
        MPI.Init(args);

        runOneSize(IS_BLOCKING, true);
        // runForDifferentSizes(rank, size);


        MPI.Finalize();
    }

    public static void runOneSize(boolean isBlocking, boolean checkResults) {
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();


        int n = 3 * 222;

        if (n % size != 0) {
            throw new IllegalArgumentException("Matrix size should be divisible by number of processors");
        }

        Matrix A = Matrix.generateRandom(n, n);
        Matrix B = Matrix.generateRandom(n, n);
        double[] C = new double[n * n];

        if (isBlocking)
            Blocking.multiply(rank, size, n, A, B, C);
        else
            NonBlocking.multiply(rank, size, n, A, B, C);


        if (rank == 0) {
            System.out.println("Matrix A:");
//            A.print2D(n, n);
            System.out.println("Matrix B:");
//            B.print2D(n, n);
            System.out.println("Result:");
            final Matrix result = new Matrix(C, n * n, 1);
//            result.print2D(n, n);

            final Matrix expected = A.multiply(B);

            if (checkResults) {
                for (int i = 0; i < expected.getMatrix().length; i++) {
                    if (expected.getMatrix()[i] != result.getMatrix()[i]) {
                        System.out.println("Error in the result");
                        System.out.println("Expected: " + expected.getMatrix()[i] + " but got: " + result.getMatrix()[i]);
                        break;
                    }
                }
                System.out.println("\n\nResults are correct!");
            }
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