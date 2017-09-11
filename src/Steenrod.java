import java.math.*;
import java.util.*;


//TODO the format here is "2 3 + 5 21 5 + 5" etc, NOT in list form, change everything to SteenrodElement



//Adem relations will only give a sum as Serre-Cartan basis elements, but will NOT break those into indecomposables
//this may or may not be important later...


//if you want all elements in a certain dimension, you need to run over all partitions of that dimension

public class Steenrod implements Algebra {
	
	public static final String ZERO = "zero";
	
	//the input should be in the form "# # + # # # + #" etc.
	//output is in Serre-Cartan basis
	//the output will have lots of redundancies and "+ zero", so run cleanup if this matters
	public static String writeAsBasis(String input) {
		if(input.equals("") || input.equals(ZERO))
			return input;
		
		//if the input has +'s in it, then split it up and treat each piece separately
		if(input.contains("+")) {
			String[] split = input.split(" [+] ");
			String total = new String(writeAsBasis(split[0]));
			for(int i = 1; i <= split.length-1; i++) {
				total += " + " + writeAsBasis(split[i]);
			}
			return total;
		}
		
		String[] powers = input.split(" ");
		
		if(powers.length == 1)
			return input;
		
		int ademI = 0, ademJ = 0;
		
		//try to find inadmissible pairs, starting from the rightmost pair
		for(int i = 0; i <= powers.length-2; i++) {
			ademI = Integer.parseInt(powers[powers.length-2-i]);
			ademJ = Integer.parseInt(powers[powers.length-1-i]);
			
			//if the pair is inadmissible, run the adem relations and return the basis on a new string
			if(!(ademI >= 2*ademJ)) {
				String adem = adem(ademI, ademJ);
				
				if(adem.equals(ZERO))
					return ZERO;
				
				String[] postAdemList = adem.split(" [+] ");
				
				//finalString will be the result of distributing the adem rels: # # # (adem rels on ademI ademJ) # # #
				String finalString = "";
				String beforeAdem, afterAdem;
				
				//if ademI is the first square in the string, there is no "before" relations
				//otherwise, beforeRel should look like "# # # " (space after last square if beforeRel is not empty). 
				//the last # is right before ademI (which is at length-2-i)
				if((powers.length-2-i) == 0)
					beforeAdem = "";
				else {
					beforeAdem = powers[0] + " ";
					for(int j = 1; j < powers.length-2-i; j++) {
						beforeAdem += powers[j] + " ";
					}
				}
				
				//now determine afterAdem, what comes after the adem relations. include a space before if afterAdem is not empty.
				//if ademJ is the last square, there is no "after". otherwise, go from one to the right of ademJ until the end of powers.
				if(i == 0) 
					afterAdem = "";
				else {
					afterAdem = " " + powers[powers.length-i];
					for(int j = powers.length-i+1; j < powers.length; j++) {
						afterAdem += " " + powers[j];
					}
				}
					
				//now distribute the adem relations
				/* this common theme of first declaring a string to be the first index outside of the loop is because there is an
				   annoying issue of a "hanging" space or plus, either on the left or the right */
				finalString = beforeAdem + postAdemList[0] + afterAdem;
				
				for(int j = 1; j < postAdemList.length; j++) {
					finalString += " + " + beforeAdem + postAdemList[j] + afterAdem;
				}
				
				return writeAsBasis(finalString);
			}	
		}
		
		//if the input made it through the above loop, then it is in admissible form and has no +
		//first get rid of possible 0s at the end (if it's admissible, they have to be at the end)
		while(input.contains(" 0")) 
			input = input.substring(0, input.lastIndexOf('0') - 1);
		return input;
	}
	
	//input: # # + # # # + #, etc
	//NOTE: this is slightly different than just running a reduceMod2 after converting input to a sum, because
	//      input could have "zero" floating in it somewhere! need to fix somewhere else.
	public static String cleanup(String input) {
		if(input==null) {
			return input;
		}
		
		HashMap<String, Integer> entryCounts = new HashMap<String, Integer>();
		String[] inputList = input.split(" [+] ");
		
		for(int i = 0; i < inputList.length; i++) {
			Integer currentCount = entryCounts.get(inputList[i]);
			//if nothing was there, change value to 1, otherwise increase the value by 1.
			int newCount = (currentCount == null ? 1 : currentCount+1);
			entryCounts.put(inputList[i], newCount);
		}
		
		Set<String> keys = entryCounts.keySet();
		Iterator<String> iter = keys.iterator();
		String cleanString = "";
		String entry;
		
		while(iter.hasNext()) {
			entry = iter.next();
			if((entryCounts.get(entry).intValue() % 2) == 0 || entry.equals(ZERO))
				entry = "";
			
			//if this is the first entry or if it's empty, don't put a plus
			cleanString += ((cleanString.length() > 0  && !entry.equals("")) ? " + " : "") + entry;
		}
		
		if(cleanString.equals(""))
			return ZERO;
		else
			return cleanString;
	}
	
