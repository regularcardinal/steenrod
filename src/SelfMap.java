import java.io.IOException;
import java.util.*;

public class SelfMap {
	public static Map<List<Integer>, List<int[][]>> coproductData = new HashMap<List<Integer>, List<int[][]>>();
	private static Scanner reader;
	private static final String NO_S_MAP = "No s map exists.";
	private static final String NO_J_MAP = "No j map exists.";
	private static final int ALL = 0;
	private static final int FILL = 1;
	private static final int PICK = 2;
	private static Function savedSMap = null;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		long start = 0;
		long end = 0;
		reader = new Scanner(System.in);
		List<?> lastOutput = null;
		Function sMap = null, jMap = null;
		int savedBigDim = 0;
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
				
				System.out.print("Enter n for A(n): ");
				int bigDim = Integer.parseInt(reader.nextLine());
				dualAn = new DualAn(bigDim);
				
				sMap = dualAn.generateSMap();
				Integer[] sMapDimensions = dualAn.sMapDimensions();
				
				DualSteenrod AmodAn = new DualSteenrod(DualSteenrod.getDualAModAnGenerators(bigDim));
				//strictly speaking, this may have dimensions that don't appear in sMap, but it won't matter
				//I don't think that can happen anyway; A(n)* has elements in every dimension(?)
				Map<Integer, List<MilnorElement>> AmodAnMap = AmodAn.getMonomialsAtOrBelow(sMapDimensions[sMapDimensions.length-1]);
				
				if(str.indexOf(" ") != -1 && str.substring(str.indexOf(" ")+1).equals("search")) {
					search(dualAn, AmodAnMap);
					sMap = savedSMap;
					continue;
				}
				
				while(true) {					
					System.out.print("Enter dimension to edit (keywords: all, fill, list, pick, check, save, load, done): ");
					
					String next = reader.nextLine();
					int fillIndex = 0;
					List<Integer> pickMono = null;
					int mode = 0;
					
					//format for key words: (dim) (keyword) (index/monomial)
					if(next.contains("done"))
						break;
					else if(next.contains("list")) {
						System.out.println("Dimensions: " + Arrays.toString(sMapDimensions));
						continue;
					}
					else if(next.contains("save")) {
						savedSMap = new Function(sMap);
						savedBigDim = bigDim;
						System.out.println("Saved s map.");
						continue;
					}
					else if(next.contains("load")) {
						if(savedSMap == null)
							System.out.println("Can't load: no s map saved.");
						else if(savedBigDim != bigDim)
							System.out.println("Can't load: wrong dimension n.");
						else {
							sMap = new Function(savedSMap);
							System.out.println("Loaded s map.");
						}
							
						continue;
					}
					else if(next.contains("check")) {
						//TODO check the value of a given dim/mono
					}
					else if(next.contains("fill")) {
						fillIndex = Integer.parseInt(next.substring(next.lastIndexOf(" ") + 1));
						mode = FILL;
					}
					else if(next.contains("pick")) {
						//example format: 8 pick 1 5 2 1
						pickMono = Tools.intArrayToList(Tools.parseSumFromString(next.substring(next.indexOf("k") + 2)).get(0)); 
						mode = PICK;
					}
					else if(next.contains("all")) 
						mode = ALL;
					else
						continue;
					
					next = next.substring(0, next.indexOf(" "));
					int dim = 0;
					
					try {
						dim = Integer.parseInt(next);
					}
					catch(NumberFormatException e) {
						System.err.println("Not a number.");
						continue;
					}
					
					if(!Arrays.asList(sMapDimensions).contains(dim)) {
						System.out.println("Invalid dimension.");
						continue;
					}
					
					System.out.println("Editing dimension " + dim + ".");
					Map<List<Integer>, MilnorElement> map = sMap.getMapByDimension(dim);
					
					if(mode == PICK) {
						if(!map.containsKey(pickMono) || pickMono == null) {
							System.out.println(pickMono + " is not a valid monomial.");
							continue;
						}
					}
					
					for (Map.Entry<List<Integer>, MilnorElement> entry : map.entrySet()) {
						MilnorElement target = new MilnorElement(0);
						
						if(mode == ALL || mode == PICK) {
							if( (mode == PICK) && (!entry.getKey().equals(pickMono)) )
								continue;
							
							System.out.print("Enter target for " + entry.getKey() + " (list of choices: " + AmodAnMap.get(dim) + "; Enter 0 for zero): ");
							
							next = reader.nextLine();
							String[] split = next.split(" "); //TODO this only accepts monomials, not sums. easy to fix later
							int[] userMono = new int[split.length];
							for(int i = 0; i < split.length; i++)
								userMono[i] = Integer.parseInt(split[i]);
							
							if(userMono[0] == 0)
								target = new MilnorElement(0);
							else if(Tools.milnorDimension(Tools.listToIntArray(entry.getKey())) != Tools.milnorDimension(userMono)) {
								System.out.println("Dimension mismatch!");
								target = new MilnorElement(0);
							}
							else
								target = new MilnorElement(userMono);			
							
						}
						else if(mode == FILL) {
							if(fillIndex >= AmodAnMap.get(dim).size()) {
								System.out.println("Fill index (" + fillIndex + ") too large, setting to max");
								fillIndex = AmodAnMap.get(dim).size() - 1;
							}
							
							target = AmodAnMap.get(dim).get(fillIndex);				
						}
						
						sMap.set(Tools.listToIntArray(entry.getKey()), target);
						System.out.println("ADDED " + entry.getKey() + " -> " + target);
					}
				}
				
