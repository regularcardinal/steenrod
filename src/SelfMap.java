import java.io.IOException;
import java.util.*;

public class SelfMap {
	public static Map<List<Integer>, List<int[][]>> coproductData = new HashMap<List<Integer>, List<int[][]>>();
	private static Scanner reader;
	private static final String NO_S_MAP = "No s map exists.";
	private static final String NO_J_MAP = "No j map exists.";
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		long start = 0;
		long end = 0;
		reader = new Scanner(System.in);
		List<?> lastOutput = null;
		Function sMap = null, jMap = null;
		DualAn dualAn = new DualAn(0);
		
		/*
		System.out.println(Arrays.toString(DualSteenrod.applyRelations(new int[]{1,8}, DualAn.getRelations(2))));
		System.out.println(Arrays.toString(DualSteenrod.remainder(new int[]{1,8}, DualAn.getRelations(2))));
		System.out.println(new MilnorElement(new int[0]).getAsList().size());
		System.out.println(Tools.intArrayToList(new int[0]).size());
		*/
		
		while(true) {
			System.out.print("Enter a command: ");
			 
			String str = reader.nextLine();
			String keyWord = (str.indexOf(" ") == -1) ? str : str.substring(0, str.indexOf(" "));
			
			if(keyWord.equals("reduce") || keyWord.equals("coprod") || keyWord.equals("remove")) {
				String next = str.substring(str.indexOf(" ") + 1);
				
				if(next.equals("last")) {
					if(keyWord.equals("reduce"))
						lastOutput = DualSteenrod.reduceMod2(lastOutput);
					if(keyWord.equals("remove"))
						DualSteenrod.removePrimitives((List<int[][]>) lastOutput);
				}
				else {							
					if(keyWord.equals("reduce"))
						lastOutput = DualSteenrod.reduceMod2(Tools.parseSumFromString(next));
					if(keyWord.equals("coprod")) {
						start = System.nanoTime();
						lastOutput = DualSteenrod.coproduct(Tools.parseSumFromString(next));
						end = System.nanoTime();
					}
				}
				
				System.out.println("(" + ((double)(end-start))/1000000 + " ms) " + Tools.sumToString(lastOutput));
			}
			else if(keyWord.equals("sMap")) {
				jMap = null;
				
				System.out.print("Enter dimension for An: ");
				int bigDim = Integer.parseInt(reader.nextLine());
				dualAn = new DualAn(bigDim);
				sMap = dualAn.generateSMap();
				Map<List<Integer>, MilnorElement> map;
				
				Integer[] keys = dualAn.sMapDimensions();
				
				DualSteenrod AmodAn = new DualSteenrod(DualSteenrod.getDualAModAnGenerators(bigDim));
				//strictly speaking, this may have dimensions that don't appear in sMap, but it won't matter
				//I don't think that can happen anyway; A(n)* has elements in every dimension(?)
				Map<Integer, List<MilnorElement>> AmodAnMap = AmodAn.getMonomialsAtOrBelow(keys[keys.length-1]);
				
				while(true) {
					System.out.print("Enter dimension to edit (type done when finished): ");
					String next = reader.nextLine();
					
					if(next.equals("done"))
						break;
					
					int dim = 0;
					
					try {
						dim = Integer.parseInt(next);
					}
					catch(NumberFormatException e) {
						System.err.println("Not a number");
						continue;
					}
					
					if(!Arrays.asList(keys).contains(dim)) {
						System.err.println("Invalid dimension");
						continue;
					}
					
					System.out.println("Editing dimension " + dim + ".");
					map = sMap.getMapByDimension(dim);
					
					for (Map.Entry<List<Integer>, MilnorElement> entry : map.entrySet()) {
						System.out.print("Enter target for " + entry.getKey() + " (list of choices: " + AmodAnMap.get(dim) + "; Enter 0 for zero): ");
						
						next = reader.nextLine();
						String[] split = next.split(" "); //TODO this only accepts monomials, not sums. easy to fix later
						int[] mono = new int[split.length];
						for(int i = 0; i < split.length; i++)
							mono[i] = Integer.parseInt(split[i]);
						
						MilnorElement target;
						if(mono[0] == 0)
							target = new MilnorElement(0);
						else if(Tools.milnorDimension(Tools.listToIntArray(entry.getKey())) != Tools.milnorDimension(mono)) {
							System.err.println("Dimension mismatch!");
							target = new MilnorElement(0);
						}
						else
							target = new MilnorElement(mono);
						
						sMap.set(Tools.listToIntArray(entry.getKey()), target);
						System.out.println("ADDED " + entry.getKey() + " -> " + target);
					}
				}
				
				System.out.println("S map generated. All dimensions not edited are set to 0.");
			}
			else if(keyWord.equals("jMap")) {
				if(sMap == null)
					System.err.println(NO_S_MAP);
				else {
					jMap = dualAn.generateJMap(sMap);
					System.out.println("J map generated using last s map.");
				}
			}
			else if(keyWord.equals("roth")) {
				if(jMap == null)
					System.err.println(NO_J_MAP);
				else
					System.out.println(dualAn.checkRoth(sMap, jMap));
			}
			else if(keyWord.equals("print")) {
				String next = (str.indexOf(" ") == -1) ? "" : str.substring(str.indexOf(" ") + 1);
				if(next.equals("sMap"))
					System.out.println( (sMap!=null) ? sMap : NO_S_MAP );
				else if(next.equals("jMap"))
					System.out.println( (jMap!=null) ? jMap : NO_J_MAP );	
			}
			else if(keyWord.equals("sBar")) {
				if(sMap == null)
					System.err.println(NO_S_MAP);
				else {
					String next = (str.indexOf(" ") == -1) ? "" : str.substring(str.indexOf(" ") + 1);
					
					if(next.equals("last")) {
						if(lastOutput.get(0) instanceof int[][])
							System.out.println(dualAn.sBarTensor((List<int[][]>) lastOutput, sMap));
						if(lastOutput.get(0) instanceof int[])
							System.out.println(dualAn.sBar(new MilnorElement(lastOutput), sMap));
					}
					else if(next.contains("x")) 
						System.out.println(dualAn.sBarTensor(Tools.parseTensorSumFromString(next), sMap));
					else 
						System.out.println(dualAn.sBar(new MilnorElement(Tools.parseSumFromString(next)), sMap));
				}
			}
			else if(keyWord.equals("j")) {
				if(jMap == null)
					System.err.println(NO_J_MAP);
				else {
					String next = str.substring(str.indexOf(" ") + 1);
					System.out.println(jMap.get(Tools.parseSumFromString(next).get(0)));
				}
			}
			else
				System.err.println("I don't understand.");
		}
		
		
		 //System.out.println(cleanup(writeAsBasis("31 30")));
		 
