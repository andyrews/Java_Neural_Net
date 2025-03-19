package NNetworks;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;
import Matrix.Approximator;
import Matrix.BatchResult;
import Matrix.Engine;
import Matrix.LossFunction;
import Matrix.Matrix;
import Matrix.RunningAverages;
import Matrix.Transform;
import Matrix.Util;

class NetTest {
	interface NeuNet{
		Matrix apply(Matrix e);
	}
	private Random r = new Random();
	
	@Test
	void testTrainEngine() {
		int inpR = 500;
		int col = 32;
		int outR = 3;
		
		//Matrix input = Util.generateInputMatrix(inpR, col);
		//Matrix expected = Util.generateTrainableExpectedMatrix(outR, input);
	
		Engine en = new Engine();
		en.add(Transform.DENSE, 100,inpR);
		en.add(Transform.RELU);
		en.add(Transform.DENSE, outR);
		en.add(Transform.SOFTMAX);
		
		RunningAverages rAvg = new RunningAverages(2, 500, (cNum, avg)->{
			assertTrue(avg[0] < 6);
			//System.out.printf("%d. Loss: %.3f -- Percent correct: %.2f\n", cNum, avg[0], avg[1]);
		});
		
		double initialLearningRate = 0.02;
		double learningRate = initialLearningRate;
		double iterations = 500;
		
		for(int i = 0; i < iterations; i++) {
			var tm = Util.generateTrainingMatrices(inpR, outR, col);
			var input = tm.getInput();
			var expected = tm.getOutput();
			
			BatchResult batchForward = en.forwardPass(input);
			//en.evaluate(batchForward, expected);
			//double loss1 = batchForward.getLoss();
			
			en.backwardPass(batchForward, expected);
			en.adjust(batchForward, learningRate);
			//batchForward = en.forwardPass(input);
			en.evaluate(batchForward, expected);
			
			rAvg.add(batchForward.getLoss(), batchForward.getPercentCorrect());
			
			learningRate -= (initialLearningRate/iterations);
		}
	}
	
	//@Test
	void testWeightGradient() {
		int inpRows = 4;
		int outRows = 5;
		Matrix weights = new Matrix(outRows,inpRows, i->r.nextGaussian());
		Matrix input = Util.generateInputMatrix(inpRows, 1);
		Matrix expected = Util.generateExpectedMatrix(outRows, 1);
		
		Matrix output = weights.multiply(input).softmax();
		
		Matrix loss = LossFunction.crossEntropy(expected, output);
		
		System.out.println(input + "\n" + weights + "\n" + output + "\n" + expected + "\n" + loss);
	
		Matrix calculatedError = output.apply((i,v)->v-expected.get(i));
	
		System.out.println(calculatedError);
	
		Matrix calculatedWeightGradients = calculatedError.multiply(input.transpose());
		//finding derivative of the error with respect to each weight through matrix multiplication and transpose
		
		System.out.println(calculatedWeightGradients);
		
		Matrix apporximatedWeightGradients = Approximator.weightGradient(weights, we -> {
			Matrix out = we.multiply(input).softmax();
			return LossFunction.crossEntropy(expected, out);
		});
		
		System.out.println(apporximatedWeightGradients);
		
		calculatedWeightGradients.setTolerance(0.01);
		assertTrue(calculatedWeightGradients.equals(apporximatedWeightGradients));
	}
	
