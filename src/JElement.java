import java.util.*;

public class JElement {
	List<JMonomial> sum;
	
	
	public JElement() {
		sum = new ArrayList<JMonomial>();
	}
	
	public static JElement multiply(JElement elem1, JElement elem2) {
		JElement product = new JElement();
		
		
		
		return product;
	}
	
	
	public boolean isZero() {
		if(sum.size() == 0)
			return true;
		return false;
	}
	
	public void reduceMod2() {
		
	}
}
