import java.util.*;

public class Jm {
	int M;
	//(dim, generators at that dim)
	Map<Integer, List<JElement>> module;
	
	public Jm(int m) {
		M = m;
		module = new HashMap<Integer, List<JElement>>();
		generateModule();
	}
	
	private void generateModule() {
		for(int deg = 0; deg <= M; deg++) {
			List<List<Integer>> partitions = Tools.partition(M, deg, true);			
			
			for(int i = 0; i < partitions.size(); i++) { 
				List<Integer> currentPartition = partitions.get(i);
				List<Integer> monomial = new ArrayList<Integer>();
				
				
				int currentPower = currentPartition.get(0);
				int currentCount = 0;
				
				//turn the current partition into the form of a JElement (a monomial of Jm)
				for(int j = 0; j < currentPartition.size(); j++) {
					if(currentPartition.get(j) != currentPower) {
						monomial.add(currentPower);
						monomial.add(currentCount);
						currentPower = currentPartition.get(j);
						currentCount = 0;
					}
					//if there was no change, but we made it to the last power
					if(j == currentPartition.size() - 1) {
						monomial.add(currentPower);
						monomial.add(currentCount + 1);
					}
					else 
						currentCount++;
				}
				
				if(module.get(deg) == null) 
					module.put(deg, new ArrayList<JElement>());
				
				module.get(deg).add(new JElement(monomial));
			}
		}
	}
	
	//prints all the contents of the j module
	public void print() {
		List<String> output = printAsList();
		for(String line : output) {
			System.out.println(line);
		}
	}
	
	
	public List<String> printAsTex() {
		List<String> output = new ArrayList<String>();
		
		output.add("J(" + M + "):\\\\");
		for(int deg = 0; deg <= M; deg++) {
			List<JElement> generators = module.get(deg);
			output.add("Degree " + deg + "; dimension over F2: " + (generators == null ? 0 + "\n" : generators.size()) + "\\\\");
			
			if(generators == null)
				continue;
			
			for(int l = 0; l < generators.size(); l++) {
				String jAsTex = Tools.convertJElementToTex(generators.get(l));
				jAsTex += (l == generators.size() - 1 ? "" : ", ");
				output.add(jAsTex);
			}
			
			output.add("\\\\");
		}
		
		output.add("");
		return output;
	}
	
	public List<String> printActionAsTex(SteenrodElement sq) {
		List<String> output = new ArrayList<String>();
		
		output.add(sq + " acting on J(" + M + "):\\\\");
		Set<Integer> keys = module.keySet();
		
		for(Integer deg: keys) {
			output.add("Degree: " + deg + "\\\\");
			List<JElement> jElements = module.get(deg);
			for(int i = 0; i < jElements.size(); i++) {
				JElement j = jElements.get(i);				
				output.add(Tools.convertJElementToTex(j) + " $\\rightarrow$ " + Tools.convertJElementToTex(BrownGitler.action(sq, j)) + "\\\\");
			}
		}
		
		output.add("");
		return output;
	}
	
	//TODO: put this back into print()
	public List<String> printAsList() {
		List<String> output = new ArrayList<String>();
		
		output.add("J(" + M + "):");
		for(int deg = 0; deg <= M; deg++) {
			List<JElement> monomials = module.get(deg);
			output.add("Degree " + deg + "; dimension over F2: " + (monomials == null ? 0 + "\n" : monomials.size()));
			
			if(monomials == null)
				continue;
			
			for(int i = 0; i < monomials.size(); i++) {
				output.add(monomials.get(i).toString());
			}
		}
		
		output.add("");
		return output;
	}
	
	//this conveniently prints in order by degree, because keys iterates via hashcode and the hashcode of an Integer is its intValue
	public void printAction(SteenrodElement sq) {
		List<String> output = printActionAsList(sq);
		for(String line : output) {
			System.out.println(line);
		}
	}
	
	public List<String> printActionAsList(SteenrodElement sq) {
		List<String> output = new ArrayList<String>();
		
		output.add(sq + " acting on J(" + M + "):");
		Set<Integer> keys = module.keySet();
		
		for(Integer deg: keys) {
			output.add("Degree: " + deg);
			List<JElement> jElements = module.get(deg);
			for(JElement j : jElements) {
				output.add(j.toString() + " -> " + BrownGitler.action(sq, j).toString());
			}
		}
		
		output.add("");
		return output;
	}
}
