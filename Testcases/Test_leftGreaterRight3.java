// Should be safe

public class Test_leftGreaterRight3 {
	public static void m1(int j) {
		Robot r = new Robot(6, 10);
		if (j > 5 && j < 10) {
			r.weldBetween(j, j+1);
		}
	}
}
