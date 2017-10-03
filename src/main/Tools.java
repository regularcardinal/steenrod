package main;
import java.math.BigInteger;
import java.util.*;

import elements.GenericElement;
import elements.JElement;
import elements.MilnorElement;

public class Tools {

	//non-bigint overflows fast
	public static BigInteger factorial(int i) {
		BigInteger bigI = BigInteger.valueOf(i);
		if(i>=2)
			return bigI.multiply(factorial(i-1));
		return BigInteger.valueOf(1);
	}

	//this should work (even if n<0) as long as k>=0. should never have k<0 in this context
	public static BigInteger choose(int n, int k) {
		BigInteger bigN = BigInteger.valueOf(n);
		BigInteger bigK = BigInteger.valueOf(k);
		if(bigN.compareTo(bigK) < 0)
			return BigInteger.valueOf(0);
		return factorial(n).divide(factorial(n-k).multiply(factorial(k)));
	}

	public static Integer[] keysToSortedArray(Map<Integer, ?> map) {
		Integer[] keys = (Integer[]) map.keySet().toArray(new Integer[map.keySet().size()]);
		Arrays.sort(keys);
		return keys;
	}

	//INPUT: a double array representing a SINGLE tensor, so that it is a 2 x (variable length) array
	//OUTPUT: that same array represented by a length 2 list of lists of integers (where the lists of integers represent monomials)
	//NOTE: this is needed for HashMaps, where checking keys that are int[][] does not work, because .equals for arrays, and especially multi-arrays, does not work the way we want
	public static List<List<Integer>> multiIntArrayToList(int[][] input) {
		List<List<Integer>> tensor = new ArrayList<List<Integer>>(2);
		List<Integer> mono1 = new ArrayList<Integer>(input[0].length);
		List<Integer> mono2 = new ArrayList<Integer>(input[1].length);
		
		for(int i = 0; i < input[0].length; i++) {
			mono1.add(input[0][i]);
		}
		
		for(int i = 0; i < input[1].length; i++) {
			mono2.add(input[1][i]);
		}
		
		tensor.add(mono1);
		tensor.add(mono2);
		
		return tensor;
	}

	//single list version
	public static List<Integer> intArrayToList(int[] input) {
		List<Integer> monomial = new ArrayList<Integer>(input.length);
		
		for(int i = 0; i < input.length; i++) {
			monomial.add(input[i]);
		}
		
		return monomial;
	}

	//this does the opposite of multiIntArrayToList
	public static int[][] multiListToIntArray(List<List<Integer>> input) {
		int[][] tensor = new int[2][];
		int[] mono1 = new int[input.get(0).size()];
		int[] mono2 = new int[input.get(1).size()];
		
		for(int i = 0; i < input.get(0).size(); i++) {
			mono1[i] = input.get(0).get(i);
		}
		
		for(int i = 0; i < input.get(1).size(); i++) {
			mono2[i] = input.get(1).get(i);
		}
		
		tensor[0] = mono1;
		tensor[1] = mono2;
		
		return tensor;
	}

	//this does the opposite of intArrayToList
	public static int[] listToIntArray(List<Integer> input) {
		int[] monomial = new int[input.size()];
		
		for(int i = 0; i < input.size(); i++) {
			monomial[i] = input.get(i);
		}
		
		return monomial;
	}
	
	//INPUT: a monomial in increasing form (ie applyRelations has been run)
	//OUTPUT: an int[] of the given length with the powers of generators as entries.
	//for example, {1,1,2,1} with length 5 will be [1, 1, 0, 0, 0]. {2, 3, 4, 5, 5, 7} with length 8 will be [0, 3, 0, 5, 7, 0, 0, 0].
	public static int[] dynamicToFixedForm(int[] input, int length) {
		int[] output = new int[length];
		
		for(int i = 0; i < input.length; i+=2) {
			int generator = input[i];
			int power = input[i+1];
			output[generator-1] = power;
		}
		
		return output;
	}
	
	//does the opposite of dynamicToFixedForm
	public static int[] fixedToDynamicForm(int[] input) {
		int[] output = new int[2*input.length];
		
		for(int i = 0; i < input.length; i++) {
			output[2*i] = i+1;
			output[2*i+1] = input[i];
		}
		return DualSteenrod.applyRelations(output);
	}
	
