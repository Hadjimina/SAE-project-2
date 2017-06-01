//Tests if else statement, weld at and weld between should not be ok
public class Test_notok_if {
		public static void m1(int j) {
			Robot r = new Robot(-2, 6);
			if (j > 2) {
				r.weldAt(j - 2);
				r.weldBetween(j - 4, j + 1);			
			} else {
				r.weldAt(j);
				r.weldBetween(j, j);
			}
		}
}
