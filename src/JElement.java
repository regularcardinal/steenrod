public class JElement extends Element{
	
	public JElement() {
		super();
	}
	
	public static JElement multiply(JElement elem2) {
		JElement product = new JElement();
		
		
		
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