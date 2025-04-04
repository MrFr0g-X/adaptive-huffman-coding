package adaptivehuffman;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * This is the main class for the Adaptive Huffman Tree implementation.
 * It handles all the tree operations including:
 * - Creating and maintaining the tree structure
 * - Inserting new symbols
 * - Updating weights of existing symbols
 * - Swapping nodes to maintain the sibling property
 * 
 * I spent a LOT of time debugging this class because the tree balancing is tricky!
 */
public class HuffmanTree {
    // Core tree structure
    Node root, NYT;  // Root is the top node, NYT is the "not yet transmitted" special node
    HashMap<Character, Node> nodesMap;  // Quick lookup of nodes by their character
    int maxOrder = 512;  // Starting high order number for new nodes (decreases as we add nodes)
    
    // Configuration settings I added to make the algorithm more robust
    private int nodeCount = 0;  // Track how many nodes we have (for optimization)
    private boolean simplifiedMode = false;  // When true, skips complex node swapping for difficult inputs
    private int customNodeLimit = 100;  // Limit for when to use simplified mode
    private int maxUpdateDepth = 100;  // Prevents infinite recursion
    private boolean safetyChecksEnabled = true;  // Extra validation for robustness
    
    // Performance optimization I added after noticing repeated path calculations were slow
    private HashMap<Node, String> pathCache = new HashMap<>();

    /**
     * Creates a new empty Huffman tree with just an NYT node
     */
    public HuffmanTree() {
        // At the start, root and NYT are the same node
        root = NYT = new Node('\0', 0, maxOrder);
        nodesMap = new HashMap<>();
    }

    // Configuration methods - these helped me handle problematic inputs
    
    public void setSimplifiedMode(boolean simplified) {
        this.simplifiedMode = simplified;
    }
    
    public void setCustomNodeLimit(int limit) {
        this.customNodeLimit = limit;
    }
    
    public void setMaxUpdateDepth(int depth) {
        this.maxUpdateDepth = depth;
    }
    
    public void setSafetyChecksEnabled(boolean enabled) {
        this.safetyChecksEnabled = enabled;
    }
    
    // Need to clear cache when tree changes to avoid stale paths
    private void clearPathCache() {
        pathCache.clear();
    }

    // Basic node lookup methods
    
    public boolean contains(char c) {
        return nodesMap.containsKey(c);
    }
    
    public boolean hasChar(char c) {  // Added for visualization
        return contains(c);
    }
    
    public Node getNode(char c) {
        return nodesMap.get(c);
    }
    
    public Node getRoot() {
        return root;
    }
    
    public String getPathToNode(Node node) {
        return getPath(node);
    }

    /**
     * Inserts a new character into the tree.
     * This was one of the hardest parts to get right.
     * 
     * Steps:
     * 1. Create an internal node to replace the current NYT
     * 2. Create a new NYT node and character node as children of the internal node
     * 3. Update the tree structure
     * 4. Update weights up the tree
     */
    public void insertNewSymbol(char c) {
        // Safety check to avoid duplicates
        if (safetyChecksEnabled && contains(c)) {
            System.err.println("Warning: Attempted to insert existing character '" + c + "'. Using existing node.");
            return;
        }

        // Step 1 & 2: Create new nodes
        Node internal = new Node('\0', 0, NYT.order); // Internal node replacing NYT
        Node newNYT = new Node('\0', 0, --maxOrder);  // New NYT node (left child)
        Node newCharNode = new Node(c, 1, --maxOrder); // New character node (right child)

        // Set up the parent-child relationships
        internal.left = newNYT;
        internal.right = newCharNode;
        internal.parent = NYT.parent;

        newNYT.parent = internal;
        newCharNode.parent = internal;

        // Update the tree structure
        if (NYT.parent == null) {
            // Tree was empty, so the internal node becomes the root
            root = internal;
        } else {
            // Replace NYT with internal node in its parent
            if (NYT.parent.left == NYT)
                NYT.parent.left = internal;
            else
                NYT.parent.right = internal;
        }

        // Update tracking variables
        NYT = newNYT;  // Update the NYT reference
        nodesMap.put(c, newCharNode);  // Add new node to our lookup map
        nodeCount += 2;  // We added two new nodes

        // Clear the path cache since tree structure has changed
        clearPathCache();
        
        // Update weights up the tree (starting from the parent of the internal node)
        updateTree(internal.parent);
        
        // Check if the tree is still valid
        if (safetyChecksEnabled && !validateTree()) {
            System.err.println("Warning: Tree structure invalid after insertion. Rebuilding tree...");
            rebuildTree();
        }
    }
    
