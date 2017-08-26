public class JMonomial {
	private int[] mono;

	public JMonomial(int[] inputArray) {
		mono = inputArray;	
	}

	public int degree() {
		int degree = 0;
		for(int i = 0; i < mono.length; i+=2) 
			degree += mono[i+1];
		return degree;
	}
	
	public int length() {
		return mono.length/2;
	}
}



public class BrownGitler {
	public square(JMonomial mono, int k) {
		
	}

}
