package ch.duckpond.parallel.gossip;

import java.util.Random;

import mpi.MPI;

import org.apache.log4j.Logger;

public class Main {

	private static final double FRONT_END_RATIO = 0.5;

	public final static Random RND = new Random();
	private final static Logger LOG = Logger.getLogger(Main.class);

	public static void main(String args[]) throws Exception {
		MPI.Init(args);
		final Node node;
		// at least one frontend and one replica
		if (MPI.COMM_WORLD.Size() < 2) {
			throw new RuntimeException("network must have at least 2 nodes");
		}
		if (MPI.COMM_WORLD.Rank() == 0
				|| (MPI.COMM_WORLD.Rank() != MPI.COMM_WORLD.Size() - 1 && MPI.COMM_WORLD
						.Rank() < MPI.COMM_WORLD.Size() * FRONT_END_RATIO)) {
			node = new FrontEnd();
		} else {
			node = new Replica();
		}
		// add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				node.dispose();
			}
		});
		node.start();
		LOG.info("shutting down...");
		MPI.Finalize();
	}
}
