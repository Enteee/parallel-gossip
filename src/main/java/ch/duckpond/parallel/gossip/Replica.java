package ch.duckpond.parallel.gossip;

import mpi.MPI;
import ch.duckpond.parallel.gossip.utils.TimeVector;

public class Replica extends Node {

	/**
	 * Amount of replicas to deliver with gossip messages in %.
	 */
	private static final double REPLICA_GOSSIP_PERCENTAGE = 0.1;
	/**
	 * Timeout for polling messages [s]
	 */
	private static final long TIMEOUT = 2;

	public Replica() {
		log.info("Replica started");
	}

	@Override
	public void start() {
		do {
			try {
				handleMessages(TIMEOUT * 1000);
			} catch (InterruptedException e) {
				log.info("Failed putting message");
			}
			gossip();
		} while (true);
	}

	private void gossip() {
		for (final NodeInformation replica : getRandomReplicas(REPLICA_GOSSIP_PERCENTAGE)) {
			// TODO: Send only new messages for replica
			this.sendGossipMessage(replica.getRank(), new TimeVector(
					MPI.COMM_WORLD.Size()));
		}
	}

}
