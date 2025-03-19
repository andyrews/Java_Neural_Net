package Matrix;

import java.util.Random;

public class Util {
	
	private static Random random = new Random();
	
	public static Matrix generateInputMatrix(int r, int c) {
		return new Matrix(r,c,i->random.nextGaussian());
	}
	
	public static Matrix generateExpectedMatrix(int r, int c) {
		
		Matrix expected = new Matrix(r,c, i->0);
		
		for(int co = 0; co < expected.getCol(); co++) {
			int randoRow = random.nextInt(r);
			expected.set(randoRow, co, 1);
		}
		
		
		return expected;
	}

	public static Matrix generateTrainableExpectedMatrix(int outR, Matrix input) {
		Matrix expected = new Matrix(outR, input.getCol());
		
		Matrix columnSums = input.sumColumns();
		columnSums.forEach((r,c,v)->{
			int rowIndex = (int)(outR * (Math.sin(v) + 1.0)/2.0);
			
			expected.set(rowIndex, c, 1);
		});
		
		return expected;
	}
	
	public static TrainingArrays generateTrainingArrays(int inputSize, int outputSize, int numberItems) {
		double[] input = new double[inputSize * numberItems];
		double[] output = new double[outputSize * numberItems];
		
		int inpPos = 0;
		int outPos = 0;
		
		for(int col = 0; col < numberItems; col++) {
			int radius = random.nextInt(outputSize);
			
			double[] val = new double[inputSize];
			
			double actualRadius = 0;
			for(int ro = 0; ro < inputSize; ro++) {
				val[ro] = random.nextGaussian();
				actualRadius += val[ro] * val[ro];
			}
			
			actualRadius = Math.sqrt(actualRadius);
			
			for(int ro = 0; ro < inputSize; ro++) {
				input[inpPos++] = val[ro] * radius / actualRadius;
			}
			
			output[outPos + radius] = 1;
			outPos += outputSize;
		}
		
		return new TrainingArrays(input, output);
	}
	
	public static TrainingMatrices generateTrainingMatrices(int inpR, int outR, int c) {
		
		var io = generateTrainingArrays(inpR, outR, c);
		Matrix input = new Matrix(inpR, c, io.getInput());
		Matrix output = new Matrix(outR, c, io.getOutput());
		
		return new TrainingMatrices(input, output);
	}
}
