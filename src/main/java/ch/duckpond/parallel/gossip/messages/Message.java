package ch.duckpond.parallel.gossip.messages;

import java.io.Serializable;

import ch.duckpond.parallel.gossip.utils.TimeVector;

public abstract class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private transient final int destination;
	private TimeVector timeVector;

	public Message(final int destination) {
		this.destination = destination;
	}

	public int getDestination() {
		return destination;
	}
}
