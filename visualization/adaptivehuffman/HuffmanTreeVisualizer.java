package adaptivehuffman;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * JavaFX application for visualizing the Adaptive Huffman tree in real-time.
 * 
 * This visualization component was my favorite part of the project - it made the algorithm
 * so much easier to understand and debug. I spent extra time making it interactive and 
 * educational so I could see exactly how the tree evolves character by character.
 * 
 * Key features:
 * - Real-time visualization of the adaptive Huffman tree as it evolves
 * - Step-by-step encoding and decoding modes
 * - Highlighting of active paths and nodes during operations
 * - Sibling property validation with visual feedback
 * - Interactive demo mode with predefined examples
 * 
 * This component was very helpful for finding and fixing bugs in my implementation.
 */
public class HuffmanTreeVisualizer extends Application {
    private HuffmanTree tree;
    private Encoder encoder;
    private Decoder decoder;  // Add decoder reference
    private Pane treePane; // Pane to hold the tree visualization
    private TextField inputField;
    private TextField encodedInputField;  // New field for encoded input
    private Label outputLabel;
    private Label encodedLabel;
    private Label errorLabel;
    private String currentInput = "";
    private String currentEncoded = "";
    private List<String> traversalPath = new ArrayList<>();  // Track path for visualization
    private int currentBitIndex = 0;  // Track current bit during decoding
    
    // Visual settings
    private final int NODE_RADIUS = 18;
    private final int VERTICAL_GAP = 60;
    private final int INITIAL_X = 400;
    private final int INITIAL_Y = 50;
    
    private Label siblingPropertyLabel;
    private boolean showOrderNumbers = true;
    private List<Node> lastUpdatedNodes = new ArrayList<>();
    private List<Line> lastUpdatedEdges = new ArrayList<>();
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize tree with settings optimized for visualization
        tree = new HuffmanTree();
        tree.setSimplifiedMode(false); // Show full algorithm behavior with node swaps
        tree.setCustomNodeLimit(30);    // Reasonable limit for visualization
        tree.setSafetyChecksEnabled(true);
        tree.setMaxUpdateDepth(100);
        
        encoder = new Encoder(tree);
        decoder = new Decoder(tree);  // Initialize decoder
        
        // Create main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Create pane for tree visualization
        treePane = new Pane();
        treePane.setPrefSize(800, 400);
        treePane.setStyle("-fx-background-color: #f0f0f0;");
        root.setCenter(treePane);
        
        // Create input controls
        VBox controlsBox = new VBox(10);
        controlsBox.setPadding(new Insets(10));
        
        // Encoding section
        HBox inputBox = new HBox(10);
        Label inputLabel = new Label("Encode Character:");
        inputField = new TextField();
        inputField.setPrefWidth(100);
        Button processButton = new Button("Process Character");
        inputBox.getChildren().addAll(inputLabel, inputField, processButton);
        
        // Decoding section 
        HBox decodeBox = new HBox(10);
        Label decodeLabel = new Label("Decode Binary:");
        encodedInputField = new TextField();
        encodedInputField.setPrefWidth(300);
        Button decodeButton = new Button("Decode");
        Button nextBitButton = new Button("Next Bit");
        decodeBox.getChildren().addAll(decodeLabel, encodedInputField, decodeButton, nextBitButton);
        
        // Control buttons
        HBox controlBox = new HBox(10);
        Button resetButton = new Button("Reset");
        Button demoButton = new Button("Run Demo");
        Button toggleOrderButton = new Button("Toggle Order Numbers");
        controlBox.getChildren().addAll(resetButton, demoButton, toggleOrderButton);
        
        controlsBox.getChildren().addAll(inputBox, decodeBox, controlBox);
        root.setTop(controlsBox);
        
        // Create output display
        VBox outputBox = new VBox(10);
        outputBox.setPadding(new Insets(10));
        
        Label infoLabel = new Label("Processed Characters:");
        infoLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        outputLabel = new Label("");
        encodedLabel = new Label("Encoded: ");
        errorLabel = new Label("");
        errorLabel.setTextFill(Color.RED);
        
        outputBox.getChildren().addAll(infoLabel, outputLabel, encodedLabel, errorLabel);
        
        // Add sibling property check label
        siblingPropertyLabel = new Label("Sibling Property: Valid");
        siblingPropertyLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        siblingPropertyLabel.setTextFill(Color.GREEN);
        outputBox.getChildren().add(siblingPropertyLabel);
        
