

import java.util.*;
import java.util.concurrent.CyclicBarrier;
//import java.util.concurrent.LinkedBlockingQueue; REMOVED
import java.util.stream.Collectors;

public class PageRank {
    private int num_vertices;
    private HashMap<Integer,ArrayList<Integer>> veMap; // vertex - edge map
    //private HashMap<Integer , PageRankVertex> vwMap; // vertex - worker map REMOVED
    private HashMap<Integer , msgID> vqMap;// Map between vertices and their respective msgID objects

    public PageRank(int size, int[] fromVertices, int[] toVertices) {

        this.num_vertices = size;
        //this.vwMap = new HashMap<>(); REMOVED

        // parsing to get adjacency list
        List<Integer> fromList = Arrays.stream(fromVertices).boxed().collect(Collectors.toList());
        List<Integer> toList = Arrays.stream(toVertices).boxed().collect(Collectors.toList());
        this.veMap = Helpers.parseList(fromList,toList, size);
        // parsing complete

        //System.out.println(this.veMap); print out parsed graph

        // create map from vertex ID : respective message queue object
        this.vqMap = new HashMap<>();
        for(int i=0; i<this.num_vertices; i++){
            this.vqMap.put(i, new msgID(i));
        }

    }

    public void run() throws InterruptedException {

        ArrayList<Thread> workerlist = new ArrayList<>(); // list of worker threads
        ArrayList<PageRankVertex> prVertexList = new ArrayList<>();  // list of Vertex objects

        // create the threads with parameters and add them and the vertex objects to the above list
        for(int i=0; i<this.num_vertices; i++){
            ArrayList<msgID> msgIDList = new ArrayList<>();
            //send each worker the message queues for it's neighbours
            for(int j: this.veMap.get(i)) {
                msgIDList.add(this.vqMap.get(j));
            }
            PageRankVertex pv = new PageRankVertex(this.vqMap.get(i),msgIDList, this.num_vertices);
            Thread t = new Thread(pv);
            workerlist.add(t);
            prVertexList.add(pv);

        }

        final CyclicBarrier cb = new CyclicBarrier(this.num_vertices, new Runnable(){
            @Override
            public void run(){
                //System.out.println("entered cyclic barrier");
                //This task will be executed once all thread reaches barrier
                int count = 0;

                // JUNK CODE
                //int junk = 0;
                //while(junk < 30){
                //    System.out.println(" ");
                //    junk ++;
                //}

                // count how many vertices are active
                for(PageRankVertex i : prVertexList) {
                    count += i.getStatus();
                }

                //  If even a single vertex is active,
                if(count != 0){

                    //System.out.println("starting new superstep");

                    // Vertices read all incoming messages, by draining the incoming message queue into private queue
                    for(PageRankVertex j : prVertexList){
                        j.swapQueue();
                    }

                    // set all vertices to active
                    for(PageRankVertex j : prVertexList){
                        j.setStatus(1);
                    }
                }else{ // if not a single vertex is active

                    //System.out.println("converged");
                    // retrieve all converged values from workers and store in a sorted manner
                    TreeMap<Integer, Double> vertexToValues = new TreeMap<>();
                    for(PageRankVertex j : prVertexList){
                        vertexToValues.put(j.getVertexID() , j.getValue());
                    }
                    //Trigger to make workers consensually close themselves
                    for(Thread i : workerlist){
                        i.interrupt();
                    }
                    // print out sorted list of values
                    //Double a = 0.0;       Commented out portion used to check if result sums to one
                    for(int i : vertexToValues.keySet()){
                        System.out.println(vertexToValues.get(i));
                        //a += vertexToValues.get(i);
                    }
                    //System.out.println("sum = " + a );


                }
            }
        });

        //pass the barrier as a parameter to vertices(can also be done by setting as static, and accessing from worker)
        for(PageRankVertex i : prVertexList){
            i.setBarrier(cb);
        }
        //start threads and computation
        for(Thread i : workerlist){
            i.start();
        }

    }

    public static void main(String[] args) {
        // Graph has vertices from 0 to `size-1`
        int size = 5;
        int[] from = {1,2,3,0,0,0,0,4,4,4,4,2,2,2,2};
        int[] to =   {0,0,0,1,2,3,4,0,1,2,3,0,0,0,0};

        PageRank pr = new PageRank(size, from, to);

        try {
            pr.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
