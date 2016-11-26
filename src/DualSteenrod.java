import java.util.*;

/*
  	DualSteenrod is always INFINITE DIMENSIONAL and has NO RELATIONS. Use it to represent the full dual Steenrod algebra or subalgebras.
	Assumption: all but finitely many generators xi_i are just xi_i, not xi_i^j like in A//A(n) dual
	The generators are specified by the generatorMap in the following way:
	say you want xi_1^8, xi_2^4, xi_3^2, and xi_i for i>=4. then the generatorMap is 1->8, 2->4, 3->2, and i->null otherwise (this is A//A(2) dual)
	
	NOTE: Milnor monomials are represented by int[], so that e.g. xi_1^3 xi_4^9 is represented by [1, 3, 4, 9].
		  the identity is generally represented as []
	TODO: since the degree of xi_i will often be i<10, might be cleaner and less of a headache to just represent that by [3, 0 , 0, 9, 0, 0, 0]
		  where the size would be fixed in advance depending on the dimension n
		  
	TODO: can really replace all monomials int[] as short[] because the powers are generally small (probably always less than 100)
	
	CONVENTION: a list of int[] generally represents a SUM of monomials. but in some cases, this might be a product. check carefully.
*/

public class DualSteenrod implements Algebra {	
	private Map<Integer, Integer> generatorMap;
	
	//both of these are for use only when generating monomials <= some fixed degree
	private Map<Integer, Integer> finiteGeneratorMap;
	private Map<Integer, List<MilnorElement>> truncatedMonomials;
	
	public DualSteenrod(Map<Integer, Integer> generators) { 
		generatorMap = generators;
	}
	
	//INPUT: a polynomial of milnor generators, like x_1^3 x_3^4 + x_5^1
	//OUTPUT: a polynomial of tensored generators, like x_1^2 x_2^4 \otimes x_3^2 + (stuff) [not the acutal coprod of the example input]
	//represented by a list of (size 2) arrays of int[], like [ [1, 2, 2, 4] , [3, 2] ] -> (stuff)
	//this is a function A \to A tensor A (notice the input vs the ouput)
	//NOTE: the output will NOW BE reduced mod 2
	@SuppressWarnings("unchecked")
	public static List<int[][]> coproduct(List<int[]> input) {
		List<int[][]> output;
		
		//if not a monomial, ie, if a sum of monomials
		if(input.size() > 1){
			output = new ArrayList<int[][]>();
			for(int i = 0; i < input.size(); i++) {
				output.addAll(coproduct(input.subList(i, i+1)));
			}
			
			return (List<int[][]>) reduceMod2(output);
		}
		
		int[] monomial = input.get(0);
		List<Integer> monomialAsList = Tools.intArrayToList(monomial);
		
		if(SelfMap.coproductData.get(monomialAsList) != null) 
			return SelfMap.coproductData.get(monomialAsList);
		
		//if single-term monomial of power 1
		if((monomial.length == 2) && (monomial[1] == 1)) {
			int dimension = monomial[0];
			output = new ArrayList<int[][]>(dimension+1);
			int[][] tensor;
			
			//see Wood, page 8
			for(int i = 0; i <= dimension; i++) {
				tensor = new int[2][];
				tensor[0] = applyRelations(new int[]{(dimension-i), (int) Math.pow(2, i)});
				tensor[1] = applyRelations(new int[]{i, 1});
				
				output.add(tensor);
			}
			
			output = (List<int[][]>) reduceMod2(output);
			SelfMap.coproductData.put(monomialAsList, output);
			return output;
		}
		
		List<int[]> tempList1 = new ArrayList<int[]>(1);
		List<int[]> tempList2 = new ArrayList<int[]>(1);
		int[] tempMono1, tempMono2;
		
		//if a multi-term monomial
		if(monomial.length > 2) {
			//split off the first term, and recursively multiply by the rest
			tempMono1 = new int[]{monomial[0], monomial[1]};
			tempMono2 = Arrays.copyOfRange(monomial, 2, monomial.length);
		}
		//if a single term monomial (must have monomial.length == 2 AND monomial[1]>1 to have logically made it here)
		else {
			tempMono1 = new int[]{monomial[0], 1};
			tempMono2 = new int[]{monomial[0], monomial[1]-1};
		}
		
		tempList1.add(tempMono1);
		tempList2.add(tempMono2);
		
		List<int[][]> coprod1 = SelfMap.coproductData.get(Tools.intArrayToList(tempMono1));
		List<int[][]> coprod2 = SelfMap.coproductData.get(Tools.intArrayToList(tempMono2));
		
		if(coprod1 == null) {
			coprod1 = coproduct(tempList1);
			//System.out.print(" saving " + Arrays.toString(tempMono1) + " ");
			SelfMap.coproductData.put(Tools.intArrayToList(tempMono1), coprod1);
		}
		if(coprod2 == null) {
			coprod2 = coproduct(tempList2);
			//System.out.print(" saving " + Arrays.toString(tempMono2) + " ");
			SelfMap.coproductData.put(Tools.intArrayToList(tempMono2), coprod2);
		}
		
		output = (List<int[][]>) reduceMod2(multiplyTensors(coprod1, coprod2));
		SelfMap.coproductData.put(monomialAsList, output);
		return output;
	}
	
	
	//version of coproduct for a single monomial
	public static List<int[][]> coproduct(int[] input) {
		List<int[]> temp = new ArrayList<int[]>();
		temp.add(input);
		return coproduct(temp);	
	}
	
