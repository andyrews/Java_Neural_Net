package PerceptronSample;

public class PerceptronTest {

	public static void main(String[] args) {
		//Data Set
		//Each pair of inputs have corresponding target
		PerceptronTest p = new PerceptronTest();
		//System.out.println(p.getClass() == PerceptronTest.class);
		double [][] inputs = {{0,0}, {0,1}, {1,0}, {1,1}};
		double [] targets = {0,0,0,1};
		
		double lRate = 0.1;
		int iteration = 200;
		
		Perceptron p1 = new Perceptron(inputs[0].length);
		
		p1.train(inputs, targets, lRate, iteration);
		
		/*
		double test[][] = {{0,0},{0,1},{1,0},{1,1}};
		
		double o1 = p1.calcOutput(test[0]);
		double o2 = p1.calcOutput(test[1]);
		double o3 = p1.calcOutput(test[2]);
		double o4 = p1.calcOutput(test[3]);
		
		System.out.println(o1 + "\n" + o2 + "\n" + o3 + "\n" + o4);
		*/
	}
}
 
