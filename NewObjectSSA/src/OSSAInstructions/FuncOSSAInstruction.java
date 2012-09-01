package OSSAInstructions;

import java.util.ArrayList;
import java.util.HashMap;

import OBJS.OSSAObject;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.TypeReference;
/**
 * function definition instruction. added to show the objects passed as arguments
 * @author yash
 *
 */
public class FuncOSSAInstruction extends SSAInstruction{

	int noofparams;
	String methodSign;
	public int vns[] ;
	/*
	 * map from SSA value-number to parameter type
	 */
	public HashMap<Integer, TypeReference> paramtypes =  new HashMap<Integer, TypeReference>();
	/**
	 * map from ssa value-number to OSSA object
	 */
//	public HashMap<Integer, OSSAObject> argObjs = new HashMap<Integer, OSSAObject>();
	
	/*
	 * object argument list. If arg isn't object then stores null
	 */
	public OSSAObject args[] ;// = new ArrayList<HashMap<InstanceKey,OSSAObject>>();

	
	public FuncOSSAInstruction(int noofparams, String methodSign) {
		// TODO Auto-generated constructor stub
		this.noofparams = noofparams;
		this.methodSign = methodSign;
		vns = new int[noofparams];
		//args = new ArrayList<HashMap<InstanceKey,OSSAObject>>();//[noofparams];
		args = new OSSAObject[noofparams];
		
	}
	@Override
	public SSAInstruction copyForSSA(SSAInstructionFactory insts, int[] defs,
			int[] uses) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isFallThrough() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString(SymbolTable symbolTable) {
		// TODO Auto-generated method stub
		StringBuffer strbuff =  new StringBuffer();
		strbuff.append(methodSign+"(");
		for(int i =0; i<noofparams;i++) {
			if(paramtypes.get(vns[i])!=null)
				strbuff.append(args[i]);
			else
				strbuff.append("v"+vns[i]);
			strbuff.append(", ");
		}
		strbuff.deleteCharAt((strbuff.length())-2);
		strbuff.append(")");
		return strbuff.toString();
	}

	@Override
	public void visit(IVisitor v) {
		// TODO Auto-generated method stub
		
	}
	
	

}
