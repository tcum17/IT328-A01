import java.util.ArrayList;

/**
 * Class to represent a given state of a vertex cover.
 * Includes methods for filling the cover to begin, removing a vertex, and
 * finding the best vertex to remove.
 * 
 * @author: Tom Freier
 */
public class VertexCover {
    private ArrayList<Integer> cover;
    private Graph g;
    private ArrayList<ArrayList<Integer>> adjList;

    /**
     * Creates a new VertexCover object to represent the given graph.
     * Initial state of the cover includes every node in the graph
     * 
     * @param g Graph that the vertex cover will be based on
     */
    public VertexCover(Graph g) {
        this.cover = new ArrayList<>();
        this.g = g;
        this.adjList = g.generateAdjList();
        this.fillCover();
    }

    /**
     * Copy constructor, creates a deep copy of vcToCopy
     * 
     * @param vcToCopy VertexCover to create a deap copy of
     */
    public VertexCover(VertexCover vcToCopy) {
        this.cover = new ArrayList<>(vcToCopy.cover); // need to make a deep copy of the array lists as they may be
                                                      // modified
        this.adjList = new ArrayList<>(vcToCopy.adjList);
        this.g = vcToCopy.g;
    }

    /**
     * Getter method for cover
     * 
     * @return ArrayList of the vertices included in the cover
     */
    public ArrayList<Integer> getCover() {
        return cover;
    }

    /**
     * Fills the cover so all nodes are included in it
     */
    public void fillCover() {
        cover.clear();
        for (int i = 0; i < g.getNumVertices(); i++) {
            this.cover.add(i);
        }
    }

    /**
     * Removes a given vertex from the cover
     * Precondition: Vertex must be able to be removed from the cover
     * 
     * @param vertexToRemove
     */
    public void removeVertex(int vertexToRemove) {
        cover.remove(Integer.valueOf(vertexToRemove));
    }

    /**
     * Finds the best vertex that can be removed.
     * The 'best' criteria is the vertex with the lowest degree and all it's edges
     * are covered by another node
     * 
     * @return Vertex to remove or -1 if no vertexes can be removed
     */
    public ArrayList<Integer> findBestVertexToRemove() {
        ArrayList<Integer> verticesWithMinDegrees = new ArrayList<>();
        int minDegree = Integer.MAX_VALUE;
        for (int i = 0; i < adjList.size(); i++) {
            if (cover.contains(i) && vertexCanBeRemoved(i)) {
                int degree = adjList.get(i).size();
                /* new minimum found */
                if (degree < minDegree) {
                    verticesWithMinDegrees.clear();
                    minDegree = degree;
                }

                /* has minDegree so adding it to the list */
                if (degree == minDegree) {
                    verticesWithMinDegrees.add(i);
                }
            }
        }
        return verticesWithMinDegrees;
    }

    /**
     * Determines if every edge this vertex has is covered by another vertex in the
     * cover
     * 
     * @param vertex Vertex to determine if it can be removed from the cover
     * @return True if the vertex can be removed from the cover, false if we cannot
     *         remove it
     */
    public boolean vertexCanBeRemoved(int vertex) {
        boolean edgeCovered = true;
        ArrayList<Integer> adjEdges = this.adjList.get(vertex);
        for (int i = 0; i < adjEdges.size(); i++) {
            int otherVertexInEdge = adjEdges.get(i);
            if (!cover.contains(otherVertexInEdge)) { /* edge isn't covered by another vertex so we can't remove it */
                edgeCovered = false;
                break;
            }
        }
        return edgeCovered; // edges are covered by another vertex
    }

}