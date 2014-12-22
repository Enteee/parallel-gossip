package ch.duckpond.parallel.gossip;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import mpi.MPI;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ch.duckpond.parallel.gossip.messages.Gossip;
import ch.duckpond.parallel.gossip.messages.Message;
import ch.duckpond.parallel.gossip.utils.TimeVector;

public abstract class Node {

	private static final String LOG_DIR = "bin/";

	private abstract class MessageQueue extends LinkedBlockingQueue<Message>
			implements Runnable {

		private static final long serialVersionUID = 1L;
		private final AtomicBoolean disposed = new AtomicBoolean(false);

		public void dispose() {
			disposed.set(true);
		}

		protected boolean isDisposed() {
			return disposed.get();
		}
	}

	class MessageOutQueue extends MessageQueue {

		private static final long serialVersionUID = 1L;
		/**
		 * Tag used for arbitrary message sending (Send untagged). !!!NOTE:
		 * DON't USE MPI.ANY_TAG for this!!!!
		 */
		public static final int TAG_ARBITRARY = 1234;

		@Override
		public void run() {
			try {
				while (!isDisposed()) {
					Message[] message = new Message[1];
					message[0] = take();
					log.info("sending:" + message[0]);
					MPI.COMM_WORLD.Send(message, 0, 1, MPI.OBJECT,
							message[0].getDestination(), TAG_ARBITRARY);
				}
			} catch (InterruptedException e) {
				log.info("Out queue interrupted", e);
			}
		}
	}

	class MessageInQueue extends MessageQueue {

		private static final long serialVersionUID = 1L;
		private final Message[] message = new Message[1];

		@Override
		public void run() {
			try {
				while (!isDisposed()) {
					MPI.COMM_WORLD.Recv(message, 0, 1, MPI.OBJECT,
							MPI.ANY_SOURCE, MPI.ANY_TAG);
					log.info("received:" + message[0]);
					put(message[0]);
				}
			} catch (InterruptedException e) {
				log.info("In queue interrupted", e);
			}
		}
	}

	protected final MessageOutQueue messageOutQueue = new MessageOutQueue();
	protected final MessageInQueue messageInQueue = new MessageInQueue();
	protected final Logger log;
	private final Thread messageOutQueueThread = new Thread(messageOutQueue);
	private final Thread messageInQueueThread = new Thread(messageInQueue);
	private final int rank = MPI.COMM_WORLD.Rank();
	private final TimeVector timeStamp = new TimeVector(MPI.COMM_WORLD.Size());
	private final Set<BulletinMessage> bulletinMessages = new HashSet<>();
	private final Set<BulletinMessage> futureBulletinMessages = new HashSet<>();
	private final HashMap<Integer, TimeVector> otherTimeStamps = new HashMap<>();

	protected Node() {
		// add for each node a time stamp
		for (int i = 0; i < MPI.COMM_WORLD.Size(); i++) {
			otherTimeStamps.put(i, new TimeVector(MPI.COMM_WORLD.Size()));
		}
		// Log file per node
		final FileAppender fa = new FileAppender();
		final int rank = MPI.COMM_WORLD.Rank();
		fa.setName("MPI-FileLogger");
		fa.setFile(LOG_DIR + "/node-" + rank + ".log");
		fa.setLayout(new PatternLayout(
				"%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"));
		fa.setThreshold(Level.DEBUG);
		fa.setAppend(false);
		fa.activateOptions();
		// append file logger
		log = Logger.getLogger(getClass().getName() + "[" + rank + "]");
		log.addAppender(fa);
		log.info(rank + " : " + this.getClass().getName());
		// start in & out queue
		messageInQueueThread.start();
		messageOutQueueThread.start();
	}

	@Override
	protected void finalize() throws Throwable {
		messageInQueue.dispose();
		messageOutQueue.dispose();
	}

	public int getRank() {
		return rank;
	}

	public Logger getLogger() {
		return log;
	}

	protected TimeVector getTimeStamp() {
		return timeStamp;
	}

	protected TimeVector nextTimeStamp() {
		timeStamp.increment(getRank());
		return timeStamp;
	}

	/**
	 * Add messages and max this time stamp
	 * 
	 * @param bulletinMessages
	 *            the @{link BulletinMessage}s to add.
	 */
	public void addGossipMessages(final Set<BulletinMessage> bulletinMessages) {
		for (final BulletinMessage bulletinMessage : bulletinMessages) {
			this.bulletinMessages.add(bulletinMessage);
			timeStamp.max(bulletinMessage.getTimeStamp());
		}
		// check if some of the future messages can be added now
		final HashSet<BulletinMessage> moreBulletinMessage = new HashSet<>();
		for (final BulletinMessage bulletinMessage : futureBulletinMessages) {
			if (bulletinMessage.getTimeStamp().isLessOrEqual(timeStamp)) {
				moreBulletinMessage.add(bulletinMessage);
			}
		}
		if (moreBulletinMessage.size() > 0) {
			addGossipMessages(moreBulletinMessage);
		}
	}

	public void postBulletinMessage(final BulletinMessage bulletinMessage) {
		if (bulletinMessage.getTimeStamp().isLessOrEqual(timeStamp)) {
			bulletinMessages.add(bulletinMessage);
		} else {
			futureBulletinMessages.add(bulletinMessage);
		}
	}

	public void sendGossipMessage(final int destination,
			final TimeVector timeStampAfter) {
		final HashSet<BulletinMessage> gossipMessage = new HashSet<>();
		for (final BulletinMessage bulletinMessage : this.bulletinMessages) {
			if (timeStampAfter.isLessOrEqual(bulletinMessage.getTimeStamp())) {
				gossipMessage.add(bulletinMessage);
			}
		}
		try {
			messageOutQueue.put(new Gossip(destination, gossipMessage));
		} catch (InterruptedException e) {
			log.error("gossip sending interrupted", e);
		}
	}

	/**
	 * Handles all messages, waits at least for timeout [ms] if there are no new
	 * messages.
	 * 
	 * @param timeout
	 *            the timeout [ms] to wait for messages
	 * @throws InterruptedException
	 */
	protected void handleMessages(final long timeout)
			throws InterruptedException {
		do {
			Message message = messageInQueue.poll(timeout,
					TimeUnit.MILLISECONDS);
			// timeout?
			if (message != null) {
				message.handle(this);
			}
		} while (messageInQueue.size() > 0);
	}
}
