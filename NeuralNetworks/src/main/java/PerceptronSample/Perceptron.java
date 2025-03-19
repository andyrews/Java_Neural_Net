package PerceptronSample;

import java.util.Random;

public class Perceptron {
	
	private double [] weights;
	private double bias;
	
	public Perceptron(int e) {
		
		this.weights = new double[e];
		
		/*for(int i = 0; i < e; i++) {
			this.weights[i] = new Random().nextDouble();
			System.out.println(this.weights[i] + " ");
		}*/
		
		//Initialize
		this.bias = new Random().nextDouble();
		
	}
	
	public double calcOutput(double[] input) {
		double sum = 0;
		
		//summation of p * w
		
		for(int i = 0; i < this.weights.length; i++) {
			sum = sum + input[i] * this.weights[i];
		}
		sum += this.bias;
		
		return activationFunction(sum, 1);
	}
	
	public double activationFunction(double sum, int act) {
		switch(act) {
			case 1:{
				return (sum > 0)?1:0;
			}
		}
		
		return -1;
		
	}
	
	public void train(double[][] inputs, double[] targets, double lRate, int episodes) {
		
		int weightsNo = inputs[0].length;
		int trainingNo = targets.length;
		
		double delta_w = 0;
		double delta_b = 0;
		double sumError = 0;
		
		for(int i = 0; i < episodes; i++) {
			for(int j = 0; j < weightsNo; j++) {//each weight
				
				delta_w = 0;
				delta_b = 0;
				sumError = 0;
				
				for(int n = 0; n < trainingNo; n++) {//each training sample
					double o_n = calcOutput(inputs[n]);
					double error = Math.abs(targets[n] - o_n);
					
					sumError += error;
					delta_w += error * inputs[n][j];
				}
				
				//updating
				this.weights[j] += delta_w * lRate;
				
				for(int huh = 0; huh < trainingNo; huh++) {
					double o_huh = calcOutput(inputs[huh]);
					double error = Math.abs(targets[huh] - o_huh);
					
					delta_b += error * 1;
				}
				
				this.bias += lRate * delta_b;
				
				System.out.println("Iteration " + i +  " w1 = " + this.weights[0] + " w2 = " + this.weights[1] + " Bias: " + this.bias + " Error: " + sumError);
				
			}
		}
		
	}
}

