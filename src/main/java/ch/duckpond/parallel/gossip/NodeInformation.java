package ch.duckpond.parallel.gossip;

import java.io.Serializable;

public class NodeInformation implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Class<? extends Node> nodeType;
	private final int rank;

	public NodeInformation(final Class<? extends Node> nodeType, final int rank) {
		if (nodeType == null) {
			throw new IllegalArgumentException("nodeType");
		}
		this.nodeType = nodeType;
		this.rank = rank;
	}

	public Class<? extends Node> getNodeType() {
		return nodeType;
	}

	public int getRank() {
		return rank;
	}
}
