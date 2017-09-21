package burrowsWheeler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;
import edu.princeton.cs.algs4.MinPQ;

/*************************************************************************
 *  Compilation:  javac Huffman.java
 *  Execution:    java Huffman - < input.txt   (compress)
 *  Execution:    java Huffman + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   http://algs4.cs.princeton.edu/55compression/abra.txt
 *                http://algs4.cs.princeton.edu/55compression/tinytinyTale.txt
 *
 *  Compress or expand a binary input stream using the Huffman algorithm.
 *
 *  % java Huffman - < abra.txt | java BinaryDump 60
 *  010100000100101000100010010000110100001101010100101010000100
 *  000000000000000000000000000110001111100101101000111110010100
 *  120 bits
 *
 *  % java Huffman - < abra.txt | java Huffman +
 *  ABRACADABRA!
 *
 *************************************************************************/

public class Huffman {

    // alphabet size of extended ASCII
    private static final int R = 256;
    
    public Node root; 

    // Huffman trie node
    private static class Node implements Comparable<Node> {
        private final char ch;
        private final int freq;
        private final Node left, right;

        Node(char ch, int freq, Node left, Node right) {
            this.ch    = ch;
            this.freq  = freq;
            this.left  = left;
            this.right = right;
        }

        // is the node a leaf node?
        private boolean isLeaf() {
            assert (left == null && right == null) || (left != null && right != null);
            return (left == null && right == null);
        }

        // compare, based on frequency
        public int compareTo(Node that) {
            return this.freq - that.freq;
        }
    }


    public ByteArrayOutputStream compress(Integer[] input) {
        
        // tabulate frequency counts
        int[] freq = new int[R];
        for (int i = 0; i < input.length; i++)
            freq[input[i]]++;

        // build Huffman trie
        Node root = this.buildTree(freq);

        // build code table
        String[] st = new String[R];
        buildCode(st, root, "");

        // print trie for decoder
        writeTrie(root);

        // print number of bytes in original uncompressed message
        System.out.println("Bytes in uncompressed message: " + input.length);

        // use Huffman code to encode input
        //int bitCount = 0;
        //int current = 0;
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitEncoder bitEncoder = new BitEncoder();
        for (int i = 0; i < input.length; i++) {
            String code = st[input[i]];
            for (int j = 0; j < code.length(); j++) {
            		if (code.charAt(j) == '0') {
            			bitEncoder.appendBit(false);
            		 } else if (code.charAt(j) == '1') {
            			 bitEncoder.appendBit(true);
            		 }
            		 else throw new IllegalStateException("Illegal state");
//            		current = current << 1;
//            		bitCount++;
//                if (code.charAt(j) == '0') {
//                    current += 0;
//                } else if (code.charAt(j) == '1') {
//                		current += 1;
//                }
//                else throw new IllegalStateException("Illegal state");
//                
//                if (bitCount == 8) {
//                		baos.write(current);
//                		current = 0;
//                		bitCount = 0;
//                }
            }
        }
        bitEncoder.finishWriting();
//        if (bitCount > 0) {
//        		current = current << 8 - bitCount;
//        		baos.write(current);
//        }
        System.out.println("Bytes in compressed message: " + bitEncoder.getEncoded().size());
        return bitEncoder.getEncoded();
    }
    
    // compress bytes from standard input and write to standard output
    public static void compress() {
        // read the input
        String s = BinaryStdIn.readString();
        char[] input = s.toCharArray();

        // tabulate frequency counts
        int[] freq = new int[R];
        for (int i = 0; i < input.length; i++)
            freq[input[i]]++;

        // build Huffman trie
        Node root = buildTrie(freq);

        // build code table
        String[] st = new String[R];
        buildCode(st, root, "");

        // print trie for decoder
        writeTrie(root);

        // print number of bytes in original uncompressed message
        BinaryStdOut.write(input.length);

        // use Huffman code to encode input
        for (int i = 0; i < input.length; i++) {
            String code = st[input[i]];
            for (int j = 0; j < code.length(); j++) {
                if (code.charAt(j) == '0') {
                    BinaryStdOut.write(false);
                }
                else if (code.charAt(j) == '1') {
                    BinaryStdOut.write(true);
                }
                else throw new IllegalStateException("Illegal state");
            }
        }

        // close output stream
        BinaryStdOut.close();
    }

    // build the Huffman trie given frequencies
    private static Node buildTrie(int[] freq) {

        // initialze priority queue with singleton trees
        MinPQ<Node> pq = new MinPQ<Node>();
        for (char i = 0; i < R; i++)
            if (freq[i] > 0)
                pq.insert(new Node(i, freq[i], null, null));

        // merge two smallest trees
        while (pq.size() > 1) {
            Node left  = pq.delMin();
            Node right = pq.delMin();
            Node parent = new Node('\0', left.freq + right.freq, left, right);
            pq.insert(parent);
        }
        
        return pq.delMin();
    }
    
