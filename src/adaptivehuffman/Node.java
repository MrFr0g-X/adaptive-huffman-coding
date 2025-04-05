package adaptivehuffman;

/**
 * represents a node in the adaptive huffman tree
 */
public class Node {
    char symbol;     // character this node represents
    int weight;      // frequency count
    Node left, right, parent;  // tree links
    int order;       // order number for sibling property
    
    /**
     * creates a new node
     */
    public Node(char symbol, int weight, int order) {
        this.symbol = symbol;
        this.weight = weight;
        this.order = order;
        left = right = parent = null;
    }

    /**
     * checks if node is a leaf (has no children)
     */
    public boolean isLeaf() {
        return (left == null && right == null);
    }
    
    /**
     * checks if node is the NYT node (not yet transmitted)
     */
    public boolean isNYT() {
        return isLeaf() && weight == 0;
    }
}
