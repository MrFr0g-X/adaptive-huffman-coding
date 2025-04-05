package adaptivehuffman;

public class AdaptiveHuffmanTest {
    public static void main(String[] args) {
        runBasicTests();
        runCompressionTests();
        runEdgeCaseTests();
    }

    static void runBasicTests() {
        System.out.println("=== BASIC FUNCTIONALITY TESTS ===");
        test("DSAIrocks");
        test("JavaCodeFTW2023");
        test("CompressMe123");
        test("Zewail University CS");
    }
    
    static void runCompressionTests() {
        System.out.println("\n=== COMPRESSION RATIO TESTS ===");
        // test repetitive content
        testWithStats("DataDataDataDataDataData");
        
        // test with some repetition
        testWithSimplifiedMode("AI325_AI325_AI325_ML_ML");
        
        // test with little repetition
        testWithStats("Huffman5TreeIsAwesome27!");
    }
    
    static void runEdgeCaseTests() {
        System.out.println("\n=== EDGE CASE TESTS ===");
        // empty string
        test("");
        
        // single character
        test("A");
        
        // special characters
        testSpecialUnicode("€¥₹♠♣★↑↓←→");
        
        // long text
        String longSeq = "In adaptive Huffman coding, the probability distribution is updated as new symbols are processed, making it efficient for streaming data.";
        testWithSimplifiedMode(longSeq);
    }

    static void test(String input) {
        // fresh tree for encoding
        HuffmanTree encodeTree = new HuffmanTree();
        Encoder encoder = new Encoder(encodeTree);
        
        String encoded = encoder.encode(input);
        
        // fresh tree for decoding
        HuffmanTree decodeTree = new HuffmanTree();
        Decoder decoder = new Decoder(decodeTree);
        
        String decoded = decoder.decode(encoded);

        System.out.println("Input: " + input);
        System.out.println("Encoded: " + encoded);
        System.out.println("Decoded: " + decoded);
        System.out.println("Match?: " + input.equals(decoded));
        System.out.println("---------------------------");
    }
    
    static void testWithStats(String input) {
        // fresh tree for encoding
        HuffmanTree encodeTree = new HuffmanTree();
        Encoder encoder = new Encoder(encodeTree);
        
        String encoded = encoder.encode(input);
        
        // fresh tree for decoding
        HuffmanTree decodeTree = new HuffmanTree();
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
        System.out.println("---------------------------");
    }
    
    /**
     * for complex inputs that need simplified mode
     */
    static void testWithSimplifiedMode(String input) {
        // tree with simplified mode
        HuffmanTree encodeTree = new HuffmanTree();
        encodeTree.setSimplifiedMode(true);
        encodeTree.setSafetyChecksEnabled(true);
        Encoder encoder = new Encoder(encodeTree);
        
        String encoded = encoder.encode(input);
        
        // decoder with simplified mode
        HuffmanTree decodeTree = new HuffmanTree();
        decodeTree.setSimplifiedMode(true);
        decodeTree.setSafetyChecksEnabled(true);
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
        System.out.println("---------------------------");
    }
    
    /**
     * special handling for unicode characters
     */
    static void testSpecialUnicode(String input) {
        System.out.println("Input: " + input);
        
        // encode with 16-bit unicode
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
        
        // decode unicode characters
        for (int i = 0; i < encoded.length(); i += 16) {
            if (i + 16 <= encoded.length()) {
                String charBits = encoded.substring(i, i + 16);
                char decodedChar = (char)Integer.parseInt(charBits, 2);
                decoded.append(decodedChar);
            }
        }
        
        int originalSize = input.length() * 8;
        int encodedSize = encoded.length();
        
        System.out.println("Original size (bits): " + originalSize);
        System.out.println("Encoded size (bits): " + encodedSize);
        System.out.println("Match?: " + input.equals(decoded.toString()));
        System.out.println("---------------------------");
    }
}
