package ch.duckpond.parallel.gossip;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import mpi.MPI;

import org.apache.log4j.Logger;

public class Main {

	private static final double FRONT_END_RATIO = 0.10;

	public static Random RND = new Random();
	private static Logger LOG = Logger.getLogger(Main.class);
	private static List<Replica> REPLICAS = new CopyOnWriteArrayList<>();
	private static List<FrontEnd> FRONT_ENDS = new CopyOnWriteArrayList<>();

	public static Replica getRandomReplica() {
		return REPLICAS.get(RND.nextInt(REPLICAS.size()));
	}

	public static List<Replica> getRandomReplicas(double amountPercentage) {
		if (amountPercentage < 0 || amountPercentage > 1.0) {
			throw new IllegalArgumentException("amountPercentage");
		}
		final List<Replica> randomReplicas = new ArrayList<>(REPLICAS);
		final int amount = (int) Math.round(REPLICAS.size() * amountPercentage);
		while (randomReplicas.size() > amount) {
			randomReplicas.remove(RND.nextInt(randomReplicas.size()));
		}
		return randomReplicas;
	}

	public static void main(String args[]) throws Exception {
		MPI.Init(args);
		if (MPI.COMM_WORLD.Rank() <= MPI.COMM_WORLD.Size() * FRONT_END_RATIO) {
			FRONT_ENDS.add(new FrontEnd());
		} else {
			REPLICAS.add(new Replica());
		}
		LOG.info("Shutting down...");
		MPI.Finalize();
	}
}
