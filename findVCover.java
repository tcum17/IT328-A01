import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Reads in graphs from a text file and finds the minimum vertex cover for each
 * graph.
 * Additionally includes a method for finding a k-VertexCover
 * 
 * @author: Tom Freier
 */
public class findVCover {
    private boolean[] minCover;
    private int minCoverSize;

    public static void main(String[] args) {
        String fileName;
        /* processing command line args */
        if (args.length == 0) {
            fileName = "graph.txt";
        } else {
            fileName = args[0];
        }

        /* creating new object and fining all the vertex covers for all the graphs */
        findVCover cover = new findVCover();
        cover.findAllVertexCovers(fileName);

    }

    public boolean[] getMinCover() {
        return minCover;
    }

    /**
     * Reads in a file of graphs
     * 
     * @param inputFileName Filename of the text document
     * @return Boolean reflecting if could be opened and fileFormatted correctly
     */
    public static ArrayList<Graph> readFile(String inputFileName) {
        Path path = Paths.get(inputFileName);
        ArrayList<Graph> graphs = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int numVertices = Integer.valueOf(line);
                Graph g = new Graph(numVertices);

                /* populating graph */
                for (int row = 0; row < numVertices; row++) {
                    line = reader.readLine();
                    int col = 0;
                    for (int i = 0; i < line.length(); i += 2) {
                        if (line.charAt(i) == '1') {
                            g.addEdge(row, col);
                        }
                        col++;
                    }
                }
                g.generateAdjList();
                graphs.add(g);
            }

        } catch (IOException e) {
            System.err.println("Error opening next file: " + inputFileName);
        }
        return graphs;
    }

    /**
     * Finds the vertex cover for each graph and displays an output to the console
     */
    public void findAllVertexCovers(String inputFileName) {
        ArrayList<Graph> graphs = readFile(inputFileName);
        System.out.println("* A Minimum Vertex Cover of every graph in " + inputFileName + " *");
        System.out.println("   (|V|,|E|)   (size, ms used) Vertex Cover");
        for (int i = 0; i < graphs.size(); i++) {
            long startTime = System.currentTimeMillis();
            Graph g = graphs.get(i);
            ArrayList<Integer> cover = findMinVertexCover(g);
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.format("G%d(%d, %d) (size=%d, ms=%d) ", i + 1, g.getNumVertices(),
                    g.getNumEdges(), cover.size(), elapsedTime);
            printVertexCover(cover);
        }

    }

    /**
     * Finds the minimum vertex cover by setting up the initial states and calling the recursive function on them.
     * Once the cover is found using the recursive function it creates an array list with the vertices that are in the cover
     * @param g Graph to find the minimum vertex cover of
     * @return  Array List with the vertices that are in the cover
     */
    public ArrayList<Integer> findMinVertexCover(Graph g) {
        minCover = new boolean[g.getNumVertices()];
        boolean[] cover = new boolean[g.getNumVertices()];
        minCoverSize = g.getNumVertices();
        Arrays.fill(minCover, true);
        boolean[] initialCover = new boolean[g.getNumVertices()];
        boolean[][] adjMatrix = g.getAdjMatrix();
        for(int i=0;i<adjMatrix.length;i++){
            initialCover[i] = g.getAdjList().get(i).size() > 0;
        }
        // System.out.print("[");
        // for(int i=0;i<initialCover.length-1;i++){
        //     System.out.print(initialCover[i] + ",");
        // }
        // if(initialCover.length == 0){
        //     System.out.println("}");
        // }
        // else{
        //     System.out.println(initialCover[initialCover.length-1] + "]");
        // }
        /* setting up the initial states */
        for(int i=0;i<cover.length;i++){
            cover = initialCover.clone();
            findMinVertexCoverRecursive(g, cover, i);
            int size = getCoverSize(cover);
            if (size < minCoverSize) {
                minCoverSize = size;
                minCover = cover.clone();
            }
        }

        /* weird edge case, if something is in the vertex cover and it's only edge is something else in the VC
         * only occurs when we're finding the compliment and trying to find clique
         * 
        */
        for(int i=0;i<cover.length;i++){
            if(minCover[i]){
                ArrayList<Integer> edges = g.getAdjList().get(i);
                if(edges.size() == 1 && minCover[edges.get(0)]){
                    minCover[edges.get(0)] = false;
                }
            }
        }
        if(minCover.length > 0){
            minCover[minCover.length-1] = wouldAddUncoveredEdge(g, minCover, minCover.length-1);
        }
        /* setting up the array list for our output */
        ArrayList<Integer> arrayListCover = new ArrayList<>();
        for (int i = 0; i < minCover.length; i++) {
            if (minCover[i]) {
                arrayListCover.add(i);
            }
        }
        return arrayListCover;
    }

    /**
     * Recursive driver function that finds the minimum vertex cover.
     * Works be first checking if the vertex would an uncovered edge. 
     * If this vertex would NOT add an uncovered edge we would began exploring the decesions with trying to remove each ones of it's children
     * 
     * For example, we'd remove vertex 0 from the cover. Check if we can remove 1 from this updated cover, and explore this state more. 
     * Instead, of removing 1 check if we can remove 2 from this updated cover, and explore this state more. And so on.
     * 
     * @param g         Graph that the vertex cover is based on 
     * @param cover     Current state of the vertex cover
     * @param vertex    Vertex we are considering removing from the cover
     */
    private void findMinVertexCoverRecursive(Graph g, boolean[] cover, int vertex) {
        if (wouldAddUncoveredEdge(g, cover, vertex)) { // base case 2
            cover[vertex] = true; // need to add it back, it's a covered edge
            int size = getCoverSize(cover);
            if (size < minCoverSize) {
                minCoverSize = size;
                minCover = cover.clone();
            }
            return; 
        } else{ /* recursive case */
            cover[vertex] = false; // removing the vertex from the case, we've checked the edges and they're all uncovered
            for(int i=vertex;i<cover.length-1;i++){
                /* exploring WITH vertex v_i in the cover */
                cover[i+1] = false;
                findMinVertexCoverRecursive(g, cover, i+1);
                cover[i+1] = true;
            }
        }
    }

    /**
     * Determines if removing a vertex would add an uncovered edge to the vertex cover (breaking the cover)
     * A vertex has an uncovered edge if one it's edges are NOT in the cover (ie, this is the only vertex holding that edge so we need to keep it in the cover).
     * @param g     Graph the vertex cover is based on
     * @param cover Current state of the cover
     * @param vertex    Vertex to determine if it would add an uncovered edge
     * @return          Boolean representing if or if not this vertex would add an uncovered edge.
     */
    public static boolean wouldAddUncoveredEdge(Graph g, boolean[] cover, int vertex) {
        ArrayList<ArrayList<Integer>> adjList = g.getAdjList();
        ArrayList<Integer> edges = adjList.get(vertex);
        boolean edgeCovered = false;
        if(edges.size() == 0){
            return false;
        }
        for (int otherVertex : edges) {
            if (!cover[otherVertex]) {
                edgeCovered = true;
                break;
            }
        }
        return edgeCovered;
    }

    /**
     * Sets up the initial states and finds the k cover by calling the recursive function. 
     * 
     * @param g     Graph that cover is based on
     * @param k     Desired k-cover size
     * @return  Array List with the vertices that are in the cover
     */
    public ArrayList<Integer> findKvertexCover(Graph g, int k) {
        minCover = new boolean[g.getNumVertices()];
        boolean[] cover = new boolean[g.getNumVertices()];
        minCoverSize = g.getNumVertices();
        Arrays.fill(minCover, true);
        
        /* setting up the initial covers */
        for(int i=0;i<cover.length;i++){
            Arrays.fill(cover, true);
            findKvertexCoverRecursive(g, cover, i, k);
        }
    
        /* converting the min cover to an array list so we can print it */
        ArrayList<Integer> arrayListCover = new ArrayList<>();
        for (int i = 0; i < minCover.length; i++) {
            if (minCover[i]) {
                arrayListCover.add(i);
            }
        }
        return arrayListCover;
    }

    /**
     * Recursive function that attempts to search for a k vertex cover
     * Based on the findMinCover() algorithm with an additional base case to check if a k cover has been found
     * 
     * @param vc Current state of the vertex cover
     * @param k  Desired size to the cover to search for
     */
    private void findKvertexCoverRecursive(Graph g, boolean[] cover, int vertex, int k) {
        if(minCoverSize <= k){ // base case 1, k cover found so don't need to keep searching
            return;
        }
        if (wouldAddUncoveredEdge(g, cover, vertex)) { // base case 2
            cover[vertex] = true; // need to add it back, it's a covered edge
            int size = getCoverSize(cover);
            if (size < minCoverSize) {
                minCoverSize = size;
                minCover = cover.clone();
            }
            return; 
        } else{ /* recursive case */
            cover[vertex] = false; // removing the vertex from the case, we've checked the edges and they're all uncovered
            for(int i=vertex;i<cover.length-1;i++){
                /* exploring WITH vertex v_i in the cover */
                cover[i+1] = false;
                findMinVertexCoverRecursive(g, cover, i+1);
                cover[i+1] = true;
            }
        }

    }

    /**
     * Gets the size of the cover (number of trues in the array)
     * 
     * @param cover Boolean array representing the cover. True value indicating the
     *              vertex included in cover
     * @return Number of vertices in the number
     */
    private int getCoverSize(boolean[] cover) {
        int size = 0;
        for (int i = 0; i < cover.length; i++) {
            if (cover[i]) {
                size++;
            }
        }
        return size;
    }

    /**
     * Helper function to print a vertex cover for debugging 
     * @param cover Vertex Cover to print
     */
    public static void printCover(boolean[] cover) {
        ArrayList<Integer> coverToPrint = new ArrayList<>();
        for (int i = 0; i < cover.length; i++) {
            if (cover[i]) {
                coverToPrint.add(i);
            }
        }
        printVertexCover(coverToPrint);
    }

    /**
     * Helper function to print a vertex cover for debugging purposes
     * 
     * @param vertexCover Vertex Cover to print
     */
    public static void printVertexCover(ArrayList<Integer> vertexCover) {
        System.out.print("{");
        for (int j = 0; j < vertexCover.size() - 1; j++) {
            System.out.print(vertexCover.get(j) + ", ");
        }
        if (vertexCover.size() != 0) {
            System.out.print(vertexCover.get(vertexCover.size() - 1));
        }
        System.out.print("}\n");
    }
}
