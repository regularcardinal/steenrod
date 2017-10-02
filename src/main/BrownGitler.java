package main;
import elements.JElement;
import elements.SteenrodElement;

//TODO: These can all go in the JElement class. then square would have no arguments, squareK would only have an int argument, etc


public class BrownGitler {	
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
				
				//see Schwartz 2.4(?)
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
			//multiple generators
			else {
				output = square(new JElement(new int[]{mono[0], mono[1]}));
				for(int i = 2; i < mono.length; i+=2) {
					JElement square = square(new JElement(new int[]{mono[i], mono[i+1]}));
					output = output.multiply(square);
				}
			}
		}
		//j is a sum of monomials
		else {
			for(int i = 0; i < j.length(); i++)
				output.add(square(new JElement(j.getMono(i))));
			output.reduceMod2();
		}
		
		return output;
	}
	
	//returns the (left) action of Sq^k on j
	public static JElement squareK(JElement j, int k) {
		JElement output = new JElement();
		JElement totalSquare = square(j);
		int[] degrees = j.degrees();
		
		for(int i = 0; i < totalSquare.length(); i++) {
			//if the ith mono of totalSquare is the degree of something from j + k, then it is the square k of something from j
			if(Tools.contains(degrees, totalSquare.degree(i) - k))
				output.add(totalSquare.getMono(i));
		}
		
		return output;
	}
	
	//returns the action of a general steenrod element on j
	//TODO: could absorb squareK
	public static JElement action(SteenrodElement sq, JElement j) {
		JElement sqJ = new JElement();
		
		for(int i = 0; i < sq.length(); i++) {
			int[] mono = sq.getMono(i);
			
			JElement tempSquare = j;
			for(int k = mono.length-1; k >= 0; k--) {
				tempSquare = squareK(tempSquare, mono[k]);
			}
			sqJ.add(tempSquare);
		}
		
		sqJ.reduceMod2();
		return sqJ;
	}
	
	public static JElement action(String sq, String j) {
		return action(new SteenrodElement(sq), new JElement(j));
	}
	
	
	//JElement blah;
	//blah = blah.multiply(somethingElse);
	//this kills the blah reference for garbage collection
}
