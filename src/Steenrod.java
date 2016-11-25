import java.math.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;




//TODO the format here is "2 3 + 5 21 5 + 5" etc, NOT in list form



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
			ArrayList<String> split = splitByPlus(input);
			String total = new String(writeAsBasis(split.get(0)));
			for(int i = 1; i <= split.size()-1; i++) {
				total += " + " + writeAsBasis(split.get(i));
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
				String adem = Adem(ademI, ademJ);
				
				if(adem.equals(ZERO))
					return ZERO;
				
				ArrayList<String> postAdemList = splitByPlus(adem);
				
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
				finalString = beforeAdem + postAdemList.get(0) + afterAdem;
				
				for(int j = 1; j < postAdemList.size(); j++) {
					finalString += " + " + beforeAdem + postAdemList.get(j) + afterAdem;
				}
				
				return writeAsBasis(finalString);
			}	
		}
		
		//if the input made it through the above loop, then it is in admissible form and has no +
		//first get rid of possible 0s at the end (if it's admissible, they have to be at the end)
		while(input.contains("0")) 
			input = input.substring(0, input.lastIndexOf('0') - 1);
		return input;
	}
	
	//NOTE this doesn't really need to exist. there is a function split in String.
	//input is a string with '+' characters in it. return an ArrayList that parses these into list elements
	//MUST have a space before and after each +
	//it may be better computationally to first count how many +'s there are, and then loop based on that count
	public static ArrayList<String> splitByPlus(String input) {		
		ArrayList<String> splitList = new ArrayList<String>();
		if(!input.contains("+"))
			splitList.add(input);
		else {
			String truncation = new String(input);
			int plusPos;
			
			while(truncation.contains("+")) {
				plusPos = truncation.indexOf('+');
				//the -1 is to get rid of the space to the left of the +
				splitList.add(truncation.substring(0, plusPos-1));
				truncation = truncation.substring(plusPos+2);
			}
			//there will be one left over after the last +
			splitList.add(truncation);
		}
		
		return splitList;
	}
	
	//input: # # + # # # + #, etc
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
			System.out.println(i + " " + j + " = " + Adem(i,j));
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
	public static String coproduct(String input) {
		int square = Integer.parseInt(input);
		String coprod = "" + square;
		
		for(int i = 1; i <= square; i++)
			coprod += " + " + i + " " + (square-i);
		
		return cleanup(writeAsBasis(coprod));
	}
	
	//run the Adem relations on Sq^i Sq^j. a relation that equals 0 will return zero since 0 is really Sq^0 = 1. don't run if i=j=0...
	//note this will (or should...) never return something of the form "# # # + zero".
	public static String Adem(int i, int j) {
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