	//TODO
	//should do term-by-term multiplication, given two strings of tensors, should cleanup, meaning get rid of elements that occur evenly many times
	//assumption: each int[][] in input1 and input2 is length 2
	public static List<int[][]> multiplyTensors(List<int[][]> input1, List<int[][]> input2) {
		List<int[][]> output = new ArrayList<int[][]>();
		int[][] tensor1, tensor2, tempTensor;
		
		for(int i = 0; i < input1.size(); i++) {
			for(int j = 0; j < input2.size(); j++) {
				tensor1 = input1.get(i);
				tensor2 = input2.get(j);
				tempTensor = new int[2][];
				tempTensor[0] = milnorMultiply(tensor1[0], tensor2[0]);
				tempTensor[1] = milnorMultiply(tensor1[1], tensor2[1]);
				output.add(tempTensor);
			}
		}
		
		return output;
	}
	
	//TODO: NOT TESTED!
	public static List<int[]> multiplySums(List<int[]> input1, List<int[]> input2) {
		List<int[]> output = new ArrayList<int[]>();
		
		for(int i = 0; i < input1.size(); i++) 
			for(int j = 0; j < input2.size(); j++) 
				output.add(milnorMultiply(input1.get(i), input2.get(j)));
		
		return output;
	}
	
	//TODO: NOT TESTED!
	//this should take in a list of tensors and evaluate each of them, meaning apply the multiplication A tensor A -> A
	public static List<int[]> evaluateTensors(List<int[][]> input) {
		List<int[]> output = new ArrayList<int[]>();
		for(int i = 0; i < input.size(); i++) {
			output.add(DualSteenrod.milnorMultiply(input.get(i)[0], input.get(i)[1]));
		}
		
		return output;
	}
	
	//INPUT: a list of int arrays
	//OUTPUT: a single int array representing the concatenation
	//note: originally written with even length arrays in mind, but it should work regardless
	public static int[] concatenate(List<int[]> monomials) {
		int totalLength = 0;
		for(int i = 0; i < monomials.size(); i++) {
			totalLength += monomials.get(i).length;
		}
		
		int[] output = new int[totalLength];
		int[] current;
		int index = 0;
		
		for(int i = 0; i < monomials.size(); i++) {
			current = monomials.get(i);
			for(int j = 0; j < current.length; j++) {
				output[index + j] = current[j];
			}
			index += current.length;
		}
		return output;
	}
	
	//INPUT: a list of even length int arrays representing monomials
	//OUTPUT: a single even length int array representing the product, NO RELATIONS applied (other than xi_i^0 = 1)
	public static int[] milnorMultiply(List<int[]> monomials) {
		return applyRelations(concatenate(monomials));
	}
	
	public static int[] milnorMultiply(int[] input1, int[] input2) {
		List<int[]> temp = new ArrayList<int[]>();
		temp.add(input1);
		temp.add(input2);
		return milnorMultiply(temp);
	}
	
