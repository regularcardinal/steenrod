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
	
	public void print() {
		System.out.println("J(" + M + "):");
		for(int deg = 0; deg <= M; deg++) {
			List<JElement> monomials = module.get(deg);
			System.out.println("Degree " + deg + "; dimension over F2: " + (monomials == null ? 0 + "\n" : monomials.size()));
			
			if(monomials == null)
				continue;
			
			for(int i = 0; i < monomials.size(); i++) {
				System.out.println(monomials.get(i));
			}
		}
	}
	
	//this conveniently prints in order by degree, because keys iterates via hashcode and the hashcode of an Integer is its intValue
	public void printAction(SteenrodElement sq) {
		System.out.println(sq + " acting on J(" + M + "):");
		Set<Integer> keys = module.keySet();
		
		for(Integer deg: keys) {
			System.out.println("Degree: " + deg);
			List<JElement> jElements = module.get(deg);
			for(JElement j : jElements) {
				System.out.println(j + " -> " + BrownGitler.action(sq, j));
			}
		}
		
		System.out.println("");
	}
	
	
}
