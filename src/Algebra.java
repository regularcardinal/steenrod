import java.util.List;

public interface Algebra {
	
	//monomials should be represented as integer arrays
	//whereas sums of homogeneous elements should be represented via lists.
	//example: if it's in milnor, then x_i^j is represented by [i, j] and x_i^j x_k^l is [i, j, k, l]
	//and x_i^j + x_k^l is the list [i, j] -> [k, l]
	
	final static String MILNOR = "milnor";
	final static String ADEM = "adem";
	final static String MILNOR_DUAL = "milnor dual";
	
	public String basisType();
	public boolean isInfiniteDimensional();
	public boolean hasRelations();
	
	//input is a sum of elements. check if all their dimensions agree.
	public boolean checkHomogeneous(List<int[]> input);
	
}
