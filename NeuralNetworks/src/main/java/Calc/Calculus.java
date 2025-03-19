package Calc;

import static org.junit.Assert.assertTrue;

import java.util.function.DoubleFunction;

public class Calculus {
	
	private static final double INC = 0.00001;
	
	public static double fnc1(double x) {
		return 3.7 * x + 5.3;
	}
	
	public static double fnc2(double x) {
		return x*x - 3.23;
	}
	
	public static double fnc3(double y1, double y2) {
		return y1*y2 + 4.7 * y1;
	}
	
	public static double fnc4(double x) {
		return fnc1(x) * fnc2(x) + 4.7 * fnc1(x);
	}
	
	public static double differentiate(DoubleFunction<Double> e, double x) {
		
		double out1 = e.apply(x);
		double out2 = e.apply(x+INC);

		return (out2-out1)/INC;
	}
	
	public static void main(String[] args) {
		double x = 5.76;
		double y1 = fnc1(x);
		double y2 = fnc2(x);
		double z = fnc3(y1,y2);
		
		
		double dy1dx = differentiate(Calculus::fnc1,x);
		double dy2dx = differentiate(Calculus::fnc2,x);
		double dzdy1 = differentiate(y->fnc3(y,y2),dy1dx);
		double dzdy2 = differentiate(y->fnc3(y1,y),dy2dx);
		double dzdx = dzdy1 * dy1dx + dzdy2 * dy2dx;
	
		double dzdxApp = differentiate(Calculus::fnc4, x);
		//System.out.println(dzdy1 + "\n" + dzdy2);
	System.out.println(dzdx + "\n" + dzdxApp + "\n" + fnc4(x) + " " + z);
	}
}
