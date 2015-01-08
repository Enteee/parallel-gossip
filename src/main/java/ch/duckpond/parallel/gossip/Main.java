package ch.duckpond.parallel.gossip;

import java.util.Random;

import mpi.MPI;

import org.apache.log4j.Logger;

public class Main {

	private static final double FRONT_END_RATIO = 0.10;

	public static Random RND = new Random();
	private static Logger LOG = Logger.getLogger(Main.class);

	public static void main(String args[]) throws Exception {
		MPI.Init(args);
		Node node;
		if (MPI.COMM_WORLD.Rank() < MPI.COMM_WORLD.Size() * FRONT_END_RATIO) {
			node = new FrontEnd();
		} else {
			node = new Replica();
		}
		node.start();
		LOG.info("shutting down...");
		MPI.Finalize();
	}
}
