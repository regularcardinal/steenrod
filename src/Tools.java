import java.math.BigInteger;
import java.util.*;

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
			String[] tensorSplit = tensor.split(" X ");
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
}
