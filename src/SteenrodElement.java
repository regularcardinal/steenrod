import java.util.ArrayList;

public class SteenrodElement extends Element {
	
	public SteenrodElement(String input) {
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
	public void adem() {
		String reduced = Steenrod.cleanup(Steenrod.writeAsBasis(convertToString()));
		if(reduced.equals("zero")) {
			element = new ArrayList<int[]>(0);
			return;
		}
		
		element = Tools.parseSumFromString(reduced);
	}
	
	//like toString, except get rid of the brackets
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
	
}
