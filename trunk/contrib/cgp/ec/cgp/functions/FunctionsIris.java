package ec.cgp.functions;

/**
 * 
 * Function set for the Iris species classification problem. The only difference
 * between this and the Breast Cancer (Wisconsin) classification problem is the
 * naming of the inputs.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class FunctionsIris extends FunctionsBreastCancerY {
	public String inputName(int inp, Object val) {
		if (inp == 0)
			return "sepallength";
		if (inp == 1)
			return "sepalwidth";
		if (inp == 2)
			return "petallength";
		if (inp == 3)
			return "petalwidth";
		if (inp > 3 && inp < 20)
			return "" + val;
		else
			return "UNKNOWN INPUT";
	}
}
