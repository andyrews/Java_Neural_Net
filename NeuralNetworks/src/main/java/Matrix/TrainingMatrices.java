package Matrix;

public class TrainingMatrices {
	private Matrix input, output;

	public TrainingMatrices(Matrix input, Matrix output) {
		this.input = input;
		this.output = output;
	}

	public Matrix getInput() {
		return input;
	}

	public void setInput(Matrix input) {
		this.input = input;
	}

	public Matrix getOutput() {
		return output;
	}

	public void setOutput(Matrix output) {
		this.output = output;
	}
	
	
	
}
