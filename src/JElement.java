import java.util.List;

//A JElement represents an element on the Brown Gitler modules J(m). It need not be homogeneous (to easily track total squares).
public class JElement extends Element{
	
	public JElement() {
		super();
	}
	
	public JElement(String input) {
		super(input);
	}
	
	public JElement(List<int[]> init) {
		super(init);
	}
	
	public JElement(int[] input) {
		super(input);
	}
	
	public JElement multiply(JElement element2) {
		JElement product = new JElement();
		
		for(int i = 0; i < element.size(); i++) {
			for(int j = 0; j < element2.length(); j++) {
				//product.add(DualSteenrod.milnorMultiply());
				product.add(DualSteenrod.applyRelations(Tools.concatenate(this.getMono(i), element2.getMono(j)), null));
			}
		}
		
		product.reduceMod2();
		return product;
	}
	
	//gets the degree of the monomial at the specified index
	public int degree(int index) {
		if(element.size() == 0)
			return 0;
		
		int[] mono = element.get(index);
		int degree = 0;
		for(int i = 0; i < mono.length; i+=2) 
			degree += mono[i+1];
		return degree;
	}
	
	//returns a list of all the (not necessarily homogeneous) degrees
	public int[] degrees() {
		int[] degreeArray = new int[this.length()];
		for(int i = 0; i < this.length(); i++)
			degreeArray[i] = this.degree(i);
		return degreeArray;
	}
	
	public int minDegree() {
		int min = this.degree(0);
		for(int i = 0; i < this.length(); i++)
			if(this.degree(i) < min)
				min = this.degree(i);
			
		return min;
	}
}