	//INPUT: a max dimension
	//OUTPUT: a map of all monomials in dimensions <= max dimension. keys are dimension, values are lists of elements in those dimensions.
	//this is not super efficient but it generally doesn't matter
	//there may be a bug/infinite loop if maxDimension is less than that of one of the generators in genMap
	public Map<Integer, List<MilnorElement>> getMonomialsAtOrBelow(int maxDimension) {
		int maxGenDim = 0;
		finiteGeneratorMap = new HashMap<Integer, Integer>();
		
		while(((int)Math.pow(2, maxGenDim + 1) - 1) < maxDimension)
			maxGenDim++;
		
		//fill up the finite generator map, which stores the powers of all xi's up to maxGenDim. 
		//if the entry in genMap was null, put xi_i^1. if the entry in genMap exceeds the max dimension, then replace its power by 0
		for(int i = 1; i <= maxGenDim; i++) {
			if(generatorMap.get(i) == null)
				finiteGeneratorMap.put(i, 1);
			//else if ((Math.pow(2, i) - 1) * generatorMap.get(i) <= maxDimension)
			else
				finiteGeneratorMap.put(i, generatorMap.get(i));
			//else
				//finiteGeneratorMap.put(i, 0);
		}
		
		//create initial array which will be every generator (with dim <= dimension) raised to the 0
		int[] initial = new int[2*maxGenDim];
		for(int i = 0; i < maxGenDim; i++) {
			initial[2*i] = i+1;
			initial[2*i+1] = 0;
		}
		
		truncatedMonomials = new HashMap<Integer, List<MilnorElement>>();
		generateMonomials(initial, maxDimension, 0, maxGenDim);
		return truncatedMonomials;
	}
	
	//fill up the truncatedMonomials map with every milnor monomial <= maxDimension, organized by degree
	//ALWAYS initialize truncatedMonomials first
	private void generateMonomials(int[] monomial, int maxDimension, int shift, int maxShift) {
		int[] newMono = Arrays.copyOf(monomial, monomial.length);
		int dimension;
		
		//single out the generator we're focusing on in this iteration (determined by the shift: if the curGen is xi_i, then i = shift+1)
		int[] curGenerator = new int[]{newMono[2*shift], 0};
		
		while(Tools.milnorDimension(curGenerator) <= maxDimension) {
			//if shift == maxShift-1, then we are at the furthest-right generator
			if(shift != (maxShift-1))
				generateMonomials(newMono, maxDimension, shift+1, maxShift);
			else {
				dimension = Tools.milnorDimension(newMono);
				
				if(dimension <= maxDimension) {
					if(truncatedMonomials.get(dimension) == null) {
						List<MilnorElement> temp = new ArrayList<MilnorElement>();
						temp.add(new MilnorElement(DualSteenrod.applyRelations(newMono)));
						truncatedMonomials.put(dimension, temp);
					}
					else 
						truncatedMonomials.get(dimension).add(new MilnorElement(DualSteenrod.applyRelations(newMono)));
				}
			}
			
			//apply another power to the current generator based on the genMap
			curGenerator[1] += finiteGeneratorMap.get(shift+1);
			newMono[2*shift + 1] += finiteGeneratorMap.get(shift+1);
		}
	}
	
