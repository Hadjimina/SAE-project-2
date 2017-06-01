package ch.ethz.sae;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class Test_notok_constantTest {
	private ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final boolean ansT = true;
	private final boolean ansF = false;
	private PrintStream old;
	private static String [] args;

	private void init(){
		args = new String[1];
		args[0] = "Test_notok_constant";		//Test File name
	}
	
//	@Before
//	public void setUpStreams(){
//		this.old = System.out;
//		System.setOut(new PrintStream(outContent));
//	}
	
//	@After
//	public void cleanUpStreams(){
//		//System.out.flush();
//		System.setOut(old);
//	}
	
	@Test
	public void test() {
		
		this.old = System.out;
		System.setOut(new PrintStream(outContent));
		
		init();
		
		Verifier.main(args);
		
		String output = new String(outContent.toByteArray());
		
		boolean ans1, ans2;
		ans1 = output.contains(args[0]+" WELD_AT_OK");
		ans2 = output.contains(args[0]+" WELD_BETWEEN_OK");
		System.setOut(old);
		System.out.println(args[0]);
		System.out.println("weldAt is safe: " + (ans1));
		System.out.println("weltBetween is safe: " + (ans2) + "\n");
		
		
		assertEquals(ansF, ans1);
		assertEquals(ansT, output.contains(args[0]+" WELD_AT_NOT_OK"));
		assertEquals(ansF,ans2);
		assertEquals(ansT,output.contains(args[0]+" WELD_BETWEEN_NOT_OK"));
	}
}
