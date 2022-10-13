/**
 * Java program to represent a graph data structure.
 * 
 * Author: @tomfreier
 */

import java.util.ArrayList;
import java.util.Arrays;

public class Graph{
    private boolean adjMatrix[][];
    private ArrayList<ArrayList<Integer>> adjList;
    private int numVertices, numEdges;
    
    // getters
    public int getNumVertices(){ return numVertices;}
    public int getNumEdges(){ return numEdges;}
    public boolean[][] getAdjMatrix(){ return adjMatrix;}
    public ArrayList<ArrayList<Integer>> getAdjList(){ return adjList;}
    public void setAdjMatrix(boolean[][] newAdjMatrix){ this.adjMatrix = newAdjMatrix;}

    public Graph(int numVertices){
        this.numVertices = numVertices;
        this.numEdges = 0;
        this.adjMatrix = new boolean[numVertices][numVertices];
        /* populating array with falses */
        for (boolean[]row : adjMatrix) {
            Arrays.fill(row, false);
        }
    }

    /**
     * Adds an edge to the graph and increments the numEdges
     * @param u Node u of the edge
     * @param v Node v of the edge
     */
    public void addEdge(int u, int v){
        /* same vertex so not incrementing number of edges */
        if(u==v){
            this.adjMatrix[u][u] = true;
        }

        if(u<v){
            this.adjMatrix[u][v] = true;
            this.adjMatrix[v][u] = true; // undirected graph
            this.numEdges++;
        }
    }

    public void removeEdge(int u, int v){
        /* same vertex so not incrementing number of edges */
        if(u==v){
            this.adjMatrix[u][u] = false;
        }

        if(u<v){
            this.adjMatrix[u][v] = false;
            this.adjMatrix[v][u] = false; // undirected graph
            this.numEdges--;
        }
    }

    @Override
    public String toString(){
        String retVal = Integer.toString(numVertices) + "\n";
        
        //adding edges
        for(int row=0;row<numVertices;row++){
            for(int col=0;col<numVertices;col++){
                if(adjMatrix[row][col]){
                    retVal += "1 ";
                }
                else{
                    retVal += "0 ";
                }
            }
            retVal += "\n";
        }
        return retVal;
    }

    /**
     * Generate the adjacency list of the graph
     * @return  Adjacency list of the graph
     */
    public ArrayList<ArrayList<Integer>> generateAdjList(){
        this.adjList = new ArrayList<>();
        for(int i=0;i<numVertices;i++){
            ArrayList<Integer> adjacentEdges = new ArrayList<>();
            for(int j=0;j<numVertices;j++){
                if(i==j) continue; // can't have an edge with yourself
                if(adjMatrix[i][j]){
                    adjacentEdges.add(j);
                }
            }
            adjList.add(i, adjacentEdges);
        }

        return adjList;
    }
}