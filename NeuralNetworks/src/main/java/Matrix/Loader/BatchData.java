package Matrix.Loader;

public interface BatchData {
	public void setInputBatch(double[] inp);
	public double[] getInputBatch();
	public void setExpectedBatch(double[] exp);
	public double[] getExpectedBatch();
}
