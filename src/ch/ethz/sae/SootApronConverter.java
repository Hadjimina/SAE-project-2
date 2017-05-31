package ch.ethz.sae;

import apron.Environment;
import apron.MpqScalar;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Intern;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.internal.AbstractJimpleFloatBinopExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JSubExpr;
import soot.jimple.internal.JimpleLocal;

class SootApronConverter {
	public SootApronConverter() {
		
	}
		
	public static Texpr1Intern convertArithExpression(Value expression, Environment env) {
		
		if(expression instanceof AbstractJimpleFloatBinopExpr){

			/*Check which operands are variables/constants so we
			 * can handle casting accordingly.
			 */
			int operatorCode = 0;
			Value leftVal = ((AbstractJimpleFloatBinopExpr) expression).getOp1();
			Value rightVal = ((AbstractJimpleFloatBinopExpr) expression).getOp2();
			
			if(expression instanceof JAddExpr){
				operatorCode = Texpr1BinNode.OP_ADD;
				
			}else if (expression instanceof JSubExpr) {
				operatorCode = Texpr1BinNode.OP_SUB;
				
			}else if (expression instanceof JMulExpr){
				operatorCode = Texpr1BinNode.OP_MUL;

			} else {
				throw new ConversionArgumentException("Conversion called on arithmetic expession not handled");
			}

			Texpr1BinNode opr = new Texpr1BinNode(operatorCode,
					convertSingleValue(leftVal),convertSingleValue(rightVal));			
			
			return new Texpr1Intern(env, opr);

		} else {
			throw new ConversionArgumentException("Conversion called on non-arithmetic expession");
			
		}

	}
	
	private static Texpr1Node convertSingleValue(Value val) {
		if(val instanceof IntConstant) {
			MpqScalar constRightVal = new MpqScalar(Integer.parseInt(val.toString()));
			return new Texpr1CstNode(constRightVal);
		} else {
			return new Texpr1VarNode(val.toString());
		}
	}
	
	public Texpr1Node convertValueExpression(Value expression) {
		if(expression instanceof JimpleLocal || expression instanceof IntConstant) {
			return convertSingleValue(expression);
		} else {
			throw new ConversionArgumentException("convertValueExpression called with invalid argument");
		}
		
		
	}
}
