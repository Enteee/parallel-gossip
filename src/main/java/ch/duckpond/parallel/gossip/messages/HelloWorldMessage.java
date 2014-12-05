package ch.duckpond.parallel.gossip.messages;

public class HelloWorldMessage extends Message {
    
    private static final long serialVersionUID = 1L;
    
    public HelloWorldMessage(int destination) {
        super(destination);
    }
}
