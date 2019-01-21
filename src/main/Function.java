package main;
import java.util.*;

import elements.MilnorElement;

//TODO: need to return zero in every dimension. maybe using null for zero is not the best idea...
//TODO: should be Map<MilnorElement, MilnorElement> in each dimension of entireFunction, no?
//      this also would allow sums to be mapped to sums if needed
//      right now, only monomials can be a domain element (targets can be sums)

public class Function {	
	//this filters the function by dimension, so for each int, there is a mapping between monomials in those given dimension
	private Map<Integer, Map<List<Integer>, MilnorElement>> entireFunction;
	
	//initialize entireFunction to have null -> null and [] -> [] in dimension 0
	public Function() {
		//using List<Integer> instead of int[] because hash maps don't work well with keys that are arrays because of .equals issues
		entireFunction = new HashMap<Integer, Map<List<Integer>, MilnorElement>>();	
		Map<List<Integer>, MilnorElement> map = new HashMap<List<Integer>, MilnorElement>();
		map.put(null, new MilnorElement(0));
		
		entireFunction.put(0, map);
		
		//shouldn't be pointer problems here
		map = new HashMap<List<Integer>, MilnorElement>();
		List<Integer> mono = new ArrayList<Integer>(0);
		map.put(mono, new MilnorElement(new int[0]));
		
		entireFunction.put(0, map);
	}
	
	//*deep* copy the contents from fun. probably inefficient.
	public Function(Function fun) {
		Map<Integer, Map<List<Integer>, MilnorElement>> tempEntireMap = fun.getEntireFunction();
		entireFunction = new HashMap<Integer, Map<List<Integer>, MilnorElement>>();
		
		for(Integer dim : tempEntireMap.keySet()) {
			Map<List<Integer>, MilnorElement> mapByDimension = new HashMap<List<Integer>, MilnorElement>();
			
			for(Map.Entry<List<Integer>, MilnorElement> entry : tempEntireMap.get(dim).entrySet()) {
				List<Integer> key = new ArrayList<Integer>(entry.getKey().size());
				for(Integer num : entry.getKey())
					key.add(new Integer(num));
				
				List<int[]> tempVal = new ArrayList<int[]>(entry.getValue().getAsList().size());
				for(int[] mono : entry.getValue().getAsList())
					tempVal.add(Arrays.copyOf(mono, mono.length));
				
				MilnorElement value = new MilnorElement(tempVal);	
				
				mapByDimension.put(key, value);
			}
			entireFunction.put(dim, mapByDimension);
		}
	}

	//INPUT: a monomial called input
	//OUTPUT: the sum of monomials that input maps to under this function
	//using milnor basis convention, return null if it maps to zero. return [] for the identity in dimension 0
	public MilnorElement get(int[] input) {
		//return the MilnorElement corresponding to the zero monomial. probably not needed anywhere.
		if(input == null)
			return entireFunction.get(0).get(null);
		
		List<Integer> inputAsList = Tools.intArrayToList(input);
		int dimension = Tools.milnorDimension(input);
		//System.out.println(Arrays.toString(input) + " dim " + dimension);
		return entireFunction.get(dimension).get(inputAsList);
	}
	
	public Map<List<Integer>, MilnorElement> getMapByDimension(int dimension) {
		return entireFunction.get(dimension);
	}
	
	public int topDimension() {
		Integer[] sortedKeys = Tools.keysToSortedArray(entireFunction);
		return sortedKeys[sortedKeys.length-1];
	}
	
	//INPUT: a pair of monomials, a source and target
	//BEHAVIOR: find the dimension of the source, add this source/target to that dimension
	//TODO source should probably be List<Integer> to avoid tons of conversions from array to list (which may have been done BEFORE calling set)
	public void set(int[] source, MilnorElement image) {
		List<Integer> sourceAsList = Tools.intArrayToList(source);
		int dimension = Tools.milnorDimension(source);
		Map<List<Integer>, MilnorElement> map = entireFunction.get(dimension);
		
		if(map == null) 
			map = new HashMap<List<Integer>, MilnorElement>();
		
		map.put(sourceAsList, image);
		entireFunction.put(dimension, map);
	}
	
	public Map<Integer, Map<List<Integer>, MilnorElement>> getEntireFunction() {
		return entireFunction;
	}
	
