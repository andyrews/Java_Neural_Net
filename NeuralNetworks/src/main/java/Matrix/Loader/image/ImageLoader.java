package Matrix.Loader.image;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Matrix.Loader.BatchData;
import Matrix.Loader.Loader;
import Matrix.Loader.MetaData;

public class ImageLoader implements Loader{
	private String imageFileName;
	private String labelFileName;
	private int batchSize;
	
	private DataInputStream dsImgs;
	private DataInputStream dsLbls;
	
	private ImageMetaData mData;
	
	private Lock readLock = new ReentrantLock();
	
	public ImageLoader(String imageFileName, String labelFileName, int batchSize) {
		this.imageFileName = imageFileName;
		this.labelFileName = labelFileName;
		this.batchSize = batchSize;
	}

	@Override
	public ImageMetaData open() {
		try {
			
			dsImgs = new DataInputStream(new FileInputStream(imageFileName));
		}catch(Exception e) {
			throw new LoaderException("Cannot open " + imageFileName, e);
		}
		
		try {
			dsLbls = new DataInputStream(new FileInputStream(labelFileName));
		}catch(Exception e) {
			throw new LoaderException("Cannot open " + labelFileName, e);
		}
		mData = readMetaData();
		return mData;
	}

	private ImageMetaData readMetaData() {
		/*
		 * Label File format:
		 * 		magic num
		 * 		num of items
		 * 		label...
		 * 
		 * Image File format:
		 * 		magic num
		 * 		num of imgs
		 * 		num of rows
		 * 		num of cols
		 * 		pixel...
		 */
		
		mData = new ImageMetaData();
		
		int numItems = 0;
		
		try {
			int magicLabelNumber = dsLbls.readInt();
			
			if(magicLabelNumber != 2049)//2049 base on docs
			{
				throw new LoaderException("Label file " + labelFileName + " has wrong format");
			}
			
			numItems = dsLbls.readInt();
			
			mData.setNumberItems(numItems);
			
		} catch (IOException e) {
			throw new LoaderException("Unable to read " + labelFileName, e);
		}
		
		try {
			int magicImgNumber = dsImgs.readInt();
			
			if(magicImgNumber != 2051)//2051 base on documentation on MNIST db
			{
				throw new LoaderException("Image file " + imageFileName + " has wrong format");
			}
			
			if(dsImgs.readInt() != numItems){
				throw new LoaderException("Image file " + imageFileName + " has different number of items to " + labelFileName);
			}
			
			int height = dsImgs.readInt();
			int width = dsImgs.readInt();
			
			mData.setHeight(height);mData.setWidth(width);
			
			mData.setInputSize(width*height);

		} catch (IOException e) {
			throw new LoaderException("Unable to read " + imageFileName, e);
		}
		
		mData.setExpectedSize(10);
		mData.setNumberBatches((int)Math.ceil((double)numItems)/batchSize);
		
		return mData;
	}
	
	@Override
	public void close() {
		
		mData = null;
		
		try {
			dsImgs.close();
		}catch(Exception e) {
			throw new LoaderException("Cannot close " + imageFileName, e);
		}
		
		try {
			dsLbls.close();
		}catch(Exception e) {
			throw new LoaderException("Cannot close " + labelFileName, e);
		}
	}

	@Override
	public ImageMetaData getMetaData() {
		return mData;
	}

	@Override
	public BatchData readBatch() {
		readLock.lock();
		
		try {
			ImageBatchData bData = new ImageBatchData();
			
			int inpItemsRead = readInputBatch(bData);
			int expItemsRead = readExpectedBatch(bData);
			
			mData.setItemsRead(inpItemsRead);
			
			if(inpItemsRead != expItemsRead) {
				throw new LoaderException("Mismatch between images read and labels read");
			}
			
			return bData;
		}finally {
			readLock.unlock();
		}
		
	}

	private int readExpectedBatch(ImageBatchData bData) {
		try {
			var totalItemsRead = mData.getTotalItemsRead();
			var numberItems = mData.getNumberItems();
		
			var numberToRead = Math.min(numberItems - totalItemsRead, batchSize);
		
			byte[] labelData = new byte[numberToRead];
			var expectedSize = mData.getExpectedSize();
			
			var numRead = dsLbls.read(labelData,0, numberToRead);
			
			if(numRead != numberToRead) {
				throw new LoaderException("Could not read sufficient bytes from image data");
			}
			
			double[] data = new double[numberToRead * expectedSize];
			
			for(int i = 0; i < numberToRead; i++) {
				byte label = labelData[i];
				
				data[i * expectedSize + label] = 1; 
			}
			
			bData.setExpectedBatch(data);
			
			return numberToRead;
		}catch(IOException ioe) {
			throw new LoaderException("Error occured reading image data", ioe);
		}
	}

	private int readInputBatch(ImageBatchData bData) {
		
		try {
			var totalItemsRead = mData.getTotalItemsRead();
			var numberItems = mData.getNumberItems();
		
			var numberToRead = Math.min(numberItems - totalItemsRead, batchSize);
		
			var inpSize = mData.getInputSize();
			var numberBytesToRead = numberToRead * inpSize;
		
			byte[] imgData = new byte[numberBytesToRead];
		
			var numRead = dsImgs.read(imgData,0, numberBytesToRead);
			
			if(numRead != numberBytesToRead) {
				throw new LoaderException("Could not read sufficient bytes from image data");
			}
			
			double[] data = new double[numberBytesToRead];
			
			for(int i = 0; i < data.length; i++) {
				//convert byte to double using bitwise AND, then find the double value of a byte(/256.0)
				data[i] = (imgData[i] & 0xFF)/256.0;

			}
			
			bData.setInputBatch(data);
			
			return numberToRead;
		}catch(IOException ioe) {
			throw new LoaderException("Error occured reading image data", ioe);
		}
	}
}
