package elements;
import java.util.*;

//this class represents lists of monomials, ie, sums of monomials
//CONVENTION: a list of size zero is 0; a list of size 1, with the array [] is 1
//maybe should be: null == 0, a list of size 0 is 1?

/* 
 * A monomial looks like [gen1, pow1, gen2, pow2, ...]
 * A better format might be [pow1, pow2, pow3, ...]
 * The advantage of the former is that xi_2^4 is [2, 4], whereas the latter would represent this as [0, 4, 0, 0, ...]
 * The latter needs a fixed length, which is the number of generators, but in practice, this isn't that bad (how many do you need?)
*/
public class MilnorElement extends Element {
	
	public MilnorElement() {
		super();
	}
	
	public MilnorElement(List<?> input) {
		super(input);
	}
	
	//TODO: DELETE. use this to initiate zero, probably not needed
	public MilnorElement(int num) {
		if(num == 0) 
			element = new ArrayList<int[]>(0);
	}
	
	//TODO: DELETE. 
	public MilnorElement(int[] input) {
		super(input);
	}
	
	//assumes homogeneous
	public int degree() {
		if(element.size() == 0)
			return 0;
		
		int[] mono = element.get(0);
		int degree = 0;
		for(int i = 0; i < mono.length; i+=2) 
			degree += (Math.pow(2, mono[i]) - 1) * mono[i+1];
		return degree;
	}
	
	/*
	public void addMod2(List<int[]> toAdd) {
		for(int i = element.size() - 1; i >= 0; i--) {
			int[] mono = element.get(i);
			int countInToAdd = 0;
			
			for(int[] addMono : toAdd) {
				if(Arrays.equals(mono, addMono))
					countInToAdd++;
			}
			
			if((countInToAdd % 2) != 0)
				element.remove(i);
			//else 
				//element.add()
		}
	}*/
	
	//shouldn't need this, bad style to get the reference for element. most uses now are for multiplying, which should happen HERE.
	public List<int[]> getAsList() {
		return element;
	}
}
