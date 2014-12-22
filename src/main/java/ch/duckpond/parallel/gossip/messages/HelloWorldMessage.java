package ch.duckpond.parallel.gossip.messages;

import ch.duckpond.parallel.gossip.Node;

public class HelloWorldMessage extends Message {

	private static final long serialVersionUID = 1L;

	public HelloWorldMessage(int destination) {
		super(destination);
	}

	@Override
	public void handle(final Node node) {
		node.getLogger().info("Hello world!");
	}
}
