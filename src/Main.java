import mpi.MPI;
import shared.Matrix;
import types.Blocking;
import types.MatrixMultiplication;
import types.NonBlocking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    final static boolean IS_BLOCKING = false;

    public static void main(String[] args) {
        MPI.Init(args);

//        runOneSize(IS_BLOCKING, true);
        runForDifferentSizes();


        MPI.Finalize();
    }

    public static void runOneSize(boolean isBlocking, boolean checkResults) {
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();


        int n = 3;

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
            A.print2D(n, n);
            System.out.println("Matrix B:");
            B.print2D(n, n);
            System.out.println("Result:");
            final Matrix result = new Matrix(C, n * n, 1);
            result.print2D(n, n);

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


    private static void runForDifferentSizes() {
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (rank == 0)
            System.out.println("Results for " + size + " processors");

        List<Integer> arraySizes = List.of(3000);
        for (int n : arraySizes) {
            if (n % size != 0) {
                throw new IllegalArgumentException("Matrix size should be divisible by number of processors");
            }

            List<Long> timesBlocking = new ArrayList<>();
            List<Long> timesNonBlocking = new ArrayList<>();
            List<Long> times = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                Matrix A = Matrix.generateRandom(n, n);
                Matrix B = Matrix.generateRandom(n, n);
                double[] C = new double[n * n];
                if (j == 0) {
                    final long start_time = System.currentTimeMillis();
                    MatrixMultiplication.multiply(A, B, n, rank, size);
                    if (rank == 0) {
                        final long time = System.currentTimeMillis() - start_time;
                        System.out.println("Sequential: " + time + " ms taken for " + n + " elements in an array");
                        times.add(time);
                    }
                }

                final long start_time_blocking = System.currentTimeMillis();

                Blocking.multiply(rank, size, n, A, B, C);
                MPI.COMM_WORLD.Barrier();
                if (rank == 0) {
                    final long timeBlock = System.currentTimeMillis() - start_time_blocking;
                    System.out.println("BL: " + timeBlock + " ms taken for " + n + " elements in an array");
                    timesBlocking.add(timeBlock);
                }

                final long start_time_non_blocking = System.currentTimeMillis();
                NonBlocking.multiply(rank, size, n, A, B, C);
                MPI.COMM_WORLD.Barrier();
                if (rank == 0) {
                    final long timeNonBl = System.currentTimeMillis() - start_time_non_blocking;
                    System.out.println("NON-BL: " + timeNonBl + " ms taken for " + n + " elements in an array");
                    timesNonBlocking.add(timeNonBl);
                }
            }

            // count average time
            if (rank == 0) {
                System.out.println("\r\n\n");
                long sumBlock = 0;
                long sumNonBlock = 0;
                for (Long time : timesBlocking) {
                    sumBlock += time;
                }
                for (Long time : timesNonBlocking) {
                    sumNonBlock += time;
                }
                final double avgBlock = (double) sumBlock / timesBlocking.size();
                final double avgNonBlock = (double) sumNonBlock / timesBlocking.size();
                final double avg = (double) times.stream().mapToLong(Long::longValue).sum() / times.size();
                System.out.println("BLOCKING Average time for " + n + " elements in an array: " + avgBlock + " ms");
                System.out.println("NON-BLOCKING Average time for " + n + " elements in an array: " + avgNonBlock + " ms");
                System.out.println("\r\n\n");
                System.out.println("Speedup Blocking " + avg / avgBlock);
                System.out.println("Speedup Non-Blocking " + avg / avgNonBlock);
                System.out.println("\r\n\n");
            }

        }
    }


}