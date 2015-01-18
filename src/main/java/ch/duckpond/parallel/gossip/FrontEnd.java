package ch.duckpond.parallel.gossip;

import java.util.Set;

import ch.duckpond.parallel.gossip.messages.PostMessage;
import ch.duckpond.parallel.gossip.messages.QueryMessage;
import ch.duckpond.parallel.gossip.utils.Utils;

public class FrontEnd extends Node {

	/**
	 * Maximum timeout [s]
	 */
	private static final double MAX_TIMEOUT = 2;
	/**
	 * Minimum timeout [s]
	 */
	private static final double MIN_TIMEOUT = 0;
	/**
	 * Relative chance for a post message being sent
	 */
	private static final int CHANCE_POST = 5;
	/**
	 * Relative chance for a query message being sent.
	 */
	private static final int CHANCE_QUERY = 4;
	/**
	 * Total of all chances
	 */
	private static final int CHANCE_TOTAL = CHANCE_POST + CHANCE_QUERY;
	/**
	 * Author prefix
	 */
	private static final String AUTHOR_PREFIX = "AutoFrontEnd";

	public FrontEnd() {
		log.info("FrontEnd started");
	}

	public void run() {
		while (!isDisposed()) {
			try {
				// random timeout [ms]
				long timeOut = (long) (MIN_TIMEOUT * 1000 + Main.RND
						.nextInt((int) ((MAX_TIMEOUT - MIN_TIMEOUT) * 1000)));
				handleMessages(timeOut);
			} catch (InterruptedException e) {
				log.info("sleep interrupted", e);
			}
			// perform a random action
			int rndSelect = Main.RND.nextInt(CHANCE_TOTAL);
			if ((rndSelect -= CHANCE_POST) < 0) {
				post();
			} else if ((rndSelect -= CHANCE_QUERY) < 0) {
				query();
			} else {
				log.fatal("something went wrong when selecting action");
			}
		}
	}

	@Override
	public void addGossipMessages(Set<BulletinMessage> bulletinMessages) {
		log.info("Messages:");
		for (final BulletinMessage bulletinMessage : bulletinMessages) {
			log.info(bulletinMessage);
		}
		super.addGossipMessages(bulletinMessages);
	}

	private void post() {
		try {
			messageOutQueue.put(new PostMessage(getRandomReplica().getRank(),
					new BulletinMessage(AUTHOR_PREFIX + getRank(), Utils
							.rndString(), Utils.rndString(), getTimeStamp(),
							getTimeStamp().toString())));
		} catch (InterruptedException e) {
			log.error("message post interrupted", e);
		}
	}

	private void query() {
		try {
			messageOutQueue.put(new QueryMessage(getRandomReplica().getRank(),
					getRank(), getTimeStamp()));
		} catch (InterruptedException e) {
			log.error("message query interrupted", e);
		}
	}
}
