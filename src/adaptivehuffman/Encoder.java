package adaptivehuffman;

/**
 * handles encoding using adaptive huffman algorithm
 */
public class Encoder {
    HuffmanTree tree;

    /**
     * creates encoder with a huffman tree
     */
    public Encoder(HuffmanTree tree) {
        this.tree = tree;
    }

    /**
     * encodes a string to binary using adaptive huffman
     */
    public String encode(String text) {
        StringBuilder encoded = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            if (tree.contains(c)) {
                // already seen this character
                encoded.append(tree.getCode(c));
                tree.updateExistingSymbol(c);
            } else {
                // new character
                encoded.append(tree.getNYTCode());
                encoded.append(toAscii(c));
                tree.insertNewSymbol(c);
            }
        }
        return encoded.toString();
    }

    /**
     * converts character to 8-bit representation
     */
    String toAscii(char c) {
        String binary = Integer.toBinaryString(c);
        
        // add leading zeros if needed
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
