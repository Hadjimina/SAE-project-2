//tests weld at and weld between only with constants, weld at and weld between not okay, 
public class Test_notok_constant {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		r.weldAt(7);
		r.weldBetween(-2, 7);					
	}
}
