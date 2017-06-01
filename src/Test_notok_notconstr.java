//Tests when everything should be top (not sure, but polyhedra doesn't deal with mult of local variables
public class Test_notok_notconstr {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		int i = 1;
		int f = 1;
		int q = j * j;
		int v = j * f;
		r.weldAt(q);
		r.weldAt(v);
		r.weldBetween(f, v);
	}
}
