package Matrix;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

class MatrixTest {	
	private Random r = new Random();
	private int row = 3, col = 4;
	
	@Test
	public void testGetGreatestRowNumber() {
		double [] values = {2,-6,7,7,2,-6,11,-1,1};
		Matrix m = new Matrix(3,3, i->values[i]);
		
		Matrix result = m.getGreatestRowNumbers();
		
		double [] expectedValues = {2,1,0};
		Matrix expected = new Matrix(1,3,i->expectedValues[i]);
		System.out.println(result + "\n" + m);
		
		assertTrue(expected.equals(result));
	}
	
	//@Test
	public void testAverageColumn() {
		Matrix m = new Matrix(row, col, i->2 * i - 3);
		
		double averageIndex = (col - 1)/2.0;
		
		Matrix expected = new Matrix(row, 1);
		expected.modify((ro, co, va)->2 * (ro*col + averageIndex) - 3);
		
		Matrix result = m.averageColumn();
		
		System.out.println(m + "\n" + expected + "\n" + result);
		
		assertTrue(expected.equals(result));
	}
	
	//@Test
	public void testTranspose() {
		Matrix m = new Matrix(2,3, i->i);
		
		Matrix r = m.transpose();
		
		System.out.println(m+"\n"+r);
	}
	
	//@Test
	public void testAddInc() {
		Matrix m = new Matrix(5,8, i->r.nextGaussian());
		
		int r = 3, col = 2;
		double inc = 10;
		
		Matrix res = m.addIncrement(r, col, inc);
		
		double incVal = res.get(r,col);
		assertTrue(Math.abs(incVal - (m.get(r,col)+inc)) < 0.00001);
		
		System.out.println(m + "\n" + res);
	}
	
	//@Test
	public void testSoftmax() {
		Matrix res = new Matrix(5, 8, i->r.nextGaussian());
		
		Matrix result = res.softmax();
		
		
		
		System.out.println(res + " \n" + result);
		
		double[] cSums = new double[8];
		
		result.forEach((r,c,v)->{
			assertTrue(v >= 0 && v <= 1);
			cSums[c] += v;
		});
		
		for(var sum : cSums) {
			assertTrue(Math.abs(sum - 1.0) < 0.00001);
		}
	}
	
	//@Test
	public void testSum() {
		Matrix res = new Matrix(4,5,i->i);
		
		Matrix r = res.sumColumns();
		System.out.println(res + "\n" + r);
		
		double[] expected = {+30.00000,   +34.00000,   +38.00000,   +42.00000,   +46.00000};
		Matrix e = new Matrix(1,5,i->expected[i]);
		
		//assertTrue(e.equals(r));
		
		
	}
	
	//@Test
	public void testEquals() {
		Matrix m1 = new Matrix(row,col,h-> 0.5 * (h-6));
		Matrix m2 = new Matrix(row,col,h-> 0.5 * (h-6));
		Matrix m3 = new Matrix(row,col,h-> 0.5 * (h-6.2));
		
		assertTrue(m1.equals(m2));
		assertFalse(m1.equals(m3));
	}
	
	//@Test 
	public void testMultipl() {
		double[] expectedVal = {10,13,28,40};
		Matrix m1 = new Matrix(2,3, i->i);
		Matrix m2 = new Matrix(3,2, i->i);
	
		Matrix m3 = new Matrix(2,2, i->expectedVal[i]);
		
		Matrix result = m1.multiply(m2);
		//System.out.println(result);
		
		assertTrue(m3.equals(result));
	}
	//@Test
	public void multiplySpeed() {
		int rows = 500, cols = 500, mid = 50;
		
		Matrix m1 = new Matrix(rows,mid, i->i);
		Matrix m2 = new Matrix(mid,cols, i->i);
		
		var start = System.currentTimeMillis();
		m1.multiply(m2);
		var end = System.currentTimeMillis();
		
		System.out.printf("Time: %dms\n", end-start);
	}
	
	//@Test
	public void testAdd() {
		Matrix m1 = new Matrix(2,2, i->i);
		Matrix m2 = new Matrix(2,2, i->i*1.5);
		Matrix expect = new Matrix(2,2, i->i * 2.5);
		
		Matrix result = m1.apply((index, value) -> value + m2.get(index));
		
		assertTrue(expect.equals(result));

	}
	
	//@Test
	public void testMultiply() {
		Matrix m = new Matrix(row,col,h-> 0.5 * (h-6));
		double x = 0.5;
			
		Matrix expected = new Matrix(row,col,h-> x * 0.5 * (h-6));
		
		Matrix res = m.apply((index, value) -> x * value);
		
		//System.out.println(res);
		
		assertTrue(res.equals(expected));
		
		assertTrue(Math.abs(res.get(1) + 1.25000) < 0.0001);
	}
	
	//@Test
	public void testToString() {
		Matrix m = new Matrix(row,col,i->i*2);
		String text = m.toString();
			
		//System.out.println(text);
			
		double[] expected = new double[row*col];
			
		for(int i = 0; i < expected.length; i++) {
			expected[i] = i * 2;
		}
			
		var r = text.split("\n");
			
		assertTrue(r.length == row);
			
		int index = 0;
			
		for(var ro : r) {
			var val = ro.split("\\s+");
				
			for(var c : val) {
				if(c.length() == 0)continue;
				var doubleVal = Double.valueOf(c);
					
				assertTrue(Math.abs(doubleVal-expected[index]) < 0.0001);
					
				++index;
			}
		}
	}
}
