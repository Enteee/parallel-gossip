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

	private final int source;
	private final TimeVector after;

	public QueryMessage(final int destination, final int source,
			final TimeVector after) {
		super(destination);
		this.source = source;
		this.after = after;
	}

	@Override
	public void handle(final Node node) {
		node.sendGossipMessage(source, after);
	}

}
