//import java.lang.reflect.Array; //REMOVED
import java.util.*;
//import java.util.concurrent.BlockingQueue; //REMOVED
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;


public class PageRankVertex implements Vertex<Double, Double> {
    private static final Double damping = 0.85; // the damping ratio, as in the PageRank paper
    private static final Double tolerance = 1e-4; // the tolerance for converge checking

    private msgID vInfo; // MsgID object for the parent Vertex with public queue used for message receiving
    private Double value; // value for the vertex
    private LinkedBlockingQueue<Double> inCurrentStep; //private queue used for compute
    private HashMap<Integer , msgID> msgIDMap; // map from neighbour ID to neighbour's queue object
    private int numVertices; //total number of vertices in the graph
    private int status = 1; //1 if active, 0 if not
    private CyclicBarrier barrier;
    private int superstep =1; // used only for special case for 1st superstep

    public PageRankVertex(msgID vInfo, ArrayList<msgID> msgIDList , Integer numVertices) {
        this.vInfo = vInfo;
        this.numVertices = numVertices;
        this.inCurrentStep = new LinkedBlockingQueue<>();
        this.value = 1 / (double) numVertices; // initialize value as per the pregel paper
        //System.out.println(this.value);

        this.msgIDMap = new HashMap<>();
        for(msgID i : msgIDList){
            this.msgIDMap.put(i.vID,i);
        }

        //System.out.println("thread created");
    }


    @Override
    public int getVertexID() { return this.vInfo.vID; }


    @Override
    public Double getValue() {
        return this.value;
    }

    @Override
    public void compute(LinkedBlockingQueue<Double> messages){

        if(this.status==1){
            Double sum = 0.0;
            if(this.superstep!=1){ // skip this for super step one as per pregel paper
                while (messages.peek() != null) {
                    sum += messages.poll(); // take a sum over all elements in the queue
                }
                Double newVal = ((1-damping) / (float) this.numVertices) + damping * (sum); // compute new value
                //System.out.println(newVal);
                if ((newVal - this.value) >= tolerance) { // check against threshold
                    this.value = newVal;
                } else { // if less then threshold then vote to halt
                    this.value = newVal;
                    voteToHalt();
                }
            }

            this.superstep ++;

            for (int i : this.msgIDMap.keySet()) { // send out messages to neighbours
                this.sendMessageTo(i, this.value / this.msgIDMap.size());
            }

            try {
                //System.out.println("Thread waiting");
                this.barrier.await(); // wait for prompt from master to start new super step
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException f) {
                f.printStackTrace();
            }

        }
    }

    @Override
    public void sendMessageTo(int vertexID, Double message) {
        try {
            this.msgIDMap.get(vertexID).msgQ.put(message); //put message in neighbours queue
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void voteToHalt() {
        this.status = 0;
    }


    public void setStatus(int update){
        this.status = update;
    }

    public int getStatus(){
        return this.status;
    }

    public void setBarrier(CyclicBarrier barrier){
        this.barrier = barrier; //Used once by master to pass barrier
    }

    //called by master in every super step to receive the sent messages before beginning the next superstep
    public void swapQueue(){
        this.vInfo.msgQ.drainTo(this.inCurrentStep);
    }

    @Override
    public void run() {
        //System.out.println("thread run started");
        try {
            while(!Thread.currentThread().isInterrupted()) {
                this.compute(this.inCurrentStep);
            }
        } catch (Exception consumed){
            //System.out.println("thread converged");
        }
    }
}
