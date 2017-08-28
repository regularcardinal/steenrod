import java.util.List;

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
	
	//assumes homogeneous
	public int degree() {
		if(element.size() == 0)
			return 0;
		
		int[] mono = element.get(0);
		int degree = 0;
		for(int i = 0; i < mono.length; i+=2) 
			degree += mono[i+1];
		return degree;
	}
}