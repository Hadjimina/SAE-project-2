package ch.ethz.sae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Interval;
import apron.Manager;
import apron.MpqScalar;
import apron.Polka;
import apron.Tcons1;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Intern;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import soot.IntegerType;
import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.Unit;
import soot.UnknownType;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractJimpleIntBinopExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JNeExpr;
import soot.jimple.internal.JSubExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import soot.util.Chain;

// Implement your numerical analysis here.
public class Analysis extends ForwardBranchedFlowAnalysis<AWrapper> {
	boolean flag;

	private static final int WIDENING_THRESHOLD = 6;

	private HashMap<Unit, Counter> loopHeads, backJumps;
	
	public List<JInvokeStmt> weldAtCalls = new ArrayList<JInvokeStmt>();
	public List<JInvokeStmt> weldBetweenCalls = new ArrayList<JInvokeStmt>();
	public List<JInvokeStmt> robotProperties = new ArrayList<JInvokeStmt>();
	
	
	private void recordIntLocalVars() {

		Chain<Local> locals = g.getBody().getLocals();

		int count = 0;
		Iterator<Local> it = locals.iterator();
		while (it.hasNext()) {
			JimpleLocal next = (JimpleLocal) it.next();
			if (next.getType() instanceof IntegerType)
				count += 1;
		}

		local_ints = new String[count];

		int i = 0;
		it = locals.iterator();
		while (it.hasNext()) {
			JimpleLocal next = (JimpleLocal) it.next();
			String name = next.getName();
			if (next.getType() instanceof IntegerType)
				local_ints[i++] = name;
		}
	}

	private void recordIntClassVars() {

		Chain<SootField> ifields = jclass.getFields();

		int count = 0;
		Iterator<SootField> it = ifields.iterator();
		while (it.hasNext()) {
			SootField next = it.next();
			if (next.getType() instanceof IntegerType)
				count += 1;
		}

		class_ints = new String[count];

		int i = 0;
		it = ifields.iterator();
		while (it.hasNext()) {
			SootField next = it.next();
			String name = next.getName();
			if (next.getType() instanceof IntegerType)
				class_ints[i++] = name;
		}
	}

	/* Builds an environment with integer variables. */
	public void buildEnvironment() {

		recordIntLocalVars();
		recordIntClassVars();

		String ints[] = new String[local_ints.length + class_ints.length];

		/* add local ints */
		for (int i = 0; i < local_ints.length; i++) {
			ints[i] = local_ints[i];
		}

		/* add class ints */
		for (int i = 0; i < class_ints.length; i++) {
			ints[local_ints.length + i] = class_ints[i];
		}

		env = new Environment(ints, reals);
	}

	/* Instantiate a domain. */
	private void instantiateDomain() {
		man = new Polka(true);
	}

	/* === Constructor === */
	public Analysis(UnitGraph g, SootClass jc) {
		super(g);
		this.flag = true;


		this.g = g;
		this.jclass = jc;

		buildEnvironment();
		instantiateDomain();

		loopHeads = new HashMap<Unit, Counter>();
		backJumps = new HashMap<Unit, Counter>();
		for (Loop l : new LoopNestTree(g.getBody())) {
			loopHeads.put(l.getHead(), new Counter(0));
			backJumps.put(l.getBackJumpStmt(), new Counter(0));
		}
	}

	void run() {
		doAnalysis();
	}

