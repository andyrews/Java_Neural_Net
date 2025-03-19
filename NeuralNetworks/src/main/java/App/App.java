package App;

import java.io.File;
import java.io.IOException;

import Matrix.NeuralNetwork;
import Matrix.Transform;
import Matrix.Loader.BatchData;
import Matrix.Loader.Loader;
import Matrix.Loader.MetaData;
import Matrix.Loader.image.ImageLoader;
import Matrix.Loader.test.TestLoader;

public class App {

	public static void main(String[] args) {
		final String file = "mnistNeural0.net";
		if(args.length == 0) {
			System.out.println("usage: [app] <MNIST DATA DIRECTORY>");
			return;
		}
		
		String directory = args[0];
		
		if(!new File(args[0]).isDirectory()) {
			System.out.println("'" + directory + "' is not a directory");
			return;
		}
		
		final String trainImgs = String.format("%s%s%s", directory, File.separator,"train-images.idx3-ubyte");
		final String trainLbls = String.format("%s%s%s", directory, File.separator,"train-labels.idx1-ubyte");
		final String testImgs = String.format("%s%s%s", directory, File.separator, "t10k-images.idx3-ubyte");
		final String testLbls = String.format("%s%s%s", directory, File.separator, "t10k-labels.idx1-ubyte");
	
		Loader trainLoader = new ImageLoader(trainImgs, trainLbls, 32);
		Loader testLoader = new ImageLoader(testImgs, testLbls, 32);
	
		MetaData metadata = trainLoader.open();
		int inpSize = metadata.getInputSize();
		int outSize = metadata.getExpectedSize();
		trainLoader.close();
		
		NeuralNetwork neuralNet = NeuralNetwork.load(file);
		
		if(neuralNet == null) {
			System.out.println("Unable to load network from save. Default creating...");
			neuralNet = new NeuralNetwork();
			
			neuralNet.setScaleInitialWeights(0.2);
			neuralNet.setThreads(5);
			neuralNet.setEpoch(50);
			neuralNet.setLearningRates(0.02, 0.001);
			
			neuralNet.add(Transform.DENSE, 200, inpSize);
			neuralNet.add(Transform.RELU);
			neuralNet.add(Transform.DENSE, outSize);
			neuralNet.add(Transform.SOFTMAX);
		
		}else {
			System.out.println("Loaded from " + file);
		}
		System.out.println(neuralNet);
		int inpR = 10, outR = 3;
		
		neuralNet.fit(trainLoader, testLoader);
		
		if(neuralNet.save(file)) {
			System.out.println("Saved to: " + file);
		}else System.out.println("Unable to save to: " + file);
	}
}
