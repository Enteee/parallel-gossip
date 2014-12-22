package ch.duckpond.parallel.gossip;

import java.io.Serializable;

import mpi.MPI;
import ch.duckpond.parallel.gossip.utils.TimeVector;

public class NodeInformation implements Serializable {
	private static final long serialVersionUID = 1L;

	private TimeVector timeStamp = new TimeVector(MPI.COMM_WORLD.Size());
	private Class<? extends Node> nodeType;
	private int rank;

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