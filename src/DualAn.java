import java.util.*;

public class DualAn implements Algebra {
	
	public int N;
	//a map is used rather than an array because we can detect not having a relation in dimension i if the ith key has null value
	private HashMap<Integer, Integer> relationMap = new HashMap<Integer, Integer>();
	private ArrayList<Integer> generatorList = new ArrayList<Integer>();
	private HashMap<Integer, List<int[]>> allMonomials = new HashMap<Integer, List<int[]>>();
	
	public DualAn(int n) {
		N = n;
		
		//index 0 corresponds to xi_0 = 1
		generatorList.add(-1);
		for(int i = 1; i <= (n+1); i++) {
			generatorList.add(1);;
		}
		
		//fill up the relation map: key i corresponds to xi_i, and the value is the power j such that xi_i^j = 0
		for(int i = 1; i <= (n+1); i++) {
			relationMap.put(i, (int)Math.pow(2, n+2-i));
		}
		
		//create initial array which will be every generator raised to the 0
		int[] initial = new int[2*(N+1)];
		for(int i = 0; i <= N; i++) {
			initial[2*i] = i+1;
			initial[2*i+1] = 0;
		}
		
		generateMonomials(initial, 0);
	}
	
	//fill up the allMonomials map with every milnor monomial in the algebra, organized by degree
	private void generateMonomials(int[] monomial, int shift) {
		int[] newMono = Arrays.copyOf(monomial, monomial.length);
		int dimension;

		for(int i = 0; i < relationMap.get(shift+1); i++) {			
			if(shift != N)
				generateMonomials(newMono, shift+1);
			else {
				dimension = DualSteenrod.milnorDimension(newMono);
				if(allMonomials.get(dimension) == null) {
					List<int[]> temp = new ArrayList<int[]>();
					temp.add(DualSteenrod.applyRelations(newMono, relationMap));
					allMonomials.put(dimension, temp);
				}
				else 
					allMonomials.get(dimension).add(DualSteenrod.applyRelations(newMono, relationMap));		
			}
			newMono[2*shift + 1]++;
		}
	}
	
	//INPUT: a max dimension
	//OUTPUT: a map of all monomials in dimensions <= max dimension. keys are dimension, values are lists of elements in those dimensions.
	public Map<Integer, List<int[]>> getMonomialsAtOrBelow(int maxDimension) {
		HashMap<Integer, List<int[]>> truncatedMonomials = new HashMap<Integer, List<int[]>>();
		
		for(int i = 0; i <= maxDimension; i++) {
			if(allMonomials.get(i) != null)
				truncatedMonomials.put(i, allMonomials.get(i));
		}
		
		return truncatedMonomials;
	}
	
	//INPUT: a list of dimensions (should all by >= 0)
	//OUTPUT: a map of monomials which exist only in the dimensions given
	public Map<Integer, List<int[]>> getMonomialsByFilter(Integer[] dimensions) {
		Map<Integer, List<int[]>> truncatedMonomials = new HashMap<Integer, List<int[]>>();

		for(int i = 0; i < dimensions.length; i++) {
			if((dimensions[i] < 0) || (allMonomials.get(dimensions[i]) == null))
				continue;
			truncatedMonomials.put(dimensions[i], allMonomials.get(dimensions[i]));
		}

		return truncatedMonomials;
	}
	
	//returns the top dimension class
	public int[] topClass() {
		int[] topclass = new int[2*(N+1)];
		for(int i = 0; i <= N; i++) {
			topclass[2*i] = i + 1;
			topclass[2*i+1] = (int)Math.pow(2, N+1-i) - 1;
		}
		return topclass;
	}
	
	
	@SuppressWarnings("unchecked")
	public Function generatejMap(Function sMap) {
		Function jMap = new Function();
		Map<List<Integer>, MilnorElement> map;
		Iterator<List<Integer>> iter;
		MilnorElement target;
		
		for(int dimension = 1; dimension <= sMap.topDimension(); dimension++) {
			map = sMap.getMapByDimension(dimension);
			
			System.out.print("dim: " + dimension);
			
			if(map == null)
				continue;
			
			iter = map.keySet().iterator();
			
			while(iter.hasNext()) {
				List<Integer> mono = iter.next();
				
				long start = System.nanoTime();
				System.out.print(" computing coproduct of " + mono + ": ");
				
				List<int[][]> coprod = DualSteenrod.coproduct(Tools.listToIntArray(mono));
				coprod = (List<int[][]>) DualSteenrod.reduceMod2(coprod);
				
				System.out.print(((double)(System.nanoTime()-start))/1000000 + " ms; "); 
				
				DualSteenrod.removePrimitives(coprod);
				
				//a + s(a)
				target = new MilnorElement(mono);
				target.add(map.get(mono).getAsList());
				
				//add the j(pi(a')) \cdot i(s_bar(a'')), summing over all a', a''
				for(int i = 0; i < coprod.size(); i++) {
					//applyRelations is applying the quotient map pi
					int[] mono1 = DualSteenrod.applyRelations(coprod.get(i)[0], this.getRelations());
					int[] mono2 = coprod.get(i)[1];
					//int dim1 = DualSteenrod.milnorDimension(mono1);
					//int dim2 = DualSteenrod.milnorDimension(mono2);
					
					//represent mono2 as an elt in A(n)* tensor an elt in A//A(n)*
					//TODO: this can probably be done much more elegantly using MilnorElements.
					int[] mono2_1 = DualSteenrod.applyRelations(mono2, getRelations()); //in A(n)*
					int[] mono2_2 = DualSteenrod.remainder(mono2, getRelations()); //in A//A(n)*
					List<int[]> sMono2_1 = (List<int[]>) DualSteenrod.reduceMod2(sMap.get(mono2_1).getAsList());
					
					if(sMono2_1.size() == 0) {
						//System.out.print("mono: " + mono + "; s map is zero for " + Arrays.toString(mono2_2) + "; ");
						continue;
					}
					
					
					List<int[]> mono2_2AsList = new ArrayList<int[]>(1);
					mono2_2AsList.add(mono2_2);
					List<int[]> multiplied = (List<int[]>) DualSteenrod.reduceMod2(DualSteenrod.multiplySums(sMono2_1, mono2_2AsList));
					
					target.add(DualSteenrod.multiplySums(jMap.get(mono1).getAsList(),  multiplied    ));
					System.out.println("nonzero s map: " + target);
				}
				
				target.reduceMod2();
				jMap.set(Tools.listToIntArray(mono), target);
				
			}
			System.out.println("");
		}
		
		
		return jMap;
	}
	
