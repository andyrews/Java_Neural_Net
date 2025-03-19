package Matrix.Loader;

public abstract class AbstractBatchData implements BatchData {
	
	private double[]inpBatch;
	private double[]expectedBatch;
	
	@Override
	public void setInputBatch(double[] inp) {
		this.inpBatch = inp;
	}

	@Override
	public double[] getInputBatch() {
		return this.inpBatch;
	}

	@Override
	public void setExpectedBatch(double[] exp) {
		this.expectedBatch = exp;
	}

	@Override
	public double[] getExpectedBatch() {
		return this.expectedBatch;
	}

}
