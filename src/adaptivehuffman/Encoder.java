package adaptivehuffman;

/**
 * This class handles the encoding process for our Adaptive Huffman implementation.
 * It takes an input string and converts it to a binary representation using
 * the adaptive Huffman algorithm.
 */
public class Encoder {
    HuffmanTree tree;  // Reference to our Huffman tree

    /**
     * Creates a new encoder using the provided tree
     */
    public Encoder(HuffmanTree tree) {
        this.tree = tree;
    }

    /**
     * Encodes a string to binary using adaptive Huffman coding.
     * 
     * The basic process for each character:
     * 1. If we've seen the character before, output its code and update its frequency
     * 2. If it's a new character, output the NYT code followed by the character's 
     *    ASCII representation, then add it to the tree
     * 
     * The tree gets updated after each character is processed.
     */
    public String encode(String text) {
        StringBuilder encoded = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            if (tree.contains(c)) {
                // Case 1: We've seen this character before
                encoded.append(tree.getCode(c));  // Get the current code for the character
                tree.updateExistingSymbol(c);     // Update its frequency in the tree
            } else {
                // Case 2: This is a new character
                encoded.append(tree.getNYTCode());  // Output the NYT code
                encoded.append(toAscii(c));         // Output the character's ASCII representation
                tree.insertNewSymbol(c);            // Add the character to the tree
            }
        }
        return encoded.toString();
    }

    /**
     * Converts a character to its 8-bit binary ASCII representation.
     * This is needed when we encounter a new character.
     * 
     * I had to add padding for characters with fewer than 8 bits.
     */
    String toAscii(char c) {
        String binary = Integer.toBinaryString(c);
        
        // Add leading zeros if needed to make it 8 bits
        if (binary.length() < 8) {
            StringBuilder padded = new StringBuilder();
            for (int i = binary.length(); i < 8; i++) {
                padded.append('0');
            }
            padded.append(binary);
            return padded.toString();
        }
        
        return binary;
    }
}
