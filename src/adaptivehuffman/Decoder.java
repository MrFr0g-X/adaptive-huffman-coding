package adaptivehuffman;

/**
 * This class handles the decoding process for our Adaptive Huffman implementation.
 * It takes a binary string (encoded text) and converts it back to the original text.
 */
public class Decoder {
    HuffmanTree tree;  // Reference to our Huffman tree

    /**
     * Creates a new decoder using the provided tree
     */
    public Decoder(HuffmanTree tree) {
        this.tree = tree;
    }

    /**
     * Decodes a binary string back to the original text.
     * 
     * The process:
     * 1. Start at the root of the tree
     * 2. Follow the path specified by the bits until reaching a leaf node
     * 3. If we reach the NYT node, read the next 8 bits as an ASCII character
     * 4. If we reach a character node, output that character
     * 5. Update the tree just like in the encoding process
     * 6. Start over from the root for the next character
     * 
     * This was one of the trickier parts to get right, especially edge cases.
     */
    public String decode(String bits) {
        String decoded = "";
        Node current = tree.root;
        int i = 0;

        // Handle empty string case
        if (bits.isEmpty()) {
            return "";
        }

        while (i <= bits.length()) {
            // When we reach a leaf node, process it
            if (current.isLeaf()) {
                if (current == tree.NYT) {
                    // This is the NYT node, so read the next 8 bits as an ASCII character
                    if (i + 8 > bits.length()) break;  // Not enough bits left
                    
                    String asciiBits = bits.substring(i, i + 8);
                    char c = (char)Integer.parseInt(asciiBits, 2);
                    decoded += c;
                    tree.insertNewSymbol(c);  // Add this new symbol to the tree
                    i += 8;  // Skip past the ASCII bits
                } else {
                    // This is a character node, output its symbol
                    decoded += current.symbol;
                    tree.updateExistingSymbol(current.symbol);  // Update its frequency
                }
                
                // Reset to the root for the next character
                current = tree.root;
                
                // Break if we've consumed all bits and processed the last node
                if (i >= bits.length()) {
                    break;
                }
            } else {
                // We're at an internal node, navigate based on the current bit
                if (i < bits.length()) {
                    if (bits.charAt(i) == '0') {
                        current = current.left;  // 0 = go left
                    } else {
                        current = current.right;  // 1 = go right
                    }
                    i++;  // Move to the next bit
                } else {
                    // We've run out of bits but haven't reached a leaf
                    // This shouldn't happen with valid input
                    break;
                }
            }
        }
        return decoded;
    }
}
