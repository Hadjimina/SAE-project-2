//tests multiple robots
public class Test_while3 {
	public static void m1(int j) {
		Robot r = new Robot(-20, 20);
		int i = 0;
		while (i < 8) {
			i++;			
		}
		r.weldAt(i+2);
		r.weldBetween(i-10, i);
	}
}
