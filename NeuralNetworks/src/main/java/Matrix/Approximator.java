package Matrix;

import java.util.function.Function;

public class Approximator {
	public static Matrix gradient(Matrix input, Function<Matrix,Matrix> trans) {
		final double INCREMENT = 0.00000001;
		
		Matrix loss1 = trans.apply(input);
		
		assert loss1.getCol() == input.getCol():"Input/loss columns incompatible";
		assert loss1.getRows() == 1:"Transform does not return 1 row";
		
		//System.out.println(input+"\n"+loss1);
		
		Matrix result = new Matrix(input.getRows(),input.getCol(), i->0);

		input.forEach((r,c,i,v)->{
			Matrix incremented = input.addIncrement(r, c, INCREMENT);
			Matrix loss2 = trans.apply(incremented);
			double rate = (loss2.get(c) - loss1.get(c))/INCREMENT;
			
			result.set(r, c, rate);
		});
		
		return result;
	}
	
	public static Matrix weightGradient(Matrix weights, Function<Matrix,Matrix> trans) {
		
		final double INCREMENT = 0.00000001;
		
		Matrix loss1 = trans.apply(weights);
		
		//System.out.println(input+"\n"+loss1);
		
		Matrix result = new Matrix(weights.getRows(), weights.getCol(), i->0);
		
		weights.forEach((r,c,i,v)->{
			Matrix incremented = weights.addIncrement(r, c, INCREMENT);
			Matrix loss2 = trans.apply(incremented);
			
			double rate = (loss2.get(0) - loss1.get(0))/INCREMENT;
			
			result.set(r, c, rate);
		});
		
		return result;
	}
	
}
