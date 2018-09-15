import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by anish on 11/17/2017.
 * Message queue object for handling message passing
 * Contains a vertex ID and corresponding message queue
 */


public class msgID {
    public LinkedBlockingQueue<Double> msgQ;
    public int vID;
    public msgID(int VertexID) {
        this.vID = VertexID;
        this.msgQ = new LinkedBlockingQueue<>();
    }
}
