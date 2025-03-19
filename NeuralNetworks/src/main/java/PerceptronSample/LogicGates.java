package PerceptronSample;

public class LogicGates {
	
	static double perceptron(double[] inp, double[] w, double bias) {
		double z = 0.0;
		
		for(int i = 0; i < inp.length; i++) {
			z+= inp[i] * w[i];
		}
		z += bias;
		
		return z > 0 ? 1.0:0.0;
	}
	
	static double and(double x1, double x2) {
		return perceptron(new double[] {x1,x2}, new double[] {1,1}, -1);
	}
	
	static double or(double x1, double x2) {
		return perceptron(new double[] {x1,x2}, new double[] {1,1}, 0);
	}
	
	static double xor(double x1, double x2) {
		return and(or(x1,x2), nand(x1,x2));
	}
	
	static double xnor(double x1, double x2) {
		return(xor(x1,x2)==1)?0:1;
	}
	
	static double nor(double x1, double x2) {
		return (or(x1,x2) == 1)?0:1;
	}
	
	static double nand(double x1, double x2) {
		return (and(x1,x2) == 1)?0:1;
	}
	
	public static void main(String[] args) {
		for(int i = 0; i < 4; i++) {
			double x1 = i/2;
			double x2 = i%2;
			
			System.out.println(xnor(x1, x2));
		}
	}
}
