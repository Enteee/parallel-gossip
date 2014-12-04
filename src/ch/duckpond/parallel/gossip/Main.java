package ch.duckpond.parallel.gossip;

import mpi.MPI;

public class Main {
    
    public static void main(String args[]) throws Exception {
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println("Hi from <" + me + ">");
        MPI.Finalize();
    }
}
