import java.util.*;

public class Element {
	protected List<int[]> element;
	
	//empty list represents zero
	public Element() {
		element = new ArrayList<int[]>();
	}
	
	//create from a string, e.g. "# # + # # + # # # #"
	public Element(String input) {
		element = Tools.parseSumFromString(input);
	}
	
	public Element(List<int[]> init) {
		element = init;
	}
	
	public Element(int[] input) {
		element = new ArrayList<int[]>(1);
		element.add(input);
	}
	
	//TODO: should eventually make most code use Elements instead of ad hoc int[] or List<int[]>
	//		this would make it easier to test if some int[] represents zero as well
	//      note that for different types (dual, J(n), etc) zero might mean something different
	public boolean isZero() {
		return (element.size() == 0);
	}
	
	public int length() {
		return element.size();
	}
	
	//gets the monomial (not really a monomial for steenrod...) at the specified index
	public int[] getMono(int index) {
		return element.get(index);
	}
	
	public void add(int[] mono) {
		element.add(mono);
	}
	
	public void add(List<int[]> monos) {
		element.addAll(monos);
	}
	
	public void add(Element elem) {
		for(int i = 0; i < elem.length(); i++)
			element.add(elem.getMono(i));
	}
	
	@SuppressWarnings("unchecked")
	public void reduceMod2() {
		element = (List<int[]>) Tools.reduceMod2(element);
	}
	
	@Override
	public String toString() {
		if (element == null)
			return null;
		if(element.size() == 0)
			return "zero";
		
		String output = "";
		for (int i = 0 ; i < element.size(); i++) {
			output += Arrays.toString(element.get(i)) + ((i != element.size() - 1) ? " + " : "");
		}
		return output;
	}
}