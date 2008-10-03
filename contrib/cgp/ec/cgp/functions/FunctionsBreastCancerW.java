package ec.cgp.functions;

/**
 * 
 * Function set for the Breast Cancer (Yugoslavia) classification problem. The
 * only difference between this and the Breast Cancer (Wisconsin) classification
 * problem is the naming of the inputs.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class FunctionsBreastCancerW extends FunctionsBreastCancerY {
	public String inputName(int inp, Object val) {
		if (inp == 0)
			return "clumpThickness";
		if (inp == 1)
			return "cellSizeUniformity";
		if (inp == 2)
			return "cellShapeUniformity";
		if (inp == 3)
			return "marginalAdhesion";
		if (inp == 4)
			return "singleEpiCellSize";
		if (inp == 5)
			return "bareNuclei";
		if (inp == 6)
			return "blandChromatin";
		if (inp == 7)
			return "normalNucleoli";
		if (inp == 8)
			return "mitoses";
		if (inp > 8 && inp < 18)
			return "" + val;
		else
			return "UNKNOWN INPUT";
	}
}
