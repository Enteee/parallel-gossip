package ch.duckpond.parallel.gossip;

import mpi.MPI;
import ch.duckpond.parallel.gossip.messages.HelloWorldMessage;
import ch.duckpond.parallel.gossip.messages.Message;
import ch.duckpond.parallel.gossip.utils.TimeVector;

public class Replica extends Node {

	private final TimeVector timeVector = new TimeVector(MPI.COMM_WORLD.Size());

	public Replica() {
		try {
			messageOutQueue.put(new HelloWorldMessage(1));
			Message message[] = { new HelloWorldMessage(0) };
			logger.info("MESSAGE GOT:" + messageInQueue.take().getDestination());
		} catch (InterruptedException e) {
			logger.info("Failed putting message");
		}
	}
}