	//@Test
	void testBackProp() {
		final int inpR = 4;
		final int c = 5;
		final int outR = 4;
		
		Matrix input = new Matrix(inpR,c,i->this.r.nextGaussian());
		Matrix expected = new Matrix(outR,c, i->0);
	
		Matrix weights = new Matrix(outR, inpR, i->this.r.nextGaussian());
		Matrix bias = new Matrix(outR, 1, i->this.r.nextGaussian());
		
		for(int co = 0; co < expected.getCol(); co++) {
			int randoRow = this.r.nextInt(outR);
			expected.set(randoRow, co, 1);
		}
		
		NeuNet neuralNet = m->{
			
			Matrix out = m.apply((i,v)->v>0?v:0);//relu

			out = weights.multiply(out);//weights
			out.modify((r,co,v)->v + bias.get(r));//bias
			
			return out.softmax();//activation function
		}; 
		
		Matrix softmaxOutput = neuralNet.apply(input);
		
		Matrix approximatedRes = Approximator.gradient(input, inp->{
			Matrix out = neuralNet.apply(inp);
			return LossFunction.crossEntropy(expected, out);
		});
		
		Matrix actualRes = softmaxOutput.apply((i,v)->
			v-expected.get(i)
		);
		System.out.println(softmaxOutput + "\n" + expected + "\n" + actualRes);
		
		
		System.out.println("Transpose:\n" + weights.transpose());
		
		actualRes = weights.transpose().multiply(actualRes);
		System.out.println("Multiplied:\n" + actualRes);
		actualRes = actualRes.apply((i,v)->input.get(i)>0?v:0);//base actualRes value to the input values inputted thru relu
		
		System.out.println("Inp:\n" + input + "Res:\n" + actualRes);
		assertTrue(approximatedRes.equals(actualRes));
		
		
	}
	
	//@Test
	void testSoftCrossEntropyGradient() {
		final int r = 4;
		final int c = 5;
		Matrix input = new Matrix(r,c,i->this.r.nextGaussian());
		Matrix expected = new Matrix(r,c, i->0);
		
		for(int co = 0; co < expected.getCol(); co++) {
			int randoRow = this.r.nextInt(r);
			expected.set(randoRow, co, 1);
		}
		
		Matrix softmaxOutput = input.softmax();
		
		Matrix result = Approximator.gradient(input, inp->{
			return LossFunction.crossEntropy(expected, inp.softmax());
		});
		
		result.forEach((i,v)->{
			double softMV = softmaxOutput.get(i);
			double expV = expected.get(i);
			
			assertTrue(Math.abs(v - (softMV - expV)) < 0.01);
			System.out.println(v + ", " + (softMV - expV));
		});
		
	}
	
	//@Test
	void testApproximator() {
		final int r = 4;
		final int c = 5;
		Matrix input = new Matrix(r,c,i->this.r.nextGaussian()).softmax();
		Matrix expected = new Matrix(r,c, i->0);
		
		for(int co = 0; co < expected.getCol(); co++) {
			int randoRow = this.r.nextInt(r);
			expected.set(randoRow, co, 1);
		}
		Matrix result = Approximator.gradient(input, inp->{
			return LossFunction.crossEntropy(expected, inp);
		});
		
		input.forEach((i,v)->{
			double resV = result.get(i);
			double expV = expected.get(i);
			
			if(expV < 0.001) {
				assertTrue(Math.abs(resV) < 0.01);
			}else {
				assertTrue(Math.abs(resV + 1.0/v) < 0.01);
			}
		
		});
	}
	
	//@Test
	void testCrossEntropy() {
		double[] expected = {1,0,0,0,0,1,0,1,0};
		Matrix exp = new Matrix(3,3,i->expected[i]);
		
		System.out.println(exp);
		
		Matrix data = new Matrix(3,3,i-> 0.05 * i*i).softmax();
		System.out.println(data);
	
		Matrix result = LossFunction.crossEntropy(exp, data);
		
		System.out.println(result);
		
		data.forEach((r,c,i,v)->{
			double expectVal = exp.get(i);
			
			double loss = result.get(c);
			
			if(expectVal > 0.9) {
				assertTrue(Math.abs(Math.log(v) + loss)<0.001);
			}
		});
		
	}
	
