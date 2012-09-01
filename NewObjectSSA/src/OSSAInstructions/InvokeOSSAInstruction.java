package OSSAInstructions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import OBJS.OSSAObject;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SymbolTable;

public class InvokeOSSAInstruction extends SSAInstruction{
	
	public SSAInvokeInstruction origInstrctn;
	
	/**
	 * The object returned by function call, not used anymore..though directly. used with retphi instruction
	 */
	public OSSAObject defObj;
	/*
	 * object argument list. If arg isn't object then stores null
	 */
	public ArrayList<HashMap<InstanceKey, OSSAObject>> args ;//= new ArrayList<HashMap<InstanceKey,OSSAObject>>();
	//array doesn't work as ther can be more than one objs with one arg with diffrent asis. therefore arraylist is used.
//	public OSSAObject argsarray;
	/*
	 * Stores the new definition creating argPhis instructions for function arguments.
	 */
	public ArrayList<ArgPhiOSSAInstruction> argPhiInstrctns;
	/*
	 * list of instructions containing objs coming with ASI outside the fuction, returned by a function call
	 */
	LinkedList<OutsideObjOSSAInstruction> outsideObjInstructions = new LinkedList<OutsideObjOSSAInstruction>();
	/*
	 * Objects returned from function, which might not be created in caller function.
	 */
	public OSSAObject outsideObjs[];
	/**
	 * retPhi instructino to merge definition of Objects returned from function call.
	 */
	public RetPhiOSSAInstruction retPhi;
	
	/*
	 * List of argument objects changed as a side-effect of function call. If arg isn't object then stores null
	 */
	public ArrayList<OSSAObject> changedArgs ;
	
	public InvokeOSSAInstruction(SSAInvokeInstruction originstructn, OSSAObject defObj, ArrayList<HashMap<InstanceKey, OSSAObject>> args, ArrayList<OSSAObject> changedArgs
							,ArrayList<ArgPhiOSSAInstruction> argPhiInstrctns, LinkedList<OutsideObjOSSAInstruction> outsideObjInstructions, RetPhiOSSAInstruction retPhi
							) {
		this.origInstrctn = originstructn;
		this.defObj = defObj;
		this.args = args;
		this.changedArgs = changedArgs;
		this.argPhiInstrctns = argPhiInstrctns;
		this.outsideObjInstructions = outsideObjInstructions;
		this.retPhi = retPhi;
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
		StringBuffer retStr = new StringBuffer();
		if(defObj!=null&&retPhi==null)
			retStr.append(defObj+" = ");
		retStr.append(origInstrctn.getDeclaredTarget().getName().toString()+"(");
		int noofparams = args.size();
		for(int i=0;i<noofparams;i++) {
			HashMap<InstanceKey, OSSAObject>arg = args.get(i);
			if(arg!=null) {
				retStr.append("{");
				for(Iterator<OSSAObject>argelems=arg.values().iterator();argelems.hasNext();) {
					retStr.append(argelems.next()+", ");
				}
				retStr.deleteCharAt((retStr.length())-2); 
				retStr.append("}");
			}
			else {
				retStr.append("v"+origInstrctn.getUse(i));
			}
			retStr.append(", ");
		}
		retStr.deleteCharAt((retStr.length())-2); 
		retStr.append(")\n");//->(");
		
		//PrintargPhiInstrctns
		for(Iterator<ArgPhiOSSAInstruction>argphiinstrctniterator = argPhiInstrctns.iterator();argphiinstrctniterator.hasNext();) {
			retStr.append(argphiinstrctniterator.next()+"\n");
		}
		
		
		//Print outside objs
		for(Iterator<OutsideObjOSSAInstruction>outsideobjsinstiter = outsideObjInstructions.iterator();outsideobjsinstiter.hasNext();) {
			retStr.append(outsideobjsinstiter.next()+"\n");
		}
		
		
		//print retphi
		if(retPhi!=null)
		retStr.append(retPhi+"\n");
//		else if(defObj!=null)
//			retStr.append("")
		
		/*for(int i=0;i<noofparams;i++) {
			HashMap<InstanceKey, OSSAObject>arg = changedArgs.get(i);
			if(arg!=null) {
				retStr.append("{");
				for(Iterator<OSSAObject>argelems=arg.values().iterator();argelems.hasNext();) {
					retStr.append(argelems.next()+", ");
				}
				retStr.deleteCharAt((retStr.length())-2); 
				retStr.append("}");
			}
			OSSAObject retArgObj = changedArgs.get(i);
			if(retArgObj!=null) {
				retStr.append(retArgObj);
			}
			else {
				retStr.append("v"+origInstrctn.getUse(i));
			}
			retStr.append(", ");
		}
		retStr.deleteCharAt((retStr.length())-2); 
		retStr.append(")]");*/
		
		return retStr.toString();
	}

	@Override
	public void visit(IVisitor v) {
		// TODO Auto-generated method stub
		
	}

}