        root.setBottom(outputBox);
        
        // Setup event handlers with error handling
        processButton.setOnAction(e -> {
            try {
                String text = inputField.getText();
                if (!text.isEmpty()) {
                    // Process first character only for visual clarity
                    char c = text.charAt(0);
                    processCharacter(c);
                    inputField.setText("");
                }
            } catch (Exception ex) {
                errorLabel.setText("Error: " + ex.getMessage());
            }
        });
        
        // Add decode button handler
        decodeButton.setOnAction(e -> {
            try {
                String encodedBits = encodedInputField.getText().trim();
                if (!encodedBits.isEmpty()) {
                    // Reset for fresh decoding
                    resetDecodingState();
                    // Start decoding visualization
                    visualizeDecoding(encodedBits);
                }
            } catch (Exception ex) {
                errorLabel.setText("Error: " + ex.getMessage());
            }
        });
        
        // Add next bit button handler
        nextBitButton.setOnAction(e -> {
            try {
                String encodedBits = encodedInputField.getText().trim();
                if (currentBitIndex < encodedBits.length()) {
                    processNextBit();
                } else {
                    errorLabel.setText("End of bit stream reached");
                }
            } catch (Exception ex) {
                errorLabel.setText("Error: " + ex.getMessage());
            }
        });
        
        resetButton.setOnAction(e -> {
            resetTree();
            errorLabel.setText("");
        });
        
        demoButton.setOnAction(e -> {
            resetTree();
            runDemo();
        });
        
        // Add toggle order button action
        toggleOrderButton.setOnAction(e -> {
            showOrderNumbers = !showOrderNumbers;
            updateTreeVisualization();
        });
        
        // Initial draw
        updateTreeVisualization();
        