	public static void listDimensionalDoubles(int dimension) {
		int j;
		for(int i = 0; i<= dimension; i++) {
			j = dimension - i;
			System.out.println(i + " " + j + " = " + adem(i,j));
			//System.out.println(j + " " + i + " = " + Adem(i,j));
		}
	}
	
	public static void printIndecomposables(int n) {
		for(int i=0; i<=n; i++) {
			System.out.println((int)Math.pow(2,i));
		}
	}
	
	//input in form #
	//TODO: make this work with arbitrary length (note the coproduct is a ring hom: see Milnor)
	//TODO: needs to return a tensor!
	//TODO: can change to SteenrodElement[] (length 2)
	@SuppressWarnings("unchecked")
	public static List<int[][]> coproduct(SteenrodElement sq) {
		List<int[][]> output = new ArrayList<int[][]>();
		
		//sq is a sum
		if(sq.length() > 1) {
			for(int i = 0; i < sq.length(); i++) {
				output.addAll(coproduct((SteenrodElement) sq.get(i)));
			}
		}
		//sq is a "monomial" steenrod element
		else {
			int[] mono = sq.getMono(0);
			
			if(mono.length == 1) {
				int square = mono[0];
				String coprodAsString = "0" + " x " + square;
				
				for(int j = 1; j <= square; j++)
					coprodAsString += " + " + j + " x " + (square-j);
				
				output = Tools.parseTensorSumFromString(coprodAsString);
			}
			else {
				output = Tools.concatenateTensors(coproduct(new SteenrodElement("" + mono[0])), 
						coproduct(new SteenrodElement("" + mono[1])));
				for(int j = 1; j < mono.length; j++) {
					output = Tools.concatenateTensors(output, coproduct(new SteenrodElement("" + mono[j])));
				}
			}
		}
				
		output = ademTensors(output);
		output = (List<int[][]>) Tools.reduceMod2(output);
		return output;
	}
	
	//run the Adem relations on Sq^i Sq^j. a relation that equals 0 will return zero since 0 is really Sq^0 = 1. don't run if i=j=0...
	//note this will (or should...) never return something of the form "# # # + zero".
	//TODO: accept steenrodelement, that way you can send this things like "Q1 2"
	public static String adem(int i, int j) {
		String relations = "";
		BigInteger binomial;
		
		//already in basis form
		if(i >= 2*j) {
			if(j==0)
				return Integer.toString(i);
			return i + " " + j;
		}
		
		//see Wood's paper example 1.11. doesn't seem to affect speed much.
		if(i == (2*j - 1))
			return ZERO;
			
		
		for(int k=0; k<=(int)Math.floor(i/2); k++) {				
			binomial = Tools.choose(j-k-1, i-2*k).mod(BigInteger.valueOf(2));
			
			//if the binomial is 1 mod 2
			if(binomial.compareTo(BigInteger.valueOf(1)) == 0) {
				if(!relations.equals(""))
					relations += " + ";
				
				//these checks are so that e.g. 5 0 or 0 5 doesn't appear. instead, 5 does.
				if(k == 0) 
					relations += (i+j-k);
				else if((i+j-k) == 0) 
					relations += k;
				else 
					relations += (i+j-k) + " " + k;
			}
		}
		if(relations.equals(""))
			relations = ZERO;
		
		return relations;
	}
	
	//apply adem relations to a sum of tensors.
	public static List<int[][]> ademTensors(List<int[][]> input) {
		List<int[][]> output = new ArrayList<int[][]>();
		List<int[][]> reducedOnLeft = new ArrayList<int[][]>();
		
		for(int[][] tensor : input) {
			SteenrodElement left = new SteenrodElement(tensor[0]);
			int[] right = tensor[1];
			left.adem();
			
			for(int i = 0; i < left.length(); i++) {
				reducedOnLeft.add(new int[][] {left.getMono(i), right} );
			}
		}
		
		for(int[][] tensor : reducedOnLeft) {
			int[] left = tensor[0];
			SteenrodElement right = new SteenrodElement(tensor[1]);
			right.adem();
			
			for(int i = 0; i < right.length(); i++) {
				output.add(new int[][] {left, right.getMono(i)} );
			}
		}
		
		return output;
	}

	
	//INPUT: sum of monos # # + # # # etc
	//OUTPUT: total excess of the sum (eg schwartz pg 23-24)
	public static int excess(String input) {
		List<int[]> monos = Tools.parseSumFromString(input);
		int excess = 0;
		for(int[] mono : monos) {
			for(int i = 0; i < mono.length; i++)
				excess += (i == 0 ? mono[i] : mono[i]*(-1));
		}
		
		return excess;
	}
	
	@Override
	public String basisType() {
		return Algebra.ADEM;
	}

	@Override
	public boolean isInfiniteDimensional() {
		return true;
	}

	@Override
	public boolean hasRelations() {
		return true;
	}

	@Override
	public boolean checkHomogeneous(List<int[]> input) {
		// TODO Auto-generated method stub
		return false;
	}
}