//tests weld at and weld between only with constants, weld at and weld between okay, corner of interval
public class Test_ok_const_cornercases {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		r.weldAt(-2);
		r.weldBetween(-2, 6);					
	}
}
