//weldAt not safe
//weltBetween safe
public class Test_unknownFunction2 {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		f();
		r.weldAt(2);
		r.weldBetween(-1, 5);					
	}
	
	public static void f() {
		Robot s = new Robot(-2, 6);
		s.weldAt(-10);
	}
}
