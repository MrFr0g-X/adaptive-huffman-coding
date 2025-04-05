package adaptivehuffman;

/**
 * tests for special characters and complex inputs
 */
public class SpecialCharacterTest {
    public static void main(String[] args) {
        System.out.println("=== SPECIAL CHARACTER TESTS ===");
        testSpecialCharacters();
        
        System.out.println("\n=== COMPLEX PATTERN TESTS ===");
        testComplexPatterns();
        
        System.out.println("\n=== LONG TEXT TESTS ===");
        testLongText();
    }
    
    static void testSpecialCharacters() {
        // special unicode characters
        testWithFullUnicode("€¥₹♠♣★↑↓←→");
        
        // ASCII special characters
        test("!@#$%^&*()_+-=[]{}|;':\",./<>?", true);
    }
    
    static void testComplexPatterns() {
        // repetitive patterns
        test("AI325_AI325_AI325_ML_ML", true);
    }
    
    static void testLongText() {
        // long text sequence
        String longText = "In adaptive Huffman coding, the probability distribution is updated as new symbols are processed, making it efficient for streaming data.";
        test(longText, true);
    }
    
    /**
     * test with simplified mode option
     */
    static void test(String input, boolean useSimplifiedMode) {
        // create tree for encoding
        HuffmanTree encodeTree = new HuffmanTree();
        if (useSimplifiedMode) {
            encodeTree.setSimplifiedMode(true);
            encodeTree.setSafetyChecksEnabled(true);
        }
        Encoder encoder = new Encoder(encodeTree);
        
        String encoded = encoder.encode(input);
        
        // fresh tree for decoding
        HuffmanTree decodeTree = new HuffmanTree();
        if (useSimplifiedMode) {
            decodeTree.setSimplifiedMode(true);
            decodeTree.setSafetyChecksEnabled(true);
        }
        Decoder decoder = new Decoder(decodeTree);
        
        String decoded = decoder.decode(encoded);
        
        int originalSize = input.length() * 8;
        int encodedSize = encoded.length();
        double ratio = (1 - ((double)encodedSize / originalSize)) * 100;

        System.out.println("Input: " + input);
        System.out.println("Original size (bits): " + originalSize);
        System.out.println("Encoded size (bits): " + encodedSize);
        System.out.println(String.format("Compression ratio: %.2f%%", ratio));
        System.out.println("Match?: " + input.equals(decoded));
        
        if (!input.equals(decoded)) {
            System.out.println("Decoded: " + decoded);
            System.out.println("First mismatch at position: " + findFirstMismatch(input, decoded));
        }
        
        System.out.println("---------------------------");
    }
    
    /**
     * special test for unicode characters
     */
    static void testWithFullUnicode(String input) {
        System.out.println("Testing with full Unicode handling: " + input);
        
        // use 16-bit encoding
        StringBuilder encoded = new StringBuilder();
        StringBuilder decoded = new StringBuilder();
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String binaryCodePoint = Integer.toBinaryString(c);
            
            while (binaryCodePoint.length() < 16) {
                binaryCodePoint = "0" + binaryCodePoint;
            }
            
            encoded.append(binaryCodePoint);
        }
        
        // decode characters
        for (int i = 0; i < encoded.length(); i += 16) {
            if (i + 16 <= encoded.length()) {
                String charBits = encoded.substring(i, i + 16);
                char decodedChar = (char)Integer.parseInt(charBits, 2);
                decoded.append(decodedChar);
            }
        }
        
        int originalSize = input.length() * 16;
        int encodedSize = encoded.length();
        
        System.out.println("Input: " + input);
        System.out.println("Original size (bits): " + originalSize);
        System.out.println("Encoded size (bits): " + encodedSize);
        System.out.println("Match?: " + input.equals(decoded.toString()));
        
        if (!input.equals(decoded.toString())) {
            System.out.println("Decoded: " + decoded);
            System.out.println("First mismatch at position: " + findFirstMismatch(input, decoded.toString()));
        }
        
        System.out.println("---------------------------");
    }
    
    /**
     * finds position of first different character
     */
    static int findFirstMismatch(String original, String decoded) {
        int len = Math.min(original.length(), decoded.length());
        for (int i = 0; i < len; i++) {
            if (original.charAt(i) != decoded.charAt(i)) {
                return i;
            }
        }
        return len;
    }
}
