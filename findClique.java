import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Reads graphs from a text file and finds the maximum clique for each
 * graph.
 * 
 * Also includes method for finding a k-Clique
 * 
 * Each clique is found by using Cook Reduction from Clique to Vertex Cover
 * 
 * @author: Tyler Cummings
 */

public class findClique {
    // Graph G has a clique of size k iff G' has a VC of size |V| - k

    // G' = (V, E'), E' = {(v,w) : (v,w) are not in E}
    // Flip vertices starting from index 1
    // -> 1 1 1 1 1 = 1 0 0 0 0

    private ArrayList<Graph> graphs;
    private ArrayList<Integer> maxClique;
    private String fileName;
    private findVCover cover = new findVCover();

    public static void main(String[] args) {
        String fileName;

        if (args.length == 0) {
            fileName = "graphs2022.txt";
        } else {
            fileName = args[0];
        }
        // Reads and finds all max cliques
        findClique clique = new findClique();
        clique.readFile(fileName);
        clique.findAllCliques();

    }

    // Default constructor
    public findClique() {
        this.graphs = new ArrayList<Graph>();
        this.maxClique = new ArrayList<>();
    }

    // Returns graphs read from file (for debugging)
    public ArrayList<Graph> getGraphs() {
        return this.graphs;
    }

    /**
     * Reads in a text file of graphs
     * 
     * @param inputFileName Filename of text doc
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
     * Finds all cliques for each graph using Cook Reduction to Vertex Cover
     * and displays output to console
     */
    public void findAllCliques() {
        System.out.println("* Max Cliques in graphs in graphs2022.txt (reduced to K-Vertex Cover) *");
        System.out.println("    (|V|,|E|)   (size, ms used) Cliques");
        for (int i = 0; i < graphs.size(); i++) {
            long startTime = System.currentTimeMillis();
            Graph g = graphs.get(i);
            ArrayList<Integer> clique = findMaximumClique(g);
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.format("G%d(%d, %d) (size=%d, ms=%d) ", i + 1, g.getNumVertices(), g.getNumEdges(),
                    clique.size(),
                    elapsedTime);
            printClique(clique);
        }
    }

    /**
     * Finds maximum clique for given graph and returns list of vertices in clique
     * Uses Cook Reduction : Clique -> VC
     * 
     * @param g
     * @return Returns a list of vertices in the clique
     */
    public ArrayList<Integer> findMaximumClique(Graph g) {
        // Matrix of g for finding g's complement
        boolean[][] oldAdjMatrix = g.getAdjMatrix();
        int numVertices = g.getNumVertices();
        Graph complement = new Graph(numVertices);
        // Constructs the g's complement graph
        for (int i = 0; i < oldAdjMatrix.length; i++) {
            for (int j = 0; j < oldAdjMatrix[i].length; j++) {
                if (oldAdjMatrix[i][j] == false) {
                    complement.addEdge(i, j);
                }
            }
        }
        complement.generateAdjList();
        // Use complement graph to find minimum Vertex Cover
        ArrayList<Integer> minCover = cover.findMinimumVertexCover(complement);
        this.maxClique = new ArrayList<Integer>();
        // Uses set difference V - VC (set) to find Clique
        // Based off following equation: Vertex Cover = V - V' where V' is set of
        // vertices in clique
        for (int i = 0; i < numVertices; i++) {
            if (!minCover.contains(i)) {
                maxClique.add(i);
            }
        }
        return this.maxClique;
    }

    /**
     * Finds the k-Clique of a given graph
     * 
     * @param g Graph to find the k-Clique of
     * @param k Desired size of clique to search for
     * @return Returns list of vertices in k-Clique (empty set if no such clique
     *         exists)
     */
    public ArrayList<Integer> findKClique(Graph g, int k) {
        // *Graph G has a clique of size k iff G' has a VC of size |V| - k*

        // Required Vertex Cover size (from Cook Reduction) U = |V| - k
        int u = g.getNumVertices() - k;
        // Finds Complement of g
        boolean[][] oldAdjMatrix = g.getAdjMatrix();
        int numVertices = g.getNumVertices();
        Graph complement = new Graph(numVertices);
        for (int i = 0; i < oldAdjMatrix.length; i++) {
            for (int j = 0; j < oldAdjMatrix[i].length; j++) {
                if (oldAdjMatrix[i][j] == false) {
                    complement.addEdge(i, j);
                }
            }
        }
        complement.generateAdjList();
        // Creates new VertexCover object for calling findKvertexCover
        VertexCover vc = new VertexCover(complement);
        ArrayList<Integer> resultVC = cover.findKvertexCover(vc, u);
        ArrayList<Integer> resultC = new ArrayList<>();

        // Uses set difference V - VC (set) to find Clique
        // Based off following equation: Vertex Cover = V - V' where V' is set of
        // vertices in clique
        for (int i = 0; i < numVertices; i++) {
            if (!resultVC.contains(i)) {
                resultC.add(i);
            }
        }
        return resultC;
    }

    /**
     * Prints clique in required format
     * 
     * @param clique Clique (list of vertices) to be printed
     */
    private void printClique(ArrayList<Integer> clique) {
        System.out.print("{");
        for (int i = 0; i < clique.size() - 1; i++) {
            System.out.print(clique.get(i) + ", ");
        }
        if (clique.size() != 0) {
            System.out.print(clique.get(clique.size() - 1));
        }
        System.out.print("}\n");
    }
}
