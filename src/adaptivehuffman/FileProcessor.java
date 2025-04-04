package adaptivehuffman;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * This class handles file-based encoding and decoding operations using Adaptive Huffman coding.
 * It provides functionality to process files instead of just strings in memory.
 * 
 * I created this to make testing with real files easier, especially for the project submission.
 */
public class FileProcessor {
    
    public static void main(String[] args) {
        if (args.length < 3) {
            printUsage();
            return;
        }
        
        String operation = args[0].toLowerCase();
        String inputFile = args[1];
        String outputFile = args[2];
        
        try {
            switch (operation) {
                case "encode":
                    encodeFile(inputFile, outputFile);
                    break;
                case "decode":
                    decodeFile(inputFile, outputFile);
                    break;
                default:
                    System.out.println("Unknown operation: " + operation);
                    printUsage();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Reads a file, encodes its content using Adaptive Huffman coding, and writes the result to another file.
     * This handles the entire encoding process from start to finish.
     * 
     * @param inputFile The file containing text to encode
     * @param outputFile Where to write the encoded binary string
     */
    public static void encodeFile(String inputFile, String outputFile) throws IOException {
        System.out.println("Encoding file: " + inputFile + " -> " + outputFile);
        
        // Read the input file
        String content = new String(Files.readAllBytes(Paths.get(inputFile)));
        System.out.println("Input file size: " + content.length() + " characters");
        
        // Create a fresh tree with robust settings
        HuffmanTree tree = new HuffmanTree();
        tree.setSimplifiedMode(false); // Use full algorithm for best compression
        tree.setSafetyChecksEnabled(true);
        
        // Create encoder
        Encoder encoder = new Encoder(tree);
        
        // Perform encoding with timing
        long startTime = System.currentTimeMillis();
        String encoded = encoder.encode(content);
        long encodingTime = System.currentTimeMillis() - startTime;
        
        // Write to output file
        Files.writeString(Paths.get(outputFile), encoded);
        
        // Calculate compression stats
        int originalSize = content.length() * 8;
        int compressedSize = encoded.length();
        double compressionRatio = (1 - ((double)compressedSize / originalSize)) * 100;
        
        // Display results
        System.out.println("Encoding completed in " + encodingTime + "ms");
        System.out.println("Original size: " + originalSize + " bits");
        System.out.println("Compressed size: " + compressedSize + " bits");
        System.out.printf("Compression ratio: %.2f%%\n", compressionRatio);
        System.out.println("Output written to: " + outputFile);
    }
    
    /**
     * Reads an encoded file, decodes its content using Adaptive Huffman coding, and writes the result to another file.
     * The decoder recreates the same tree structure that the encoder built.
     * 
     * @param inputFile The file containing the encoded binary string
     * @param outputFile Where to write the decoded text
     */
    public static void decodeFile(String inputFile, String outputFile) throws IOException {
        System.out.println("Decoding file: " + inputFile + " -> " + outputFile);
        
        // Read the input file
        String encoded = new String(Files.readAllBytes(Paths.get(inputFile)));
        System.out.println("Input file size: " + encoded.length() + " bits");
        
        // Create a fresh tree
        HuffmanTree tree = new HuffmanTree();
        tree.setSimplifiedMode(false);
        tree.setSafetyChecksEnabled(true);
        
        // Create decoder
        Decoder decoder = new Decoder(tree);
        
        // Perform decoding with timing
        long startTime = System.currentTimeMillis();
        String decoded = decoder.decode(encoded);
        long decodingTime = System.currentTimeMillis() - startTime;
        
        // Write to output file
        Files.writeString(Paths.get(outputFile), decoded);
        
        // Display results
        System.out.println("Decoding completed in " + decodingTime + "ms");
        System.out.println("Decoded size: " + (decoded.length() * 8) + " bits");
        System.out.println("Output written to: " + outputFile);
    }
    
    /**
     * Helper method to verify decoded file matches the original
     */
    public static boolean verifyFiles(String original, String decoded) {
        try {
            byte[] originalBytes = Files.readAllBytes(Paths.get(original));
            byte[] decodedBytes = Files.readAllBytes(Paths.get(decoded));
            
            if (originalBytes.length != decodedBytes.length) {
                System.out.println("Files have different lengths!");
                return false;
            }
            
            for (int i = 0; i < originalBytes.length; i++) {
                if (originalBytes[i] != decodedBytes[i]) {
                    System.out.printf("Mismatch at byte %d: %02X vs %02X\n", 
                                     i, originalBytes[i], decodedBytes[i]);
                    return false;
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error comparing files: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Shows usage information for the command-line interface
     */
    private static void printUsage() {
        System.out.println("Usage: java -cp bin adaptivehuffman.FileProcessor <operation> <input-file> <output-file>");
        System.out.println("  where <operation> is one of:");
        System.out.println("    encode - Compress the input file");
        System.out.println("    decode - Decompress the input file");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -cp bin adaptivehuffman.FileProcessor encode input.txt encoded.bin");
        System.out.println("  java -cp bin adaptivehuffman.FileProcessor decode encoded.bin output.txt");
        System.out.println();
        System.out.println("To verify decoded output matches original:");
        System.out.println("  java -cp bin adaptivehuffman.FileProcessor verify original.txt decoded.txt");
    }
}