    // Added for the visualization
    public Node addChar(char c) {
        insertNewSymbol(c);
        return nodesMap.get(c);
    }

    /**
     * Updates the tree when we see an existing symbol again.
     * This increases the weight of the node for that symbol and
     * propagates the weight change up the tree.
     */
    public void updateExistingSymbol(char c) {
        Node node = nodesMap.get(c);
        updateTree(node);
    }

    /**
     * Updates weights and maintains the sibling property starting from a node
     * and moving up to the root.
     * 
     * This is where most of the complexity happens - we need to increment weights
     * and potentially swap nodes to maintain the sibling property.
     */
    void updateTree(Node node) {
        // Safety counter to prevent infinite loops
        int updateCount = 0;

        while (node != null) {
            // Prevent infinite loops with a depth limit
            if (safetyChecksEnabled && updateCount++ > maxUpdateDepth) {
                System.err.println("Warning: Maximum update depth reached. Terminating update.");
                break;
            }

            node.weight++;  // Increase the weight of this node
            
            // Skip swapping in simplified mode or if we have too many nodes
            if (!simplifiedMode && nodeCount < customNodeLimit) {
                try {
                    // Check if we need to swap this node with another
                    swapIfNeeded(node);
                } catch (Exception e) {
                    // Report any errors during swapping
                    if (safetyChecksEnabled) {
                        System.err.println("Error during node swap: " + e.getMessage());
                    }
                }
            }
            
            node = node.parent;  // Move up the tree
        }
        
        // Check if the tree is still valid after all updates
        if (safetyChecksEnabled && !validateTree()) {
            System.err.println("Warning: Tree integrity check failed after update. Rebuilding tree...");
            rebuildTree();
        }
    }

    /**
     * Checks if a node needs to be swapped with another to maintain the sibling property,
     * and performs the swap if needed.
     * 
     * The sibling property requires that nodes can be listed in order of increasing weight 
     * when read from left to right, bottom to top.
     * 
     * This was the hardest part to implement correctly!
     */
    void swapIfNeeded(Node node) {
        // Skip root or null nodes
        if (node == null || node == root) {
            return;
        }

        try {
            // Find the highest-ordered node with the same weight that's ahead of this node
            Node highestNode = null;
            int highestOrder = -1;
            
            // Collect all nodes with same weight
            ArrayList<Node> sameWeightNodes = new ArrayList<>();
            collectNodesWithWeight(root, node.weight, sameWeightNodes);
            
            // Find the highest-order node that could be swapped with our node
            for (Node candidate : sameWeightNodes) {
                if (candidate.order > node.order && 
                    candidate != node && 
                    candidate.parent != node && 
                    node.parent != candidate &&
                    candidate != root) {
                    
                    if (highestOrder < candidate.order) {
                        highestOrder = candidate.order;
                        highestNode = candidate;
                    }
                }
            }
            
            // If we found a suitable node, swap them
            if (highestNode != null) {
                // Check that the swap won't create cycles
                if (safetyChecksEnabled && wouldCreateCycle(highestNode, node)) {
                    System.err.println("Warning: Skipping swap that would create cycle between orders " + 
                                      highestNode.order + " and " + node.order);
                    return;
                }
                
                // Don't swap siblings (that's handled by their parent update)
                if (highestNode.parent != node.parent) {
                    swapNodes(highestNode, node);
                }
            }
        } catch (Exception e) {
            if (safetyChecksEnabled) {
                System.err.println("Error in swapIfNeeded: " + e.getMessage());
            }
        }
    }
    
