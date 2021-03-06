package ch.ethz.sae;

import java.util.HashMap;
import java.util.List;

import apron.Abstract1;
import apron.ApronException;
import apron.Interval;
import apron.MpqScalar;
import apron.Tcons1;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Intern;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.AbstractJimpleFloatBinopExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.PAG;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.toolkits.graph.BriefUnitGraph;

public class Verifier {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -classpath soot-2.5.0.jar:./bin ch.ethz.sae.Verifier <class to test>");
            System.exit(-1);
        }
        String analyzedClass = args[0];
        SootClass c = loadClass(analyzedClass);

        PAG pointsToAnalysis = doPointsToAnalysis(c);

        int weldAtFlag = 1;
        int weldBetweenFlag = 1;

        for (SootMethod method : c.getMethods()) {

            if (method.getName().contains("<init>")) {
                // skip constructor of the class
                continue;
            }
            Analysis analysis = new Analysis(new BriefUnitGraph(method.retrieveActiveBody()), c);
            analysis.run();
            collector = new AllocNodeCollector(analysis, pointsToAnalysis);
            if (!verifyWeldAt(method, analysis, pointsToAnalysis)) {
                weldAtFlag = 0;
            }
            if (!verifyWeldBetween(method, analysis, pointsToAnalysis)) {
                weldBetweenFlag = 0;
            }
        }
        
        // Do not change the output format
        if (weldAtFlag == 1) {
            System.out.println(analyzedClass + " WELD_AT_OK");
        } else {
            System.out.println(analyzedClass + " WELD_AT_NOT_OK");
        }
        if (weldBetweenFlag == 1) {
            System.out.println(analyzedClass + " WELD_BETWEEN_OK");
        } else {
            System.out.println(analyzedClass + " WELD_BETWEEN_NOT_OK");
        }
    }

    private static boolean verifyWeldBetween(SootMethod method, Analysis fixPoint, PAG pointsTo) {
    	/* TODO: check whether all calls to weldBetween respect Property 2 */
    	boolean isGud = true;
    	int abstractNumber = 0;
    	for(JInvokeStmt call : fixPoint.weldBetweenCalls){
    		Texpr1Node node1;
    		Texpr1Node node2;
    		Abstract1 flowBefore = fixPoint.weldBetweenAbstracts.get(abstractNumber);
    		abstractNumber++;
 	
    		JVirtualInvokeExpr virExpr = (JVirtualInvokeExpr) call.getInvokeExprBox().getValue();
    		Value callArg1 = virExpr.getArg(0);
    		Value callArg2 = virExpr.getArg(1);
    		
    		
    		JimpleLocal robot = (JimpleLocal) virExpr.getBase();
    		Interval robotInterval = collector.getInterval(robot);
    		//callArg ist entweder Variable oder Konstante. Wie klären?
    		if(callArg1 instanceof JimpleLocal){
	    		node1 = new Texpr1VarNode(((JimpleLocal) callArg1).getName());
    		}
    		else{
    			IntConstant c = (IntConstant) callArg1;
	    		node1 = new Texpr1CstNode((new MpqScalar(c.value)));
    		}
    		if(callArg2 instanceof JimpleLocal){
	    		node2 = new Texpr1VarNode(((JimpleLocal) callArg2).getName());
    		}
    		else{
    			IntConstant c = (IntConstant) callArg2;
	    		node2 = new Texpr1CstNode((new MpqScalar(c.value)));
    		}
    		Texpr1Intern apronArg1 = new Texpr1Intern(fixPoint.env, node1 );
    		Texpr1Intern apronArg2 = new Texpr1Intern(fixPoint.env, node2 );
    		Texpr1Node subtraction = new Texpr1BinNode(Texpr1BinNode.OP_SUB, node1, node2);
    		Tcons1 constraint = new Tcons1(fixPoint.env, Tcons1.SUPEQ, subtraction);
    		try {
    			Abstract1 tempAbstract = new Abstract1(fixPoint.man, flowBefore);
    			tempAbstract.meet(fixPoint.man, constraint);
    			
				Interval currentBounds1 = flowBefore.getBound(fixPoint.man, apronArg1);
				Interval currentBounds2 = flowBefore.getBound(fixPoint.man, apronArg2);
				Interval mergedBounds = new Interval(currentBounds1.inf(), currentBounds2.sup());
				
				if(flowBefore.isBottom(fixPoint.man)){
					isGud = false;
				}
				else if((tempAbstract.isBottom(fixPoint.man))){	
					if(!(mergedBounds.isLeq(robotInterval))){
						isGud = false;
						
					}
				}
		
				else if(!(currentBounds1.sup().cmp(currentBounds2.inf()) == -1)){
					isGud = false;

				}
				else if(!(mergedBounds.isLeq(robotInterval))){
					isGud = false;

				}


			} catch (ApronException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
        return isGud;
    }

    private static boolean verifyWeldAt(SootMethod method, Analysis fixPoint, PAG pointsTo) {
    	/* TODO: check whether all calls to weldAt respect Property 1 */
    	boolean isGud = true;
    	int abstractNumber = 0;
    	for(JInvokeStmt call : fixPoint.weldAtCalls){
    		Texpr1Node node;
    		Abstract1 flowBefore = fixPoint.weldAtAbstracts.get(abstractNumber);
    		abstractNumber++;
    		try {
				if(flowBefore.isBottom(fixPoint.man)){
					break;
				}
			} catch (ApronException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		JVirtualInvokeExpr virExpr = (JVirtualInvokeExpr) call.getInvokeExprBox().getValue();
    		Value callArg = virExpr.getArg(0);
    		JimpleLocal robot = (JimpleLocal) virExpr.getBase();
    		Interval robotInterval = collector.getInterval(robot);
    		//callArg ist entweder Variable oder Konstante. Wie klären?
    		if(callArg instanceof JimpleLocal){
	    		node = new Texpr1VarNode(((JimpleLocal) callArg).getName());
    		}
    		else{
    			IntConstant c = (IntConstant) callArg;
	    		node = new Texpr1CstNode((new MpqScalar(c.value)));
    		}
    		Texpr1Intern apronArg = new Texpr1Intern(fixPoint.env, node );
    		try {
				Interval currentBounds = flowBefore.getBound(fixPoint.man, apronArg);
				
				if(!(currentBounds.isLeq(robotInterval))){
					isGud = false;
				}


			} catch (ApronException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
        return isGud;
    }

    private static SootClass loadClass(String name) {
        SootClass c = Scene.v().loadClassAndSupport(name);
        c.setApplicationClass();
        return c;
    }

    // Performs Points-To Analysis
    private static PAG doPointsToAnalysis(SootClass c) {
        Scene.v().setEntryPoints(c.getMethods());

        HashMap<String, String> options = new HashMap<String, String>();
        options.put("enabled", "true");
        options.put("verbose", "false");
        options.put("propagator", "worklist");
        options.put("simple-edges-bidirectional", "false");
        options.put("on-fly-cg", "true");
        options.put("set-impl", "double");
        options.put("double-set-old", "hybrid");
        options.put("double-set-new", "hybrid");

        SparkTransformer.v().transform("", options);
        PAG pag = (PAG) Scene.v().getPointsToAnalysis();

        return pag;
    }
    private static AllocNodeCollector collector;
    

}
