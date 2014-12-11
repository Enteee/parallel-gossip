package ch.duckpond.parallel.gossip;

import mpi.MPI;

import org.apache.log4j.Logger;

public class Main {

	private static final double FRONT_END_RATIO = 0.10;

	public static Logger LOG = Logger.getLogger(Main.class);

	public static void main(String args[]) throws Exception {
		MPI.Init(args);
		int message[] = { 1 };
		// if (MPI.COMM_WORLD.Rank() <= MPI.COMM_WORLD.Size() * FRONT_END_RATIO)
		// {
		if (MPI.COMM_WORLD.Rank() == 1) {
			// send
			MPI.COMM_WORLD.Isend(message, 0, 1, MPI.INT, 0, MPI.ANY_TAG);
		} else if (MPI.COMM_WORLD.Rank() == 0) {
			// send
			MPI.COMM_WORLD.Isend(message, 0, 1, MPI.INT, 1, MPI.ANY_TAG);
		} else {
			throw new RuntimeException("TOO many nodes!");
		}
		// null
		message = new int[1];
		// receive
		MPI.COMM_WORLD
				.Recv(message, 0, 1, MPI.INT, MPI.ANY_SOURCE, MPI.ANY_TAG);
		LOG.info("MESSAGE GOT:" + message[0]);
		LOG.info("Shutting down...");
		MPI.Finalize();
	}
}
