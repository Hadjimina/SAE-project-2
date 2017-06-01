public class Test_5 {
	public static void m5(int j) {
		Robot r = new Robot(0, 8);
		if (j < 6) {
			r.weldAt(j - 2);
		}
		if (j == 6) {
			r.weldAt(j - 2);
		}
		if (j <= 6) {
			r.weldAt(j - 2);
		}
		if (j != 6) {
			r.weldAt(j - 2);
		}
		if (j >= 6) {
			r.weldAt(j - 2);
		}
		if (j > 6) {
			r.weldAt(j - 2);
		}
	}
}
