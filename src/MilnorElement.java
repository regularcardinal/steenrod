import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//this class represents lists of monomials, ie, sums of monomials
//CONVENTION: a list of size zero is 0; a list of size 1, with the array [] is 1
public class MilnorElement extends Element {
	private List<int[]> element;
	
	public MilnorElement() {
		super();
	}
	
	//use this to initiate zero
	public MilnorElement(int num) {
		if(num == 0) 
			element = new ArrayList<int[]>(0);
	}
	
	public MilnorElement(int[] input) {
		if(input == null)
			return;
		
		element = new ArrayList<int[]>(1);
		element.add(input);
	}
	
	@SuppressWarnings("unchecked")
	public MilnorElement(List<?> input) {
		if(input == null)
			return;
		if(input.size() == 0) {
			element = new ArrayList<int[]>(0);
			return;
		}
			
		
		//if this is just a single monomial in the form of List<Integer>
		if(input.get(0) instanceof Integer) {
			element = new ArrayList<int[]>(1);
			element.add(Tools.listToIntArray((List<Integer>) input));
		}
		//if this is already a list of monomials
		else if(input.get(0) instanceof int[])
			element = (List<int[]>) input;
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
	
	@SuppressWarnings("unchecked")
	public void reduceMod2() {
		element = (List<int[]>) DualSteenrod.reduceMod2(element);
	}
}
