package burrowsWheeler;

public class BitDecoder {
	
	private byte[] encoded;
	int i = 0;
	int bit = 7;
	
	public BitDecoder(byte[] encoded) {
		this.encoded = encoded;
	}
	
	public boolean readNext() {
		if (i >= encoded.length) 
			return false;
		boolean res;
		if ((encoded[i] & (1 << bit)) != 0) {
			res = true;
		} else {
			res = false;
		}
		
		if (bit == 0) {
			bit = 7;
			i++;
		} else {
			bit--;
		}
		
		return res;
	}
	
	public boolean hasNext() {
		if (i < encoded.length)
			return true;
		else 
			return false;
	}

}
