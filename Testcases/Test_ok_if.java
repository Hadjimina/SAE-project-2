//Tests if else statement, weld at and weld between are okay
public class Test_ok_if {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		int i = 0;
		if (j > 2 && j < 6) {
			r.weldAt(j - 2);
			r.weldBetween(j - 4, j + 1);			
		} else if (j > -2 && j < 6){
			r.weldAt(j);
			r.weldBetween(j, j);
		}
		r.weldAt(i);
	}
}
