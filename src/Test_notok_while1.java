//Tests while stmt, should not be ok
public class Test_notok_while1 {
		public static void m1(int j) {
			Robot r = new Robot(-2, 6);
			int i = 0;
			int f = i;
			while (f < 5) {
				r.weldAt(f*2);
				r.weldBetween(f-1,f*3);
				f++;
			} 
		}
}