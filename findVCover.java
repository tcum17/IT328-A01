import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Reads in graphs from a text file and finds the minimum vertex cover for each
 * graph.
 * Additionally includes a method for finding a k-VertexCover
 * 
 * @author: Tom Freier
 */
public class findVCover {
    private ArrayList<Graph> graphs;
    private ArrayList<Integer> minCover;
    private int minCoverSize;
    private String fileName;

    public static void main(String[] args) {
        String fileName;
        /* processing command line args */
        if (args.length == 0) {
            fileName = "testGraph2.txt";
        } else {
            fileName = args[0];
        }

        /* creating new object and fining all the vertex covers for all the graphs */
        findVCover cover = new findVCover();
        cover.readFile(fileName);
        cover.findAllVertexCovers();
    }

    /**
     * Default Constructor for findVCover class.
     */
    public findVCover() {
        this.graphs = new ArrayList<Graph>();
        this.minCover = new ArrayList<>();
    }

    public ArrayList<Integer> getMinCover(){ return minCover;}
    /**
     * Reads in a file of graphs
     * 
     * @param inputFileName Filename of the text document
     * @return Boolean reflecting if could be opened and fileFormatted correctly
     */
    public boolean readFile(String inputFileName) {
        this.fileName = inputFileName;
        boolean fileFormatCorrect = true;
        Path path = Paths.get(inputFileName);
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
            fileFormatCorrect = false;
        }
        return fileFormatCorrect;
    }

    /**
     * Finds the vertex cover for each graph and displays an output to the console
     */
    public void findAllVertexCovers() {
        System.out.println("* A Minimum Vertex Cover of every graph in " + this.fileName + " *");
        System.out.println("   (|V|,|E|)   (size, ms used) Vertex Cover");
        for (int i = 0; i < graphs.size(); i++) {
            long startTime = System.currentTimeMillis();
            Graph g = graphs.get(i);
            ArrayList<Integer> cover = findMinimumVertexCover(g);
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.format("G%d(%d, %d) (size=%d, ms=%d) ", i + 1, g.getNumVertices(),
                    g.getNumEdges(), cover.size(), elapsedTime);
            printVertexCover(cover);
        }
    }

    /**
     * Finds the minimum vertex cover of a given graph by trying all the possible
     * starting states
     * 
     * @param g Graph to find the vertex cover of
     * @return ArrayList with the vertices included in the cover
     */
    public ArrayList<Integer> findMinimumVertexCover(Graph g) {
        VertexCover vc = new VertexCover(g);
        this.minCover = new ArrayList<Integer>(vc.getCover());
        for (int i = 0; i < g.getNumVertices(); i++) {
            vc = new VertexCover(g);
            if (vc.vertexCanBeRemoved(i)) {
                vc.removeVertex(i);
                findMinimumVertexCover(vc);
            }
        }
        Collections.sort(this.minCover);
        return this.minCover;
    }

    /**
     * Recursive function that finds the minimum vertex cover by removing the vertex
     * with the least number of edges that can be removed.
     * 
     * @param vc Current state of the vertex cover
     *           The minimum vertex cover will be stored at this.minCover
     */
    public void findMinimumVertexCover(VertexCover vc) {
        ArrayList<Integer> canidates = vc.findBestVertexToRemove();
        /* recursive calls to search all possible canidates */
        for (int i = 0; i < canidates.size(); i++) {
            VertexCover newVertexCover = new VertexCover(vc);
            newVertexCover.removeVertex(canidates.get(i)); // found n-1 cover
            findMinimumVertexCover(newVertexCover); // searching for n-2 cover
            if (newVertexCover.getCover().size() < this.minCover.size()) {
                this.minCover = new ArrayList<>(newVertexCover.getCover());
            }
        }
        if (vc.getCover().size() < this.minCover.size()) {
            this.minCover = new ArrayList<>(vc.getCover());
        }
    }
    
    /**
     * Recursive function that attempts to search for a k vertex cover
     * 
     * @param vc Current state of the vertex cover
     * @param k  Desired size to the cover to search for
     *           The minimum vertex cover (that was found) will be stored at
     *           this.minCover
     */
    public void findKvertexCover(VertexCover vc, int k) {
        /* base case */
        if (this.minCover.size() <= k) {
            return;
        } else { /* recursive calls to search all possible canidates */
            ArrayList<Integer> canidates = vc.findBestVertexToRemove();
            for (int i = 0; i < canidates.size(); i++) {
                VertexCover newVertexCover = new VertexCover(vc);
                newVertexCover.removeVertex(canidates.get(i)); // found n-1 cover
                findKvertexCover(newVertexCover, k); // searching for n-2 cover
                if (newVertexCover.getCover().size() < this.minCover.size()) {
                    this.minCover = new ArrayList<>(newVertexCover.getCover());
                }
            }
            if (vc.getCover().size() < this.minCover.size()) {
                this.minCover = new ArrayList<>(vc.getCover());
            }
        }

    }

    /**
     * Finds the minimum vertex cover using a boolean array representation
     * 
     * @param g Graph that the vertex cover will be based on
     * @return  ArrayList containing the vertices included in the cover.
     */
    @Deprecated // failed implementation, doesn't find the correct vertex cover for G47-G49 
    public ArrayList<Integer> findMinimumVertexCoverUsingBooleanArray(Graph g) {
        minCoverSize = g.getNumVertices();
        boolean[] minCover = new boolean[g.getNumVertices()];
        boolean[] cover = new boolean[g.getNumVertices()];
        /* find |V| number of Vertex Covers */
        for (int i = 0; i < g.getNumVertices(); i++) {
            /* create new initial state of the vertex cover */
            fillCover(cover);
            cover[i] = false; // removing initial vertex i from cover

            /* find the minimum cover from this initial state */
            cover = getMinimumVertexCoverUsingBooleanArray(cover, g);
            int size = getCoverSize(cover);

            /* updating min cover */
            if (size < minCoverSize) {
                minCoverSize = size;
                minCover = cover.clone();
            }
        }

        ArrayList<Integer> arrayListCover = new ArrayList<>();
        for (int i = 0; i < minCover.length; i++) {
            if (minCover[i]) {
                arrayListCover.add(i);
            }
        }
        return arrayListCover;
    }

    /**
     * Recursive function that finds the minimum vertex cover using a boolean array representation
     * 
     * @param cover Current state of the cover
     * @param g     Graph the cover is based on
     * @return      New state of the vertex cover
     */
    private boolean[] getMinimumVertexCoverUsingBooleanArray(boolean[] cover, Graph g) {
        int bestVertex = findBestVertexToRemove(cover, g);
        /* base case: no vertex can be removed from the cover */
        if (bestVertex < 0) {
            return cover;
        }
        /*
         * recursive case: removing the best vertex from the cover and calling this
         * function again
         */
        else {
            cover[bestVertex] = false; // removing vertex from cover
            cover = getMinimumVertexCoverUsingBooleanArray(cover, g);
            cover[bestVertex] = true;

            /* back tracking call */
            return getMinimumVertexCoverUsingBooleanArray(cover, g);
        }
    }
    /**
     * Gets the size of the cover (number of trues in the array)
     * 
     * @param cover Boolean array representing the cover. True value indicating the vertex included in cover
     * @return  Number of vertices in the number
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
     * Fills the cover so all vertices are included in it
     * 
     * @param coverToFill   Boolean array which represents the vertex cover
     */
    private void fillCover(boolean[] coverToFill) {
        for (int i = 0; i < coverToFill.length; i++) {
            coverToFill[i] = true;
        }
    }

    /**
     * Finds the best vertex that can be removed.
     * The 'best' criteria is the vertex with the lowest degree and all it's edges
     * are covered by another node
     * 
     * @param cover Boolean array with true values indicating the vertex included in cover
     * @param g     Graph the vertex cover is based on
     * @return      Best vertex to remove or -1 if no vertex can be removed
     */
    private int findBestVertexToRemove(boolean[] cover, Graph g) {
        int bestIndex = -1;
        int maxRemovableVertices = 0;
        ArrayList<ArrayList<Integer>> adjList = g.getAdjList();
        for (int i = 0; i < cover.length; i++) {
            if (cover[i] && vertexCanBeRemoved(cover, g, i)) {
                int removableVertices = 0;
                ArrayList<Integer> edges = adjList.get(i);
                cover[i] = false;
                for (int otherVertexInEdge : edges) {
                    if (vertexCanBeRemoved(cover, g, otherVertexInEdge)) {
                        removableVertices++;
                    }
                }
                cover[i] = true;
                if (removableVertices >= maxRemovableVertices) {
                    maxRemovableVertices = removableVertices;
                    bestIndex = i;
                }
            }
        }

        return bestIndex;
    }

    /**
     * Determines if a vertex can be removed from the graph.
     * A vertex can be removed if all its edges are covered by another vertex in the cover.
     * 
     * @param cover    Boolean array with true values indicating the vertex included in cover 
     * @param g         Graph the vertex cover is based on
     * @param vertex    Vertex to be checked if it can be removed
     * @return      Boolean value indicating if the vertex can be removed
     */
    private boolean vertexCanBeRemoved(boolean[] cover, Graph g, int vertex) {
        ArrayList<ArrayList<Integer>> adjList = g.getAdjList();
        ArrayList<Integer> adjEdges = adjList.get(vertex);
        boolean edgeCovered = true;
        for (int otherVertexInEdge : adjEdges) {
            if (!cover[otherVertexInEdge]) { /*
                                              * edge isn't covered by another vertex so
                                              * we can't remove it
                                              */
                edgeCovered = false;
                break;
            }
        }
        return edgeCovered; // edges are covered by another vertex
    }

    /**
     * Helper function to print a vertex cover for debugging purposes
     * 
     * @param vertexCover Vertex Cover to print
     */
    private void printVertexCover(boolean[] vertexCover) {
        ArrayList<Integer> coverToPrint = new ArrayList<>();
        for (int i = 0; i < vertexCover.length; i++) {
            if (vertexCover[i]) {
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
    private void printVertexCover(ArrayList<Integer> vertexCover) {
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
