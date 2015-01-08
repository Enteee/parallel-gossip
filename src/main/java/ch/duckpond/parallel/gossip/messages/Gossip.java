package ch.duckpond.parallel.gossip.messages;

import java.util.Set;
import java.util.TreeSet;

import ch.duckpond.parallel.gossip.BulletinMessage;
import ch.duckpond.parallel.gossip.Node;

public class Gossip extends Message {

	private static final long serialVersionUID = 1L;

	private final Set<BulletinMessage> bulletinMessages = new TreeSet<>();

	public Gossip(int destination, final Set<BulletinMessage> bulletinMessages) {
		super(destination);
		if (bulletinMessages == null) {
			throw new IllegalArgumentException("bulletinMessages || timeStamp");
		}
		this.bulletinMessages.addAll(bulletinMessages);
	}

	@Override
	public void handle(final Node node) {
		node.addGossipMessages(bulletinMessages);
	}
}
