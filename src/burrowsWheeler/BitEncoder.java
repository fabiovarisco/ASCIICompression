package burrowsWheeler;

import java.io.ByteArrayOutputStream;

public class BitEncoder {

	ByteArrayOutputStream baos;
	private int bitCount;
	private int currentBit; 
	
	public BitEncoder() {
		this.bitCount = 0;
		this.currentBit = 0;
		this.baos = new ByteArrayOutputStream();
	}
	
	public void appendBit(boolean bit) {
		currentBit = currentBit << 1;
		if (bit) {
			currentBit += 1;
		} else {
    			currentBit += 0;
		}
		bitCount++;
		
		if (bitCount == 8) {
    			baos.write(currentBit);
    			currentBit = 0;
    			bitCount = 0;
		}
	}
	
	public void finishWriting() {
		if (bitCount > 0) {
    			currentBit = currentBit << 8 - bitCount;
    			baos.write(currentBit);
		}
	}
	
	public ByteArrayOutputStream getEncoded() {
		return this.baos;
	}
	
}
