import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//this class represents lists of monomials, ie, sums of monomials
//CONVENTION: a list of size zero is 0; a list of size 1, with the array [] is 1
public class MilnorElement {
	private List<int[]> element;
	
	public MilnorElement() {
		element = new ArrayList<int[]>();
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
	
	public void add(int[] mono) {
		element.add(mono);
	}
	
	public void add(List<int[]> monos) {
		element.addAll(monos);
	}
	
	public boolean singleMonomial() {
		return (element.size() == 1 ? true : false);
	}
	
	public int length() {
		return element.size();
	}
	
	public List<int[]> getAsList() {
		return element;
	}
	
	public int[] getSingleMonomial() {
		return element.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public void reduceMod2() {
		element = (List<int[]>) DualSteenrod.reduceMod2(element);
	}
	
	@Override
	public String toString() {
		if (element == null)
			return null;
		
		String output = "";
		for (int i = 0 ; i < element.size(); i++) {
			output += Arrays.toString(element.get(i)) + ((i != element.size() - 1) ? " + " : "");
		}
		return output;
	}
	
	//TODO: should eventually make most code use MilnorElements instead of ad hoc int[] or List<int[]>
	//		this would make it easier to test if some int[] represents zero as well
	public boolean isZero() {
		return (element.size() == 0);
	}
}
