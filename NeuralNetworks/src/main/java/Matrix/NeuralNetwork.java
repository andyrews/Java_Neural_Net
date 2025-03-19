package Matrix;


import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Matrix.Loader.*;

public class NeuralNetwork implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Engine e;
	
	private int epochs = 20;
	private double initialLearningRate = 0.01;
	private double finalLearningRate = 0.001;
	private int threads = 2;
	
	transient private double learningRate;
	transient private Object lock = new Object();
	
	
	public NeuralNetwork() {
		this.e = new Engine();
	}
	
	public void setThreads(int t) {
		threads = t;
	}
	
	public void setScaleInitialWeights(double sc) {
		e.setScaleInitialWeights(sc);
	}
	
	public void add(Transform t, double... param) {
		this.e.add(t, param);
	}
	
	public void setLearningRates(double inLearning, double finLearning) {
		initialLearningRate = inLearning;
		finalLearningRate = finLearning;
	}
	
	public void setEpoch(int e) {
		epochs = e;
	}
	
	public double[] predict(double [] inpData) {
		Matrix inp = new Matrix(inpData.length, 1, i->inpData[i]);
		
		BatchResult bResult = e.forwardPass(inp);
		
		return bResult.getOutput().get();
	}
	
	public void fit(Loader trainL, Loader evalL) {
		learningRate = initialLearningRate;
		
		for(int ep = 0; ep < epochs; ep++) {
			System.out.printf("Epoch %3d", ep + 1);
			
			runEpoch(trainL, true);
			
			if(evalL != null)runEpoch(evalL, false);
			
			System.out.println();
			
			learningRate -= (initialLearningRate - finalLearningRate)/epochs;
		}
	}
								
	private void runEpoch(Loader loader, boolean mode) {
		//true = training, false = evaluating
		loader.open();
		
		var queue = createBatchTasks(loader, mode);
		consumeBatchTasks(queue, mode);
		
		loader.close();
	}

	private void consumeBatchTasks(LinkedList<Future<BatchResult>> batches, boolean mode) {
		int numBatches = batches.size();
		int index = 0;
		
		double avgLoss = 0;
		double avgPercentCorrect = 0;
		
		for(var batch : batches) {
			try {
				var batchResult = batch.get();
				
				if(!mode) {
					avgLoss += batchResult.getLoss();
					avgPercentCorrect += batchResult.getPercentCorrect();
				}
			}catch (Exception e) {
				throw new RuntimeException("Execution error: " + e.getMessage());
			}
			
			int printDot = numBatches/30;
			
			if(mode && index++ % printDot == 0) {
				System.out.print(".");
			}
		}
		
		if(!mode) {
			avgLoss /= batches.size();
			avgPercentCorrect /= batches.size();
			
			System.out.printf("Loss: %.3f -- Percent correct: %.2f", avgLoss, avgPercentCorrect);
		}
	}

	private LinkedList<Future<BatchResult>> createBatchTasks(Loader loader, boolean mode) {
		
		LinkedList<Future<BatchResult>> batches = new LinkedList<>();
		
		MetaData metadata = loader.getMetaData();
		int numBatches = metadata.getNumberBatches();

		var executor = Executors.newFixedThreadPool(threads);
		
		for(int i = 0; i < numBatches; i++) {
			batches.add(executor.submit(()->runBatch(loader, mode)));
		}
		
		executor.shutdown();
		
		return batches;
	}

	private BatchResult runBatch(Loader loader, boolean mode) {

		MetaData metData = loader.getMetaData();
	
		BatchData bData = loader.readBatch();
		
		int itemsRead = metData.getItemsRead();
		int inpSize = metData.getInputSize();
		int expSize = metData.getExpectedSize();
			
		Matrix input = new Matrix(inpSize, itemsRead, bData.getInputBatch());
		Matrix expected = new Matrix(expSize, itemsRead, bData.getExpectedBatch());
		
		BatchResult bRes = e.forwardPass(input);
		
		if(mode) {
			e.backwardPass(bRes, expected);
			
			synchronized(lock) {
				e.adjust(bRes, learningRate);
			}
		}else {
			e.evaluate(bRes, expected);
		}		
		return bRes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Epochs: %d\n", epochs));
		sb.append(String.format("Initial Learning Rate: %.5f\n", initialLearningRate));
		sb.append(String.format("Final Learning Rate: %.5f\n", finalLearningRate));
		sb.append(String.format("Threads: %d\n", threads));
		
		sb.append("\nEngine Configuration");
		sb.append("\n---------------------\n");
		sb.append(e);
		
		return sb.toString();
	}

	public boolean save(String file) {
		// TODO Auto-generated method stub
		try(var ds = new ObjectOutputStream(new FileOutputStream(file))){
			ds.writeObject(this);
		}catch(IOException io) {
			System.err.println("Unable to save to " + file);
			io.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static NeuralNetwork load(String file) {
		// TODO Auto-generated method stub
		
		NeuralNetwork neuralNetwork = null;
		
		try(var ds = new ObjectInputStream(new FileInputStream(file))){
			neuralNetwork = (NeuralNetwork)ds.readObject();
		}catch(Exception io) {
			System.err.println("Unable to load from " + file);
			io.printStackTrace();
		}
		
		return neuralNetwork;
	}
	
	public Object readResolve() {
		this.lock = new Object();
		return this;
	}
}
