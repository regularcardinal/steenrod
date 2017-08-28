public class BrownGitler {
	public JElement square(JElement mono, int k) {
		
		return null;
	}
	
	
	//returns total square
	public static JElement square(JElement j) {
		JElement output = new JElement();
		
		//j is a monomial
		if(j.length() == 1) {
			int[] mono = j.getMono(0);
			
			//one generator
			if(mono.length == 2) {
				int generator = mono[0];
				int power = mono[1];
				
				if(power == 1) {
					if(generator == 0) 
						output.add(mono);
					else {
						output.add(new int[]{generator, 1});
						output.add(new int[]{generator-1, 2});
					}
				}
				else {
					JElement square = square(new JElement(new int[]{generator, 1}));
					output = square;
					for(int i = 0; i < power - 1; i++) {
						output = output.multiply(square);
					}
				}
			}
		}
		
		
		
		return output;
	}
	
	public JElement action(SteenrodElement sq, JElement j) {
		JElement sqJ = new JElement();
		
		
		
		
		return sqJ;
	}
	
	
	//JElement blah;
	//blah = blah.multiply(somethingElse);
	//this kills the blah reference for garbage collection
}
