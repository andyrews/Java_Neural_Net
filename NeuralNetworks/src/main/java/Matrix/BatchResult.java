package Matrix;

import java.util.LinkedList;

public class BatchResult {
	private LinkedList<Matrix> io = new LinkedList<>();
	private LinkedList<Matrix> weightErrors = new LinkedList<>();
	private LinkedList<Matrix> weightInput = new LinkedList<>();
	private Matrix inputError;
	private double loss;
	private double percentCorrect;
	
	public void addWeightInput(Matrix inp) {
		this.weightInput.add(inp);
	}
	
	public LinkedList<Matrix> getWeightInputs(){
		return this.weightInput;
	}
	
	public LinkedList<Matrix> getIo(){
		return this.io;
	}
	
	public void addIo(Matrix m) {
		this.io.add(m);
	}
	
	public Matrix getOutput() {
		return this.io.getLast();
	}
	
	public LinkedList<Matrix> getWeightErrors() {
		return weightErrors;
	}

	public void addWeightError(Matrix weightError) {
		this.weightErrors.addFirst(weightError);
	}

	public Matrix getInputError() {
		return inputError;
	}

	public void setInputError(Matrix inputError) {
		this.inputError = inputError;
	}

	public void setLoss(double loss) {
		this.loss = loss;
	}
	
	public double getLoss() {
		return this.loss;
	}

	public void setPercentCorrect(double percentCorrect) {
		this.percentCorrect = percentCorrect;
	}
	
	public double getPercentCorrect() {
		return this.percentCorrect;
	}
}
