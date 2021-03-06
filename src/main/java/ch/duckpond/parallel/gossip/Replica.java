package ch.duckpond.parallel.gossip;

import mpi.MPI;
import ch.duckpond.parallel.gossip.utils.TimeVector;

public class Replica extends Node {

	/**
	 * Amount of replicas to deliver with gossip messages in %.
	 */
	private static final double REPLICA_GOSSIP_PERCENTAGE = 0.1;
	/**
	 * Timeout for polling messages [ms]
	 */
	private static final long TIMEOUT = 10000;

	public Replica() {
		log.info("Replica started");
	}

	@Override
	public void run() {
		while (!isDisposed()) {
			try {
				handleMessages(TIMEOUT);
			} catch (final InterruptedException e) {
				log.info("Failed putting message");
			}
			log.debug("messages count:" + getBulletinMessages().size());
			gossip();
		}
	}

	private void gossip() {
		for (final NodeInformation replica : getRandomReplicas(
				REPLICA_GOSSIP_PERCENTAGE, false)) {
			// TODO: Send only new messages for replica
			sendGossipMessage(replica.getRank(),
					new TimeVector(MPI.COMM_WORLD.Size()));
		}
	}

}