	public void printMonomialsAtOrBelow(int maxDimension) {
		truncatedMonomials = new HashMap<Integer, List<MilnorElement>>();
		truncatedMonomials = getMonomialsAtOrBelow(maxDimension);
		
		//TODO total is just printing number of dimns...
		System.out.println("Printing monomials in subalgebra of dual Steenrod of dimension at most " + maxDimension + "; total: " + truncatedMonomials.size() + "\n");
		
		//first convert from a set of keys to a sorted Integer array of keys
		Integer[] sortedKeys = Tools.keysToSortedArray(truncatedMonomials);
		
		List<MilnorElement> tempList;
		
		//for each key, get the list
		for(int i = 0; i < sortedKeys.length; i++) {
			tempList = truncatedMonomials.get(sortedKeys[i]);
			System.out.println("dim = " + sortedKeys[i] + ": ");
			
			//print the list corresponding to this dimension
			for(int j=0; j < tempList.size(); j++)
				System.out.println(tempList.get(j) + " ");
			
			System.out.println("");
		}
	}
	
	
	//INPUT: a sum of monomials OR a sum of tensors
	//OUTPUT: that same sum of monomials/tensors, but reduced mod 2, addition-wise
	//TODO: there should be a better way, probably with MilnorElements, to make this much more concise
	public static List<?> reduceMod2(List<?> input) {
		if(input == null || input.size() == 0)
			return input;
		
		if(input.get(0) instanceof int[][]) {
			List<int[][]> output = new ArrayList<int[][]>();		
			//List<List<Integer>> is used in place of int[][] because int[][] doesn't work well with HashMaps (.equals in particular)
			HashMap<List<List<Integer>>, Integer> entryCounts = new HashMap<List<List<Integer>>, Integer>();
			Integer currentCount;
			int newCount;
			
			for(int i = 0; i < input.size(); i++) {
				currentCount = entryCounts.get(Tools.multiIntArrayToList((int[][]) input.get(i))); 
				
				//if nothing was there, change value to 1, otherwise increase the value by 1.
				newCount = (currentCount == null ? 1 : currentCount+1);
				
				entryCounts.put(Tools.multiIntArrayToList((int[][]) input.get(i)), newCount);
				//System.out.println("new count "  + newCount + " entry counts size " + entryCounts.size() + " " + Arrays.toString(input.get(i)));
			}
			
			Set<List<List<Integer>>> keys = entryCounts.keySet();
			Iterator<List<List<Integer>>> iter = keys.iterator();
			
			List<List<Integer>> tensor;
			
			while(iter.hasNext()) {
				tensor = iter.next();
				
				if((entryCounts.get(tensor).intValue() % 2) != 0)
					output.add(Tools.multiListToIntArray(tensor));
			}
			
			return output;
		}
		else if(input.get(0) instanceof int[]) {
			List<int[]> output = new ArrayList<int[]>();		
			HashMap<List<Integer>, Integer> entryCounts = new HashMap<List<Integer>, Integer>();
			Integer currentCount;
			int newCount;
			
			for(int i = 0; i < input.size(); i++) {
				currentCount = entryCounts.get(Tools.intArrayToList((int[]) input.get(i))); 
				
				//if nothing was there, change value to 1, otherwise increase the value by 1.
				newCount = (currentCount == null ? 1 : currentCount+1);
				
				entryCounts.put(Tools.intArrayToList((int[]) input.get(i)), newCount);
			}
			
			Set<List<Integer>> keys = entryCounts.keySet();
			Iterator<List<Integer>> iter = keys.iterator();
			
			List<Integer> monomial;
			
			while(iter.hasNext()) {
				monomial = iter.next();
				
				//don't want to include empty arrays
				if(  ((entryCounts.get(monomial).intValue() % 2) != 0)  &&  monomial.size() > 0)
					output.add(Tools.listToIntArray(monomial));
			}
			
			return output;
		}
		
		return null;
	}
	
	public static void removePrimitives(List<int[][]> input) {
		for(int i = input.size() - 1; i >= 0; i--) {
			int[][] tensor = input.get(i);
			if(tensor[0].length == 0 || tensor[1].length == 0) 
				input.remove(i);
		}
	}
	
