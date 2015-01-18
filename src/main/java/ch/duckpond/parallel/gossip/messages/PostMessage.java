package ch.duckpond.parallel.gossip.messages;

import ch.duckpond.parallel.gossip.BulletinMessage;
import ch.duckpond.parallel.gossip.Node;

public class PostMessage extends Message {
	private static final long serialVersionUID = 1L;

	private final BulletinMessage bulletinMessage;

	public PostMessage(final int destination,
			final BulletinMessage bulletinMessage) {
		super(destination);
		this.bulletinMessage = bulletinMessage;
	}

	@Override
	public void handle(final Node node) {
		node.postBulletinMessage(bulletinMessage);
	}

}
