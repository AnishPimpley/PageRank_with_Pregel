import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;


public interface Vertex<V, M> extends Runnable{
    int getVertexID();
    V getValue();

    void compute(LinkedBlockingQueue<M> messages);
    void sendMessageTo(int vertexID, M message);
    void voteToHalt();
}
