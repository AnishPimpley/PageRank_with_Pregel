import java.util.*;

/**
 * Created by anish on 11/17/2017.
 * Contains parsing function from input format to <Vertex, EdgeList> Hashmap
 */

public  class Helpers{

    public static HashMap<Integer,ArrayList<Integer>> parseList(List<Integer> fromList, List<Integer> toList, int numVertices){

        HashMap<Integer,ArrayList<Integer>> veMap = new HashMap<>();

        Set<Integer> from = new HashSet(fromList);


        ArrayList<Integer> vertexList = new ArrayList<>();
        for(int i=0;i<numVertices;i++){vertexList.add(i);}
        Set<Integer> all_edit =  new HashSet(vertexList);
        Set<Integer> all =  new HashSet(vertexList);


        /*
        The below 2 operations convert
        new to -> to - from = vertices that are sinks
        new from -> from + new to = old_to UNION old_from = set of all vertices
         */

        all_edit.removeAll(from);


        for(int i=0; i < fromList.size(); i++){
            if( veMap.get(fromList.get(i)) == null){
                veMap.put(fromList.get(i),new ArrayList<>());
                veMap.get(fromList.get(i)).add(toList.get(i));
            }else{
                veMap.get(fromList.get(i)).add(toList.get(i));
            }
        }

        for(int i : all_edit){
            veMap.put(i ,new ArrayList<Integer>());
            for(int j : all){
                if(j != i){                                     // This comparison can be a bottle neck.
                    veMap.get(i).add(j);
                }
            }
        }

        return veMap;

    }

    public static void main(String[] args){
        ArrayList<Integer> fromList = new ArrayList<>(Arrays.asList(1,1,2,2,3,5));
        ArrayList<Integer> toList = new ArrayList<>(Arrays.asList(2,7,3,7,5,7));
        System.out.println(parseList(fromList,toList,8));
    }

}