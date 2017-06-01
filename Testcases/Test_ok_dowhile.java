public class Test_ok_dowhile {
	public static void m1(int j) {
		Robot r = new Robot(-8, 8);
		int i = 8;

		do{
			r.weldBetween(i*-1,i);
			i *= -1;
			
		}while(i==8);
	}
}