	public static String sumToString(List<?> input) {
		if(input == null)
			return null;
		if(input.size() == 0)
			return "";
		
		String output = "";
		
		if(input.get(0) instanceof int[]) 
			for(int i = 0; i < input.size(); i++) 
				output += Arrays.toString((int[]) input.get(i)) + ( (i != input.size() - 1) ? " + " : "" );
		
		if(input.get(0) instanceof int[][]) {
			for(int i = 0; i < input.size(); i++) {
				int[][] tensor = (int[][]) input.get(i);
				output += Arrays.toString(tensor[0]) + " x " + Arrays.toString(tensor[1]) + ( (i != input.size() - 1) ? " + " : "" );
			}
		}
		
		return output;
	}

	//input: milnor monomial eg xi_1^5 x_3^4
	//output: degree eg (5) + (7)(4) = 33
	public static int milnorDimension(int[] monomial) {
		int size = monomial.length;
		int degree = 0;
		
		//error
		if((size % 2) != 0)
			return -1;
		
		for(int i = 0; i < size; i+=2) {
			degree += ((Math.pow(2, monomial[i]) - 1) * monomial[i+1]);
		}
		
		return degree;
	}
	
	//parses # # # + # # into a list of size two, consisting of arrays [#, #, #] and [#, #]
	//note it doesn't matter if the input is a dual basis or anything else in particular
	public static List<int[]> parseSumFromString(String input) {		
		String[] split = input.split(" [+] ");
		
		List<int[]> sum = new ArrayList<int[]>(split.length);
		
		for(String mono : split) {
			String[] monoAsString = mono.split(" ");
			int[] monoAsInt = new int[monoAsString.length];
			
			try {
				for(int i = 0; i < monoAsString.length; i++)
					monoAsInt[i] = Integer.parseInt(monoAsString[i]);
			}
			catch(NumberFormatException e) {
				System.err.println("Wrong format: " + e.getMessage());
				return new ArrayList<int[]>(0);
			}
			
			sum.add(monoAsInt);
		}
		
		return sum;
	}
	
	public static List<int[][]> parseTensorSumFromString(String input) {
		String[] split = input.split(" [+] ");
		
		List<int[][]> sum = new ArrayList<int[][]>(split.length);
		
		for(String tensor : split) {
			String[] tensorSplit = tensor.split(" x ");
			String[] mono1AsString = tensorSplit[0].split(" ");
			String[] mono2AsString = tensorSplit[1].split(" ");
			int[] mono1AsInt = new int[mono1AsString.length];
			int[] mono2AsInt = new int[mono2AsString.length];
			
			try {
				for(int i = 0; i < mono1AsString.length; i++)
					mono1AsInt[i] = Integer.parseInt(mono1AsString[i]);
				
				for(int i = 0; i < mono2AsString.length; i++)
					mono2AsInt[i] = Integer.parseInt(mono2AsString[i]);
			}
			catch(NumberFormatException e) {
				System.err.println("Wrong format: " + e.getMessage());
				return new ArrayList<int[][]>(0);
			}
			
			int[][] parsedTensor = new int[2][];
			parsedTensor[0] = mono1AsInt;
			parsedTensor[1] = mono2AsInt;
			
			sum.add(parsedTensor);
		}
		
		return sum;
	}

	public static int countSMaps(DualAn dualAn, DualSteenrod AmodAn) {
		int count = 1;
		int topClassDim = milnorDimension(dualAn.topClass());
		Map<Integer, List<MilnorElement>> AmodAnMonomials = AmodAn.getMonomialsAtOrBelow(topClassDim);
		Map<Integer, List<int[]>> dualAnMonomials = dualAn.getMonomialsByFilter(keysToSortedArray(AmodAnMonomials));
		
		for(int i = 1; i <= topClassDim; i++) {
			//if both dual An and A mod An have monomials in dimension i, count the number of maps.
			//each monomial from An can map to any of the monomials from A mod An OR zero (so that's what the  + 1 is)
			if((dualAnMonomials.get(i) != null) && (AmodAnMonomials.get(i) != null)) 
				count *= Math.pow(AmodAnMonomials.get(i).size() + 1, dualAnMonomials.get(i).size());
		}
		
		return count;
	}

