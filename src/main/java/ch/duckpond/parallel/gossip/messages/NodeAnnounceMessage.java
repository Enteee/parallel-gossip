package ch.duckpond.parallel.gossip.messages;

import ch.duckpond.parallel.gossip.Node;
import ch.duckpond.parallel.gossip.NodeInformation;

public class NodeAnnounceMessage extends Message {

	private static final long serialVersionUID = 1L;

	private final NodeInformation nodeInformation;

	public NodeAnnounceMessage(final int destination,
			final NodeInformation nodeInformation) {
		super(destination);
		this.nodeInformation = nodeInformation;
	}

	@Override
	public void handle(Node node) {
		node.addNodeInformation(nodeInformation);
	}

}
