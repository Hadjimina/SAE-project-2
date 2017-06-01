//tests multiple robots
public class Test_while2 {
	public static void m1(int j) {
		Robot r = new Robot(-6, 6);
		int i = 0;
		while (i < 4) {
			i++;			
		}
		r.weldAt(i+2);
		r.weldBetween(i-10, i);
	}
}
