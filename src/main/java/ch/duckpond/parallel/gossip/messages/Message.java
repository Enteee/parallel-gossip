package ch.duckpond.parallel.gossip.messages;

import java.io.Serializable;

import ch.duckpond.parallel.gossip.Node;

public abstract class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private transient final int destination;

	public Message(final int destination) {
		this.destination = destination;
	}

	public int getDestination() {
		return destination;
	}

	/**
	 * Handle the message
	 *
	 * @param node
	 *            target node
	 */
	abstract public void handle(final Node node);
}
