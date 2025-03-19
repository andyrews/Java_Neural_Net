package App;

import Matrix.NeuralNetwork;
import Matrix.Transform;
import Matrix.Loader.*;
import Matrix.Loader.test.*;

public class GeneratedDataApp {
	public static void main(String[] args) {
		String file = "neurally.net";
		
		NeuralNetwork neuralNet = NeuralNetwork.load(file);
		
		if(neuralNet == null) {
			System.out.println("Unable to load network from save. Default creating...");
			
			int inpR = 10, outR = 3;
			
			neuralNet = new NeuralNetwork();
			neuralNet.add(Transform.DENSE, 100,inpR);
			neuralNet.add(Transform.RELU);
			neuralNet.add(Transform.DENSE, 50);
			neuralNet.add(Transform.RELU);
			neuralNet.add(Transform.DENSE, outR);
			neuralNet.add(Transform.SOFTMAX);
			
			neuralNet.setThreads(5);
			neuralNet.setEpoch(5);
			neuralNet.setLearningRates(0.02, 0.001);
			
		}else {
			System.out.println("Loaded from " + file);
		}
		System.out.println(neuralNet);
		
		Loader trainL = new TestLoader(60_000, 32);
		Loader testL = new TestLoader(10_000, 32);
		
		neuralNet.fit(trainL, testL);
		
		if(neuralNet.save(file)) {
			System.out.println("Saved to: " + file);
		}else System.out.println("Unable to save to: " + file);
	}
}
