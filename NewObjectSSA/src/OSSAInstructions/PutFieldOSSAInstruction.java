package OSSAInstructions;

import java.util.HashMap;
import java.util.Iterator;

import OBJS.OSSAObject;

import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class PutFieldOSSAInstruction extends SSAInstruction{

	/**
	 * original wala ir instruction which is being translated.
	 */
	public SSAPutInstruction originst;
	
	public OSSAObject defObj;
	
	public HashMap<InstanceKey,OSSAObject> fieldRefObjs;
	
	public OSSAObject oldObj;
	
	public DPhiOSSAInstruction dphi;
	
	/**
	 * Constructor for Reference field types
	 * @param originst
	 * @param defObj
	 * @param oldObj
	 * @param fieldRefObjs
	 * @param dphi
	 */
	public PutFieldOSSAInstruction(SSAPutInstruction originst, OSSAObject defObj, OSSAObject oldObj, HashMap<InstanceKey,OSSAObject> fieldRefObjs, DPhiOSSAInstruction dphi) {
		this.originst = originst;
		this.defObj = defObj;
		this.oldObj = oldObj;
		this.fieldRefObjs = fieldRefObjs;
		this.dphi = dphi;
	}
	
	/**
	 * Constructor for fields with primitive field types
	 */
	public PutFieldOSSAInstruction(SSAPutInstruction originst, OSSAObject defObj, OSSAObject oldObj, DPhiOSSAInstruction dphi) {
		this.originst = originst;
		this.defObj = defObj;
		this.oldObj = oldObj;
		this.fieldRefObjs = null;
		this.dphi = dphi;
	}
	
	public Boolean staticinst = false;
	public OSSAObject staticobj;
	public Boolean isStaic() {
		return staticinst;
	}
	/*
	 * Constructor for get ossa instructions when getting static object :D
	 */
	public PutFieldOSSAInstruction(SSAPutInstruction originst, OSSAObject staticobj, HashMap<InstanceKey,OSSAObject> fieldRefObjs){
		staticinst = true;
		this.originst = originst;
		this.staticobj = staticobj;
		this.fieldRefObjs=fieldRefObjs;
	}
	
	
	public String toString() {
		StringBuffer strBuff = new StringBuffer();
		if(staticinst) {
			if(staticobj.objectVersionNo==0)
				return originst.toString();
			strBuff.append("PutFieldInstruction: ");
			strBuff.append(staticobj+" <"+originst.getDeclaredField().getName().toString()+"> = ");
			strBuff.append(staticobj+" <");
			return strBuff.toString();
		}
		else {
		strBuff.append("PutFieldInstruction: ");
		strBuff.append(defObj+" <"+originst.getDeclaredField().getName().toString()+"> = ");
		strBuff.append(oldObj+" <");
		}
		if(originst.getDeclaredFieldType().isReferenceType()) {
			
			for(Iterator<OSSAObject>fieldObjs = fieldRefObjs.values().iterator();fieldObjs.hasNext();) {
				OSSAObject fieldObj = fieldObjs.next();
				strBuff.append("ref"+fieldObj+", ");
			}
			strBuff.deleteCharAt((strBuff.length())-2); 	//deletes last ','
		}
		else {
			strBuff.append("v_"+originst.getUse(1));
		}
		strBuff.append(">");
		return strBuff.toString();
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
		return null;
	}

	@Override
	public void visit(IVisitor v) {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
