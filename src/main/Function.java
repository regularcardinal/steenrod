package main;
import java.util.*;

import elements.MilnorElement;

//TODO: need to return zero in every dimension. maybe using null for zero is not the best idea...
//TODO: should be Map<MilnorElement, MilnorElement> in each dimension of entireFunction, no?
//      this also would allow sums to be mapped to sums if needed

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
	public String printToTex(String name) {
		String output = "";
		Integer[] sortedDimensions = Tools.keysToSortedArray(this.getEntireFunction());
		int dim;
		
		for(int i = 0; i < sortedDimensions.length; i++) {
			dim = sortedDimensions[i];
			Map<List<Integer>, MilnorElement> dimFunction = this.getEntireFunction().get(dim);
			for(Map.Entry<List<Integer>, MilnorElement> pair : dimFunction.entrySet()) {
				//convert key to ME
				//convert that to Tex
				//output += "$s(" + 
				
			}
			
			
			
			/*
			Iterator<Entry<String, String>> iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				sb
			    sb.append(entry.getKey());
			    sb.append('=').append('"');
			    sb.append(entry.getValue());
			    sb.append('"');
			    if (iter.hasNext()) {
			        sb.append(',').append(' ');
			    }
			}*/
		}
		
		
		return output;	
	}
}