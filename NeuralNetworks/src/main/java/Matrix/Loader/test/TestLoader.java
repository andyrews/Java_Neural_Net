package Matrix.Loader.test;

import Matrix.Util;
import Matrix.Loader.BatchData;
import Matrix.Loader.MetaData;

public class TestLoader implements Matrix.Loader.Loader {
	private MetaData metData;
	
	private int numberItems = 9;
	private int inputSize = 500;
	private int expectedSize = 3;
	private int numberBatches;
	private int totalItemsRead;
	private int itemsRead;
	private int batchSize = 0;

	public TestLoader(int numberItems, int batchSize) {
		
		this.numberItems = numberItems;
		this.batchSize = batchSize;
		
		metData = new TestMetaData();
		metData.setNumberItems(numberItems);

		numberBatches = numberItems/batchSize;

		if(numberItems % batchSize != 0) {
			numberBatches++;
		}//we want a numberBatch integer value, to account for the remainder of the batch
		
		metData.setNumberBatches(numberBatches);
		metData.setInputSize(inputSize);
		metData.setExpectedSize(expectedSize);
	}
	
	@Override
	public MetaData open() {
		return this.metData;
	}

	@Override
	public void close() {
		totalItemsRead = 0;
	}

	@Override
	public MetaData getMetaData() {
		return this.metData;
	}

	@Override
	public synchronized BatchData readBatch() {
		if(totalItemsRead == numberItems) {
			return null;
		}
		itemsRead = batchSize;
		
		totalItemsRead += itemsRead;
		
		int excessItems = totalItemsRead - numberItems;
		if(excessItems > 0) {
			totalItemsRead -= excessItems;
			itemsRead -= excessItems;
		}
		
		var io = Util.generateTrainingArrays(inputSize, expectedSize, itemsRead);
	
		var batchD = new TestBatchData();
		batchD.setInputBatch(io.getInput());
		batchD.setExpectedBatch(io.getOutput());
		
		metData.setTotalItemsRead(totalItemsRead);
		metData.setItemsRead(itemsRead);
		return batchD;
	}
}
