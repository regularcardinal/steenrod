package main;
import java.util.List;


/* 
 * Algebra is the main interface for algebras of finite or infinite dimensions
 *
 * Monomials should be represented as integer arrays
 * Sums of homogeneous elements should be represented via lists
 *      Example: Milnor basis: xi_i^j is represented by [i, j] and x_i^j x_k^l is [i, j, k, l]
 *      and x_i^j + x_k^l is the list [i, j] -> [k, l]
 */
public interface Algebra {
	//standard DUAL milnor basis i.e. xi_n
    final static String MILNOR = "milnor";
    //Serre-Cartan basis i.e. Sq^i
	final static String ADEM = "adem";
	
	public String basisType();
	public boolean isInfiniteDimensional();
	public boolean hasRelations();
	
	//input is a sum of elements. check if all their dimensions agree.
	public boolean checkHomogeneous(List<int[]> input);	
}
