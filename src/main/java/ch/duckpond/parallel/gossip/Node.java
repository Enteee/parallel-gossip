package ch.duckpond.parallel.gossip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import mpi.MPI;
import mpi.Request;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ch.duckpond.parallel.gossip.messages.Gossip;
import ch.duckpond.parallel.gossip.messages.Message;
import ch.duckpond.parallel.gossip.messages.NodeAnnounceMessage;
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
					log.debug("sending: " + message[0]);
					MPI.COMM_WORLD.Isend(message, 0, 1, MPI.OBJECT,
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
		private Request req;

		@Override
		public void run() {
			try {
				while (!isDisposed()) {
					req = MPI.COMM_WORLD.Irecv(message, 0, 1, MPI.OBJECT,
							MPI.ANY_SOURCE, MPI.ANY_TAG);
					req.Wait();
					log.debug("received: " + message[0]);
					put(message[0]);
				}
			} catch (InterruptedException e) {
				log.info("In queue interrupted", e);
			}
		}

		@Override
		public void dispose() {
			if (req != null && !req.Is_null()) {
				req.Cancel();
			}
			super.dispose();
		}
	}

	private CyclicBarrier disposalBarrier = new CyclicBarrier(2);
	protected final MessageOutQueue messageOutQueue = new MessageOutQueue();
	protected final MessageInQueue messageInQueue = new MessageInQueue();
	protected final Logger log;
	private final NodeInformation nodeInformation = new NodeInformation(
			getClass(), MPI.COMM_WORLD.Rank());
	private final Thread messageOutQueueThread = new Thread(messageOutQueue);
	private final Thread messageInQueueThread = new Thread(messageInQueue);
	private final int rank = MPI.COMM_WORLD.Rank();
	private final TimeVector timeStamp = new TimeVector(MPI.COMM_WORLD.Size());
	private final Set<BulletinMessage> bulletinMessages = new TreeSet<>();
	private final List<BulletinMessage> bulletinMessagesOrdered = new LinkedList<>();
	private final Set<BulletinMessage> futureBulletinMessages = new TreeSet<>();
	private final HashMap<Integer, TimeVector> otherNodes = new HashMap<>();
	private List<NodeInformation> replicas = new ArrayList<>();
	private List<NodeInformation> front_ends = new ArrayList<>();
	private AtomicBoolean disposed = new AtomicBoolean(false);

	protected Node() {
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
		// announce this node
		for (int i = 0; i < MPI.COMM_WORLD.Size(); i++) {
			try {
				messageOutQueue
						.put(new NodeAnnounceMessage(i, nodeInformation));
			} catch (InterruptedException e) {
				log.error("interrupted while sending node announcement", e);
			}
			otherNodes.put(i, new TimeVector(MPI.COMM_WORLD.Size()));
		}
		do {
			try {
				handleMessage(messageInQueue.take());
			} catch (InterruptedException e) {
				log.error("interrupted while reading announcement messages");
			}
		} while (replicas.size() <= 0 && front_ends.size() <= 0);

	}

	final public void start() {
		run();
		awaitDisposal();
	}

	abstract public void run();

	/**
	 * Stops this node an blocks until stopped.
	 */
	public void dispose() {
		log.debug("dispose..");
		messageInQueue.dispose();
		messageOutQueue.dispose();
		disposed.set(true);
		awaitDisposal();
		printBulletinMessagesOrdered();
	}

	private final void awaitDisposal() {
		try {
			log.debug(String.format("disposalBarrier (%d / %d)",
					disposalBarrier.getNumberWaiting(),
					disposalBarrier.getParties()));
			disposalBarrier.await();
			log.debug("...disposalBarrier");
		} catch (InterruptedException | BrokenBarrierException e) {
			log.error("Failed awaiting shutdown", e);
		}

	}

	protected boolean isDisposed() {
		return disposed.get();
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

	protected NodeInformation getRandomReplica() {
		return replicas.get(Main.RND.nextInt(replicas.size()));
	}

	/**
	 * Get some random replicas but at lest one
	 * 
	 * @param amountPercentage
	 *            amount of random replicas in % of total number of replicas
	 * @param containsSelf
	 *            if @{code true} the list might contain this
	 * @return a list of random replicas
	 */
	protected List<NodeInformation> getRandomReplicas(double amountPercentage,
			boolean containsSelf) {
		if (amountPercentage < 0 || amountPercentage > 1.0) {
			throw new IllegalArgumentException("amountPercentage");
		}
		final List<NodeInformation> randomReplicas = new ArrayList<>(replicas);
		if (!containsSelf) {
			randomReplicas.remove(this);
		}
		final int amount = (int) Math.round(randomReplicas.size()
				* amountPercentage);
		while (randomReplicas.size() > amount && randomReplicas.size() > 1) {
			randomReplicas.remove(Main.RND.nextInt(randomReplicas.size()));
		}
		return randomReplicas;
	}

	public Set<BulletinMessage> getBulletinMessages() {
		return bulletinMessages;
	}

	private void printBulletinMessagesOrdered() {
		for (final BulletinMessage bulletinMessage : bulletinMessagesOrdered) {
			log.info(bulletinMessage);
		}
	}

	public void addBulletinMessage(final BulletinMessage bulletinMessage) {
		if (bulletinMessages.add(bulletinMessage)) {
			bulletinMessagesOrdered.add(bulletinMessage);
		}
		getTimeStamp().max(bulletinMessage.getTimeStamp());
	}

	public void addNodeInformation(final NodeInformation nodeInformation) {
		if (nodeInformation.getNodeType() == Replica.class) {
			replicas.add(nodeInformation);
		} else if (nodeInformation.getNodeType() == FrontEnd.class) {
			front_ends.add(nodeInformation);
		} else {
			log.fatal("invalid node class");
		}
	}

	/**
	 * Add messages and max this time stamp
	 * 
	 * @param bulletinMessages
	 *            the @{link BulletinMessage}s to add.
	 */
	public void addGossipMessages(final Set<BulletinMessage> bulletinMessages) {
		for (final BulletinMessage bulletinMessage : bulletinMessages) {
			addBulletinMessage(bulletinMessage);
		}
		// check if some of the future messages can be added now
		final Set<BulletinMessage> moreBulletinMessages = new TreeSet<>();
		for (final BulletinMessage futureBulletinMessage : futureBulletinMessages) {
			if (futureBulletinMessage.getTimeStamp().isLessOrEqual(timeStamp)) {
				log.debug("future post is now");
				moreBulletinMessages.add(futureBulletinMessage);
			}
		}
		futureBulletinMessages.removeAll(moreBulletinMessages);
		bulletinMessages.addAll(moreBulletinMessages);
	}

	/**
	 * Post a new message
	 * 
	 * @param bulletinMessage
	 *            the new message to post
	 */
	public void postBulletinMessage(final BulletinMessage bulletinMessage) {
		getTimeStamp().increment(getRank());
		bulletinMessage.getTimeStamp().max(getTimeStamp());
		if (bulletinMessage.getTimeStamp().isLessOrEqual(timeStamp)) {
			log.debug("posted");
			addBulletinMessage(bulletinMessage);
		} else {
			log.debug("future post");
			futureBulletinMessages.add(bulletinMessage);
		}
	}

	public void sendGossipMessage(final int destination,
			final TimeVector timeStampAfter) {
		final HashSet<BulletinMessage> gossipMessage = new HashSet<>();
		for (final BulletinMessage bulletinMessage : getBulletinMessages()) {
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
	 * @param timeOut
	 *            the timeout [ms] to wait for messages
	 * @throws InterruptedException
	 */
	protected void handleMessages(final long timeOut)
			throws InterruptedException {
		// time of the loop start [ms]
		long handleStartTime = System.currentTimeMillis();
		// random timeout [ms]
		long timeOutLeft = timeOut;
		do {
			Message message = messageInQueue.poll(timeOutLeft,
					TimeUnit.MILLISECONDS);
			// timeout?
			if (message != null) {
				handleMessage(message);
			}
			timeOutLeft = timeOut
					- (System.currentTimeMillis() - handleStartTime);
		} while (timeOutLeft > 0 && !isDisposed());
	}

	/**
	 * Handle the specific message
	 * 
	 * @param message
	 *            the message to handle
	 */
	private void handleMessage(final Message message) {
		if (message == null) {
			throw new IllegalArgumentException("message");
		}
		message.handle(this);
	}
}