	@Override
	public String toString() {
		String output = "";
		
		for(int i = 1; i <= topDimension(); i++) {
			output += "Dimension: " + i + "\n";
			Map<List<Integer>, MilnorElement> map = getMapByDimension(i);
			
			if(map == null)
				continue;
			
			output += map.toString();
			output += "\n\n";
		}
		
		return output;
	}
	
	//INPUT: name of function, say f
	//OUTPUT: a bunch of Tex lines that look like f(x) = y, for each (x,y) pair in every dimension
	public List<String> printToTex(String name) {
		List<String> output = new ArrayList<String>();
		Integer[] sortedDimensions = Tools.keysToSortedArray(this.getEntireFunction());
		
		for(int i = 0; i < sortedDimensions.length; i++) {
			output.addAll(printToTex(name, sortedDimensions[i]));
			output.add("\n\\medskip");
		}	
		return output;	
	}
	
	//prints the function just in the specified dimension (see other printToTex for format)
	public List<String> printToTex(String name, int dimension) {
		List<String> output = new ArrayList<String>();
		
		//if the specified dimension is not in the function
		if(!this.getEntireFunction().containsKey(dimension))
			return output;
		
		Map<List<Integer>, MilnorElement> dimFunction = this.getEntireFunction().get(dimension);
		for(Map.Entry<List<Integer>, MilnorElement> pair : dimFunction.entrySet()) {
			//DON'T PRINT THE LINE IF SOMETHING IS ZERO, EDIT THIS LATER
			if(pair.getValue().isZero())
				continue;
			
			//convert key, i.e. some element of the domain, to ME
			MilnorElement domain = new MilnorElement(Tools.listToIntArray(pair.getKey()));
			//convert key and value (domain and target) to Tex
			String domainAsTex = domain.convertToTex();
			String targetAsTex = pair.getValue().convertToTex();
			
			String pairAsTex = "$" + name + "(" + domainAsTex + ") = " + targetAsTex + "$\\\\";
			
			output.add(pairAsTex);
		}	
		return output;	
	}
	
	/* 
	 * This will take the current function and return a list of functions such that
	 * each function in the list is the same as the current function in every dimension
	 * EXCEPT the specified dimension.
	 * 
	 * In the specified dimension, this method will cycle through every possible permutation
	 * of domain/target pair. So if there are 4 domain elements and 1 possible target element,
	 * there will be 2^4 = 16 different functions returned.
	 * 
	 * NOTE: Right now, this is written to only work in this very particular scenario (dim 16, and 16 variations). 
	 * See Roth p.26 or p.28 for more explanation.
	 * (So we're assuming that we have an s map.)
	 */
	public List<Function> varyInDimension(int dimension) {
		List<Function> allVariations = new ArrayList<Function>();
		//get all the domain elements of the function in the specified dimension
		List<List<Integer>> domainElements = new ArrayList<List<Integer>>(this.getMapByDimension(dimension).keySet());
		
		String target = "1 16";
		final int n = 4;
		
		//cycle through all binary strings of length n, and add 0s at the front if necessary
		//eg 0000, 0001, 0010, ..., 1111
		//this will determine whether or not to map the domain to the target (xi_1^16 right now)
		for (int i = 0; i < Math.pow(2, n); i++) {
		    String bin = Integer.toBinaryString(i);
		    while (bin.length() < n)
		        bin = "0" + bin;
		    
		    //turn string into array
		    int[] binArray = new int[n];
		    for(int j = 0; j < n; j++)
		    	binArray[j] = Integer.parseInt(bin.substring(j, j+1));
		    
		    Function tempEntire = new Function(this);
		    
		    //just to be safe wrt hashcodes vs .equals() and things like that
		    tempEntire.getEntireFunction().remove(dimension);
		    
		    for(int j = 0; j < domainElements.size(); j++) {
		    	if(binArray[j] == 1)
		    		tempEntire.set(Tools.listToIntArray(domainElements.get(j)), new MilnorElement(target));
		    	else
		    		tempEntire.set(Tools.listToIntArray(domainElements.get(j)), new MilnorElement());
		    }
		    
		    allVariations.add(tempEntire);    
		}
		
		return allVariations;		
	}
}