	//@Test
	void testEngine() {
		Engine e = new Engine();
		
		int inpRows = 5;
		int col = 6;
		int outRows = 4;
		
		e.add(Transform.DENSE, 8, 5);
		e.add(Transform.RELU);
		
		e.add(Transform.DENSE, 5);
		e.add(Transform.RELU);
		
		e.add(Transform.DENSE, 4);
		
		e.add(Transform.SOFTMAX);
		e.setStoreInputError(true);
		
		Matrix input = Util.generateInputMatrix(inpRows, col);
		Matrix expected = Util.generateExpectedMatrix(outRows, col);
		
		Matrix approximatedError = Approximator.gradient(input, inp->{
			System.out.println("Inp" + inp);
			BatchResult batchResult = e.forwardPass(inp);
			System.out.println("Last: " + batchResult.getOutput());
			return LossFunction.crossEntropy(expected, batchResult.getOutput());
		});
		
		BatchResult batchResult = e.forwardPass(input);
		//System.out.println(e + "\n\n" + output);
		e.backwardPass(batchResult, expected);
		
		Matrix calculatedError = batchResult.getInputError();
	
		calculatedError.setTolerance(0.01);
		//difference between values may be too precise, play around with it
		
		System.out.println(approximatedError + "\n" + calculatedError);
		
		
		assertTrue(approximatedError.equals(calculatedError));
	}
	
	//@Test
	void testTemplate() {
		int inputSize = 5;
		int layer1Size = 6;
		int layer2Size = 4;
		
		Matrix input = new Matrix(inputSize, 1, i->r.nextGaussian());
		Matrix layer1weights = new Matrix(layer1Size, inputSize, i->r.nextGaussian());
		Matrix layer1bias = new Matrix(layer1Size, 1, i->r.nextGaussian());
		
		Matrix layer2weights = new Matrix(layer2Size, layer1weights.getRows(), i->r.nextGaussian());
		Matrix layer2bias = new Matrix(layer2Size, 1, i->r.nextGaussian());
	
		var output = input;
		
		System.out.println("Orig:" + output);
		
		//First Hidden Layer
		output = layer1weights.multiply(output);
		System.out.println("Weighted:" + output);
		
		output = output.modify((r,c,v)->v+layer1bias.get(r));
		System.out.println("Bias:" + output);
		
		output = output.modify(val -> val>0?val:0);
		System.out.println("Relu:" + output);
		
		//Second HiddenLayer
		output = layer2weights.multiply(output);
		System.out.println("=====2nd=====\nWeighted: " + output);
		output = output.modify((r,c,v)->v+layer2bias.get(r));
		System.out.println("Bias:" + output);
		
		output = output.softmax();
		System.out.println("Soft:" + output + "\n" + output.sumColumns());
	}
	
	//@Test
	void testAddBias() {
		Matrix input = new Matrix(3,3,i->(i+1));
		Matrix weights = new Matrix(3,3,i->(i+1));
		//since 3 rows of inputs, we need 3 columns of weights, assuming 3 neurons
		Matrix bias = new Matrix(3,1,i->(i+1));
		
		Matrix result = weights.multiply(input).modify((r,c,v)->v+bias.get(r));
		
		double [] expectedValues = {+31.00000 ,  +37.00000,   +43.00000,
				   					+68.00000,   +83.00000,   +98.00000,
				   					+105.00000 , +129.00000,  +153.00000};
		
		Matrix expect = new Matrix(3,3, i->expectedValues[i]);
		
		assertTrue(expect.equals(result));
		
		//System.out.println(input + "\n" + weights + "\n" + bias + "\n" + result);
	}
	//@Test
	void testReLu() {
		
		final int numNeurons = 5;
		final int numInputs = 6;
		final int inputSize = 4;
		
		Matrix input = new Matrix(inputSize, numInputs,i->r.nextDouble());
		Matrix weights = new Matrix(numNeurons, inputSize, i->r.nextGaussian());
		//since 3 rows of inputs, we need 3 columns of weights, assuming 3 neurons
		Matrix bias = new Matrix(numNeurons, 1, i->r.nextGaussian());
		
		Matrix result = weights.multiply(input).modify((r,c,v)->v+bias.get(r));
		
		Matrix finalres = result.modify(val -> val>0?val:0);
		
		finalres.forEach((index,v)->{
			double orig = result.get(index);
			
			if(orig > 0)assertTrue(Math.abs(orig-v) < 0.000001);
			else assertTrue(Math.abs(v) < 0.000001);
			
		});
		
		//System.out.println(input + "\n" + weights + "\n" + bias + "\n" + result);
	}
	
}

