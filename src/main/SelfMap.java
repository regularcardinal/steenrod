package main;
import java.util.*;

import elements.MilnorElement;
import elements.SteenrodElement;

/* NOTE: The original code used (and a lot of it still uses) an int[] to represent Milnor elements in the 
 *       dual Steenrod algebra. There are multiple better alternatives. Some (a lot?) of it has been converted
 *       now to MilnorElement, which internally holds this int[], and can do operations on it. There are still
 *       better alternatives. See the header note of DualSteenrod.java for more info.
 *       
 *       Also, other things like JElement or GenericElement use a similar scheme. 
 *       SteenrodElement is different.
 */

public class SelfMap {
	private enum Keyword {
		REDUCE,
		COPROD,
		REMOVE,
		SMAP,
		JMAP,
		VARIATE,
		ROTH,
		PRINT,
		SBAR,
		J,
		ADEM,
		EXCESS,
		COPROD_STEEN,
		ACT,
		TEXJ,
		TEX,
		HELP,
		QUIT;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	//TODO: public because called in DualSteenrod. bad! (shouldn't have access)
	public static Map<List<Integer>, List<int[][]>> coproductData = new HashMap<>();
	private static Function savedSMap = null;

	public static void main(String[] args) {
		userLoop();
	}

	private static void userLoop() {
		final String NO_S_MAP = "No s map exists.";
		final String NO_J_MAP = "No j map exists.";
		final int ALL = 0;
		final int FILL = 1;
		final int PICK = 2;

		Scanner reader;
		long start = 0;
		long end = 0;
		reader = new Scanner(System.in);
		//last output generated by user
		List<?> lastOutput = null;
		Function sMap = null, jMap = null;
		int savedBigDim = 0;
		DualAn dualAn = new DualAn(0);

		//user loop
		scanner:
		while(true) {
			System.out.print("Enter a command (type 'help' for help): ");

			String str = reader.nextLine();
			String key = (!str.contains(" ")) ? str : str.substring(0, str.indexOf(" "));
			Keyword keyword;

			try {
				keyword = Keyword.valueOf(key.toUpperCase());
			}
			catch(IllegalArgumentException e) {
				System.out.println("Not a valid command.");
				continue;
			}

			long timer = System.nanoTime();

			switch (keyword) {
				case HELP: {
					System.out.println(Arrays.toString(Keyword.values()));
				}
				case REDUCE:
				case COPROD:
				case REMOVE: {
					String next = str.substring(str.indexOf(" ") + 1);
					start = System.nanoTime();

					//TODO: clean this up: fewer lines
					if (next.equals("last")) {
						if (keyword == Keyword.REDUCE)
							lastOutput = Tools.reduceMod2(lastOutput);
						if (keyword == Keyword.REMOVE)
							DualSteenrod.removePrimitives((List<int[][]>) lastOutput);
						if (keyword == Keyword.COPROD)
							lastOutput = DualSteenrod.coproduct((List<int[]>) lastOutput);

					} else {
						if (keyword == Keyword.REDUCE)
							lastOutput = Tools.reduceMod2(Tools.parseSumFromString(next));
						if (keyword == Keyword.COPROD)
							lastOutput = DualSteenrod.coproduct(Tools.parseSumFromString(next));
					}
					end = System.nanoTime();
					System.out.println("(" + ((double) (end - start)) / 1000000 + " ms) " + Tools.sumToString(lastOutput));
					break;
				}
				case SMAP:
					jMap = null;

					System.out.print("Enter n for A(n): ");
					int bigDim = Integer.parseInt(reader.nextLine());
					dualAn = new DualAn(bigDim);

					sMap = dualAn.generateSMap();
					Integer[] sMapDimensions = dualAn.sMapDimensions();

					DualSteenrod AmodAn = new DualSteenrod(DualSteenrod.getDualAModAnGenerators(bigDim));
					//strictly speaking, this may have dimensions that don't appear in sMap, but it won't matter
					//I don't think that can happen anyway; A(n)* has elements in every dimension(?)
					Map<Integer, List<MilnorElement>> AmodAnMap = AmodAn.getMonomialsAtOrBelow(sMapDimensions[sMapDimensions.length - 1]);

					if (str.contains(" ") && str.substring(str.indexOf(" ") + 1).equals("search")) {
						search(dualAn, AmodAnMap);
						sMap = savedSMap;
						continue;
					}

					while (true) {
						System.out.print("Enter dimension to edit (keywords: all, fill, list, pick, check, save, load, done): ");

						String next = reader.nextLine();
						int fillIndex = 0;
						List<Integer> pickMono = null;
						int mode = 0;

						//format for key words: (dim) (keyword) (index/monomial)
						if (next.contains("done"))
							break;
						else if (next.contains("list")) {
							System.out.println("Dimensions: " + Arrays.toString(sMapDimensions));
							continue;
						} else if (next.contains("save")) {
							savedSMap = new Function(sMap);
							savedBigDim = bigDim;
							System.out.println("Saved s map.");
							continue;
						} else if (next.contains("load")) {
							if (savedSMap == null)
								System.out.println("Can't load: no s map saved.");
							else if (savedBigDim != bigDim)
								System.out.println("Can't load: wrong dimension n.");
							else {
								sMap = new Function(savedSMap);
								System.out.println("Loaded s map.");
							}

							continue;
						} else if (next.contains("check")) {
							//TODO check the value of a given dim/mono
						} else if (next.contains("fill")) {
							fillIndex = Integer.parseInt(next.substring(next.lastIndexOf(" ") + 1));
							mode = FILL;
						} else if (next.contains("pick")) {
							//example format: 8 pick 1 5 2 1
							pickMono = Tools.intArrayToList(Tools.parseSumFromString(next.substring(next.indexOf("k") + 2)).get(0));
							mode = PICK;
						} else if (next.contains("all"))
							mode = ALL;
						else
							continue;

						next = next.substring(0, next.indexOf(" "));
						int dim = 0;

						try {
							dim = Integer.parseInt(next);
						} catch (NumberFormatException e) {
							System.err.println("Not a number.");
							continue;
						}

						if (!Arrays.asList(sMapDimensions).contains(dim)) {
							System.out.println("Invalid dimension.");
							continue;
						}

						System.out.println("Editing dimension " + dim + ".");
						Map<List<Integer>, MilnorElement> map = sMap.getMapByDimension(dim);

						if (mode == PICK) {
							if (!map.containsKey(pickMono) || pickMono == null) {
								System.out.println(pickMono + " is not a valid monomial.");
								continue;
							}
						}

						for (Map.Entry<List<Integer>, MilnorElement> entry : map.entrySet()) {
							MilnorElement target = new MilnorElement(0);

							if (mode == ALL || mode == PICK) {
								if ((mode == PICK) && (!entry.getKey().equals(pickMono)))
									continue;

								System.out.print("Enter target for " + entry.getKey() + " (list of choices: " + AmodAnMap.get(dim) + "; Enter 0 for zero): ");

								next = reader.nextLine();
								String[] split = next.split(" "); //TODO this only accepts monomials, not sums. easy to fix later
								int[] userMono = new int[split.length];
								for (int i = 0; i < split.length; i++)
									userMono[i] = Integer.parseInt(split[i]);

								if (userMono[0] == 0)
									target = new MilnorElement(0);
								else if (Tools.milnorDimension(Tools.listToIntArray(entry.getKey())) != Tools.milnorDimension(userMono)) {
									System.out.println("Dimension mismatch!");
									target = new MilnorElement(0);
								} else
									target = new MilnorElement(userMono);

							} else if (mode == FILL) {
								if (fillIndex >= AmodAnMap.get(dim).size()) {
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
					break;
				case JMAP:
					if (sMap == null)
						System.out.println(NO_S_MAP);
					else {
						start = System.nanoTime();
						jMap = dualAn.generateJMap(sMap);
						System.out.println("J map generated using last s map. (" + ((double) (System.nanoTime() - start)) / 1000000 + " ms)");
						Tex.writeToFile(jMap.printToTex("j"), "j_map.tex");
					}
					break;
				case VARIATE:
					if (sMap == null)
						System.out.println(NO_S_MAP);
					else {
						start = System.nanoTime();
						List<Function> variations = sMap.varyInDimension(16);
						List<String> texOutput = new ArrayList<>();
						for (Function var : variations) {
							jMap = dualAn.generateJMap(var);
							texOutput.add("\\textbf{s map} (roth: " + dualAn.checkRoth(var, jMap) + ")\\\\");
							texOutput.addAll(var.printToTex("s"));
							texOutput.add("\\bigskip");
							texOutput.addAll(jMap.printToTex("j", 23));
							texOutput.add("\\newpage");
						}

						Tex.writeToFile(texOutput, "s_maps_" + texOutput.hashCode() + ".tex");
						System.out.println("Tex output of variations using last s map. (" + ((double) (System.nanoTime() - start)) / 1000000 + " ms)");
					}
					break;
				case ROTH:
					if (jMap == null)
						System.out.println(NO_J_MAP);
					else {
						start = System.nanoTime();
						System.out.println(dualAn.checkRoth(sMap, jMap) + " (" + ((double) (System.nanoTime() - start)) / 1000000 + " ms)");
					}
					break;
				case PRINT: {
					String next = (!str.contains(" ")) ? "" : str.substring(str.indexOf(" ") + 1);
					if (next.equals("sMap"))
						System.out.println((sMap != null) ? sMap : NO_S_MAP);
					else if (next.equals("jMap"))
						System.out.println((jMap != null) ? jMap : NO_J_MAP);
					else if (next.equals("saved"))
						System.out.println((savedSMap != null) ? savedSMap : "Nothing saved.");
					break;
				}
				case SBAR:
					if (sMap == null)
						System.out.println(NO_S_MAP);
					else {
						String next = (!str.contains(" ")) ? "" : str.substring(str.indexOf(" ") + 1);

						if (next.equals("last")) {
							if (lastOutput.get(0) instanceof int[][])
								System.out.println(dualAn.sBarTensor((List<int[][]>) lastOutput, sMap));
							if (lastOutput.get(0) instanceof int[])
								System.out.println(dualAn.sBar(new MilnorElement(lastOutput), sMap));
						} else if (next.contains("x"))
							System.out.println(dualAn.sBarTensor(Tools.parseTensorSumFromString(next), sMap));
						else
							System.out.println(dualAn.sBar(new MilnorElement(Tools.parseSumFromString(next)), sMap));
					}
					break;
				case J:
					if (jMap == null)
						System.out.println(NO_J_MAP);
					else {
						String next = str.substring(str.indexOf(" ") + 1);
						MilnorElement last = jMap.get(Tools.parseSumFromString(next).get(0));
						lastOutput = last.getAsList();
						System.out.println(last);
					}
					break;
				case ADEM:
					System.out.println(Steenrod.cleanup(Steenrod.writeAsBasis(str.substring(str.indexOf(" ") + 1))));
					break;
				case EXCESS:
					System.out.println(Steenrod.excess(str.substring(str.indexOf(" ") + 1)));
					break;
				case COPROD_STEEN:
					SteenrodElement input = new SteenrodElement(str.substring(str.indexOf(" ") + 1));
					List<int[][]> coprod = Steenrod.coproduct(input);
					System.out.println(Tools.sumToString(coprod));
					break;
				//act sq, j
				case ACT: {
					String next = str.substring(str.indexOf(" ") + 1);
					String[] param = next.split(", ");
					System.out.println(BrownGitler.action(param[0], param[1]));
					break;
				}
				//tex n sq (writes a tex file with the action of sq on J(n))
				//OR tex n (wrties a tex file for J(n))
				case TEXJ: {
					String next = str.substring(str.indexOf(" ") + 1);
					if (next.equals("") || next.equals(keyword))
						System.out.println("Format: 'tex n' to write a file for J(n) OR 'tex n sq' to write the action of sq on J(n)");
					else {
						String[] param = next.split(" ");
						int dim = Integer.parseInt(param[0]);
						Jm jModule = new Jm(dim);
						System.out.println("quick check");
						if (param.length == 1) {
							Tex.writeToFile(jModule.printAsTex(), "j" + dim + ".tex");
							System.out.println("Tex file written");
						} else if (param.length == 2) {
							SteenrodElement sq = new SteenrodElement(param[1]);
							Tex.writeToFile(jModule.printActionAsTex(sq), sq.texFormat() + "_j" + dim + ".tex");
							System.out.println("Tex file written");
						} else
							System.out.println("Wrong format, no file written");

					}
					break;
				}
				//testing
				case TEX: {
					String next = str.substring(str.indexOf(" ") + 1);
					if (next.equals("") || next.equals(keyword))
						System.out.println("Format: 'tex n' to write a file for J(n) OR 'tex n sq' to write the action of sq on J(n)");
					else {
						String[] param = next.split(" ");
						int dim = Integer.parseInt(param[0]);
						Jm jModule = new Jm(dim);
						if (param.length == 1) {
							Tex.writeToFile(jModule.printAsTex(), "j" + dim + ".tex");
							System.out.println("Tex file written");
						} else if (param.length == 2) {
							SteenrodElement sq = new SteenrodElement(param[1]);
							Tex.writeToFile(jModule.printActionAsTex(sq), sq.texFormat() + "_j" + dim + ".tex");
							System.out.println("Tex file written");
						} else
							System.out.println("Wrong format, no file written");

					}
					break;
				}
				case QUIT:
					break scanner;
				default:
					System.out.println("I don't understand.");
					break;
			}

			System.out.println("(Total command time: " + (System.nanoTime()-timer)/1000000000.0 + " seconds)");
		}
	}

	private static void search(DualAn dualAn, Map<Integer, List<MilnorElement>> AmodAnMap) {
		int count = 1;
		long initial = System.nanoTime();
		Integer[] sMapDimensions = dualAn.sMapDimensions();

		while(true) {
			System.out.print("Searching " + count + "... ");
			long start = System.nanoTime();

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
