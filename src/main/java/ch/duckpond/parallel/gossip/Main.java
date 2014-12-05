package ch.duckpond.parallel.gossip;

import mpi.MPI;

public class Main {
    
    private static final double FRONT_END_RATIO = 0.10;
    
    public static void main(String args[]) throws Exception {
        MPI.Init(args);
        if (MPI.COMM_WORLD.Rank() <= MPI.COMM_WORLD.Size() * FRONT_END_RATIO) {
            new FrontEnd();
        } else {
            new Replica();
        }
        MPI.Finalize();
    }
}
