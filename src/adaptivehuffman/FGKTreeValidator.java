package adaptivehuffman;

import java.util.*;

/**
 * Utility class to validate and debug Adaptive Huffman (FGK) trees.
 * 
 * This class provides functionality to check if a tree satisfies the FGK algorithm properties,
 * especially the sibling property, which states that nodes can be listed in order of
 * increasing weight when read from left to right, bottom to top.
 * 
 * I created this class while debugging because validating the tree properly was crucial
 * for finding errors in my implementation.
 */
public class FGKTreeValidator {
    
    /**
     * Main validation method that checks if a tree satisfies all required properties
     * 
     * @param tree The HuffmanTree to validate
     * @return true if the tree is valid, false otherwise
     */
    public static boolean validateTree(HuffmanTree tree) {
        System.out.println("Validating FGK tree...");
        
        Node root = tree.getRoot();
        if (root == null) {
            System.out.println("Empty tree is valid.");
            return true;
        }
        
        // First check structure integrity (no cycles, proper parent-child links)
        if (!validateStructure(root)) {
            System.out.println("Tree structure validation failed!");
            return false;
        }
        System.out.println("Tree structure is valid.");
        
        // Then check sibling property and node ordering
        if (!validateSiblingProperty(root)) {
            System.out.println("Sibling property validation failed!");
            return false;
        }
        System.out.println("Sibling property is valid.");
        
        return true;
    }
    
    /**
     * Verify the basic tree structure - no cycles, proper parent-child links
     * 
     * This was super important because tree corruption was one of the hardest bugs to fix
     */
    private static boolean validateStructure(Node root) {
        Set<Node> visited = new HashSet<>();
        return validateNodeLinks(root, visited);
    }
    
    /**
     * Check that parent-child links are consistent and no cycles exist
     */
    private static boolean validateNodeLinks(Node node, Set<Node> visited) {
        if (node == null) return true;
        
        // Check for cycles
        if (visited.contains(node)) {
            System.out.println("Cycle detected at node: weight=" + node.weight + 
                              ", order=" + node.order);
            return false;
        }
        visited.add(node);
        
        // Check parent-child relationships
        if (node.left != null) {
            if (node.left.parent != node) {
                System.out.println("Left child's parent link is broken at node: weight=" + 
                                  node.weight + ", order=" + node.order);
                return false;
            }
            if (!validateNodeLinks(node.left, visited)) return false;
        }
        
        if (node.right != null) {
            if (node.right.parent != node) {
                System.out.println("Right child's parent link is broken at node: weight=" + 
                                  node.weight + ", order=" + node.order);
                return false;
            }
            if (!validateNodeLinks(node.right, visited)) return false;
        }
        
        return true;
    }
    
    /**
     * Verify that the tree satisfies the sibling property:
     * 1. All nodes (except root) have a sibling
     * 2. Nodes can be listed in order of increasing weight
     * 
     * This was the most complex validation I had to do, since the algorithm's
     * correctness relies on this property.
     */
    private static boolean validateSiblingProperty(Node root) {
        // Get all nodes in the tree
        List<Node> allNodes = new ArrayList<>();
        collectAllNodes(root, allNodes);
        
        // Check that all internal nodes have two children
        for (Node node : allNodes) {
            if (!node.isLeaf()) {
                if (node.left == null || node.right == null) {
                    System.out.println("Internal node missing child: weight=" + 
                                      node.weight + ", order=" + node.order);
                    return false;
                }
            }
        }
        
        // Check that siblings are properly ordered (increasing weight)
        return validateSiblingWeightOrder(root);
    }
    
    /**
     * Check that siblings are properly ordered by weight (left <= right)
     */
    private static boolean validateSiblingWeightOrder(Node root) {
        if (root == null) return true;
        
        if (root.left != null && root.right != null) {
            // Make sure left subtree weight <= right subtree weight
            if (root.left.weight > root.right.weight) {
                System.out.println("Sibling weight order violated at node: weight=" + 
                                  root.weight + ", order=" + root.order + 
                                  " (left=" + root.left.weight +
                                  ", right=" + root.right.weight + ")");
                return false;
            }
        }
        
        // Check children recursively
        if (root.left != null && !validateSiblingWeightOrder(root.left)) return false;
        if (root.right != null && !validateSiblingWeightOrder(root.right)) return false;
        
        return true;
    }
    
    /**
     * Collect all nodes in the tree into a list
     */
    private static void collectAllNodes(Node node, List<Node> result) {
        if (node == null) return;
        
        result.add(node);
        collectAllNodes(node.left, result);
        collectAllNodes(node.right, result);
    }
    
    /**
     * Print the tree structure to help with debugging
     * 
     * This was a lifesaver for complex debugging - I could see the full tree
     * and identify where things were going wrong
     */
    public static void printTree(Node root) {
        System.out.println("\nTree Structure:");
        printNode(root, 0);
    }
    
    private static void printNode(Node node, int depth) {
        if (node == null) return;
        
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) indent.append("  ");
        
        String symbol;
        if (node.isNYT()) symbol = "NYT";
        else if (node.isLeaf()) symbol = String.valueOf(node.symbol);
        else symbol = "*";
        
        System.out.println(indent + symbol + " (weight=" + node.weight + 
                          ", order=" + node.order + ")");
        
        printNode(node.left, depth + 1);
        printNode(node.right, depth + 1);
    }
}
