package ch.duckpond.parallel.gossip.messages;

import ch.duckpond.parallel.gossip.Node;
import ch.duckpond.parallel.gossip.utils.TimeVector;

/**
 * Query all messages after a certain time stamp.
 * 
 * @author ente
 *
 */
public class QueryMessage extends Message {
	private static final long serialVersionUID = 1L;

	private int source;
	private TimeVector after;

	public QueryMessage(int destination, int source, final TimeVector after) {
		super(destination);
		this.source = source;
		this.after = after;
	}

	@Override
	public void handle(Node node) {
		node.sendGossipMessage(source, after);
	}

}
