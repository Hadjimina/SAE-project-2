// Not safe

public class Test_leftGreaterRight2 {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		int i = 0;
		int k = 5;
		r.weldBetween(k, i);
	}
}
