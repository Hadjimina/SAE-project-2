public class Test_nestedwhile {
	public static void m1(int j) {
		Robot r = new Robot(-7, -1);
		int i = 8;
		while(i>0)
		{
			while(j <= 0){
				while( j > -8){
					i--;
					r.weldAt(j);
					r.weldBetween(-i,j);
				}
			}
		}
		r.weldAt(i);
//weldAt ok
//weldBetween notok
	}
}
