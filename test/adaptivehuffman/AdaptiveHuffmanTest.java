package adaptivehuffman;

public class AdaptiveHuffmanTest {
    public static void main(String[] args) {
        runBasicTests();
        runCompressionTests();
        runEdgeCaseTests();
    }

    static void runBasicTests() {
        System.out.println("=== BASIC FUNCTIONALITY TESTS ===");
        test("AAA");
        test("ABABABC");
        test("MISSISSIPPI");
        test("Hello World!");
    }
    
    static void runCompressionTests() {
        System.out.println("\n=== COMPRESSION RATIO TESTS ===");
        // Test with highly repetitive content (should compress well)
        testWithStats("AAAAAAAAAAAAAAAAAAAA");
        
        // Test with some repetition
        testWithStats("ABCABCABCABCABCABC");
        
        // Test with little repetition (won't compress well)
        testWithStats("ABCDEFGHIJKLMNOPQRST");
    }
    
    static void runEdgeCaseTests() {
        System.out.println("\n=== EDGE CASE TESTS ===");
        // Empty string
        test("");
        
        // Single character
        test("Z");
        
        // Special characters
        test("!@#$%^&*()_+");
        
        // Long sequence 
        String longSeq = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        test(longSeq);
    }

    static void test(String input) {
        HuffmanTree tree = new HuffmanTree();
        Encoder encoder = new Encoder(tree);
        Decoder decoder = new Decoder(tree);

        String encoded = encoder.encode(input);
        String decoded = decoder.decode(encoded);

        System.out.println("Input: " + input);
        System.out.println("Encoded: " + encoded);
        System.out.println("Decoded: " + decoded);
        System.out.println("Match?: " + input.equals(decoded));
        System.out.println("---------------------------");
    }
    
    static void testWithStats(String input) {
        HuffmanTree tree = new HuffmanTree();
        Encoder encoder = new Encoder(tree);
        Decoder decoder = new Decoder(tree);

        String encoded = encoder.encode(input);
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
}
