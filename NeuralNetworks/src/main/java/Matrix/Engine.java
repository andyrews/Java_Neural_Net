package Matrix;

import java.io.Serializable;
import java.util.*;

public class Engine implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private LinkedList<Transform> transforms = new LinkedList<>();
	private LinkedList<Matrix> weights = new LinkedList<>();
	private LinkedList<Matrix> biases = new LinkedList<>();
	private Random r = new Random();
	
	private LossFunctions lossFunction = LossFunctions.CROSSENTROPY;
	private double scaleInitialWeights = 1;
	
	private boolean storeInpError = false;
	
	public void setScaleInitialWeights(double inW) {
		scaleInitialWeights = inW;
		
		if(weights.size() != 0) {
			throw new RuntimeException("Must call setScaleInitialWeights before adding transforms");
		}
	}
	
	public void evaluate(BatchResult bResult, Matrix expected) {
		if(lossFunction != LossFunctions.CROSSENTROPY) {
			throw new UnsupportedOperationException("Only crossentropy function please");
		}
		
		double loss = LossFunction.crossEntropy(expected, bResult.getOutput()).averageColumn().get(0);
		
		Matrix predictions = bResult.getOutput().getGreatestRowNumbers();
		Matrix actual = expected.getGreatestRowNumbers();
		
		int correct = 0;
		for(int i = 0; i < actual.getCol(); i++) {
			if((int)actual.get(i) == (int)predictions.get(i)) {
				++correct;
			} 
		}
		
		double percentCorrect = (100.0 * correct)/actual.getCol();
		
		
		bResult.setLoss(loss);
		bResult.setPercentCorrect(percentCorrect);
	}
	
	public BatchResult forwardPass(Matrix input) {
		BatchResult bResult = new BatchResult();
		Matrix output = input;
		
		int denseIndex = 0;
		
		bResult.addIo(output);
		
		for(var t : transforms) {
			if(t==Transform.DENSE) {
				
				bResult.addWeightInput(output);
				Matrix w = weights.get(denseIndex);
				Matrix b = biases.get(denseIndex);
				
				output = w.multiply(output).modify((r,c,v)->v+b.get(r));
				
				++denseIndex;	
				
			}else if(t==Transform.RELU) {
				
				output = output.modify(val -> val>0?val:0);
				
			}else if(t==Transform.SOFTMAX) {
				output = output.softmax();
			}
			
			bResult.addIo(output);
		}
		
		return bResult;
	}
	
	public void adjust(BatchResult bRes, double learningRate) {
		var weightInputs = bRes.getWeightInputs();
		var weightErrors = bRes.getWeightErrors();
		
		assert weightInputs.size() == weightErrors.size();
		assert weightInputs.size() == weights.size();
		
		for(int i = 0; i < this.weights.size(); i++) {
			var weight = this.weights.get(i);
			var bias = this.biases.get(i);
			var error = weightErrors.get(i);
			var input = weightInputs.get(i);
			
			assert weight.getCol() == input.getRows();
			
			var weightAdjust = error.multiply(input.transpose());
			var biasAdjust = error.averageColumn();
			
			double rate = learningRate/input.getCol();
			
			weight.modify((in,v)->v - rate * weightAdjust.get(in));
		
			bias.modify((r,c,v)->v - learningRate * biasAdjust.get(r));
		}
	}
	
	public void backwardPass(BatchResult bRes, Matrix expected) {//backpropagation
		
		var transformsIt = transforms.descendingIterator();
		
		if(lossFunction != LossFunctions.CROSSENTROPY || transforms.getLast()!=Transform.SOFTMAX) {
			throw new UnsupportedOperationException("Loss function must be cross entropy and last transform must be softmax");
		}
		
		var ioIterator = bRes.getIo().descendingIterator();
		var weightIterator = weights.descendingIterator();
		Matrix softmaxOutput = ioIterator.next();//softmax output
		
		Matrix error = softmaxOutput.apply((i,v)->v-expected.get(i));
		
		while(transformsIt.hasNext()) {
			Transform t = transformsIt.next();
			
			Matrix input = ioIterator.next();//softmax input/hidden layer weighted sum output..., weighted sum input...
			
			switch(t) {
			case DENSE:
				Matrix weight = weightIterator.next();
				
				bRes.addWeightError(error);
				
				if(weightIterator.hasNext() || storeInpError)
				error = weight.transpose().multiply(error);
				
				break;
			case RELU:
				error = error.apply((i,v)->input.get(i)>0?v:0);
				//base actualRes value to the input values inputted thru relu
				
				break;
			case SOFTMAX:
				break;
			default:
				throw new UnsupportedOperationException("transform invalid");
			}
			
			//System.out.println(t);
		}
		
		if(this.storeInpError) {
			bRes.setInputError(error);
		}
	}
	
	public void add(Transform t, double... param) {
		if(t == Transform.DENSE) {
			int numNeurons = (int)param[0];
			int weightPerNeuron = weights.size()==0?(int)param[1]:weights.getLast().getRows();
			//if first dense layer, then weights is equal to column(2nd) param
			
			Matrix weight = new Matrix(numNeurons, weightPerNeuron, i->scaleInitialWeights * r.nextGaussian());
			Matrix bias = new Matrix(numNeurons, 1, i->0);
			
			weights.add(weight);
			biases.add(bias);
		}
		this.transforms.add(t);
	}
	
	public void setStoreInputError(boolean v) {
		this.storeInpError = v;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Scale initial weights: %3f\n", scaleInitialWeights));
		
		sb.append("\nTransforms:\n");
		
		int weightIndex = 0;
		for(var r : transforms) {
			
			sb.append(r);
			
			if(r==Transform.DENSE)sb.append(" ").append(weights.get(weightIndex++).toString(false));
			sb.append("\n");
		}
		return sb.toString();
	}	
}
