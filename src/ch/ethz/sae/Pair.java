package ch.ethz.sae;

public class Pair<T,U> {
	//CAUTION: Don't use as key, because values aren't final.
	private T value1;
	private U value2;

	public Pair(T v1, U v2) {
		value1 = v1;
		value2 = v2;
	}
	
	public void setV1(T v) {
		value1 = v;
	}
	
	public T getV1() {
		return value1;
	}
	
	public void setV2(U v) {
		value2 = v;
	}
	
	public U getV2() {
		return value2;
	}

}