    // build the Huffman trie given frequencies
    private Node buildTree(int[] freq) {

        // initialze priority queue with singleton trees
        MinPQ<Node> pq = new MinPQ<Node>();
        for (char i = 0; i < R; i++)
            if (freq[i] > 0)
                pq.insert(new Node(i, freq[i], null, null));

        // merge two smallest trees
        while (pq.size() > 1) {
            Node left  = pq.delMin();
            Node right = pq.delMin();
            Node parent = new Node('\0', left.freq + right.freq, left, right);
            pq.insert(parent);
        }
        
        this.root = pq.min();
        return pq.delMin();
    }


    // write bitstring-encoded trie to standard output
    private static void writeTrie(Node x) {
        if (x.isLeaf()) {
            BinaryStdOut.write(true);
            BinaryStdOut.write(x.ch, 8);
            return;
        }
        BinaryStdOut.write(false);
        writeTrie(x.left);
        writeTrie(x.right);
    }
    
    public ByteArrayOutputStream transformTreeToByteArray() {
    		BitEncoder bitEncoder = new BitEncoder();
    		writeTree(root, bitEncoder);
    		bitEncoder.finishWriting();
    		return bitEncoder.baos;
    }
   
    private static void writeTree(Node x, BitEncoder bitEncoder) {
    		if (x.isLeaf()) {
    			bitEncoder.appendBit(true);
    			int charByte = x.ch;
    			for (int i = 7; i >= 0; i--) {
    				if ((charByte & (1 << i)) != 0) {
    					bitEncoder.appendBit(true);
    				} else {
    					bitEncoder.appendBit(false);
    				}
    			}
    			return;
    		}
    		bitEncoder.appendBit(false);
    		writeTree(x.left, bitEncoder);
    		writeTree(x.right, bitEncoder);
    }
    
    public void readTree(byte[] hfTree) {
    		BitDecoder bitString = new BitDecoder(hfTree);
    		this.root = readTree(bitString);
    }
    
    private static Node readTree(BitDecoder bitString) {
    		if (bitString.hasNext()) {
    			boolean isLeaf = bitString.readNext();
    			if (isLeaf) {
    				int charByte = 0;
        			for (int i = 7; i >= 0; i--) {
        				if (bitString.readNext())
        					charByte += 1 << i;
        			}
        			return new Node((char) charByte, -1, null, null);
    			} else {
    				return new Node('\0', -1, readTree(bitString), readTree(bitString));
    			}
    		}
    		System.out.println("Returned null");
    		return null;
    }

    // make a lookup table from symbols and their encodings
    private static void buildCode(String[] st, Node x, String s) {
        if (!x.isLeaf()) {
            buildCode(st, x.left,  s + '0');
            buildCode(st, x.right, s + '1');
        }
        else {
            st[x.ch] = s;
        }
    }

    
    public Integer[] expand(byte[] encoded) {
    		ArrayList<Integer> result = new ArrayList<>();
        int i = 0;
    		int bit = 7; 
    		Node x = root;
    		while (i < encoded.length) {
    			if ((encoded[i] & (1 << bit)) != 0) {
    				x = x.right;
    			} else {
    				x = x.left;
    			}
    			if (x.isLeaf()) {
    				result.add((int) x.ch);
    				x = root;
    			}
    			if (bit == 0) {
    				bit = 7;
    				i++;
    			} else {
    				bit--;
    			}
    		}
    		Integer[] res = new Integer[result.size()];
        return result.toArray(res);
    }

    // expand Huffman-encoded input from standard input and write to standard output
    public static void expand() {

        // read in Huffman trie from input stream
        Node root = readTrie(); 

        // number of bytes to write
        int length = BinaryStdIn.readInt();

        // decode using the Huffman trie
        for (int i = 0; i < length; i++) {
            Node x = root;
            while (!x.isLeaf()) {
                boolean bit = BinaryStdIn.readBoolean();
                if (bit) x = x.right;
                else     x = x.left;
            }
            BinaryStdOut.write(x.ch, 8);
        }
        BinaryStdOut.close();
    }


    private static Node readTrie() {
        boolean isLeaf = BinaryStdIn.readBoolean();
        if (isLeaf) {
            return new Node(BinaryStdIn.readChar(), -1, null, null);
        }
        else {
            return new Node('\0', -1, readTrie(), readTrie());
        }
    }


    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
