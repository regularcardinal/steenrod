import java.util.*;

//TODO: need to return zero in every dimension. maybe using null for zero is not the best idea...

public class Function {	
	//this filters the function by dimension, so for each int, there is a mapping between monomials in those given dimension
	private Map<Integer, Map<List<Integer>, MilnorElement>> entireFunction;
	
	//initialize entireFunction to have null -> null and [] -> [] in dimension 0
	public Function() {
		//using List<Integer> instead of int[] because hash maps don't work well with keys that are arrays because of .equals issues
		entireFunction = new HashMap<Integer, Map<List<Integer>, MilnorElement>>();	
		Map<List<Integer>, MilnorElement> map = new HashMap<List<Integer>, MilnorElement>();
		map.put(null, new MilnorElement((int[]) null));
		
		entireFunction.put(0, map);
		
		//shouldn't be pointer problems here
		map = new HashMap<List<Integer>, MilnorElement>();
		List<Integer> mono = new ArrayList<Integer>(0);
		map.put(mono, new MilnorElement(new int[0]));
		
		entireFunction.put(0, map);
	}

	//INPUT: a monomial called input
	//OUTPUT: the sum of monomials that input maps to under this function
	//using milnor basis convention, return null if it maps to zero. return [] for the identity in dimension 0
	public MilnorElement get(int[] input) {
		//return the MilnorElement corresponding to the zero monomial. probably not needed anywhere.
		if(input == null)
			return entireFunction.get(0).get(null);
		
		List<Integer> inputAsList = Tools.intArrayToList(input);
		int dimension = DualSteenrod.milnorDimension(input);
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
	public void set(int[] source, MilnorElement image) {
		List<Integer> sourceAsList = Tools.intArrayToList(source);
		int dimension = DualSteenrod.milnorDimension(source);
		Map<List<Integer>, MilnorElement> map = entireFunction.get(dimension);
		
		if(map == null) 
			map = new HashMap<List<Integer>, MilnorElement>();
		
		map.put(sourceAsList, image);
		entireFunction.put(dimension, map);
	}
	
	//TODO
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
}