package ch.ethz.sae;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Lincons1;
import apron.Linexpr1;
import apron.Linterm1;
import apron.Manager;
import apron.MpqScalar;
import apron.Polka;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Intern;
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
import soot.jimple.Stmt;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JIfStmt;
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

	private static final int WIDENING_THRESHOLD = 6;

	private HashMap<Unit, Counter> loopHeads, backJumps;

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

		
		Stmt s = (Stmt) op;
		System.out.println(s.toString());
		Abstract1 tempAbstract1;
		boolean branchFlag = false;
		try {
			tempAbstract1 = new Abstract1(man, env);


			if (s instanceof DefinitionStmt) {
				DefinitionStmt sd = (DefinitionStmt) s;
				Value lhs = sd.getLeftOp();
				Value rhs = sd.getRightOp();

				Value leftVal = new JimpleLocal("",new UnknownType(null)), rightVal = new JimpleLocal("",new UnknownType(null));
				String op1ClassName = "",op2ClassName = "";

				/*Check which operands are variables/constants so we
				 * can handle casting accordingly.
				 */
				int operatorCode = 0;
				if(rhs instanceof JAddExpr){
					operatorCode = Texpr1BinNode.OP_ADD;

					leftVal = ((JAddExpr) rhs).getOp1();
					rightVal = ((JAddExpr) rhs).getOp2();

					op1ClassName = ((JAddExpr) rhs).getOp1().getClass().toString();
					op2ClassName = ((JAddExpr) rhs).getOp2().getClass().toString();

				}else if (rhs instanceof JSubExpr) {
					operatorCode = Texpr1BinNode.OP_SUB;

					leftVal = ((JSubExpr) rhs).getOp1();
					rightVal = ((JSubExpr) rhs).getOp2();

					op1ClassName = ((JSubExpr) rhs).getOp1().getClass().toString();
					op2ClassName = ((JSubExpr) rhs).getOp2().getClass().toString();

				}else if (rhs instanceof JMulExpr){
					operatorCode = Texpr1BinNode.OP_MUL;

					leftVal = ((JMulExpr) rhs).getOp1();
					rightVal = ((JMulExpr) rhs).getOp2();

					op1ClassName = ((JMulExpr) rhs).getOp1().getClass().toString();
					op2ClassName = ((JMulExpr) rhs).getOp2().getClass().toString();
				}


					boolean leftVarFlag =  op1ClassName.toLowerCase().contains("jimplelocal");
					boolean rightVarFlag =  op2ClassName.toLowerCase().contains("jimplelocal");


					if(leftVarFlag && rightVarFlag){

						Texpr1VarNode leftOp = new Texpr1VarNode(leftVal.toString());
						Texpr1VarNode rightOp = new Texpr1VarNode(rightVal.toString());

						Texpr1BinNode opr = new Texpr1BinNode(operatorCode, leftOp, rightOp);
						Texpr1Intern t = new Texpr1Intern(env, opr);

						inWrapper.get().assign(man, lhs.toString(), t, tempAbstract1);

					} else if(leftVarFlag && !rightVarFlag){

						Texpr1VarNode leftOp = new Texpr1VarNode(leftVal.toString());
						MpqScalar constRightVal = new MpqScalar(Integer.parseInt(rightVal.toString()));
						Texpr1CstNode rightOp = new Texpr1CstNode(constRightVal);

						Texpr1BinNode opr = new Texpr1BinNode(operatorCode, leftOp, rightOp);
						Texpr1Intern t = new Texpr1Intern(env, opr);

						inWrapper.get().assign(man, lhs.toString(), t, tempAbstract1);

					} else if(!leftVarFlag && rightVarFlag) {

						MpqScalar constLeftVal = new MpqScalar(Integer.parseInt(leftVal.toString()));
						Texpr1CstNode leftOp = new Texpr1CstNode(constLeftVal);
						Texpr1VarNode rightOp = new Texpr1VarNode(rightVal.toString());

						Texpr1BinNode opr = new Texpr1BinNode(operatorCode, leftOp, rightOp);
						Texpr1Intern t = new Texpr1Intern(env, opr);

						inWrapper.get().assign(man, lhs.toString(), t, tempAbstract1);

					} else {

						MpqScalar constLeftVal = new MpqScalar(Integer.parseInt(leftVal.toString()));
						Texpr1CstNode leftOp = new Texpr1CstNode(constLeftVal);
						MpqScalar constRightVal = new MpqScalar(Integer.parseInt(rightVal.toString()));
						Texpr1CstNode rightOp = new Texpr1CstNode(constRightVal);

						Texpr1BinNode opr = new Texpr1BinNode(operatorCode, leftOp, rightOp);
						Texpr1Intern t = new Texpr1Intern(env, opr);

						inWrapper.get().assign(man, lhs.toString(), t, tempAbstract1);

					}
			}
		
				
	
			 else if (s instanceof JIfStmt) {
				IfStmt ifStmt = (JIfStmt) s;
				branchFlag = true;
				if(ifStmt instanceof JEqExpr){
					Value leftVal = ((JEqExpr) ifStmt).getOp1();
					MpqScalar cst = new MpqScalar((Integer.parseInt(((JEqExpr) ifStmt).getOp1().toString())));
					MpqScalar one = new MpqScalar(1);
					Linterm1[] terms = {new Linterm1(leftVal.toString(), one)};
					Linexpr1 expr = new Linexpr1(env, terms, cst );
					Lincons1[] constraints = {new Lincons1(4, expr)};
					tempAbstract1 = new Abstract1(man, constraints);
					
					
					//LinExpr1 linexp = new LinExpr1(env, left, )
					
				}
				else if(ifStmt instanceof JGtExpr){
					
				}
				else if(ifStmt instanceof JGeExpr){
					
				}
				else if(ifStmt instanceof JLtExpr){
					
				}
				else if(ifStmt instanceof JLtExpr){
					
				}
				else if(ifStmt instanceof JNeExpr){
					
				}
				else{
					System.out.println("Error in if-Statement");
				}
				
				/* TODO: handle if statement*/
				//only if, no if-else
			}
			else{
				
			}
			if(branchFlag){
				
			}
			else{
				AWrapper fallOutWrapper = new AWrapper(tempAbstract1);
				fallOutWrappers.add(fallOutWrapper);
			}
			//System.out.println(.toString(man));
			
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

	public static final boolean isIntValue(Value val) {
		return val.getType().toString().equals("int")
				|| val.getType().toString().equals("short")
				|| val.getType().toString().equals("byte");
	}


	public static Manager man;
	public static Environment env;
	public UnitGraph g;
	public String local_ints[]; // integer local variables of the method
	public static String reals[] = { "x" };
	public SootClass jclass;
	private String class_ints[]; // integer class variables where the method is
	// defined
}
