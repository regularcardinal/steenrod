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
				dimension = Tools.milnorDimension(newMono);
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
	public Function generateJMap(Function sMap) {
		Function jMap = new Function();
		Map<List<Integer>, MilnorElement> map;
		Iterator<List<Integer>> iter;
		MilnorElement target;
		
		for(int dimension = 1; dimension <= sMap.topDimension(); dimension++) {
			map = sMap.getMapByDimension(dimension);
			
			//System.out.print("dim: " + dimension);
			
			if(map == null)
				continue;
			
			iter = map.keySet().iterator();
			
			while(iter.hasNext()) {
				List<Integer> mono = iter.next();
				
				//long start = System.nanoTime();
				//System.out.print(" computing coproduct of " + mono + ": ");
				
				List<int[][]> coprod = DualSteenrod.coproduct(Tools.listToIntArray(mono));
				coprod = (List<int[][]>) DualSteenrod.reduceMod2(coprod);
				
				//System.out.print(((double)(System.nanoTime()-start))/1000000 + " ms; "); 
				
				DualSteenrod.removePrimitives(coprod);
				
				//a + s(a)
				target = new MilnorElement(mono);
				target.add(map.get(mono).getAsList());
				
				//add the j(pi(a')) \cdot i(s_bar(a'')), summing over all a', a''
				for(int i = 0; i < coprod.size(); i++) {
					//if the quotient gives zero, then this entire summand will be zero. mono1 should already be reduced
					int[] mono1 = coprod.get(i)[0];
					if(quotientIsZero(mono1)) {
						System.out.println("QUOTIENT of " + Arrays.toString(mono1) + " is zero?");
						continue;
					}
					
					int[] mono2 = coprod.get(i)[1];
					//int dim1 = DualSteenrod.milnorDimension(mono1);
					//int dim2 = DualSteenrod.milnorDimension(mono2);
					
					//represent mono2 as an elt in A(n)* tensor an elt in A//A(n)*
					//TODO: this can probably be done much more elegantly using MilnorElements. 
					//		the following should be replaced by the sBar method
					int[] mono2_1 = DualSteenrod.applyRelations(mono2, getRelations()); //in A(n)*
					int[] mono2_2 = DualSteenrod.remainder(mono2, getRelations()); //in A//A(n)*
					
					//need to check if mono2_1 is 1 here (1 is not told apart from 0 very well in some areas, an issue that goes back to probably generateMonomials
					//when using the quotient map, [] might represent zero instead...
					boolean equalsOne = ((mono2_1.length == 0) ? true : false);
					
					List<int[]> sMono2_1 = sMap.get(mono2_1).getAsList(); //removed a reduceMod2 here
					
					if(sMono2_1.size() == 0 && !equalsOne) {
						System.out.print("mono: " + mono + "; s map is zero for " + Arrays.toString(mono2_1) + "; ");
						continue;
					}
					
					List<int[]> sMono2_1AsList;
					if(equalsOne) {
						sMono2_1AsList = new ArrayList<int[]>(1);
						sMono2_1AsList.add(new int[0]);
					}
					else 
						sMono2_1AsList = sMono2_1;
					
					List<int[]> mono2_2AsList = new ArrayList<int[]>(1);
					mono2_2AsList.add(mono2_2);
					
					List<int[]> multiplied = (List<int[]>) DualSteenrod.reduceMod2(DualSteenrod.multiplySums(sMono2_1AsList, mono2_2AsList));
					
					target.add(DualSteenrod.multiplySums(jMap.get(mono1).getAsList(),  multiplied ));
					//System.out.println("nonzero s map: " + mono + " -> " + target);
				}
				
				target.reduceMod2();
				jMap.set(Tools.listToIntArray(mono), target);
				System.out.println("adding j map: " + mono + " -> " + target);
				
			}
			//System.out.println("");
		}
		
		
		return jMap;
	}
	
	public Function generateSMap() {
		Function sMap = new Function();
		
		//initialize all targets to zero
		for(Integer index : allMonomials.keySet()) {
			List<int[]> monomialList = allMonomials.get(index);
			for(int[] monomial : monomialList) {
				sMap.set(monomial, new MilnorElement(0));
			}
		}
		
		sMap.set(new int[0], new MilnorElement(new int[0]));
		
		//TODO there should be a method which does what the next 6 lines do. almost filteredDimensions() but need
		DualSteenrod AmodAn = new DualSteenrod(null);
		AmodAn.setGenerators(DualSteenrod.getDualAModAnGenerators(N));
		
		int topClassDim = Tools.milnorDimension(topClass());
		Integer[] AmodAnKeys = Tools.keysToSortedArray(AmodAn.getMonomialsAtOrBelow(topClassDim));
		Map<Integer, List<int[]>> filteredMonomials = getMonomialsByFilter(AmodAnKeys);
		Integer[] dualAnKeys = Tools.keysToSortedArray(filteredMonomials);
		
		//this is the only place where the targets of the s map can be NONZERO
		for(int i = 0; i < dualAnKeys.length; i++) {
			List<int[]> monomialList = filteredMonomials.get(dualAnKeys[i]);
			for(int[] monomial : monomialList) {
				sMap.set(monomial, new MilnorElement(0));
			}
		}
		
		return sMap;
	}
	
	@SuppressWarnings("unchecked")
	public MilnorElement sBar(MilnorElement element, Function sMap) {
		MilnorElement target = new MilnorElement();
		
		for(int i = 0; i < element.length(); i++) {
			int[] mono = element.getAsList().get(i);
			int[] mono1 = DualSteenrod.applyRelations(mono, getRelations()); //in A(n)*
			int[] mono2 = DualSteenrod.remainder(mono, getRelations()); //in A//A(n)*
			
			//need to check if mono1 is 1 here (1 is not told apart from 0 very well in some areas, an issue that goes back to probably generateMonomials)
			//if applyRelations returns [], it is representing 1 because applyRelations is really splitting up mono.
			//	when using the quotient map, [] might represent zero instead.
			boolean mono1EqualsOne = ((mono1.length == 0) ? true : false);
			
			MilnorElement sMono1 = sMap.get(mono1);
			
			//if the image under s is zero. ignore if mono1 = 1 (note that s must map 1 to 1, else it's the zero map)
			if(sMono1.length() == 0 && !mono1EqualsOne) 
				continue;
			
			//sMono1 might be zero even though it should be one in this case
			if(mono1EqualsOne) 
				sMono1 = new MilnorElement(new int[0]);
			
			List<int[]> mono2AsList = new ArrayList<int[]>(1);
			mono2AsList.add(mono2);
			
			List<int[]> multiplied = (List<int[]>) DualSteenrod.reduceMod2(DualSteenrod.multiplySums(sMono1.getAsList(), mono2AsList));
			target.add(multiplied);
		}
		
		target.reduceMod2();
		return target;
	}
	
	public MilnorElement sBarTensor(List<int[][]> sumOfTensors, Function sMap) {
		MilnorElement target = new MilnorElement();
		
		for(int[][] tensor : sumOfTensors) {
			MilnorElement mono1 = new MilnorElement(tensor[0]);
			MilnorElement mono2 = new MilnorElement(tensor[1]);
			//these ARE sums, right?
			target.add(DualSteenrod.multiplySums(sBar(mono1, sMap).getAsList(), sBar(mono2, sMap).getAsList()));
		}
		
		target.reduceMod2();
		return target;
	}
	
	public boolean checkRoth(Function sMap, Function jMap) {
		MilnorElement imJ = jMap.get(topClass());
		List<int[][]> coprodImJ = DualSteenrod.coproduct(imJ.getAsList());
		MilnorElement target = sBarTensor(coprodImJ, sMap);
		System.out.println(target);
		return (target.isZero());
	}
	
	//returns the only dimensions (domain-wise) where an s map can possibly be nonzero
	public Integer[] sMapDimensions() {
		DualSteenrod AmodAn = new DualSteenrod(DualSteenrod.getDualAModAnGenerators(N));
		int topClassDim = Tools.milnorDimension(topClass());
		Integer[] AmodAnKeys = Tools.keysToSortedArray(AmodAn.getMonomialsAtOrBelow(topClassDim));
		Map<Integer, List<int[]>> filteredMonomials = getMonomialsByFilter(AmodAnKeys);
		return Tools.keysToSortedArray(filteredMonomials);
	}
	
	//assumes input is simplified
	public boolean quotientIsZero(int[] input) {
		boolean isZero = false;
		for(int i = 0; i < input.length; i+=2) {
			int generator = input[i];
			if( (getRelations().get(generator) != null) && (input[i+1] >= getRelations().get(generator)) ) {
				isZero = true;
				break;
			}
		}
		return isZero;
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
		System.out.println("A" + N + " dual; Dimension: " + getDimension() + "; Top class: " + Arrays.toString(topClass()) + "; Top class dim: " + Tools.milnorDimension(topClass()) + "\n");
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
	
	public static Map<Integer, Integer> getRelations(int n) {
		return (new DualAn(n)).getRelations();
	}

	@Override
	public boolean checkHomogeneous(List<int[]> input) {
		// TODO Auto-generated method stub
		return false;
	}

}