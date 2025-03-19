package Matrix;

import java.util.stream.DoubleStream;

public class RunningAverages {
	private int nCalls = 0;
	private double[][] val;
	private Callback cBack;
	private int pos = 0;
	
	public interface Callback{
		public void apply(int callNum, double[] avg);
	}
	
	public RunningAverages(int numAvg, int windowSize, Callback cBack) {
		this.cBack = cBack;
		this.val = new double[numAvg][windowSize];
		
		//System.out.println(val.length + "\n" + val[0].length);
	}
	
	public void add(double ...arg) {
		for(int i = 0; i < this.val.length; i++) {
			this.val[i][pos] = arg[i];
		}
		if(++pos == val[0].length) {
			//checks if pos is in the last element of the row
			double[] avg = new double[val.length];
			for(int i = 0; i < val.length; i++) {
				avg[i] = DoubleStream.of(val[i]).average().getAsDouble();
			}
			
			cBack.apply(++nCalls, avg);
			
			pos = 0;
		}
	}
}