	@Override
	protected void flowThrough(AWrapper inWrapper, Unit op,
			List<AWrapper> fallOutWrappers, List<AWrapper> branchOutWrappers) {
		if(this.flag) {
			this.flag = false;
		}
		
		Stmt s = (Stmt) op;
		//System.out.println(s.toString());

		try {
			if (s instanceof DefinitionStmt) {
				DefinitionStmt sd = (DefinitionStmt) s;
				Value lhs = sd.getLeftOp();
				Value rhs = sd.getRightOp();

				if(rhs instanceof JAddExpr || rhs instanceof JSubExpr || rhs instanceof JMulExpr) {
					Texpr1Intern t = converter.convertArithExpression(rhs, env);
					

					inWrapper.get().assign(man, lhs.toString(), t, null);
			
				}
			}
		
			
	
			 else if (s instanceof JIfStmt) {
				Value expr = ((JIfStmt) s).getCondition();
				Value leftValue = null;
				Value rightValue = null;
				
				//Makes two expression trees in apron (Bsp: a-9 --> a-9 and 9-a
				if(expr instanceof AbstractJimpleIntBinopExpr){
					leftValue = ((AbstractJimpleIntBinopExpr) expr).getOp1();
					rightValue = ((AbstractJimpleIntBinopExpr) expr).getOp2();
				}

				Texpr1Node leftNode = converter.convertValueExpression(leftValue);
				Texpr1Node rightNode = converter.convertValueExpression(rightValue);
				Texpr1Node leftMinusRight = new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode);
				Texpr1Node rightMinusLeft = new Texpr1BinNode(Texpr1BinNode.OP_SUB, rightNode, leftNode);
				
				Abstract1 falloutAbstractFinal = null;
				Abstract1 branchAbstractFinal = null;
				
								
				//check which expression and creates constraints for branching and fallout
				// "=="
				if(expr instanceof JEqExpr){
					//branch-case
					Tcons1 branchConstraint = new Tcons1(env, Tcons1.EQ, leftMinusRight);
					branchAbstractFinal = inWrapper.get().meetCopy(man, branchConstraint);
					
					//fallout-case
					Tcons1 falloutConstraint1 = new Tcons1(env, Tcons1.SUP, leftMinusRight);
					Tcons1 falloutConstraint2 = new Tcons1(env, Tcons1.SUP, rightMinusLeft);
					Abstract1 falloutAbstract1 = inWrapper.get().meetCopy(man, falloutConstraint1);
					Abstract1 falloutAbstract2 = inWrapper.get().meetCopy(man, falloutConstraint2);
					falloutAbstractFinal = falloutAbstract1.joinCopy(man, falloutAbstract2);
								
				}
				//">"
				else if(expr instanceof JGtExpr){
					//branch-case
					Tcons1 branchConstraint = new Tcons1(env, Tcons1.SUP, leftMinusRight);
					branchAbstractFinal = inWrapper.get().meetCopy(man, branchConstraint);

					//fallout-case
					Tcons1 falloutConstraint = new Tcons1(env, Tcons1.SUPEQ, rightMinusLeft);
					falloutAbstractFinal = inWrapper.get().meetCopy(man, falloutConstraint);
					
				}
				//">="
				else if(expr instanceof JGeExpr){
					//branch-case
					Tcons1 branchConstraint = new Tcons1(env, Tcons1.SUPEQ, leftMinusRight);
					branchAbstractFinal = inWrapper.get().meetCopy(man, branchConstraint);

					//fallout-case
					Tcons1 falloutConstraint = new Tcons1(env, Tcons1.SUP, rightMinusLeft);
					falloutAbstractFinal = inWrapper.get().meetCopy(man, falloutConstraint);
					
				}
				//"<"
				else if(expr instanceof JLtExpr){
					//branch-case
					Tcons1 branchConstraint = new Tcons1(env, Tcons1.SUP, rightMinusLeft);
					branchAbstractFinal = inWrapper.get().meetCopy(man, branchConstraint);

					//fallout-case
					Tcons1 falloutConstraint = new Tcons1(env, Tcons1.SUPEQ, leftMinusRight);
					falloutAbstractFinal = inWrapper.get().meetCopy(man, falloutConstraint);
				}
				//"<="
				else if(expr instanceof JLeExpr){
					//branch-case
					Tcons1 branchConstraint = new Tcons1(env, Tcons1.SUPEQ, rightMinusLeft);
					branchAbstractFinal = inWrapper.get().meetCopy(man, branchConstraint);

					//fallout-case
					Tcons1 falloutConstraint = new Tcons1(env, Tcons1.SUP, leftMinusRight);
					falloutAbstractFinal = inWrapper.get().meetCopy(man, falloutConstraint);
				}
				//"!="
				else if(expr instanceof JNeExpr){
					//branch-case
					Tcons1 branchConstraint1 = new Tcons1(env, Tcons1.SUP, leftMinusRight);
					Tcons1 branchConstraint2 = new Tcons1(env, Tcons1.SUP, rightMinusLeft);
					Abstract1 branchAbstract1 = inWrapper.get().meetCopy(man, branchConstraint1);
					Abstract1 branchAbstract2 = inWrapper.get().meetCopy(man, branchConstraint2);
					branchAbstractFinal = branchAbstract1.joinCopy(man, branchAbstract2);
					
					//fallout-case
					Tcons1 falloutConstraint = new Tcons1(env, Tcons1.EQ, leftMinusRight);
					falloutAbstractFinal = inWrapper.get().meetCopy(man, falloutConstraint);			
				}
				else{
					System.out.println("Error in if-Statement");
				}
				fallOutWrappers.set(0,new AWrapper(falloutAbstractFinal));
				branchOutWrappers.set(0,new AWrapper(branchAbstractFinal));
				
				

			}
			 else if (s instanceof JInvokeStmt){
				JInvokeStmt funcCall = (JInvokeStmt) s;
				InvokeExpr invExpr = funcCall.getInvokeExpr();
				String methodName = invExpr.getMethod().getName();
				String className = invExpr.getMethod().getDeclaringClass().getName();
				
				if(methodName.equals("weldAt") && className.equals("Robot")){
					weldAtCalls.add(funcCall);
				}
				else if(methodName.equals("weldBetween") && className.equalsIgnoreCase("Robot")){
					weldBetweenCalls.add(funcCall);
				}
				else if(methodName.equals("<init>") && className.equals("Robot")){
					robotProperties.add(funcCall);
				}

				 
			 }
			System.out.println(local_ints.length);
			for(String valName : local_ints){
	    		Texpr1Node node = new Texpr1VarNode(valName);
	    		Texpr1Intern apronArg = new Texpr1Intern(env, node);
	    		try {
	    			System.out.println(this.getFlowBefore(s).get().toString());
					Interval currentBounds = this.getFlowBefore(s).get().getBound(man, apronArg);
		    		System.out.println("At Line: "+s.toString()+" The Variable "+valName+" can have Value: "+currentBounds.toString());


				} catch (ApronException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


			
			
		} catch (ApronException e1) {
			System.out.println("Flowtrought error");
			e1.printStackTrace();
		}
		
	}

	@Override
	protected void copy(AWrapper source, AWrapper dest) {
		try {
			dest.set(new Abstract1(man, source.get()));
		} catch (ApronException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected AWrapper entryInitialFlow() {
		Abstract1 top = null;
		try {
			top = new Abstract1(man, env);
		} catch (ApronException e) {
		}
		return new AWrapper(top);
	}

	private static class Counter {
		int value;

		Counter(int v) {
			value = v;
		}
	}

	//widen operator
	@Override
	protected void merge(Unit succNode, AWrapper w1, AWrapper w2, AWrapper w3) {
		Counter count = loopHeads.get(succNode);

		Abstract1 a1 = w1.get();
		Abstract1 a2 = w2.get();
		Abstract1 a3 = null;

		try {
			if (count != null) {
				++count.value;
				if (count.value < WIDENING_THRESHOLD) {
					a3 = a1.joinCopy(man, a2);
				} else {
					a3 = a1.widening(man, a2);
				}
			} else {
				a3 = a1.joinCopy(man, a2);
			}
			w3.set(a3);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Override
	protected void merge(AWrapper src1, AWrapper src2, AWrapper trg) {

		Abstract1 a1 = src1.get();
		Abstract1 a2 = src2.get();
		Abstract1 a3 = null;

		try {
			a3 = a1.joinCopy(man, a2);
		} catch (ApronException e) {
			e.printStackTrace();
		}
		trg.set(a3);
	}

	@Override
	protected AWrapper newInitialFlow() {
		Abstract1 bot = null;

		try {
			bot = new Abstract1(man, env, true);
		} catch (ApronException e) {
		}
		AWrapper a = new AWrapper(bot);
		a.man = man;
		return a;

	}
 //mcSwag
	public static final boolean isIntValue(Value val) {
		return val.getType().toString().equals("int")
				|| val.getType().toString().equals("short")
				|| val.getType().toString().equals("byte");
	}

	public static SootApronConverter converter = new SootApronConverter();
	public static Manager man;
	public static Environment env;
	public UnitGraph g;
	public String local_ints[]; // integer local variables of the method
	public static String reals[] = { "x" };
	public SootClass jclass;
	private String class_ints[]; // integer class variables where the method is
	// defined
}
