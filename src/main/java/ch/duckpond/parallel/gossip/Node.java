package ch.duckpond.parallel.gossip;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import mpi.MPI;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ch.duckpond.parallel.gossip.messages.Message;

public abstract class Node {
    
    private abstract class MessageQueue extends LinkedBlockingQueue<Message> implements Runnable {
        
        private static final long   serialVersionUID = 1L;
        private final AtomicBoolean disposed         = new AtomicBoolean(false);
        
        public void dispose() {
            disposed.set(true);
        }
        
        protected boolean isDisposed() {
            return disposed.get();
        }
    }
    class MessageOutQueue extends MessageQueue {
        
        private static final long serialVersionUID = 1L;
        
        @Override
        public void run() {
            try {
                while (!isDisposed()) {
                    Message[] message = new Message[1];
                    message[0] = take();
                    logger.info("sending:" + message[0]);
                    MPI.COMM_WORLD.Send(message, 0, 1, MPI.OBJECT, message[0].getDestination(), MPI.ANY_TAG);
                }
            } catch (InterruptedException e) {
                logger.info("Out queue interrupted", e);
            }
        }
    }
    class MessageInQueue extends MessageQueue {
        
        private static final long serialVersionUID = 1L;
        private final Message[]   message          = new Message[1];
        
        @Override
        public void run() {
            try {
                while (!isDisposed()) {
                    MPI.COMM_WORLD.Recv(message, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, MPI.ANY_TAG);
                    put(message[0]);
                }
            } catch (InterruptedException e) {
                logger.info("In queue interrupted", e);
            }
        }
    }
    private static final String     LOG_DIR               = "bin/";
    protected final Logger          logger;
    protected final MessageOutQueue messageOutQueue       = new MessageOutQueue();
    protected final MessageInQueue  messageInQueue        = new MessageInQueue();
    private final Thread            messageOutQueueThread = new Thread(messageOutQueue);
    private final Thread            messageInQueueThread  = new Thread(messageInQueue);
    
    public Node() {
        // Log file per node
        final FileAppender fa = new FileAppender();
        final int rank = MPI.COMM_WORLD.Rank();
        fa.setName("MPI-FileLogger");
        fa.setFile(LOG_DIR + "/node-" + rank + ".log");
        fa.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"));
        fa.setThreshold(Level.DEBUG);
        fa.setAppend(false);
        fa.activateOptions();
        // append file logger
        logger = Logger.getLogger(getClass().getName() + ":" + rank);
        logger.addAppender(fa);
        logger.info(rank + " : " + this.getClass().getName());
        // start in & out queue
        messageInQueueThread.start();
        messageOutQueueThread.start();
    }
    
    @Override
    protected void finalize() throws Throwable {
        messageInQueue.dispose();
        messageOutQueue.dispose();
    }
}
