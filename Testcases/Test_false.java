public class Test_false {
	public static void m2(int a) {
		Robot r = new Robot(0, 7);
		if (1 < 0) {
			r.weldAt(42);
			r.weldBetween(121, 4326);
		}
	}
}

