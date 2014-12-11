package ch.duckpond.parallel.gossip;

import mpi.MPI;

import org.apache.log4j.Logger;

public class Main {

	private static final double FRONT_END_RATIO = 0.10;

	public static Logger LOG = Logger.getLogger(Main.class);

	public static void main(String args[]) throws Exception {
		MPI.Init(args);
		if (MPI.COMM_WORLD.Rank() <= MPI.COMM_WORLD.Size() * FRONT_END_RATIO) {
			new Replica();
		} else {
			new Replica();
		}
		LOG.info("Shutting down...");
		MPI.Finalize();
	}
}
