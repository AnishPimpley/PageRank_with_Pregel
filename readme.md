# Instruction for running the code

Run : pagerank.java : main function of the Pagerank class

# Input/Output Format

Imput/Output in main is in the same format as provided. Print statements to check intermediate progress have been commented out. You can uncomment them to check functioning of core elements.

# Assumptions
Tests will not check for the assumptions. They are assumed to be true.
- Input is in the form of 2 int-arrays. Therefore, all elements in both lists are integers.
- If number of vertices = 'n', no value in the 2 lists exceed 'n'.
- If a node has no outgoing edges, it's value is distributed over all vertices in the graph, except itself.
- If a node has no incoming or outgoing edges, it is still intialized to a value of 1/n in the 1st super step.
- Every vertex is assigned one thread, and the number of threads created due to a large number of vertices is not an issue.
- The size of both to,from lists is the same

# Tests and Results
- Stability of values : Results are unchanged over multiple trials (over 15 significant digits printed)
- Invariant to duplicate edges 
- Runtime : Fraction of a second per trail and stable over trials
- Correctness of values : Correct values according to the assumptions made above, all results sum to 1.
 (varying only due to double floating point precision in conducted experiments, never more than 10^-6)
- Works for empty lists
- Works for cases where a vertex explicitly points to itself.(in such a case, that vertex does not distribute it's value to other edges)

# Pregel's core functionaly
- Pagerank's main thread functions as master. 
- The core functionality is implemented using blocking queues & cyclic barriers. All queues here are blocking queues

.....................................

- The Master parses through the graph to get an adjacency list (stored as a hashmap)
- Master stores a map from vertex ID : the message queue object for that vertex
- 'n' threads are created, each thread defining a pageRankVertex object for the Vertex assigned to that thread
- Each pageRankVertex recieves all message queue objects of its neighbors and its own message queue from where it reads.
- Master creates Cyclic barrier
- All threads call thread.run() -> 1st superstep starts
- Each thread reads from it' private queue, and computes a sum
- Sum is used to compute value at current step
- check value at current step vs at last step, and if less then threshold then vote to halt, else continue below,
- This value/num_neighbors is then propagted by adding to message queues of neighbours
- cyclic barrier.await()

.....................................

- Once cyclic barrier is triggered (all thread's compute steps are completed), the following happens before next super step
- If even one thread is active:
   - set all threads to active
   - instruct each thread to drain it's message queue into a private queue. ie. read messages from last superstep
   - Start next superstep
- Else, ie. if all threads have voted to halt, pagerank has converged
   - Ask threads for values corresponding to their vertex
   - Store sorted according to vertex ID (TreeMap)
   - Print out converged values
   
# Note about gradle:

Build, assemble ,test ran successfully without any errors. However, running Pagerank.java would be the preferred way of execution.