	//this is the multiply function for MilnorElements (dual elements). should go into the MilnorElement class.
	public static List<int[]> multiplySums(List<int[]> input1, List<int[]> input2) {
		List<int[]> output = new ArrayList<int[]>();
		
		for(int i = 0; i < input1.size(); i++) {
			for(int j = 0; j < input2.size(); j++) {
				int[] multiplied = DualSteenrod.milnorMultiply(input1.get(i), input2.get(j));
				boolean skip = false;
				
				for(int[] mono : output) {
					if(Arrays.equals(mono, multiplied)) {
						output.remove(mono);
						skip = true;
						break;
					}							
				}
				
				if(!skip)
					output.add(multiplied);
			}
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
	
	//concat for two monomials
	public static int[] concatenate(int[] mono1, int[] mono2) {
		List<int[]> temp = new ArrayList<int[]>(2);
		temp.add(mono1);
		temp.add(mono2);
		return concatenate(temp);
	}
	
	//concatenates tensors term-wise
	//copied from DualSteenrod
	public static List<int[][]> concatenateTensors(List<int[][]> input1, List<int[][]> input2) {
		List<int[][]> output = new ArrayList<int[][]>();
		int[][] tensor1, tensor2, tempTensor;
		
		for(int i = 0; i < input1.size(); i++) {
			for(int j = 0; j < input2.size(); j++) {
				tensor1 = input1.get(i);
				tensor2 = input2.get(j);
				tempTensor = new int[2][];
				tempTensor[0] = concatenate(tensor1[0], tensor2[0]);
				tempTensor[1] = concatenate(tensor1[1], tensor2[1]);
				output.add(tempTensor);
			}
		}
		
		return output;
	}
	

	//INPUT: a sum of monomials OR a sum of tensors
	//OUTPUT: that same sum of monomials/tensors, but reduced mod 2, addition-wise
	//TODO: there should be a better way, probably with Elements, to make this much more concise
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
				currentCount = entryCounts.get(multiIntArrayToList((int[][]) input.get(i))); 
				
				//if nothing was there, change value to 1, otherwise increase the value by 1.
				newCount = (currentCount == null ? 1 : currentCount+1);
				
				entryCounts.put(multiIntArrayToList((int[][]) input.get(i)), newCount);
				//System.out.println("new count "  + newCount + " entry counts size " + entryCounts.size() + " " + Arrays.toString(input.get(i)));
			}
			
			Set<List<List<Integer>>> keys = entryCounts.keySet();
			Iterator<List<List<Integer>>> iter = keys.iterator();
			
			List<List<Integer>> tensor;
			
			while(iter.hasNext()) {
				tensor = iter.next();
				
				if((entryCounts.get(tensor).intValue() % 2) != 0)
					output.add(multiListToIntArray(tensor));
			}
			
			return output;
		}
		else if(input.get(0) instanceof int[]) {
			List<int[]> output = new ArrayList<int[]>();		
			HashMap<List<Integer>, Integer> entryCounts = new HashMap<List<Integer>, Integer>();
			Integer currentCount;
			int newCount;
			List<Integer> monomial;
			
			for(int i = 0; i < input.size(); i++) {
				monomial = intArrayToList((int[]) input.get(i));
				currentCount = entryCounts.get(monomial); 
				
				//if nothing was there, change value to 1, otherwise increase the value by 1.
				newCount = (currentCount == null ? 1 : currentCount+1);
				
				entryCounts.put(monomial, newCount);
			}
			
			Set<List<Integer>> keys = entryCounts.keySet();
			Iterator<List<Integer>> iter = keys.iterator();
			
			while(iter.hasNext()) {
				monomial = iter.next();
				
				//don't want to include empty arrays
				if(  ((entryCounts.get(monomial).intValue() % 2) != 0)  &&  monomial.size() > 0)
					output.add(listToIntArray(monomial));
			}
			
			return output;
		}
		
