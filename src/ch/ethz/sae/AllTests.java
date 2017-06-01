package ch.ethz.sae;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ Test_1Test.class, Test_2Test.class, Test_3Test.class,
		Test_falseTest.class, Test_leftGreaterRight1Test.class,
		Test_leftGreaterRight2Test.class, Test_leftGreaterRight3Test.class,
		Test_leftGreaterRight4Test.class,
		Test_multiplicationInputVarsTest.class, Test_nestedwhileTest.class,
		Test_notok_constantTest.class, Test_notok_ifTest.class,
		Test_notok_notconstrTest.class, Test_notok_while1Test.class,
		Test_notok_whileTest.class, Test_ok_const_cornercasesTest.class,
		Test_ok_constTest.class, Test_ok_dowhileTest.class,
		Test_ok_forifTest.class, Test_ok_ifTest.class,
		Test_ok_multipleRobotsTest.class, Test_ok_while1Test.class,
		Test_ok_whileTest.class, Test_trueTest.class,
		Test_unknownFunction2Test.class, Test_unknownFunctionTest.class,
		Test_while2Test.class, Test_while3Test.class })
public class AllTests {

}