	/* 
	 * INPUT: an EVEN length milnor monomial with possible duplicate generators (xi_i's), eg [3, 0, 4, 1, 1, 7, 2, 2, 1, 5]
	 * OUTPUT: an EVEN length milnor monomial with no duplicate generators and in increasing order by generator, eg [1, 12, 2, 2, 4, 1]
	 * if there are relations in relationMap, then it will abide by them
	 * if there are any generators raised to the 0 power, it will delete them
	 * note this will even work with negative powers (although no guarantees...)
	 * TODO can combine both of the applyRelations methods into one by using a MilnorElement as input!
	 * WARNING: right now, what this really does is, say we're given relMap = A(n)*. then it returns the part of monomial that is in A(n)* if we split
	 * 			A* as A(n)* \otimes A//A(n)*. this is NOT the same as applying the quotient map! thus:
	 * 			TODO rename this as simplify, or turn into two functions: simplify for no relations, and split for relations.
	 * 				 if you turn this into two methods, you have to be careful. different things are calling this for various reasons.
	 */
	public static int[] applyRelations(int[] monomial, Map<Integer, Integer> relationMap) {
		Map<Integer, Integer> powers = new HashMap<Integer, Integer>();
		int generator, power, newPower; 
		Integer relation;
		boolean relationExists;
		
		//transfer from monomial to map (powers), one generator at a time, checking for relations along the way
		for(int i = 0; i < monomial.length; i += 2) {
			relationExists = false;
			
			generator = monomial[i];
			power = monomial[i+1];
			relation = relationMap.get(generator);
			
			if(relation != null) {
				relationExists = true;
				//if the power coming from the monomial is a multiple of the relation for that given generator, we get the identity, so skip this entry
				if( (power % (int)relation) == 0)
					continue;
			}
			
			//we ignore if power = 0 because we get the identity. 
			if(power == 0) 
				continue;
			
			//if the generator hasn't been logged yet, input its power (or power mod the relation if it exists)
			if(powers.get(generator) == null) {
				if(relationExists)
					powers.put(generator, power % (int)relation);
				else
					powers.put(generator, power);
			}
			//if there's already a power existing, then increment it. but if we get the identity from a relation, remove it.
			else {
				newPower = ((int)powers.get(generator)) + power;
				if(relationExists) { 
					if( (newPower % (int)relation) == 0)
						powers.remove(generator);
					else
						powers.put(generator, (newPower % (int)relation));
				}
				else
					powers.put(generator, newPower);
			}
		}
		
		//this is to delete anything of the form [0, <nonzero>] which may have slid through
		if(powers.get(0) != null)
			powers.remove(0);
		
		int[] output = new int[powers.size()*2];
		int outputIndex = 0;
		int keyIndex = 1;
		Integer mapPower;
		
		//create the cleaned-up monomial; it will be in increasing generator form, ie x_1 before x_2 before x_3 etc
		while(powers.size() >= 1) {
			mapPower = powers.get(keyIndex);
			
			//a key should never be assigned a null value on purpose
			if(mapPower != null) {
				output[outputIndex] = keyIndex;
				output[outputIndex+1] = (int)mapPower;
				outputIndex += 2;
				powers.remove(keyIndex);
			}
			
			keyIndex += 1;
		}
		return output;
	}
	
	//can call without relations
	public static int[] applyRelations(int[] monomial) {
		Map<Integer, Integer> emptyRelations = new HashMap<Integer, Integer>();
		return applyRelations(monomial, emptyRelations);
	}
	
	//applies relations term by term
	public static List<int[]> applyRelations(List<int[]> sumOfMonos, Map<Integer, Integer> relationMap) {
		List<int[]> output = new ArrayList<int[]>(sumOfMonos.size());
		for(int i = 0; i < sumOfMonos.size(); i++) {
			output.add(applyRelations(sumOfMonos.get(i), relationMap));
		}
		return output;
	}
	
	//NOTE: this assumes input is reduced!
	//TODO: double check this does what you really want the s_bar map to be doing. picking off elements in A//A(n)* and then checking what's left over
	//		should be the same as first reducing mod A(n)* and seeing what's left over, precisely because A(n)* and A//A(n)* are in some sense
	//		complements.
	public static int[] remainder(int[] input, Map<Integer, Integer> relationMap) {
		int length = 8;
		int[] remainder = new int[length];
		int[] reduced = applyRelations(input, relationMap);
		
		//if reduced is zero
		if(reduced.length == 0)
			return input;
		
		reduced = Tools.dynamicToFixedForm(reduced, length);
		input = Tools.dynamicToFixedForm(input, length);
		
		for(int i = 0; i < length; i++) {
			remainder[i] = Math.abs(reduced[i] - input[i]);
		}
		
		return Tools.fixedToDynamicForm(remainder);
	}
	
	public static Map<Integer, Integer> getDualAModAnGenerators(int n) {
		HashMap<Integer, Integer> generators = new HashMap<Integer, Integer>();
	
		for(int i = 1; i <= (n+1); i++) {
			generators.put(i, (int) Math.pow(2, n+2-i));
		}
		
		return generators; 
	}
	
	@Override
	public String basisType() {
		return Algebra.MILNOR;
	}

	@Override
	public boolean isInfiniteDimensional() {
		return true;
	}

	@Override
	public boolean hasRelations() {
		return false;
	}

	@Override
	public boolean checkHomogeneous(List<int[]> input) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Map<Integer, Integer> getGenerators() {
		return generatorMap;
	}
	
	public void setGenerators(Map<Integer, Integer> generators) {
		generatorMap = generators;
	}
}