		 /*
		 Function function = new Function();
		 MilnorElement temp = new MilnorElement(new int[]{1,1});
		 temp.add(new int[]{1,1});
		 function.set(new int[]{1,1}, temp);
		 System.out.println(function.get(new int[]{1,1}));
		 System.out.println(function.get(new int[0]));
		 System.out.println(function.get(null));
		 
		 HashMap<List<Integer>, int[]> test = new HashMap<List<Integer>, int[]>();
		 test.put(null, null);
		 System.out.println(test.size());
		 */
		 
		 
		 /*
		 DualAn dualA2 = new DualAn(2);
		 //dualA2.printAllData();
		 //dualA2.printMonomialsAtOrBelow(DualSteenrod.milnorDimension(dualA2.topClass()));
		 //dualA2.printMonomialsAtOrBelow(5);
		 
		 DualSteenrod A_mod_A2 = new DualSteenrod(null);
		 A_mod_A2.setGenerators(DualSteenrod.getDualAModAnGenerators(2));
		 
		 
		 System.out.println(sMaps(dualA2, A_mod_A2));
		 
		 int topClassDim = DualSteenrod.milnorDimension(dualA2.topClass());
		 Integer[] keys = Tools.keysToSortedArray(A_mod_A2.getMonomialsAtOrBelow(topClassDim));
		 Map<Integer, List<int[]>> filteredMonomials = dualA2.getMonomialsByFilter(keys);
		 dualA2.printMonomials(filteredMonomials);
		 
		 dualA2.printMonomialsAtOrBelow(topClassDim);
		 A_mod_A2.printMonomialsAtOrBelow(topClassDim);
		 */
		 
		 
		 /*
		 System.out.println(Arrays.toString(Tools.dynamicToFixedForm(DualSteenrod.applyRelations(new int[]{1,2,1,1,2,1,5,6,5,7,8,1}), 8)));
		 System.out.println(Arrays.toString(DualSteenrod.applyRelations(new int[]{1,2, 2,0, 3, 7, 19, 1})));
		 System.out.println(Arrays.toString(Tools.fixedToDynamicForm(new int[]{3, 0, 0, 5, 0, 0, 3, 0, 0, 0, 0, 13})));
		 
		 DualAn dualAn = new DualAn(2);
		 int[] test = new int[]{1,9, 2, 5, 3, 5};
		 System.out.println(Arrays.toString(DualSteenrod.applyRelations(test, dualAn.getRelations())));
		 System.out.println(Arrays.toString(DualSteenrod.remainder(test, dualAn.getRelations())));
		 
		 int[][] test2 = new int[1][1];
		 System.out.println(instanceOfTest(test2));
		 */
		 
		 /*
		 List<int[]> tempList = new ArrayList<int[]>();
		 tempList.add(new int[0]);
		 System.out.println(Arrays.toString(tempList.get(0)));
		 tempList = (List<int[]>) DualSteenrod.reduceMod2(tempList);
		 
		 for(int i = 0; i<tempList.size(); i++) 
			 System.out.println(Arrays.toString(tempList.get(i)));
		 */
		 
		
		 
		// System.out.println(Arrays.toString(DualSteenrod.cleanup(new int[]{1, 5, 2, 1, 1, -1}, dualA2.getRelations())));
		//System.out.println(dualA2.getRelations().get(4));	
		
		//long end = System.nanoTime();
		//System.out.println("time: " + ((double)(end-start))/1000000 + " ms");
	}
	
	public static boolean instanceOfTest(Object o) {
		return (o instanceof int[]);
	}
}