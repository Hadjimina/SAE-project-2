//Tests while, weld at and weld between should be okay
public class Test_ok_while {
		public static void m1(int j) {
			Robot r = new Robot(-2, 6);
			int i = 0;
			while (i < 4) {
				i ++;			
			} 
			r.weldAt(i-2);
			r.weldBetween(i-5, i);
		}
}




