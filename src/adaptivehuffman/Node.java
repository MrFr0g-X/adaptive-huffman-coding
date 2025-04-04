package adaptivehuffman;

/**
 * This class represents a node in our Adaptive Huffman Tree.
 * Each node stores a character, its frequency (weight), pointers to children and parent,
 * and an order number used for maintaining the sibling property.
 */
public class Node {
    char symbol;     // The character this node represents (only meaningful for leaf nodes)
    int weight;      // How many times this symbol has appeared so far
    Node left, right, parent;  // Connections to other nodes in the tree
    int order;       // Order number for maintaining the sibling property
    
    /**
     * Creates a new node with the given symbol, weight, and order
     * This constructor is called when building/updating the tree
     */
    public Node(char symbol, int weight, int order) {
        this.symbol = symbol;
        this.weight = weight;
        this.order = order;
        left = right = parent = null;  // Initialize all links to null
    }

    /**
     * Checks if this node is a leaf node (has no children)
     * Leaf nodes represent actual characters in our tree
     */
    public boolean isLeaf() {
        return (left == null && right == null);
    }
    
    /**
     * Checks if this node is the NYT (Not Yet Transmitted) node
     * The NYT node is a special leaf node with weight 0 that represents characters
     * not yet seen in the input
     */
    public boolean isNYT() {
        return isLeaf() && weight == 0;
    }
}
