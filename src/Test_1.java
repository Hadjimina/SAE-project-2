public class Test_1 {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		if (j > 2 && j < 6) {
			r.weldAt(j - 2);
			r.weldBetween(j - 4, j + 1);
			j = 5;
		}
		else {
			r.weldAt(j);
			j = 5;
		}
		j = j + 1;
	}
}
