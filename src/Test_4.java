public class Test_4 {
	public static void m2(int a) {
		Robot r = new Robot(0, 7);
		Robot rr = new Robot(2,3);
		r = rr;
		if (2*a < 9 && a > 0) {
			r.weldBetween(a, a + (a + 2));
			if (a > 5) {
				r.weldAt(a + 4);
			}
			rr.weldBetween(2,4);
		}
	}
}

