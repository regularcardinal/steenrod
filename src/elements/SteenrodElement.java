package elements;
import java.util.ArrayList;

import main.Steenrod;
import main.Tools;

//This class represents a sum of Steenrod squares, eg, Sq^2 Sq^1 + Sq^4
//Not necessarily homogeneous
public class SteenrodElement extends Element {
	
	public SteenrodElement() {
		super();
	}
	
	public SteenrodElement(int[] input) {
		super(input);
	}
	
	public SteenrodElement(String input) {
		if(input.equals("Q1"))
			input = "2 1 + 3";
		if(input.equals("Q2"))
			input = "7 + 6 1 + 5 2 + 4 2 1";
		element = Tools.parseSumFromString(input);
	}
	
	//assumes homogeneous
	public int degree() {
		if(element.size() == 0)
			return 0;
		
		int[] mono = element.get(0);
		int degree = 0;
		for(int i = 0; i < mono.length; i++) 
			degree += mono[i];
		return degree;
	}
	
	//apply adem relations
	//TODO: avoid strings all together. this should be self-contained. no need to convert to string, then write as basis
	//then cleanup (which converts string to list<int[]> then BACK to string). the only thing you need to check SOMEWHERE is whether
	//or not the string is zero i.e. the list has length 0.
	public void adem() {
		String reduced = Steenrod.cleanup(Steenrod.writeAsBasis(convertToString()));
		if(reduced.equals("zero")) {
			element = new ArrayList<int[]>(0);
			return;
		}
		
		element = Tools.parseSumFromString(reduced);
	}
	
	//like toString, except get rid of the brackets
	//what about 1 (if we allow non-homogeneous for example), 1 <-> "" right now
	public String convertToString() {
		if (element == null)
			return null;
		if(element.size() == 0)
			return "zero";
		
		String output = "";
		for (int i = 0 ; i < element.size(); i++) {
			int[] mono = element.get(i);
			String monoToString = "";
			
			for(int j = 0; j < mono.length; j++)
				monoToString += mono[j] + ((j != mono.length - 1) ? " " : "");
			
			output += monoToString + ((i != element.size() - 1) ? " + " : "");
		}
		return output;
	}
	
	public SteenrodElement multiply(SteenrodElement element2) {
		SteenrodElement product = new SteenrodElement();
		
		for(int i = 0; i < element.size(); i++) {
			for(int j = 0; j < element2.length(); j++) {
				product.add(Tools.concatenate(this.getMono(i), element2.getMono(j)));
			}
		}
		
		product.adem();
		//should already be reduced mod 2 (currently adem is based on steenrod.cleanup)
		return product;
	}
	
	//returns the ith monomial as a single term element
	public SteenrodElement get(int index) {
		return new SteenrodElement(element.get(index));
	}
	
	public String texFormat() {
		if (element == null)
			return null;
		if(element.size() == 0)
			return "zero";
		
		String output = "";
		for (int i = 0 ; i < element.size(); i++) {
			int[] mono = element.get(i);
			String monoToString = "";
			
			for(int j = 0; j < mono.length; j++)
				monoToString += "sq" + mono[j];
			
			output += monoToString + ((i != element.size() - 1) ? "+" : "");
		}
		return output;
	}
}
