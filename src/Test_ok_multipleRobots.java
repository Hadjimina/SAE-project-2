//tests multiple robots
public class Test_ok_multipleRobots {
	public static void m1(int j) {
		Robot r = new Robot(-6, 6);
		Robot s = new Robot(7, 8);
		int i = 0;
		int k = 8;
		while (i < 4) {
			i++;			
		}
		r.weldAt(i+2);
		s.weldAt(k);
		r.weldBetween(i-10, i);
		s.weldBetween(k-1, k);
	}
}
