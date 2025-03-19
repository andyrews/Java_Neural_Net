package loader.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import Matrix.Matrix;
import Matrix.Loader.BatchData;
import Matrix.Loader.Loader;
import Matrix.Loader.MetaData;
import Matrix.Loader.test.TestLoader;

public class TestTestLoader {

	@Test
	void test() {
		
		int batchSize = 33;
		Loader testLoader = new TestLoader(600, batchSize);
		
		MetaData metData = testLoader.open();
		
		int numItems = metData.getNumberItems();
		
		int lastBatchSize = numItems % batchSize;
		
		int numBatches = metData.getNumberBatches();
		
		for(int i = 0; i < numBatches; i++) {
			BatchData bData = testLoader.readBatch();
			
			assertTrue(bData != null);
			
			int itemsRead = metData.getItemsRead();
			int inpSize = metData.getInputSize();
			int expSize = metData.getExpectedSize();
			
			Matrix input = new Matrix(inpSize, itemsRead, bData.getInputBatch());
			Matrix expected = new Matrix(expSize, itemsRead, bData.getExpectedBatch());
			
			assertTrue(input.sum() != 0);
			assertTrue(expected.sum() == itemsRead);
			
			if(i == numBatches - 1) 
				assertTrue(itemsRead == lastBatchSize);
			else
				assertTrue(itemsRead == batchSize);
		}
	}

}
