package ch.ethz.sae;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import apron.Interval;
import soot.PointsToSet;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;

public class AllocNodeCollector extends P2SetVisitor {
	
	List<AllocNode> nodes;
	
	PAG pag;
	Analysis an;
	HashMap<AllocNode, Interval> robotIntervals;
	
	
	public AllocNodeCollector(Analysis an, PAG pag) {
		this.pag = pag;
		this.an = an;
		robotIntervals = new HashMap<AllocNode, Interval>();
		AllocNode currAN;
		Interval currInt;
		for(JSpecialInvokeExpr invExpr : an.initCalls) {
			 currAN = getNodes((JimpleLocal) invExpr.getBase()).get(0);
			 currInt = new Interval(Integer.parseInt(invExpr.getArg(0).toString()),
					 Integer.parseInt(invExpr.getArg(1).toString()));
			 robotIntervals.put(currAN, currInt);
		}
	}
	
	public Interval getInterval(JimpleLocal pointer) {
		AllocNode node = getNodes(pointer).get(0);
		return robotIntervals.get(node);
	}
		
    private List<AllocNode> getNodes(JimpleLocal pointer) {
    	this.nodes = new LinkedList<AllocNode>();
    	PointsToSetInternal pts = (PointsToSetInternal) pag.reachingObjects(pointer);
    	pts.forall(this);
    	if(nodes.size() == 0) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("The pointer " + pointer.toString() + " points nowhere");
    		throw new RuntimeException(sb.toString());
    	} else if(nodes.size() > 1) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("The pointer " + pointer.toString() + " points to ");
    		for(AllocNode n : nodes) {
    			sb.append(n.toString());
    		}
    		throw new RuntimeException(sb.toString());
    	}
        return nodes;
    }
    
    @Override
    public void visit(Node n) {
        nodes.add((AllocNode)n);
    }

}

