//Tests while, weld at and weld between shouldn't be okay
public class Test_notok_while {
		public static void m1(int j) {
			Robot r = new Robot(-2, 6);
			int i = 0;
			while (i < 5) {
				i++;			
			} 
			r.weldAt(i+2);
			r.weldBetween(i-10, i);
		}
}
