package natureoverhaul;

public class Utils {
	/**
	* Calculate an optimal distance coefficient. This is for cases
	* where a larger number is desired the further you get from
	* the optimal value. The minimum is 1, so multiplying any number "r"
	* by the result of this operation will result in "r" if r is equal to "opt".
	* If r is extremely far from opt, the coefficient will be extremely large
	*
	* @param	rain		Current value
	* @param	opt		Optimal value
	* @param	tol		tolerance (lower = Higher probability)
	* @return The modifier. Output always >= 1, where 1 is "just as likely" and
	* 		higher is "less likely"
	*/
	protected static float getOptValueMult(float rain, float opt, float tol) {	
		return tol * (float) Math.pow(opt - rain, 2) + 1;
	}
}
