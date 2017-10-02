package elements;
import java.util.*;

//this class represents lists of monomials, ie, sums of monomials
//CONVENTION: a list of size zero is 0; a list of size 1, with the array [] is 1
//maybe should be: null == 0, a list of size 0 is 1?
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
		if(input == null)
			return;
		
		element = new ArrayList<int[]>(1);
		element.add(input);
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