		return null;
	}
	
	public static boolean contains(int[] array, int k) {
		for(int i = 0; i < array.length; i++)
			if(array[i] == k)
				return true;
		return false;
	}
	
	//lists all ways to partition m into k powers of 2
	//returns empty List<List<Integer>> if there is no such way to partition m
	//listAsPowers is for listing the partitions as the powers, like n=3, vs the numbers themselves, like 2^n=8.
	//Jm prefers listing as powers. partition function itself and visual things prefer numbers themselves
	//TODO: this code can probably be made more efficient: only need to check m-2^n and m-2^(n-1)?
	public static List<List<Integer>> partition(int m, int k, boolean listAsPowers) {
		List<List<Integer>> partitions;
		
		if(m == 0 && k == 0) {
			partitions = new ArrayList<List<Integer>>();
			partitions.add(new ArrayList<Integer>());
			return partitions;
		}
		
		if(m == 0 || k == 0) 
			return new ArrayList<List<Integer>>();
		
		int maxDivider = 0;
		
		//find largest n such that 2^n <= m
		while(true) {
			if(Math.pow(2, maxDivider + 1) > m) 
				break;
			maxDivider++;
		}
		
		partitions = new ArrayList<List<Integer>>();
		
		for(int i = maxDivider; i >= 0; i--) {
			List<List<Integer>> lowerPartitions = partition(m - (int)Math.pow(2, i), k-1, listAsPowers);
			if(lowerPartitions == null)
				continue;
			
			for(int j = 0; j < lowerPartitions.size(); j++) {
				List<Integer> temp = lowerPartitions.get(j);
				
				//non-decreasing order
				if(temp.size() > 0 && temp.get(temp.size() - 1) > (listAsPowers ? i : (int) Math.pow(2, i)))
					continue;
				
				//listing as powers will just record the powers of 2 rather than the numbers themselves (eg 3 vs 8=2^3)
				if(listAsPowers)
					temp.add(i);
				else
					temp.add((int)Math.pow(2, i));
				partitions.add(temp);
			}
		}
		
		return partitions;
	}
	
	//default mode
	public static List<List<Integer>> partition(int m, int k) {
		return partition(m, k, false);
	}
	
	public static void printPartition(List<List<Integer>> input) {
		if(input.size() == 0 || input == null) {
			System.out.println("none");
			return;
		}
		for(int i = 0; i < input.size(); i++)
			System.out.println(input.get(i));
	}
	
	public static String convertJElementToTex(JElement j) {
		String jAsTex = "";
		
		//loop only has one iteration if j is a monomial
		for(int i = 0; i < j.length(); i++) {
			int[] monomial = j.getMono(i);
			jAsTex += "$";
			for(int k = 0; k < monomial.length; k+=2) {
				jAsTex += "x_{" + monomial[k] + "}";
				//if the power is 1, don't write x_i^1, just write x_i
				if(monomial[k+1] != 1)
					jAsTex += "^{" + monomial[k+1] + "}";
			}
			jAsTex += (i == j.length() - 1 ? "$" : "$ + ");
		}
		
		return jAsTex;
	}
	
	//apply P_t^s to element
	//right now written for P_2^1 on 2 elements
	public static GenericElement Pst(GenericElement element) { 
		GenericElement output = new GenericElement();
		output.setVars(element.getVars());
		
		int[] mono = element.getMono(0);		
		
		if(mono.length == 4) {
			int pow1 = mono[1];
			int pow2 = mono[3];
			for(int j = 0; j < 3; j++) {
				int coeff1 = choose(pow1, j).intValue() % 2;
				int coeff2 = choose(pow2, 2-j).intValue() % 2;
				System.out.println(coeff1 + " " + coeff2);
				if(coeff1 == 0 || coeff2 == 0)
					continue;
				
				int[] newMono = new int[4];
				newMono[0] = mono[0];
				newMono[1] = 6 - (3*j) + pow1;
				newMono[2] = mono[2];
				newMono[3] = 3*j + pow2;
				output.add(newMono);
			}
		}
		
		
		if(mono.length == 6) {
			int pow1 = mono[1];
			int pow2 = mono[3];
			int pow3 = mono[5];
			for(int j = 0; j < 6; j++) {
				double interPoly1 = Math.round(j*j*j*j*j/40.0 - 7*j*j*j*j/24.0 + 23*j*j*j/24.0 - 5*j*j/24.0 - 149*j/60.0 + 2);
				double interPoly2 = Math.round(7*j*j*j*j*j/40.0 - 9*j*j*j*j/4.0 + 247*j*j*j/24.0 - 79*j*j/4.0 + 203*j/15.0);
				double interPoly3 = Math.round(-j*j*j*j*j/5.0 + 61*j*j*j*j/24.0 - 45*j*j*j/4.0 + 479*j*j/24.0 - 221*j/20.0);
				//System.out.println(interPoly1 + " " + interPoly2 + " " + interPoly3);
				int coeff1 = choose(pow1, (int)interPoly1).intValue() % 2;
				int coeff2 = choose(pow2, (int)interPoly2).intValue() % 2;
				int coeff3 = choose(pow3, (int)interPoly3).intValue() % 2;
				System.out.println(coeff1 + " " + coeff2 + " " + coeff3);
				if(coeff1 == 0 || coeff2 == 0 || coeff3 == 0)
					continue;
				
				
				
				int[] newMono = new int[6];
				newMono[0] = mono[0];
				newMono[1] = 3*((int)interPoly1) + pow1;
				newMono[2] = mono[2];
				newMono[3] = 3*((int)interPoly2) + pow2;
				newMono[4] = mono[4];
				newMono[5] = 3*((int)interPoly3) + pow3;
				
				output.add(newMono);
			}
		}
		
		return output;
	}
	
	/* Elements are possibly already sums...
	 * 
	 * public static Element multiplySums(Element elem1, Element elem2) {
		Element product = new Element();
		for(int i = 0; i < elem1.length(); i++) {
			for(int j = 0; j < elem2.length() j++) {
				elem1.multiply(elem2);
			}
		}
	}*/
	
}