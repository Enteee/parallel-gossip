package ch.duckpond.parallel.gossip;

import ch.duckpond.parallel.gossip.messages.PostMessage;
import ch.duckpond.parallel.gossip.messages.QueryMessage;
import ch.duckpond.parallel.gossip.utils.Utils;

public class FrontEnd extends Node {

	/**
	 * Maximum timeout [s]
	 */
	private static final double MAX_TIMEOUT = 10;
	/**
	 * Minimum timeout [s]
	 */
	private static final double MIN_TIMEOUT = 2;
	/**
	 * Relative chance for a post message being sent
	 */
	private static final int CHANCE_POST = 1;
	/**
	 * Relative chance for a query message being sent.
	 */
	private static final int CHANCE_QUERY = 1;
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

	public void start() {
		do {
			// time of the loop start [ms]
			long loopStartTime = System.currentTimeMillis();
			// random timeout [ms]
			long timeOut = (long) (MIN_TIMEOUT * 1000 + Main.RND
					.nextInt((int) ((MAX_TIMEOUT - MIN_TIMEOUT) * 1000)));
			long timeOutLeft = timeOut;
			try {
				while (timeOutLeft > 0) {
					handleMessages(timeOutLeft);
					timeOutLeft = timeOut
							- (System.currentTimeMillis() - loopStartTime);
				}
			} catch (InterruptedException e) {
				log.info("sleep interrupted", e);
			}
			// perform a random action
			int rndSelect = Main.RND.nextInt(CHANCE_TOTAL);
			if (rndSelect < CHANCE_POST) {
				post();
			}
			rndSelect -= CHANCE_POST;
			if (rndSelect < CHANCE_QUERY) {
				query();
			}
			rndSelect -= CHANCE_QUERY;
		} while (true);
	}

	private void post() {
		try {
			messageOutQueue.put(new PostMessage(getRandomReplica().getRank(),
					new BulletinMessage(AUTHOR_PREFIX + getRank(), Utils
							.rndString(), Utils.rndString(), nextTimeStamp())));
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
