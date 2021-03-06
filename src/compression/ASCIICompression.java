package compression;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

import burrowsWheeler.BurrowsWheeler;
import burrowsWheeler.Huffman;
import burrowsWheeler.HuffmanDoubleDigit;
import burrowsWheeler.MoveToFront;
import lzw.LZString;

public class ASCIICompression {

	private static String DECODED_INPUT = "ABRACADABRA!";
	
	public static final byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte) (value >>> 24),
	            (byte) (value >>> 16),
	            (byte) (value >>> 8),
	            (byte) value };
	}
	
	public static final int byteArrayToInt (byte[] byteArray) {
		int res = 0;
		for (int i = 3; i >= 0; i--) {
			int aux = ((byteArray[3 - i] & 0xFF) << i * 8);
			res = res | aux;
		}
		return res;
	}
	
	public static ByteArrayOutputStream encode(String input) throws IOException {
		BurrowsWheeler bwt = new BurrowsWheeler();
		bwt.encode(input);
		System.out.println(bwt.getFirstChar());
		Integer[] mvtResult = MoveToFront.encode(bwt.getEncoded());
		
		Huffman hf = new Huffman();
		
		ByteArrayOutputStream compressedData = hf.compress(mvtResult);
		
		ByteArrayOutputStream hfTree = hf.transformTreeToByteArray();
		
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		
		result.write(intToByteArray(bwt.getFirstChar()));
		
		result.write(intToByteArray(hfTree.size()));
		
		result.write(hfTree.toByteArray());
		
		result.write(compressedData.toByteArray());
		
		return result;
	}
	
	public static String decode(ByteArrayInputStream input) throws IOException {
		byte[] in1 = new byte[4];
		input.read(in1);
		
		int bwtFirstChar = byteArrayToInt(in1);
		
		in1 = new byte[4];
		input.read(in1);
		
		int treeSize = byteArrayToInt(in1);
		
		byte[] hfTree = new byte[treeSize];
		input.read(hfTree);
		
		byte[] data = new byte[input.available()];
		input.read(data);
		Huffman hf = new Huffman();
		hf.readTree(hfTree);
		Integer[] hfDecoded = hf.expand(data);
		
		String mvtDecoded = MoveToFront.decode(hfDecoded);
		
		String decoded = BurrowsWheeler.decode(mvtDecoded, bwtFirstChar);
		
		return decoded;
	}
	
	public static void test(String input, boolean verbose) {
		BurrowsWheeler bwt = new BurrowsWheeler();
		bwt.encode(input);
		if (verbose)
			System.out.println(String.format("Burrows-Wheeler Re-org: \nFirst char: %d, Encoded string: %s",
					bwt.getFirstChar(), bwt.getEncoded()));

		Integer[] mvtResult = MoveToFront.encode(bwt.getEncoded());

		if (verbose) {
			StringBuilder sb = new StringBuilder();
			Arrays.stream(mvtResult).forEach(x -> sb.append(x + " "));
			System.out.println("\nMVT Result: " + sb.toString() + "\n");
		}
		
		Huffman hf = new Huffman();
		ByteArrayOutputStream baos = hf.compress(mvtResult);
		if (verbose) { 
			StringBuilder hfComp = new StringBuilder();
			for (byte b : baos.toByteArray()) {
				hfComp.append(b + " ");
			}
			System.out.println("Huffman encoded: " + hfComp.toString() + "");
		}

		Integer[] hfDecoded = hf.expand(baos.toByteArray());
		if (verbose) {
			StringBuilder sb2 = new StringBuilder();
			Arrays.stream(hfDecoded).forEach(x -> sb2.append(x + " "));
			System.out.println("\nHuffman decoded: " + sb2.toString());
		}
		
		String decoded = MoveToFront.decode(hfDecoded);
		if (verbose)
			System.out.println("\nMVT Decoded: " + decoded);
		String result = BurrowsWheeler.decode(decoded, bwt.getFirstChar());
		if (verbose)
			System.out.println("\nBurrows-Wheeler Reverse Org: " + result);
	}
	
	public static void testWithFile(String filename) throws IOException {
		String text = new String(Files.readAllBytes(Paths.get(filename)));
		test(text.toString(), false);
	}
	
	public static ByteArrayOutputStream encodeFile(String filename) throws IOException {
		String text = new String(Files.readAllBytes(Paths.get(filename)));
		ByteArrayOutputStream baos = encode(text);
		return baos;
	}
	
	public static void encodeFile(String inputFileName, String outputFileName) throws IOException {
		ByteArrayOutputStream baos = encodeFile(inputFileName);
		writeToFile(outputFileName, baos);
	}
	
	public static void decodeFromFile(String inputFile, String outputFile) throws IOException {
		ByteArrayInputStream bais = readBytesFromFile(inputFile);
		String decoded = decode(bais);
		try (PrintStream out = new PrintStream(new FileOutputStream(outputFile))) {
		    out.print(decoded);
		    out.close();
		} 
	}
	
	public static boolean writeToFile(String filename, ByteArrayOutputStream baos) {
		try {
			OutputStream outputStream = new FileOutputStream(filename);
			baos.writeTo(outputStream);
			outputStream.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static ByteArrayInputStream readBytesFromFile (String filename) throws IOException {
		return new ByteArrayInputStream(Files.readAllBytes(new File(filename).toPath()));
	}

	public static void main(String[] args) throws Exception {
		String filename = ".//files//alice29.txt";
		String output = ".//files//alice29.fgzip";
		String decodedFile = ".//files//alice29-decoded.txt";
		
//		//ByteArrayOutputStream baos = encode(DECODED_INPUT);
//		ByteArrayOutputStream baos = encodeFile(filename);
//		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//		String decoded = decode(bais);
//		System.out.println(decoded);
		encodeFile(filename, output);
		decodeFromFile(output, decodedFile);
	}
	
	public static void testLZString(String input) throws Exception  { 
		String compressed = LZString.compressToBase64(input);
		System.out.println(String.format("Compressed (%d bytes): %s", compressed.length(), compressed));
		
		String decompressed = LZString.decompressFromBase64(compressed);
		System.out.println(String.format("Decompressed (%d bytes), %s", decompressed.length(), decompressed));
	}

	public static void encodeLZStringFile(String filename, String outputFile) throws Exception { 
		String text = new String(Files.readAllBytes(Paths.get(filename)));
		String compressed = LZString.compress(text);
		System.out.println(String.format("Compressed (%d length of string)", compressed.length()));
		
		try (PrintStream out = new PrintStream(new FileOutputStream(outputFile))) {
		    out.print(compressed);
		    out.close();
		} 
		
		String decompressed = LZString.decompress(compressed);
		System.out.println(String.format("Decompressed (%d bytes)", decompressed.length()));
	}
	
	

}