				System.out.println("S map generated. All dimensions not edited are set to 0.");
			}
			else if(keyWord.equals("jMap")) {
				if(sMap == null)
					System.out.println(NO_S_MAP);
				else {
					start = System.nanoTime();
					jMap = dualAn.generateJMap(sMap);
					System.out.println("J map generated using last s map. (" + ((double)(System.nanoTime()-start))/1000000 + " ms)");
				}
			}
			else if(keyWord.equals("roth")) {
				if(jMap == null)
					System.out.println(NO_J_MAP);
				else {
					start = System.nanoTime();
					System.out.println(dualAn.checkRoth(sMap, jMap) + " (" + ((double)(System.nanoTime()-start))/1000000 + " ms)");
				}
			}
			else if(keyWord.equals("print")) {
				String next = (str.indexOf(" ") == -1) ? "" : str.substring(str.indexOf(" ") + 1);
				if(next.equals("sMap"))
					System.out.println( (sMap!=null) ? sMap : NO_S_MAP );
				else if(next.equals("jMap"))
					System.out.println( (jMap!=null) ? jMap : NO_J_MAP );
				else if(next.equals("saved"))
					System.out.println( (savedSMap!=null) ? savedSMap : "Nothing saved." );
			}
			else if(keyWord.equals("sBar")) {
				if(sMap == null)
					System.out.println(NO_S_MAP);
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
					System.out.println(NO_J_MAP);
				else {
					String next = str.substring(str.indexOf(" ") + 1);
					System.out.println(jMap.get(Tools.parseSumFromString(next).get(0)));
				}
			}
			else if(keyWord.equals("quit"))
				break;
			else
				System.out.println("I don't understand.");
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
	
	public static void search(DualAn dualAn, Map<Integer, List<MilnorElement>> AmodAnMap) {
		int count = 1;
		long initial = System.nanoTime();
		long start = initial;
		Integer[] sMapDimensions = dualAn.sMapDimensions();
		
		while(true) {
			System.out.print("Searching " + count + "... ");
			start = System.nanoTime();
			
			Function sMap = dualAn.generateSMap();
			
			for(Integer dim : sMapDimensions) {
				Map<List<Integer>, MilnorElement> sMapByDimension = sMap.getMapByDimension(dim);
				
				int choices = AmodAnMap.get(dim).size(); //at least 1
				
				for(List<Integer> mono : sMapByDimension.keySet()) {
					int random = (int) Math.round((double)choices * Math.random());
					if(random == choices)
						sMap.set(Tools.listToIntArray(mono), new MilnorElement(0));
					else
						sMap.set(Tools.listToIntArray(mono), new MilnorElement(AmodAnMap.get(dim).get(random).getAsList()));
				}
			}
			
			Function jMap = dualAn.generateJMap(sMap);
			
			//System.out.print( "Checking Roth... " );
			
			if(dualAn.checkRoth(sMap, jMap)) {
				System.out.println("Found and saved!");
				savedSMap = new Function(sMap);
				return;
			}
			
			System.out.println(((double)(System.nanoTime()-initial))/1000000 + "ms (+" + ((double)(System.nanoTime()-start))/1000000 + " ms)" );
			count++;
		}
	}
	
	public static boolean instanceOfTest(Object o) {
		return (o instanceof int[]);
	}
}