        // Create scene
        Scene scene = new Scene(root, 800, 650);  // Increased height for new controls
        primaryStage.setTitle("Adaptive Huffman Tree Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Runs a demonstration of the adaptive Huffman algorithm using the "bookkeeper" example.
     * This example was chosen because it shows interesting tree evolution with character repeats.
     * 
     * This demo processes one character at a time so users can see the tree evolve step by step.
     */
    private void runDemo() {
        // For better demonstration of FGK algorithm, use "bookkeeper" example
        String demoText = "bookkeeper";
        errorLabel.setText("Running demo with: " + demoText);
        
        new Thread(() -> {
            try {
                for (char c : demoText.toCharArray()) {
                    final char currentChar = c;
                    Thread.sleep(2000); // Longer delay for better observation
                    
                    Platform.runLater(() -> {
                        try {
                            processCharacter(currentChar);
                            // Add debugging output
                            debugTree();
                        } catch (Exception e) {
                            errorLabel.setText("Error: " + e.getMessage());
                        }
                    });
                }
                
                Platform.runLater(() -> {
                    errorLabel.setText("Demo completed! Tree shows the final state for 'bookkeeper'");
                });
                
            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    errorLabel.setText("Demo interrupted!");
                });
            }
        }).start();
    }
    
    /**
     * Outputs the current tree state to the console for debugging.
     * This was invaluable during development for verifying the tree structure.
     */
    private void debugTree() {
        System.out.println("Current Tree State:");
        printNodeInfo(tree.getRoot(), 0);
        System.out.println("Sibling Property Valid: " + checkSiblingProperty(tree.getRoot()));
    }
    
    private void printNodeInfo(Node node, int depth) {
        if (node == null) return;
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) sb.append("  ");
        
        String symbol = node.isNYT() ? "NYT" : (node.isLeaf() ? String.valueOf(node.symbol) : "*");
        sb.append(symbol + " (w:" + node.weight + ", o:" + node.order + ")");
        System.out.println(sb.toString());
        
        printNodeInfo(node.left, depth + 1);
        printNodeInfo(node.right, depth + 1);
    }
    
    // More thorough sibling property check
    private boolean checkSiblingProperty(Node root) {
        if (root == null) return true;
        
        // First check: nodes should be in weight order (bottom-up, left-to-right)
        ArrayList<Node> allNodes = new ArrayList<>();
        collectAllNodes(root, allNodes);
        
        // Sort by weight and position in tree
        for (int i = 0; i < allNodes.size() - 1; i++) {
            Node current = allNodes.get(i);
            Node next = allNodes.get(i+1);
            
            // Find siblings
            if (current.parent == next.parent) {
                // Siblings should have same weight or left should be <= right
                if (current.weight > next.weight) {
                    System.out.println("Sibling property violated: siblings out of order");
                    System.out.println("  " + nodeToString(current) + " > " + nodeToString(next));
                    return false;
                }
            }
        }
        
        // Second check: all internal nodes should have two children
        for (Node node : allNodes) {
            if (!node.isLeaf()) {
                if (node.left == null || node.right == null) {
                    System.out.println("Sibling property violated: internal node missing child");
                    System.out.println("  " + nodeToString(node));
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private String nodeToString(Node node) {
        if (node == null) return "NULL";
        
        String symbol = node.isNYT() ? "NYT" : (node.isLeaf() ? String.valueOf(node.symbol) : "*");
        return symbol + " (w:" + node.weight + ", o:" + node.order + ")";
    }
    
    private void collectAllNodes(Node node, ArrayList<Node> result) {
        if (node == null) return;
        
        result.add(node);
        collectAllNodes(node.left, result);
        collectAllNodes(node.right, result);
    }
    
    /**
     * Processes a single character through the adaptive Huffman encoding process.
     * For each character:
     * 1. If it's already in the tree, output its code and update its frequency
     * 2. If it's new, output NYT code + ASCII and add it to the tree
     * 
     * The visualization highlights the relevant nodes and paths during this process.
     */
    private void processCharacter(char c) {
        // Update current input
        currentInput += c;
        
        // Encode one character at a time to see the tree evolve
        StringBuilder sb = new StringBuilder();
        
        try {
            // Track the nodes that will be updated for highlighting
            lastUpdatedNodes.clear();
            lastUpdatedEdges.clear();
            
            if (tree.hasChar(c)) {
                Node node = tree.getNode(c);
                // Track this node for highlighting
                lastUpdatedNodes.add(node);
                
                String code = tree.getPathToNode(node);
                sb.append(code);
                currentEncoded += code;
                
                // This will trigger swaps that maintain the sibling property
                tree.updateTree(node);
            } else {
                String nytCode = tree.getNYTCode();
                sb.append(nytCode);
                
                // Convert character to 8-bit binary
                String asciiCode = Integer.toBinaryString(c);
                while (asciiCode.length() < 8) {
                    asciiCode = "0" + asciiCode;
                }
                
                sb.append(asciiCode);
                currentEncoded += nytCode + asciiCode;
                
                // Add to tree - this will create new nodes and potentially trigger swaps
                Node newNode = tree.addChar(c);
                // Track this node for highlighting
                lastUpdatedNodes.add(newNode);
            }
            
            // Update labels
            outputLabel.setText(currentInput);
            encodedLabel.setText("Encoded: " + currentEncoded);
            
            // Update visualization
            updateTreeVisualization();
            errorLabel.setText("");
            
            // Check if tree satisfies the sibling property
            boolean validSibling = checkSiblingProperty(tree.getRoot());
            if (validSibling) {
                siblingPropertyLabel.setText("Sibling Property: Valid ✓");
                siblingPropertyLabel.setTextFill(Color.GREEN);
            } else {
                siblingPropertyLabel.setText("Sibling Property: Invalid ✗");
                siblingPropertyLabel.setTextFill(Color.RED);
            }
            
        } catch (Exception e) {
            errorLabel.setText("Error processing '" + c + "': " + e.getMessage());
        }
    }
    
    /**
     * Resets the tree to its initial state with just the NYT node.
     * This allows exploring multiple examples without restarting the application.
     */
    private void resetTree() {
        tree = new HuffmanTree();
        tree.setSimplifiedMode(false); // We want to see the node swaps
        tree.setCustomNodeLimit(50);    // Allow more nodes for educational visualization
        tree.setSafetyChecksEnabled(true);
        tree.setMaxUpdateDepth(100);
        
        encoder = new Encoder(tree);
        currentInput = "";
        currentEncoded = "";
        outputLabel.setText("");
        encodedLabel.setText("Encoded: ");
        
        updateTreeVisualization();
    }
    
    // Reset tracking variables for a fresh decoding session
    private void resetDecodingState() {
        currentBitIndex = 0;
        traversalPath.clear();
        errorLabel.setText("");
        updateTreeVisualization();
    }
    
    /**
     * Initializes the step-by-step decoding visualization.
     * This feature allows users to see exactly how decoding works bit by bit.
     */
    private void visualizeDecoding(String encodedBits) {
        resetDecodingState();
        errorLabel.setText("Ready to decode. Press 'Next Bit' to step through.");
        outputLabel.setText("Decoded so far: ");
    }
    
    /**
     * Processes a single bit during step-by-step decoding visualization.
     * This shows how the decoder traverses the tree based on the encoded bits.
     * 
     * It handles both normal node traversal and the special NYT + ASCII case.
     */
    private void processNextBit() {
        String encodedBits = encodedInputField.getText().trim();
        if (currentBitIndex >= encodedBits.length()) {
            errorLabel.setText("End of bit stream reached");
            return;
        }
        
        try {
            // Get current node in the traversal
            Node current = traversalPath.isEmpty() ? tree.getRoot() : findNodeInTraversal();
            
            if (current.isLeaf()) {
                // We're at a leaf node - process it
                if (current == tree.NYT) {
                    // NYT node - read the next 8 bits for ASCII
                    if (currentBitIndex + 8 > encodedBits.length()) {
                        errorLabel.setText("Not enough bits for ASCII character after NYT");
                        return;
                    }
                    
                    String asciiBits = encodedBits.substring(currentBitIndex, currentBitIndex + 8);
                    char c = (char)Integer.parseInt(asciiBits, 2);
                    
                    // Add character to decoded output
                    String decodedSoFar = outputLabel.getText();
                    outputLabel.setText(decodedSoFar + c);
                    
                    // Insert into tree
                    tree.insertNewSymbol(c);
                    
                    // Update bit index and clear traversal path
                    currentBitIndex += 8;
                    traversalPath.clear();
                    
                    errorLabel.setText("Decoded NYT + ASCII to character: '" + c + 
                                      "', added to tree. Starting next character.");
                } else {
                    // Leaf node with a symbol
                    char c = current.symbol;
                    
                    // Add character to decoded output
                    String decodedSoFar = outputLabel.getText();
                    outputLabel.setText(decodedSoFar + c);
                    
                    // Update tree
                    tree.updateExistingSymbol(c);
                    
                    // Clear traversal path to start next character
                    traversalPath.clear();
                    
                    errorLabel.setText("Decoded leaf node to character: '" + c + 
                                      "'. Tree updated. Starting next character.");
                }
            } else {
                // Not at a leaf - continue traversing based on next bit
                char bit = encodedBits.charAt(currentBitIndex++);
                
                if (bit == '0') {
                    traversalPath.add("left");
                    errorLabel.setText("Following '0' to left child. Bit " + currentBitIndex + " of " + 
                                      encodedBits.length());
                } else {
                    traversalPath.add("right");
                    errorLabel.setText("Following '1' to right child. Bit " + currentBitIndex + " of " + 
                                      encodedBits.length());
                }
            }
            
            // Highlight the current path
            lastUpdatedNodes.clear();
            highlightDecodingPath();
            updateTreeVisualization();
            
        } catch (Exception e) {
            errorLabel.setText("Error during decoding: " + e.getMessage());
        }
    }
    
    // Find the current node in the traversal
    private Node findNodeInTraversal() {
        Node current = tree.getRoot();
        
        for (String direction : traversalPath) {
            if (direction.equals("left")) {
                current = current.left;
            } else {
                current = current.right;
            }
            
            // Safety check
            if (current == null) {
                throw new RuntimeException("Invalid traversal path - reached null node");
            }
        }
        
        return current;
    }
    
    // Highlight the current decoding path
    private void highlightDecodingPath() {
        Node current = tree.getRoot();
        lastUpdatedNodes.add(current);
        
        for (String direction : traversalPath) {
            if (direction.equals("left")) {
                current = current.left;
            } else {
                current = current.right;
            }
            
            if (current != null) {
                lastUpdatedNodes.add(current);
            }
        }
    }
    
    /**
     * Updates the visual representation of the tree.
     * This is where most of the drawing logic happens - positioning nodes, 
     * drawing connections, and adding labels.
     */
    private void updateTreeVisualization() {
        Platform.runLater(() -> {
            treePane.getChildren().clear();
            
            if (tree.getRoot() == null) {
                Text emptyText = new Text(INITIAL_X, INITIAL_Y, "Empty Tree");
                emptyText.setFont(Font.font("System", FontWeight.BOLD, 14));
                treePane.getChildren().add(emptyText);
                return;
            }
            
            try {
                // Implement a layout algorithm that properly shows the FGK tree
                // with nodes ordered by weight from left to right and bottom to top
                Map<Node, Position> positions = new HashMap<>();
                calculateFGKPositions(tree.getRoot(), positions);
                
                // Draw edges first (so they appear under nodes)
                drawEdges(treePane, tree.getRoot(), positions);
                
                // Then draw nodes
                drawNodes(treePane, tree.getRoot(), positions);
            } catch (Exception e) {
                Text errorText = new Text(50, 50, "Error drawing tree: " + e.getMessage());
                errorText.setFill(Color.RED);
                treePane.getChildren().add(errorText);
            }
        });
    }
    
    /**
     * Calculates node positions in a way that properly reflects the FGK algorithm's
     * node ordering - nodes should be arranged by weight from left to right.
     * 
     * This was tricky to get right because the visualization needs to be both
     * correct and visually clear.
     */
    private void calculateFGKPositions(Node root, Map<Node, Position> positions) {
        if (root == null) return;
        
        // First collect all nodes
        ArrayList<Node> allNodes = new ArrayList<>();
        collectAllNodes(root, allNodes);
        
        // Create levels (breadth-first layout)
        Map<Integer, ArrayList<Node>> levels = new HashMap<>();
        for (Node node : allNodes) {
            int level = getNodeLevel(node, root);
            if (!levels.containsKey(level)) {
                levels.put(level, new ArrayList<>());
            }
            levels.get(level).add(node);
        }
        
        // Calculate horizontal positions level by level
        int maxLevel = levels.size();
        for (int level = 0; level < maxLevel; level++) {
            ArrayList<Node> levelNodes = levels.get(level);
            
            // Sort nodes by order for consistent placement
            Collections.sort(levelNodes, (a, b) -> {
                if (a.weight != b.weight) return Integer.compare(a.weight, b.weight);
                return Integer.compare(b.order, a.order);
            });
            
            // Position nodes
            double segmentWidth = 800.0 / (levelNodes.size() + 1);
            for (int i = 0; i < levelNodes.size(); i++) {
                Node node = levelNodes.get(i);
                positions.put(node, new Position((int)((i + 1) * segmentWidth), level * VERTICAL_GAP + 50));
            }
        }
    }
    
    private int getNodeLevel(Node node, Node root) {
        if (node == root) return 0;
        
        int level = 0;
        Node current = node;
        while (current != root && current != null) {
            level++;
            current = current.parent;
        }
        return level;
    }
    
    /**
     * Draws the edges (connections) between nodes.
     * The edges are color-coded:
     * - Gray for normal edges
     * - Orange for recently updated edges
     * - Red for the current decoding path
     * 
     * Labels show the binary code for each edge (0 = left, 1 = right).
     */
    private void drawEdges(Pane pane, Node node, Map<Node, Position> positions) {
        if (node == null) return;
        
        Position nodePos = positions.get(node);
        
        // Draw lines to children
        if (node.left != null) {
            Position leftPos = positions.get(node.left);
            Line line = new Line(nodePos.x, nodePos.y, leftPos.x, leftPos.y);
            
            // Check if this edge is part of the current decoding path
            boolean isDecodingPathEdge = false;
            if (lastUpdatedNodes.contains(node) && lastUpdatedNodes.contains(node.left)) {
                // Check if they're adjacent in the path
                int nodeIndex = lastUpdatedNodes.indexOf(node);
                int childIndex = lastUpdatedNodes.indexOf(node.left);
                if (Math.abs(nodeIndex - childIndex) == 1) {
                    isDecodingPathEdge = true;
                }
            }
            
            // Highlight decoding path edges more prominently
            if (isDecodingPathEdge) {
                line.setStroke(Color.RED);
                line.setStrokeWidth(3);
            } 
            // Standard highlighting for updated nodes
            else if (lastUpdatedNodes.contains(node.left) || lastUpdatedNodes.contains(node)) {
                line.setStroke(Color.ORANGERED);
                line.setStrokeWidth(2);
            } else {
                line.setStroke(Color.GRAY);
                line.setStrokeWidth(1);
            }
            
            lastUpdatedEdges.add(line);
            
            // Add label for edge
            Text edgeLabel = new Text((nodePos.x + leftPos.x)/2 - 10, (nodePos.y + leftPos.y)/2, "0");
            edgeLabel.setFill(Color.BLUE);
            edgeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            pane.getChildren().addAll(line, edgeLabel);
            
            drawEdges(pane, node.left, positions);
        }
        
        if (node.right != null) {
            Position rightPos = positions.get(node.right);
            Line line = new Line(nodePos.x, nodePos.y, rightPos.x, rightPos.y);
            
            // Check if this edge is part of the current decoding path
            boolean isDecodingPathEdge = false;
            if (lastUpdatedNodes.contains(node) && lastUpdatedNodes.contains(node.right)) {
                // Check if they're adjacent in the path
                int nodeIndex = lastUpdatedNodes.indexOf(node);
                int childIndex = lastUpdatedNodes.indexOf(node.right);
                if (Math.abs(nodeIndex - childIndex) == 1) {
                    isDecodingPathEdge = true;
                }
            }
            
            // Highlight decoding path edges more prominently
            if (isDecodingPathEdge) {
                line.setStroke(Color.RED);
                line.setStrokeWidth(3);
            } 
            // Standard highlighting for updated nodes
            else if (lastUpdatedNodes.contains(node.right) || lastUpdatedNodes.contains(node)) {
                line.setStroke(Color.ORANGERED);
                line.setStrokeWidth(2);
            } else {
                line.setStroke(Color.GRAY);
                line.setStrokeWidth(1);
            }
            
            lastUpdatedEdges.add(line);
            
            // Add label for edge
            Text edgeLabel = new Text((nodePos.x + rightPos.x)/2 + 5, (nodePos.y + rightPos.y)/2, "1");
            edgeLabel.setFill(Color.BLUE);
            edgeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            pane.getChildren().addAll(line, edgeLabel);
            
            drawEdges(pane, node.right, positions);
        }
    }
    
    /**
     * Draws the nodes themselves with appropriate colors and labels.
     * - Light gray: NYT node
     * - Light green: Character nodes (leaves)
     * - Light blue: Internal nodes
     * 
     * Each node shows its weight and optionally its order number.
     */
    private void drawNodes(Pane pane, Node node, Map<Node, Position> positions) {
        if (node == null) return;
        
        Position pos = positions.get(node);
        
        // Draw circle
        Circle circle = new Circle(pos.x, pos.y, NODE_RADIUS);
        
        // Choose color based on node type
        if (node.isNYT()) {
            circle.setFill(Color.LIGHTGRAY);
        } else if (node.isLeaf()) {
            circle.setFill(Color.LIGHTGREEN);
        } else {
            circle.setFill(Color.LIGHTBLUE);
        }
        
        // Highlight recently updated nodes - fix the check to match Node types
        if (lastUpdatedNodes.contains(node)) {
            circle.setStroke(Color.ORANGERED);
            circle.setStrokeWidth(3);
        } else {
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(1);
        }
        
        // Create node label
        Text nodeText;
        if (node.isNYT()) {
            nodeText = new Text(pos.x - 15, pos.y + 5, "NYT");
        } else if (node.isLeaf()) {
            nodeText = new Text(pos.x - 4, pos.y + 5, String.valueOf(node.symbol));
        } else {
            nodeText = new Text(pos.x - 4, pos.y + 5, "");
        }
        nodeText.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        // Create weight label
        Text weightText = new Text(pos.x - 10, pos.y - 25, "w:" + node.weight);
        weightText.setFont(Font.font("System", 10));
        
        pane.getChildren().addAll(circle, nodeText, weightText);
        
        // Add order number if enabled - this helps visualize the sibling property
        if (showOrderNumbers) {
            Text orderText = new Text(pos.x + 15, pos.y - 15, "#" + node.order);
            orderText.setFill(Color.DARKBLUE);
            orderText.setFont(Font.font("System", FontWeight.BOLD, 9));
            pane.getChildren().add(orderText);
        }
        
        // Draw children recursively
        if (node.left != null) {
            drawNodes(pane, node.left, positions);
        }
        
        if (node.right != null) {
            drawNodes(pane, node.right, positions);
        }
    }
    
    private static class Position {
        int x, y;
        
        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}












