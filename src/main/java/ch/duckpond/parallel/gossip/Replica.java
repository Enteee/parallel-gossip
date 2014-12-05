package ch.duckpond.parallel.gossip;

import mpi.MPI;
import ch.duckpond.parallel.gossip.messages.HelloWorldMessage;
import ch.duckpond.parallel.gossip.utils.TimeVector;

public class Replica extends Node {
    
    private final TimeVector timeVector = new TimeVector(MPI.COMM_WORLD.Size());
    
    public Replica() {
        try {
            messageOutQueue.put(new HelloWorldMessage(1));
            while (true) {
                messageInQueue.take();
                logger.info("MESSAGE GOT!");
            }
        } catch (InterruptedException e) {
            logger.info("Failed putting message");
        }
    }
}
