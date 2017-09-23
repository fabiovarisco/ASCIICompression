package burrowsWheeler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import edu.princeton.cs.algs4.MinPQ;

public class HuffmanDoubleDigit {
	
	// alphabet size of extended ASCII
    private static final int R = 256;
    
    private Node root; 

    // Huffman trie node
    private static class Node implements Comparable<Node> {
        private final char ch1;
        private final char ch2;
        private final int freq;
        private final Node left, right;

        Node(char ch1, char ch2, int freq, Node left, Node right) {
            this.ch1 = ch1;
            this.ch2 = ch2;
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
    
    // build the Huffman trie given frequencies
    private Node buildTree(HashMap<Integer, Integer> freq) {

        // initialze priority queue with singleton trees
        MinPQ<Node> pq = new MinPQ<Node>();
        int f = 0;
        for (Integer i : freq.keySet()) {
        		f = freq.get(i);
        		if (f > 0) { 
        			char ch1 = (char) (i >> 8);
        			char ch2 = (char) (i & 0xFF);
        			System.out.println("Building tree: " + (int) ch1 + " " + (int) ch2);
        			pq.insert(new Node(ch1, ch2, f, null, null));
        		}
        }

        // merge two smallest trees
        while (pq.size() > 1) {
            Node left  = pq.delMin();
            Node right = pq.delMin();
            Node parent = new Node('\0', '\0', left.freq + right.freq, left, right);
            pq.insert(parent);
        }
        
        this.root = pq.min();
        return pq.delMin();
    }
    
    // make a lookup table from symbols and their encodings
    private static void buildCode(HashMap<Integer, String> codeTable, Node x, String s) {
        if (!x.isLeaf()) {
            buildCode(codeTable, x.left,  s + '0');
            buildCode(codeTable, x.right, s + '1');
        }
        else {
        		codeTable.put(charsToInteger(x.ch1, x.ch2), s);
        }
    }
    
    private static int charsToInteger(char ch1, char ch2) { 
    		return (ch1 << 8) + ch2;
    }
    
    public ByteArrayOutputStream compress(Integer[] input) {
        // tabulate frequency counts
    		HashMap<Integer, Integer> freq = new HashMap<>();

        for (int i = 0; i < input.length - 1; i = i + 2) {
        		//int key = input[i] << 8 | input[i + 1];
        		freq.merge(charsToInteger((char) (int) input[i], (char) (int) input[i + 1]), 1, (k, v) -> (v != null) ? v++ : 1);
        }

        // build Huffman trie
        Node root = this.buildTree(freq);

        // build code table
        HashMap<Integer, String> codeTable = new HashMap<>();
        //String[] st = new String[R];
        buildCode(codeTable, root, "");

        // print trie for decoder
        //writeTrie(root);

        // print number of bytes in original uncompressed message
        System.out.println("Bytes in uncompressed message: " + input.length);

        // use Huffman code to encode input
        BitEncoder bitEncoder = new BitEncoder();
        for (int i = 0; i < input.length - 1; i = i + 2) {
        		String code = codeTable.get(charsToInteger((char)(int) input[i], (char)(int) input[i + 1]));
            //String code = st[input[i]];
            for (int j = 0; j < code.length(); j++) {
            		if (code.charAt(j) == '0') {
            			bitEncoder.appendBit(false);
            		 } else if (code.charAt(j) == '1') {
            			 bitEncoder.appendBit(true);
            		 }
            		 else throw new IllegalStateException("Illegal state");
            }
        }
        bitEncoder.finishWriting();
        System.out.println("Bytes in compressed message: " + bitEncoder.getEncoded().size());
        return bitEncoder.getEncoded();
    }
    
    public Integer[] expand(byte[] encoded) {
		ArrayList<Integer> result = new ArrayList<>();
		
		BitDecoder bitDecoder = new BitDecoder(encoded);
		Node x = root;
		while (bitDecoder.hasNext()) {
			if (bitDecoder.readNext()) {
				x = x.right;
			} else { 
				x = x.left;
			}
			if (x.isLeaf()) {
				result.add((int) x.ch1);
				result.add((int) x.ch2);
				x = root;
			}
		}
		
		Integer[] res = new Integer[result.size()];
		return result.toArray(res);
}
    

}
