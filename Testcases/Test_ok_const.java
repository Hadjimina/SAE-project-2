//tests weld at and weld between only with constants, weld at and weld between okay
public class Test_ok_const {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		r.weldAt(2);
		r.weldBetween(-1, 5);					
	}

}
