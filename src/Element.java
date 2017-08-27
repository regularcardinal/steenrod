import java.util.*;

public class Element {
	private List<int[]> element;
	
	public Element() {
		element = new ArrayList<int[]>();
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
	
	public int[] getMono(int index) {
		return element.get(index);
	}
	
	public void add(int[] mono) {
		element.add(mono);
	}
	
	public void add(List<int[]> monos) {
		element.addAll(monos);
	}
	
	@SuppressWarnings("unchecked")
	public void reduceMod2() {
		element = (List<int[]>) Tools.reduceMod2(element);
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
	
}
