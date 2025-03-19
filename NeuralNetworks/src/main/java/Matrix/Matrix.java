package Matrix;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Matrix implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String NUM_FORMAT = "%+12.5f";
	private double tolerance = 0.000001;
	
	private int rows;
	private int col;
	///////////////////////////////////////////////////
	public interface Producer{
		double produce(int index);
	}
	
	public interface IndexValueProducer{
		double produce(int index, double val);
	}
	
	public interface ValueProducer{
		double produce(double val);
	}
	
	public interface IndexValueConsumer{
		void consume(int in, double val);
	}
	
	public interface RowColValConsumer{
		void consume(int r, int c, double val);
	}
	
	public interface RowColIndexValConsumer{
		void consume(int r, int c, int index, double val);
	}
	
	public interface RowColProducer{
		double produce(int row, int col, double val);
	}
	///////////////////////////////////////////////////
	private double[] a;
	
	public Matrix(int r, int c) {
		this.rows = r;
		this.col = c;
		this.a = new double[r*c];
	}
	
	public Matrix(int r, int c, Producer prod) {
		this(r,c);
		for(int i = 0; i < this.a.length; i++) {
			a[i] = prod.produce(i);
		}
	}
	
	public Matrix(int r, int c, double[] val) {
		//this puts the elements in val array to different columns(not rows) in the a[]
		this.rows = r;
		this.col = c;
		
		Matrix temp = new Matrix(c,r);//transposed matrix
		temp.a = val;
		
		Matrix tMatrix = temp.transpose();
		this.a = tMatrix.a;
	}
	
	public double sum() {
		double sum = 0;
		
		for(var v : a) {
			sum += v;
		}
		
		return sum;
	}
	
	public Matrix getGreatestRowNumbers() {
		Matrix result = new Matrix(1, this.col);
		
		double[] greatest = new double[col];
		
		for(int i = 0; i < this.col; i++) {
			greatest[i] = Double.MIN_VALUE;
		}
		
		forEach((r,c,v)->{
			if(v > greatest[c]) {
				greatest[c] = v;
				result.a[c] = r;
			}
		});
		
		return result;
	}
	
	public Matrix sumColumns() {
		Matrix r = new Matrix(1,this.col);
		
		int in = 0;
		
		for(int i = 0; i < this.rows; i++) {
			for(int c = 0; c < this.col; c++) {
				r.a[c] += a[in++];
			}
		}
		return r;
	}
	
	public double get(int row, int co) {
		return this.a[row*this.col + co];
	} 
	
	public Matrix addIncrement(int r, int c, double inc) {
		Matrix result = apply((ind, val)->a[ind]);
		
		double origVal = get(r,c);
		
		double newVal = origVal + inc;
		
		result.set(r, c, newVal);
		
		return result;
	}
	
	public void set(int row, int col, double val) {
		this.a[row*this.col + col] = val;
	}
	
	public int getRows() {
		return this.rows;
	}
	
	public int getCol() {
		return this.col;
	}
	
	public Matrix apply(IndexValueProducer prod) {
		Matrix res = new Matrix(this.rows,this.col);
		
		for(int i = 0; i < this.a.length; i++) {
			res.a[i] = prod.produce(i, this.a[i]);
		}
		
		return res;
	}
	
	public Matrix transpose() {
		Matrix resu = new Matrix(col,rows);
		
		for(int i = 0; i < a.length; i++) {
			int r = i/col;
			int c = i% col;
			
			resu.a[c*rows+r] = a[i];
			
		}
		
		return resu;
	}
	
	public Matrix softmax() {
		Matrix res = new Matrix(this.rows, this.col, i->Math.exp(a[i]));
		
		Matrix colSum = res.sumColumns();
		
		res.modify((row,col,v)->{
			return v/colSum.get(col);
		});
		
		return res;
	}
	
	public Matrix modify(RowColProducer prod) {
		
		int i = 0;
		for(int r = 0; r < this.rows; r++) {
			for(int c = 0; c < this.col; c++) {
				
				a[i] = prod.produce(r, c, a[i]);
				
				++i;
			}
		}
		
		return this;
	}
	
	public Matrix modify(IndexValueProducer prod) {
		
		int i = 0;
		for(int r = 0; r < this.rows; r++) {
			for(int c = 0; c < this.col; c++) {
				
				a[i] = prod.produce(i, a[i]);
				
				++i;
			}
		}
		
		return this;
	}
	
	public Matrix modify(ValueProducer prod) {
		for(int i = 0; i < this.a.length; i++) {
			this.a[i] = prod.produce(a[i]);
		}
		
		return this;
	}
	
	public void forEach(RowColIndexValConsumer con) {
		int in = 0;
		for(int i = 0; i < this.rows; i++) {
			for(int j = 0; j < this.col; j++) {
				con.consume(i, j, in, a[in++]);
			}
		}
	}
	
	public void forEach(RowColValConsumer con) {
		int in = 0;
		for(int i = 0; i < this.rows; i++) {
			for(int j = 0; j < this.col; j++) {
				con.consume(i, j, a[in++]);
			}
		}
	}
	
	public void forEach(IndexValueConsumer co) {
		for(int i = 0; i < this.a.length; i++) {
			co.consume(i, this.a[i]);
		}
	}
	
	public Matrix multiply(Matrix m1) {
		Matrix result = new Matrix(rows, m1.col);
		
		assert col == m1.rows:"Cannot multiply";
		
		for(int i = 0; i < result.rows; i++) {
			for(int j = 0; j < result.col; j++) {
				for(int k = 0; k < this.col; k++) {
					result.a[i * result.col + j] += a[i*col+k]* m1.a[j+k*m1.col];
				
				}
				
				
			}
		}
		
		return result;
	}
	
	
	
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	public Matrix averageColumn() {
		Matrix res = new Matrix(this.rows, 1);
		
		forEach((ro,co,ind,va)->{
			res.a[ro] += va / this.col;		
		});
		
		return res;
	}
	
	@Override//Ill just leave it here
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(a);
		result = prime * result + Objects.hash(col, rows);
		return result;
	}
	
	public double get(int index) {
		return this.a[index];
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Matrix other = (Matrix) obj;
		
		for(int i = 0 ; i < this.a.length; i++) {
			if(Math.abs(this.a[i] - other.a[i]) > tolerance) return false;
		}
		
		return true;
	}

	public String toString(boolean showVal) {
		if(showVal)return toString();
		return rows +" x "+col;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		/*int i = 0;
		for(int row = 0; row < rows; row++) {
			for(int col = 0; col < this.col; col++) {
				sb.append(String.format(NUM_FORMAT,a[i++]));
			}
			sb.append("\n");
		}*/
		
		for(int b = 0; b < rows*col; b++) {
			if(b % this.col == 0 && b != 0) {
				sb.append("\n");
			}
			sb.append(String.format(NUM_FORMAT,a[b]));
		}
		return sb.toString() + "\n";
	}

	public double[] get() {
		return a;
	}
}
