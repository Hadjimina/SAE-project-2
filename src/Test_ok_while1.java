//Tests while stmt, should be ok
public class Test_ok_while1 {
		public static void m1(int j) {
			Robot r = new Robot(-2, 6);
			int i = 0;
			int f = i;
			while (f < 5) {
				r.weldAt(f);
				r.weldBetween(f-1,f);			
			} 
		}
}
