public class Node {
    public int value;
    public boolean clause;
    public boolean negation;
    public String name;

    public Node(int value, boolean clause, boolean negation, String name) {
        this.value = value;
        this.clause = clause;
        this.negation = negation;
        this.name = name;
    }

    public Node() {
        this.value = 0;
        this.clause = false;
        this.negation = false;
        this.name = "";
    }
}