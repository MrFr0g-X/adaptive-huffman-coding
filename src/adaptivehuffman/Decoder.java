package adaptivehuffman;

/**
 * handles decoding process for adaptive huffman
 */
public class Decoder {
    HuffmanTree tree;

    /**
     * creates decoder with a huffman tree
     */
    public Decoder(HuffmanTree tree) {
        this.tree = tree;
    }

    /**
     * decodes binary string back to original text
     * this was really tricky to get right
     */
    public String decode(String bits) {
        String decoded = "";
        Node current = tree.root;
        int i = 0;

        // empty string case
        if (bits.isEmpty()) {
            return "";
        }

        while (i <= bits.length()) {
            // leaf node found
            if (current.isLeaf()) {
                if (current == tree.NYT) {
                    // NYT node - read 8 bits as ASCII
                    if (i + 8 > bits.length()) break;
                    
                    String asciiBits = bits.substring(i, i + 8);
                    char c = (char)Integer.parseInt(asciiBits, 2);
                    decoded += c;
                    tree.insertNewSymbol(c);
                    i += 8;
                } else {
                    // character node - output symbol
                    decoded += current.symbol;
                    tree.updateExistingSymbol(current.symbol);
                }
                
                // reset to root for next character
                current = tree.root;
                
                if (i >= bits.length()) {
                    break;
                }
            } else {
                // internal node - navigate based on bit
                if (i < bits.length()) {
                    if (bits.charAt(i) == '0') {
                        current = current.left;
                    } else {
                        current = current.right;
                    }
                    i++;
                } else {
                    break;
                }
            }
        }
        return decoded;
    }
}
