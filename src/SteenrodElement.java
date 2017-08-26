import java.util.*;

public class SteenrodElement {
	List<int[]> element = new ArrayList<int[]>();
	
	public SteenrodElement(String input) {
		element = Tools.parseSumFromString(input);
	}
	
	public int length() {
		return element.size();
	}
	
	public int[] getMono(int index) {
		return element.get(index);
	}
	
	//what if element is a list consisting of the empty string?
	public boolean isZero() {
		if(element.size() == 0) 
			return true;
		return false;
	}
}