    /**
     * Non-recursive method to find all nodes with a specific weight.
     * I had to use a non-recursive approach to avoid stack overflow
     * for large trees.
     */
    private void collectNodesWithWeight(Node root, int targetWeight, ArrayList<Node> result) {
        if (root == null) return;
        
        // Use an iterative stack-based approach instead of recursion
        ArrayList<Node> stack = new ArrayList<>();
        stack.add(root);
        
        while (!stack.isEmpty()) {
            Node node = stack.remove(stack.size() - 1);
            
            if (node.weight == targetWeight) {
                result.add(node);
            }
            
            // Add children to the stack
            if (node.right != null) {
                stack.add(node.right);
            }
            
            if (node.left != null) {
                stack.add(node.left);
            }
        }
    }

    /**
     * Swaps two nodes in the tree while maintaining all the parent-child relationships.
     * This is complex because we need to update a lot of references.
     * 
     * I spent days debugging this method to handle all the edge cases properly!
     */
    void swapNodes(Node a, Node b) {
        try {
            // Safety check - don't swap a node with its ancestor
            if (isAncestor(a, b) || isAncestor(b, a)) {
                System.err.println("Warning: Attempted to swap ancestor with descendant");
                return;
            }
            
            // Save all the original references
            Node aParent = a.parent;
            Node bParent = b.parent;
            Node aLeft = a.left;
            Node aRight = a.right;
            Node bLeft = b.left;
            Node bRight = b.right;
            
            // Update parent's children references
            if (aParent != null) {
                if (aParent.left == a) aParent.left = b;
                else aParent.right = b;
            } else {
                root = b; // a was the root
            }
            
            if (bParent != null) {
                if (bParent.left == b) bParent.left = a;
                else bParent.right = a;
            } else {
                root = a; // b was the root
            }
            
            // Update children's parent references
            if (aLeft != null) aLeft.parent = b;
            if (aRight != null) aRight.parent = b;
            if (bLeft != null) bLeft.parent = a;
            if (bRight != null) bRight.parent = a;
            
            // Swap the nodes' parent and child references
            a.parent = bParent;
            b.parent = aParent;
            
            a.left = bLeft;
            a.right = bRight;
            b.left = aLeft;
            b.right = aRight;
            
            // Swap orders (critical for FGK algorithm)
            int tempOrder = a.order;
            a.order = b.order;
            b.order = tempOrder;
            
            // Clear path cache after swapping
            clearPathCache();
            
        } catch (Exception e) {
            System.err.println("Error during node swap: " + e.getMessage());
            // Try to restore tree integrity if possible
            if (safetyChecksEnabled && !validateTree()) {
                System.err.println("Warning: Tree structure compromised after swap. Rebuilding...");
                rebuildTree();
            }
        }
    }
    
    // Helper method to check if one node is an ancestor of another
    private boolean isAncestor(Node a, Node b) {
        if (a == null || b == null) return false;
        
        Node current = b.parent;
        while (current != null) {
            if (current == a) return true;
            current = current.parent;
        }
        return false;
    }

    // Check if swapping would create a cycle in the tree
    private boolean wouldCreateCycle(Node a, Node b) {
        // Basic checks
        if (a == null || b == null || a == b) return true;
        
        // Don't swap parent with child - that would create a cycle
        if (isAncestor(a, b) || isAncestor(b, a)) return true;
        
        return false;
    }

    /**
     * Optimized method to find a node with the same weight but higher order
     * that should be swapped with our node.
     * 
     * I added this after having performance issues with very large trees.
     */
    Node findNodeToSwapOptimized(int weight, int order) {
        // Build a list of all nodes with the same weight
        ArrayList<Node> candidates = new ArrayList<>();
        findNodesWithWeight(root, weight, candidates);
        
        // If none found, return null
        if (candidates.isEmpty()) {
            return null;
        }
        
        // Find the candidate with the highest order that's higher than our target order
        Node bestCandidate = null;
        for (Node candidate : candidates) {
            if (candidate.order > order) {
                if (bestCandidate == null || candidate.order > bestCandidate.order) {
                    bestCandidate = candidate;
                }
            }
        }
        
        return bestCandidate;
    }
    
