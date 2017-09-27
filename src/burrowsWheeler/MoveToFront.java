package burrowsWheeler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

/*************************************************************************
 *  Fifth week coursera course Java Algorithms part 2
 *  Burrows-Wheeler data compression
 *  @Date 18.03.14
 *  @Author Khokhlushin Mikhail
 *  
 *  Links:
 *  Specification: http://coursera.cs.princeton.edu/algs4/assignments/burrows.html
 *             http://coursera.cs.princeton.edu/algs4/checklists/burrows.html
 *             http://algs4.cs.princeton.edu/windows/
 *  Compilation:   javac-algs4 MoveToFront.java
 *  Execution:     java-algs4 MoveToFront <->/<+> < <name_of_the_data_file>
 *                   (e.g., java-algs4 MoveToFront - < ../test_files/abra.txt)
 *                 To watch Hex output use HexDump (from algs4) it must be compiled
 *                   (e.g., javac-algs4 HexDump.java;
 *                      java-algs4 MoveToFront - < ../test_files/abra.txt | 
 *                                  java-algs4 HexDump 16)
 *  
 *  Move to front encoding. More details 
 *    http://coursera.cs.princeton.edu/algs4/assignments/burrows.html
 *                     
 *  Execution only with installed algs4.jar and stdlib.jar
 *  
 *************************************************************************/

public class MoveToFront {

   private static final int CHAR_BITS = 8;
   
   private static final int ALPHABET_SIZE = 256;
   
   /**
    *  Apply move-to-front encoding, reading from standard input and writing to standard output
    */
   public static void encode() {
      List<Character> moveToFront = createANSIList();
      while (!BinaryStdIn.isEmpty()) {
         char curChar = BinaryStdIn.readChar();
         int alphabetPosition = 0;
         Iterator<Character> moveToFrontIterator = moveToFront.iterator();
         while (moveToFrontIterator.hasNext()) {
            if (moveToFrontIterator.next().equals(Character.valueOf(curChar))) {
               BinaryStdOut.write(alphabetPosition, CHAR_BITS);
               char toFront = moveToFront.get(alphabetPosition);
               moveToFront.remove(alphabetPosition);
               moveToFront.add(0, toFront);
               break;
            }
            alphabetPosition++;
         }
      }
      BinaryStdOut.close();
   }
   
   public static Integer[] encode(String input) {
	   List<Character> moveToFront = createANSIList();
	   Integer[] result = new Integer[input.length()];
	   for (int i = 0; i < input.length(); i++) {
		   char curChar = input.charAt(i);
		   int alphabetPosition = 0;
	        Iterator<Character> moveToFrontIterator = moveToFront.iterator();
	        while (moveToFrontIterator.hasNext()) {
	            if (moveToFrontIterator.next().equals(Character.valueOf(curChar))) {
	            		result[i] = alphabetPosition;
	               char toFront = moveToFront.get(alphabetPosition);
	               moveToFront.remove(alphabetPosition);
	               moveToFront.add(0, toFront);
	               break;
	            }
	            alphabetPosition++;
	         }
	   }
	   return result;
   }
   
   /**
    *  Apply move-to-front decoding, reading from standard input and writing to standard output
    */
   public static void decode() {
      List<Character> moveToFront = createANSIList();
      while (!BinaryStdIn.isEmpty()) {
         int curCharPosition = BinaryStdIn.readChar();
         BinaryStdOut.write(moveToFront.get(curCharPosition), CHAR_BITS);
         char toFront = moveToFront.get(curCharPosition);
         moveToFront.remove(curCharPosition);
         moveToFront.add(0, toFront);
      }
      BinaryStdOut.close();
   }
   
   public static String decode(Integer[] encoded) {
	   List<Character> moveToFront = createANSIList();
	   StringBuilder sb = new StringBuilder();
	   for (int i : encoded) {
		   sb.append(moveToFront.get(i));
		   char toFront = moveToFront.get(i);
	       moveToFront.remove(i);
	       moveToFront.add(0, toFront);
	   }
	   return sb.toString();
   }
   
   private static List<Character> createANSIList() {
      List<Character> ansiList = new LinkedList<Character>();
      for (int alphabetPosition = 0; alphabetPosition < ALPHABET_SIZE ; alphabetPosition++) 
         ansiList.add((char) alphabetPosition);
      return ansiList;
   }

   /**
    * Call encode or decode of Move to front transform
    * @param args
    *    args[0] = '-' apply Move to front encoding or   
    *    '+' apply Move to front decoding; 
    *    args[1] File to transform 
    */
   public static void main(String[] args) {
      if (args.length == 0) 
         throw new java.lang.IllegalArgumentException("Usage: input '+' for encoding or '-' for decoding");
      if (args[0].equals("-")) 
         encode();
      else if (args[0].equals("+")) 
         decode();
      else 
         throw new java.lang.IllegalArgumentException("Usage: input '+' for encoding or '-' for decoding");
   }
}
