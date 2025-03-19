package Matrix;

public class LossFunction {
	public static Matrix crossEntropy(Matrix expected, Matrix actual) {
		return actual.apply((ind,val)->{
			return -expected.get(ind) * Math.log(val);
			//formula for cross entropy
		}).sumColumns();
	}
}
