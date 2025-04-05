package adaptivehuffman;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * main tree for adaptive huffman algorithm
 * this was the hardest part to implement
 */
public class HuffmanTree {
    // tree structure
    Node root, NYT;
    HashMap<Character, Node> nodesMap;
    int maxOrder = 512;
    
    // settings for handling different inputs
    private int nodeCount = 0;
    private boolean simplifiedMode = false;
    private int customNodeLimit = 100;
    private int maxUpdateDepth = 100;
    private boolean safetyChecksEnabled = true;
    
    // cache for faster path lookups
    private HashMap<Node, String> pathCache = new HashMap<>();

    /**
     * creates empty tree with just NYT node
     */
    public HuffmanTree() {
        root = NYT = new Node('\0', 0, maxOrder);
        nodesMap = new HashMap<>();
    }

    // settings methods
    
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
    
    private void clearPathCache() {
        pathCache.clear();
    }

    // basic methods
    
    public boolean contains(char c) {
        return nodesMap.containsKey(c);
    }
    
    public boolean hasChar(char c) {
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
     * inserts new character into tree
     */
    public void insertNewSymbol(char c) {
        // check for duplicates
        if (safetyChecksEnabled && contains(c)) {
            System.err.println("Warning: Attempted to insert existing character '" + c + "'. Using existing node.");
            return;
        }

        // create new nodes
        Node internal = new Node('\0', 0, NYT.order);
        Node newNYT = new Node('\0', 0, --maxOrder);
        Node newCharNode = new Node(c, 1, --maxOrder);

        // set up relationships
        internal.left = newNYT;
        internal.right = newCharNode;
        internal.parent = NYT.parent;

        newNYT.parent = internal;
        newCharNode.parent = internal;

        // update tree structure
        if (NYT.parent == null) {
            root = internal;
        } else {
            if (NYT.parent.left == NYT)
                NYT.parent.left = internal;
            else
                NYT.parent.right = internal;
        }

        // update tracking
        NYT = newNYT;
        nodesMap.put(c, newCharNode);
        nodeCount += 2;

        clearPathCache();
        
        // update weights
        updateTree(internal.parent);
        
        // validate tree
        if (safetyChecksEnabled && !validateTree()) {
            System.err.println("Warning: Tree structure invalid after insertion. Rebuilding tree...");
            rebuildTree();
        }
    }
    
    // for visualization
    public Node addChar(char c) {
        insertNewSymbol(c);
        return nodesMap.get(c);
    }

    /**
     * updates tree for existing symbol
     */
    public void updateExistingSymbol(char c) {
        Node node = nodesMap.get(c);
        updateTree(node);
    }

    /**
     * updates weights and maintains sibling property
     */
    void updateTree(Node node) {
        int updateCount = 0;

        while (node != null) {
            // prevent infinite loops
            if (safetyChecksEnabled && updateCount++ > maxUpdateDepth) {
                System.err.println("Warning: Maximum update depth reached. Terminating update.");
                break;
            }

            node.weight++;
            
            // skip swapping if simplified mode
            if (!simplifiedMode && nodeCount < customNodeLimit) {
                try {
                    swapIfNeeded(node);
                } catch (Exception e) {
                    if (safetyChecksEnabled) {
                        System.err.println("Error during node swap: " + e.getMessage());
                    }
                }
            }
            
            node = node.parent;
        }
        
        // check tree validity
        if (safetyChecksEnabled && !validateTree()) {
            System.err.println("Warning: Tree integrity check failed after update. Rebuilding tree...");
            rebuildTree();
        }
    }

    /**
     * check if node needs swapping and perform swap
     * this part gave me the most trouble
     */
    void swapIfNeeded(Node node) {
        if (node == null || node == root) {
            return;
        }

        try {
            Node highestNode = null;
            int highestOrder = -1;
            
            ArrayList<Node> sameWeightNodes = new ArrayList<>();
            collectNodesWithWeight(root, node.weight, sameWeightNodes);
            
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
            
            if (highestNode != null) {
                if (safetyChecksEnabled && wouldCreateCycle(highestNode, node)) {
                    System.err.println("Warning: Skipping swap that would create cycle between orders " + 
                                      highestNode.order + " and " + node.order);
                    return;
                }
                
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
     * finds nodes with specific weight
     */
    private void collectNodesWithWeight(Node root, int targetWeight, ArrayList<Node> result) {
        if (root == null) return;
        
        ArrayList<Node> stack = new ArrayList<>();
        stack.add(root);
        
        while (!stack.isEmpty()) {
            Node node = stack.remove(stack.size() - 1);
            
            if (node.weight == targetWeight) {
                result.add(node);
            }
            
            if (node.right != null) {
                stack.add(node.right);
            }
            
            if (node.left != null) {
                stack.add(node.left);
            }
        }
    }

    /**
     * swaps two nodes in the tree
     */
    void swapNodes(Node a, Node b) {
        try {
            if (isAncestor(a, b) || isAncestor(b, a)) {
                System.err.println("Warning: Attempted to swap ancestor with descendant");
                return;
            }
            
            // save original connections
            Node aParent = a.parent;
            Node bParent = b.parent;
            Node aLeft = a.left;
            Node aRight = a.right;
            Node bLeft = b.left;
            Node bRight = b.right;
            
            // update parent connections
            if (aParent != null) {
                if (aParent.left == a) aParent.left = b;
                else aParent.right = b;
            } else {
                root = b;
            }
            
            if (bParent != null) {
                if (bParent.left == b) bParent.left = a;
                else bParent.right = a;
            } else {
                root = a;
            }
            
            // update child connections
            if (aLeft != null) aLeft.parent = b;
            if (aRight != null) aRight.parent = b;
            if (bLeft != null) bLeft.parent = a;
            if (bRight != null) bRight.parent = a;
            
            // swap node references
            a.parent = bParent;
            b.parent = aParent;
            
            a.left = bLeft;
            a.right = bRight;
            b.left = aLeft;
            b.right = aRight;
            
            // swap orders
            int tempOrder = a.order;
            a.order = b.order;
            b.order = tempOrder;
            
            clearPathCache();
            
        } catch (Exception e) {
            System.err.println("Error during node swap: " + e.getMessage());
            if (safetyChecksEnabled && !validateTree()) {
                System.err.println("Warning: Tree structure compromised after swap. Rebuilding...");
                rebuildTree();
            }
        }
    }
    
    // check if node is ancestor
    private boolean isAncestor(Node a, Node b) {
        if (a == null || b == null) return false;
        
        Node current = b.parent;
        while (current != null) {
            if (current == a) return true;
            current = current.parent;
        }
        return false;
    }

    // check if swap would create cycle
    private boolean wouldCreateCycle(Node a, Node b) {
        if (a == null || b == null || a == b) return true;
        
        if (isAncestor(a, b) || isAncestor(b, a)) return true;
        
        return false;
    }

    /**
     * finds node to swap with optimized approach
     */
    Node findNodeToSwapOptimized(int weight, int order) {
        ArrayList<Node> candidates = new ArrayList<>();
        findNodesWithWeight(root, weight, candidates);
        
        if (candidates.isEmpty()) {
            return null;
        }
        
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
    
    // recursive helper
    void findNodesWithWeight(Node node, int weight, ArrayList<Node> result) {
        if (node == null) return;
        
        if (node.weight == weight) {
            result.add(node);
        }
        
        if (node.left != null) {
            findNodesWithWeight(node.left, weight, result);
        }
        if (node.right != null) {
            findNodesWithWeight(node.right, weight, result);
        }
    }

    // compatibility method
    Node findNodeToSwap(Node current, int weight, int order) {
        return null;
    }

    Node betterNode(Node a, Node b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.order > b.order ? a : b;
    }

    /**
     * gets binary code for a character
     */
    public String getCode(char c) {
        Node node = nodesMap.get(c);
        return getPath(node);
    }

    /**
     * gets binary code for NYT node
     */
    public String getNYTCode() {
        return getPath(NYT);
    }

    /**
     * gets path from root to a node
     */
    String getPath(Node node) {
        if (pathCache.containsKey(node)) {
            return pathCache.get(node);
        }
        
        StringBuilder path = new StringBuilder();
        Node current = node;
        
        while (current != root) {
            if (current.parent.left == current) {
                path.insert(0, '0');
            } else {
                path.insert(0, '1');
            }
            current = current.parent;
        }
        
        String result = path.toString();
        pathCache.put(node, result);
        
        return result;
    }

    /**
     * gets total nodes in tree
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * checks tree structure validity
     */
    private boolean validateTree() {
        Set<Node> visited = new HashSet<>();
        return validateNode(root, visited);
    }
    
    private boolean validateNode(Node node, Set<Node> visited) {
        if (node == null) return true;
        
        if (visited.contains(node)) {
            return false;
        }
        visited.add(node);
        
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
     * rebuilds tree if corrupted
     * my last resort when things break
     */
    private void rebuildTree() {
        HashMap<Character, Integer> charWeights = new HashMap<>();
        for (Map.Entry<Character, Node> entry : nodesMap.entrySet()) {
            charWeights.put(entry.getKey(), entry.getValue().weight);
        }
        
        root = NYT = new Node('\0', 0, maxOrder);
        nodesMap.clear();
        nodeCount = 0;
        clearPathCache();
        
        for (Map.Entry<Character, Integer> entry : charWeights.entrySet()) {
            char c = entry.getKey();
            int weight = entry.getValue();
            
            insertNewSymbol(c);
            
            Node node = nodesMap.get(c);
            for (int i = 1; i < weight; i++) {
                updateExistingSymbol(c);
            }
        }
    }
}
