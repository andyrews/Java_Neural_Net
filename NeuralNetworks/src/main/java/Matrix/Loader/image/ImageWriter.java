package Matrix.Loader.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import Matrix.NeuralNetwork;
import Matrix.Loader.BatchData;

public class ImageWriter {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length == 0) {
			System.out.println("usage: [app] <MNIST DATA DIRECTORY>");
			return;
		}
		
		String directory = args[0];
		
		if(!new File(args[0]).isDirectory()) {
			System.out.println("'" + directory + "' is not a directory");
			return;
		}
		
		new ImageWriter().run(directory);
	}
	
	private int convertOneHotToInt(double[] labelData, int offset, int oneHotSize) {
		double maxVal = 0;
		int  maxInd = 0;
		
		for(int i = 0; i < oneHotSize; i++) {
			if(labelData[offset + i] > maxVal) {
				maxVal = labelData[offset + i];
				maxInd = i;
			}
		}
		return maxInd;
	}
	
	public void run(String directory) {
		final String trainImgs = String.format("%s%s%s", directory, File.separator,"train-images.idx3-ubyte");
		final String trainLbls = String.format("%s%s%s", directory, File.separator,"train-labels.idx1-ubyte");
		final String testImgs = String.format("%s%s%s", directory, File.separator, "t10k-images.idx3-ubyte");
		final String testLbls = String.format("%s%s%s", directory, File.separator, "t10k-labels.idx1-ubyte");
	
		int batchSize = 900;
		
		ImageLoader trainLoader = new ImageLoader(trainImgs, trainLbls, batchSize);
		ImageLoader testLoader = new ImageLoader(testImgs, testLbls, batchSize);
		
		ImageLoader loader = testLoader;
		ImageMetaData mData = loader.open();
		
		var neuralNet = NeuralNetwork.load("mnistNeural0.net");
		
		int imgWidth = mData.getWidth();
		int imgHeight = mData.getHeight();
		
		int labelSize = mData.getExpectedSize();
		
		for(int i = 0; i < mData.getNumberBatches(); i++) {//per batch iteration
			BatchData bData = testLoader.readBatch();
			
			var numberImages = mData.getItemsRead();
			
			int horizontalImgs = (int)Math.sqrt(numberImages);
			
			while(numberImages % horizontalImgs != 0) {
				++horizontalImgs;
			}
			
			int verticalImgs = numberImages / horizontalImgs;
			
			int canvasWidth = horizontalImgs * imgWidth;
			int canvasHeight = verticalImgs * imgHeight;
			
			String montagePath = String.format("montage%d.jpg", i);
			System.out.println("Writing " + montagePath);
			
			var montage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
			
			double[] pixelData = bData.getInputBatch();
			double[] labelData = bData.getExpectedBatch();
			int imgSize = imgWidth * imgHeight;
			
			boolean[] correct = new boolean[numberImages];
			
			for(int n = 0; n < numberImages; n++) {//checks predictions
				double[] singleImage = Arrays.copyOfRange(pixelData, n*imgSize, (n + 1) * imgSize);
				double[] singleLabel = Arrays.copyOfRange(labelData, n*labelSize, (n + 1) * labelSize);
			
				double[] predictedLabel = neuralNet.predict(singleImage);
			
				int predicted = convertOneHotToInt(predictedLabel, 0, labelSize);
				int actual = convertOneHotToInt(singleLabel, 0, labelSize);
				
				correct[n] = predicted == actual;
			}
			
			for(int pixIt = 0; pixIt < pixelData.length; pixIt++) {//pixel per batch
				int imgNumber = pixIt / imgSize;
				int pixelNumber = pixIt % imgSize;
				
				int montageRow = imgNumber / horizontalImgs;
				int montageCol = imgNumber % horizontalImgs;
				
				int pixelRow = pixelNumber / imgWidth;
				int pixelCol = pixelNumber % imgWidth;
				
				int x = montageCol * imgWidth + pixelCol;
				int y = montageRow * imgHeight + pixelRow;
				
				double pixelVal = pixelData[pixIt];
				int color = (int)(0x100 * pixelVal);
				int pixelColor = 0;
				
				if(correct[imgNumber])
					pixelColor = color;
				else
					pixelColor = (color << 16);
					
				montage.setRGB(x, y, pixelColor);
			}
			
			try {
				ImageIO.write(montage, "jpg", new File(montagePath));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			StringBuilder sb = new StringBuilder();
			
			for(int labelIn = 0; labelIn < numberImages; labelIn++) {
				
				if(labelIn % horizontalImgs == 0) {
					sb.append("\n");
				}
				
				int label = convertOneHotToInt(labelData, labelIn * labelSize, labelSize);
				sb.append(String.format("%d", label));
			}
			String labelPath = String.format("labels%d.txt", i);
			System.out.println("Writing " + labelPath);
			try {
				FileWriter fw = new FileWriter(labelPath);
				fw.write(sb.toString());
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		loader.close();
	}
}