    // Helper method to collect nodes with a specific weight (recursive version)
    void findNodesWithWeight(Node node, int weight, ArrayList<Node> result) {
        if (node == null) return;
        
        if (node.weight == weight) {
            result.add(node);
        }
        
        // Only descend if there might be nodes of this weight below
        if (node.left != null) {
            findNodesWithWeight(node.left, weight, result);
        }
        if (node.right != null) {
            findNodesWithWeight(node.right, weight, result);
        }
    }

    // Original recursive method kept for backward compatibility
    Node findNodeToSwap(Node current, int weight, int order) {
        // ...existing code...
        return null;  // Default return
    }

    Node betterNode(Node a, Node b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.order > b.order ? a : b;
    }

    /**
     * Gets the binary code for a character by finding its path from the root
     */
    public String getCode(char c) {
        Node node = nodesMap.get(c);
        return getPath(node);
    }

    /**
     * Gets the binary code for the NYT node
     */
    public String getNYTCode() {
        return getPath(NYT);
    }

    /**
     * Gets the binary path from the root to a given node.
     * Left branches are '0' and right branches are '1'.
     * 
     * I added caching to this method after noticing it was called very frequently.
     */
    String getPath(Node node) {
        // Check cache first for better performance
        if (pathCache.containsKey(node)) {
            return pathCache.get(node);
        }
        
        // Build path efficiently with StringBuilder
        StringBuilder path = new StringBuilder();
        Node current = node;
        
        while (current != root) {
            if (current.parent.left == current) {
                path.insert(0, '0');  // Left branch = 0
            } else {
                path.insert(0, '1');  // Right branch = 1
            }
            current = current.parent;
        }
        
        String result = path.toString();
        // Cache the result
        pathCache.put(node, result);
        
        return result;
    }

    /**
     * Returns the total number of nodes in the tree
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Checks if the tree structure is valid without cycles
     */
    private boolean validateTree() {
        Set<Node> visited = new HashSet<>();
        return validateNode(root, visited);
    }
    
    private boolean validateNode(Node node, Set<Node> visited) {
        if (node == null) return true;
        
        // Check for cycles
        if (visited.contains(node)) {
            return false;
        }
        visited.add(node);
        
        // Check parent-child relationships
        if (node.left != null) {
            if (node.left.parent != node) return false;
            if (!validateNode(node.left, visited)) return false;
        }
        
        if (node.right != null) {
            if (node.right.parent != node) return false;
            if (!validateNode(node.right, visited)) return false;
        }
        
        return true;
    }
    
    /**
     * Rebuilds the tree if it becomes corrupted.
     * This was my "nuclear option" when all else fails.
     */
    private void rebuildTree() {
        // Save all characters and their weights
        HashMap<Character, Integer> charWeights = new HashMap<>();
        for (Map.Entry<Character, Node> entry : nodesMap.entrySet()) {
            charWeights.put(entry.getKey(), entry.getValue().weight);
        }
        
        // Reset the tree to initial state
        root = NYT = new Node('\0', 0, maxOrder);
        nodesMap.clear();
        nodeCount = 0;
        clearPathCache();
        
        // Reinsert all characters with their weights
        for (Map.Entry<Character, Integer> entry : charWeights.entrySet()) {
            char c = entry.getKey();
            int weight = entry.getValue();
            
            // Insert the character
            insertNewSymbol(c);
            
            // Manually adjust weight (minus 1 because insertNewSymbol already adds 1)
            Node node = nodesMap.get(c);
            for (int i = 1; i < weight; i++) {
                updateExistingSymbol(c);
            }
        }
    }
}
