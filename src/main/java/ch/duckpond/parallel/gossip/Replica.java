package ch.duckpond.parallel.gossip;

import mpi.MPI;
import ch.duckpond.parallel.gossip.messages.HelloWorldMessage;
import ch.duckpond.parallel.gossip.messages.Message;
import ch.duckpond.parallel.gossip.utils.TimeVector;

public class Replica extends Node {

	private final TimeVector timeVector = new TimeVector(MPI.COMM_WORLD.Size());

	public Replica() {
		// try {
		// messageOutQueue.put(new HelloWorldMessage(1));

		Message message[] = { new HelloWorldMessage(0) };
		MPI.COMM_WORLD.Send(message, 0, 1, MPI.OBJECT,
				message[0].getDestination(), MPI.ANY_TAG);

		// while (true) {
		MPI.COMM_WORLD.Recv(message, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE,
				MPI.ANY_TAG);
		logger.info("MESSAGE GOT:" + message[0].getDestination());
		// logger.info("MESSAGE GOT:"+
		// messageInQueue.take().getDestination());
		// }

		// } catch (InterruptedException e) {
		// logger.info("Failed putting message");
		// }
	}
}
