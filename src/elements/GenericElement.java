package elements;

import java.util.*;

//an element here is an element of a commutative algebra which can be represented in the normal [gen1, pow1, gen2, pow2, ...] format
//or as x2 y3 for eg x^2y^3
public class GenericElement extends Element {
	//translates between normal format and char format; index corresponds to generator
	List<String> vars = new ArrayList<String>();
	
	//INPUT: string in format x2 y3 + x1 z2 for x^2y^3 + xz^2
	//in this example: x->0, y->1, z->2
	//NOTE: must be reduced for each monomial, meaning no x2 y3 x2 for x4 y3
	//NOTE: variables can only be length 1!
	public GenericElement(String input) {
		element = new ArrayList<int[]>();
		
		String[] splitByPlus = input.split(" [+] ");
		for(String mono : splitByPlus) {
			String[] splitMono = mono.split(" ");
			//will translate the user monomial with variables into the "classic" monomial format
			int[] monoArray = new int[2*splitMono.length];
			
			for(int i = 0; i < splitMono.length; i++) {
				String elem = splitMono[i];
				String var = elem.substring(0, 1);
				if(!vars.contains(var))
					vars.add(var);
				
				monoArray[2*i] = vars.indexOf(var);
				
				//just a variable eg x, and no power
				if(elem.length() == 1)
					monoArray[2*i+1] = 1;
				else
					monoArray[2*i+1] = Integer.parseInt(elem.substring(1));
			}
			
			element.add(monoArray);
		}
		
		System.out.println(vars.toString());
	}
	
	public GenericElement() {
		super();
	}
	
	public void setVars(List<String> input) {
		vars = input;
	}
	
	public List<String> getVars() {
		return vars;
	}
	
	public String printWithVariables() {
		String output = "";
		for(int i = 0; i < element.size(); i++) {
			int[] mono = element.get(i);
			for(int j = 0; j < mono.length; j+=2) {
				output += vars.get(mono[j]);
				//don't print power 1
				if(mono[j+1] != 1)
					output += mono[j+1];
				output += (j == mono.length - 2 ? "" : " ");
			}
			output += (i == element.size() - 1 ? "" : " + "); 
		}
		
		return output;
	}
}
