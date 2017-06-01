// SHOULD NOT BE SAFE

public class Test_leftGreaterRight4 {
	public static void m1(int j) {
		Robot r = new Robot(6, 10);
		int k = 8;
		if (j > 5 && j < 10) {
			r.weldBetween(j, k);
		}
	}
}