	public Function generatesMap() {
		Function sMap = new Function();
		
		//initialize all targets to zero
		for(Integer index : allMonomials.keySet()) {
			List<int[]> monomialList = allMonomials.get(index);
			for(int[] monomial : monomialList) {
				sMap.set(monomial, new MilnorElement(new int[0]));
			}
		}
		
		DualSteenrod AmodAn = new DualSteenrod(null);
		AmodAn.setGenerators(DualSteenrod.getDualAModAnGenerators(N));
		
		int topClassDim = DualSteenrod.milnorDimension(topClass());
		Integer[] AmodAnKeys = Tools.keysToSortedArray(AmodAn.getMonomialsAtOrBelow(topClassDim));
		Map<Integer, List<int[]>> filteredMonomials = getMonomialsByFilter(AmodAnKeys);
		Integer[] dualAnKeys = Tools.keysToSortedArray(filteredMonomials);
		
		//this is the only place where the targets of the s map can be NONZERO
		for(int i = 0; i < dualAnKeys.length; i++) {
			List<int[]> monomialList = filteredMonomials.get(dualAnKeys[i]);
			for(int[] monomial : monomialList) {
				sMap.set(monomial, new MilnorElement(new int[0]));
			}
		}
		
		return sMap;
	}
	
	
	public int getDimension() {
		return (int)Math.pow(2, Tools.choose(N+2, 2).intValue());
	}
	
	//print monomials (sorted by dimension) from the given map (this is really just a "print map" function)
	//LOGIC: first get the key set of Integers and sort it. then run through the Integer array, and for each map entry, print every monomial in the corresponding list
	public void printMonomials(Map<Integer, List<int[]>> monomials) {
		List<int[]> tempList;
		
		//first convert from a set of keys to a sorted Integer array of keys
		Integer[] sortedKeys = Tools.keysToSortedArray(monomials);
		
		//for each key, get the list
		for(int i = 0; i < sortedKeys.length; i++) {
			tempList = monomials.get(sortedKeys[i]);
			System.out.println("dim = " + sortedKeys[i] + ": ");
			
			//print the list corresponding to this dimension
			for(int j=0; j < tempList.size(); j++)
				System.out.println(Arrays.toString(tempList.get(j)) + " ");
			
			System.out.println("");
		}
	}
	
	//if no argument is passed, print all monomials
	public void printAllMonomials() {
		//TODO total is just printing number of dimns...
		System.out.println("Printing monomials in dual A" + N + " of dimension at most " + getDimension() + "; total: " + allMonomials.size() + "\n");
		printMonomials(allMonomials);
	}
	
	public void printMonomialsAtOrBelow(int maxDimension) {
		Map<Integer, List<int[]>> temp = getMonomialsAtOrBelow(maxDimension);
		System.out.println("Printing monomials in dual A" + N + " of dimension at most " + maxDimension + "; total: " + temp.size() + "\n");
		printMonomials(temp);
	}
	
	public void printAllData() {
		System.out.println("A" + N + " dual; Dimension: " + getDimension() + "; Top class: " + Arrays.toString(topClass()) + "; Top class dim: " + DualSteenrod.milnorDimension(topClass()) + "\n");
		printAllMonomials();
	}
	
	public void writeToTxt() {
		
	}

	@Override
	public String basisType() {
		return Algebra.MILNOR;
	}

	@Override
	public boolean isInfiniteDimensional() {
		return false;
	}

	@Override
	public boolean hasRelations() {
		return true;
	}
	
	public Map<Integer, Integer> getRelations() {
		return relationMap;
	}

	@Override
	public boolean checkHomogeneous(List<int[]> input) {
		// TODO Auto-generated method stub
		return false;
	}

}