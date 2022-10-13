import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CNF {
    //ArrayList<ArrayList<Integer>> neighbors;
    int cnfCount = 1;

    public static void main(String[] args) {
        String fileName;
        /* processing command line args */
        if (args.length == 0) {
            fileName = "cnfs2022.txt";
        } else {
            fileName = args[0];
        }
        CNF threeCNF = new CNF();
        threeCNF.readFile(fileName);
    }

    /**
     * Reads in a file of cnfs and calculates the solution based off of a vertex cover made using the cook reduction theorem.
     * 
     * @param inputFileName Filename of the text document
     * @return Boolean reflecting if could be opened and fileFormatted correctly
     */
    public boolean readFile(String inputFileName) {
        boolean fileFormatCorrect = true;
        Path path = Paths.get(inputFileName);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                long startTime = System.nanoTime();
                line = line.trim();

                // This section reads in the cnf of the line and puts the numbers into an array.
                String[] cnf = line.split("\\s");
                int[] cnf3 = new int[cnf.length];
                
                int numberOfVariables = 0;
                for (int i = 0; i < cnf.length; i++) {
                    int num = Integer.parseInt(cnf[i]);
                    cnf3[i] = num;
                    if (Math.abs(num) > numberOfVariables) {
                        numberOfVariables = num;
                    }
                }

                // Create my list of descriptive nodes that hold the value and whether the node is in the bar or clause gadget
                ArrayList<Node> nodes = new ArrayList<Node>();
                int numVertices = 2*numberOfVariables + cnf3.length;

                // Initialize a Graph that is an adjacency matrix to represent the cnf as a graph using both bar and clause gadgets
                Graph graph = new Graph(numVertices);

                //Adding gadgets
                int label = 1;
                for (int row = 0; row < 2*numberOfVariables; row++) {
                    graph.addEdge(row,row);
                    if (row % 2 == 0) {
                        graph.addEdge(row, row+1);
                        Node node = new Node(label, false, false, "a" + label);
                        nodes.add(node);
                    } else {
                        graph.addEdge(row - 1, row);
                        Node node = new Node(-label, false, true, "-a" + label);
                        nodes.add(node);
                        label++;
                    }
                    
                }

                // Filling the cnf values that will be the clauses into my node array
                for (int j = 0; j < cnf3.length; j++) {
                    boolean sign;
                    if (cnf3[j] < 0) {
                        sign = false;
                        nodes.add(new Node(cnf3[j], true, sign, "-a" + Math.abs(cnf3[j])));
                    } else {
                        sign = true;
                        nodes.add(new Node(cnf3[j], true, sign, "a" + Math.abs(cnf3[j])));
                    }
                    
                }
                
                //Adding clauses to the graph as well as connecing each vertex to itself
                int count = 1;
                for(int row = 2*numberOfVariables; row < numVertices; row++) {
                    graph.addEdge(row, row);
                    if(count == 1) {
                        graph.addEdge(row, row+1);
                        graph.addEdge(row, row+2);
                    } else if (count == 2) {
                        graph.addEdge(row, row+1);
                        graph.addEdge(row, row-1);
                    } else if (count == 3) {
                        graph.addEdge(row, row-1);
                        graph.addEdge(row, row-2);
                    }
                    count++;
                    if(count > 3) {
                        count = 1;
                    }
                }
                
                //Add the edges between the verticies and the gadgets in the graph
                for(int row = 0; row < numVertices; row++) {
                    for(int column = 0; column < numVertices;column++) {
                        if(nodes.get(row).name.equals(nodes.get(column).name) && nodes.get(row).clause != nodes.get(column).clause) {
                            graph.addEdge(row, column);
                        }
                    }
                }

                // This is the k value that represent the number of nodes in the vertex cover we need to find to solve the vertex cover
                int k = this.getK(cnf3.length / 3, numberOfVariables);

                // Generate the graphs adjency list from the matrix
                graph.generateAdjList();

                // Create the findCover object to pass the graph to be solved in the vertex cover solver.
                findVCover findCover = new findVCover();
                ArrayList<Integer> results = findCover.findKvertexCover(graph, k);
                
                long endTime = System.nanoTime();

                // Create the correct ouput format
                String output = "(";
                if(results.size() == k) {
                     System.out.print("3CNF No. " + cnfCount + ": [n="
                    + numberOfVariables +" k=" + cnf3.length / 3 + "] ("+ (endTime-startTime)/1000000 + " ms) Solution:["
                    );
                    for(int l = 0; l < numberOfVariables; l++) {
                        int value = nodes.get(results.get(l)).value;
                        if (value > 0) {
                            System.out.print(value + ":T ");
                        } else {
                            System.out.print(Math.abs(value) + ":F ");
                        }
                    }
                    System.out.println("]");
                    for(int i = 0; i < cnf3.length; i++) {
                        if(i == 0) {
                            if (cnf3[i] > 0){
                                output += " " + cnf3[i] + "|";
                            } else {
                                output += cnf3[i] + "|";
                            }
                        } else if((i+1) % 3 != 0 && i != cnf3.length-1) {
                            if(cnf3[i] > 0){
                                output += " " + cnf3[i] + "|";
                            } else {
                                output += cnf3[i] + "|";
                            }
                            
                        } else if(i != cnf3.length-1) {
                            if(cnf3[i] > 0){
                                output += " "+ cnf3[i] + ")^(";
                            } else {
                                output += cnf3[i] +")^(";
                            }
                            
                        } else {
                            if(cnf3[i] > 0){
                                output += " " + cnf3[i] + ")";
                            } else {
                                output += cnf3[i] + ")";
                            }
                        }
                    }

                    System.out.println(output + " ==>");
                    for (int x = 0; x < numberOfVariables; x++) {
                        int val = nodes.get(results.get(x)).value;
                        if(val > 0) {
                            output = output.replace(""+val, "T");
                            output = output.replace("-T", " F");
                        } else  {
                            output = output.replace("" + Math.abs(val), "F");
                            output = output.replace("-F", " T");
                        }
                    }
                    System.out.println(output);
                    System.out.println(" ");
                } else { // for when the cover is wrong or when there is no solution
                    int[] fakeResults = new int[numberOfVariables];
                    for(int i = 0; i < numberOfVariables; i++) {
                        fakeResults[i] = i+1;
                    }
                    System.out.print("3CNF No. " + cnfCount + ": [n="
                    + numberOfVariables +" k=" + cnf3.length / 3 + "] ("+ (endTime-startTime)/1000000 + " ms) No Solution Random:["
                    );
                    for(int l = 0; l < numberOfVariables; l++) {
                        int value = fakeResults[l];
                        if (value > 0) {
                            System.out.print(value + ":T ");
                        } else {
                            System.out.print(Math.abs(value) + ":F ");
                        }
                    }
                    System.out.println("]");
                    for(int i = 0; i < cnf3.length; i++) {
                        if(i == 0) {
                            if (cnf3[i] > 0){
                                output += " " + cnf3[i] + "|";
                            } else {
                                output += cnf3[i] + "|";
                            }
                        } else if((i+1) % 3 != 0 && i != cnf3.length-1) {
                            if(cnf3[i] > 0){
                                output += " " + cnf3[i] + "|";
                            } else {
                                output += cnf3[i] + "|";
                            }
                            
                        } else if(i != cnf3.length-1) {
                            if(cnf3[i] > 0){
                                output += " "+ cnf3[i] + ")^(";
                            } else {
                                output += cnf3[i] +")^(";
                            }
                            
                        } else {
                            if(cnf3[i] > 0){
                                output += " " + cnf3[i] + ")";
                            } else {
                                output += cnf3[i] + ")";
                            }
                        }
                    }
                    System.out.println(output + " ==>");
                    for (int x = 0; x < numberOfVariables; x++) {
                        int val = fakeResults[x];
                        if(val > 0) {
                            output = output.replace(""+val, "T");
                            output = output.replace("-T", " F");
                        } else  {
                            output = output.replace("" + val, "F");
                            output = output.replace("-F", " T");
                        }
                    }
                    System.out.println(output);
                    System.out.println(" ");
                }
                
                cnfCount++; // increase the number of lines read by one
            }

        } catch (IOException e) {
            System.err.println("Error opening next file: " + inputFileName);
            fileFormatCorrect = false;
        }
        return fileFormatCorrect;
    }

    public int getK(int numberOfClauses, int numberOfVariables) {
        return numberOfVariables + (2 * numberOfClauses);
    }
}