//import mpi.MPI;
//
//public class SyncMultiply {
//
//    public static void multiply(int rank, int size, int n, double[] A, double[] B, double[] C) {
//        int rowsPerProcess = n / size;
//
//        if (rank == 0) {
//            for (int dest = 1; dest < size; dest++) {
//                MPI.COMM_WORLD.Send(A, dest*rowsPerProcess* n, rowsPerProcess* n, MPI.DOUBLE, dest, 0);
//            }
//        } else {
//            MPI.COMM_WORLD.Recv(A, rank *rowsPerProcess* n, rowsPerProcess* n, MPI.DOUBLE, 0, 0);
//        }
//        for (int source = 0; source < size; source++) {
//            if (rank == source) {
//                MPI.COMM_WORLD.Send(B, 0, n * n, MPI.DOUBLE, (rank +1)% size, 0);
//            } else if (rank == (source+1)% size) {
//                MPI.COMM_WORLD.Recv(B, 0, n * n, MPI.DOUBLE, source, 0);
//            }
//            MPI.COMM_WORLD.Barrier();
//        }
//
//        for (int i = rank *rowsPerProcess; i < (rank +1)*rowsPerProcess; i++) {
//            for (int j = 0; j < n; j++) {
//                for (int k = 0; k < n; k++) {
//                    C[i* n +j] += A[i* n +k] * B[k* n +j];
//                }
//            }
//        }
//        if (rank == 0) {
//            for (int source = 1; source < size; source++) {
//                MPI.COMM_WORLD.Recv(C, source*rowsPerProcess* n, rowsPerProcess* n, MPI.DOUBLE, source, 0);
//            }
//        } else {
//            MPI.COMM_WORLD.Send(C, rank *rowsPerProcess* n, rowsPerProcess* n, MPI.DOUBLE, 0, 0);
//        }
//    }
//}