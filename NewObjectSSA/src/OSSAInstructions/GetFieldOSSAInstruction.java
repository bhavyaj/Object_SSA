package OSSAInstructions;

import java.util.HashMap;
import java.util.Iterator;

import javax.print.attribute.standard.MediaSize.Other;

import OBJS.OSSAObject;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.FieldReference;

/**
 * wala instructino:-v7 = getfield v2<next>
 * v7-fieldrefobj
 * v2- containerobj
 * @author yash
 *
 */
public class GetFieldOSSAInstruction extends SSAInstruction{

	/*
	 * Original Wala IR instruction
	 */
	public SSAGetInstruction originst;
	/**
	 * wala instructino:-v7 = getfield v2<next>
 * v7-fieldrefobj
 * v2- containerobj
	 */
	public HashMap<InstanceKey,OSSAObject> containerObjs;
	/*
	 * wala instructino:-v7 = getfield v2<next>
 * v7-fieldrefobj
 * v2- containerobj
	 */
	public HashMap<InstanceKey,OSSAObject> fieldRefObjs;
	
	public Boolean staticinst = false;
	public OSSAObject staticobj;
	public Boolean isStaic() {
		return staticinst;
	}
	/*
	 * Constructor for get ossa instructions when getting static object :D
	 */
	public GetFieldOSSAInstruction(SSAGetInstruction originst, OSSAObject staticobj){
		staticinst = true;
		this.originst = originst;
		this.staticobj = staticobj;
	}
	/**
	 * GetFieldOSSA constructor for getfield instructions
	 * @param originst Wala SSA instruction	
	 * @param containerObjs 
	 * @param fieldRefObjs Object referenced by field
	 */
	public GetFieldOSSAInstruction(SSAGetInstruction originst, HashMap<InstanceKey,OSSAObject> containerObjs, HashMap<InstanceKey,OSSAObject> fieldRefObjs) {
		this.originst=originst;
		this.containerObjs = containerObjs;
		this.fieldRefObjs = fieldRefObjs;
	}
	/**
	 * GetFieldOSSA constructor for getfield instructions with scalar fields
	 * @param originst Wala SSA instruction	
	 * @param containerObjs 
	 * fieldrefobjs equals null, because field is scalar
	 */
	public GetFieldOSSAInstruction(SSAGetInstruction originst, HashMap<InstanceKey,OSSAObject> containerObjs) {
		this.originst=originst;
		this.containerObjs = containerObjs;
		this.fieldRefObjs = null;
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
		StringBuffer strBuff = new StringBuffer();
		if(staticinst) {
			if(staticobj.objectVersionNo==0)
				return originst.toString();
			strBuff.append("GetstaticInstruction: "+staticobj+" = "+originst.getDeclaredField());
			return strBuff.toString();
		}
		
		strBuff.append("GetFieldInstruction: {");
		if(originst.getDeclaredFieldType().isReferenceType()) {
			for(Iterator<OSSAObject>fieldObjs = fieldRefObjs.values().iterator();fieldObjs.hasNext();) {
				OSSAObject fieldObj = fieldObjs.next();
				strBuff.append(fieldObj+", ");
			}
			strBuff.deleteCharAt((strBuff.length())-2); 	//deletes last ','
		}
		else {
			strBuff.append("v_"+originst.getDef());
		}
		
		strBuff.append("} = {");
		for(Iterator<OSSAObject>ContainerObjs = containerObjs.values().iterator();ContainerObjs.hasNext();) {
			OSSAObject containObj = ContainerObjs.next();
			strBuff.append(containObj+", ");
		}
		strBuff.deleteCharAt((strBuff.length())-2); 	//deletes last ','
		strBuff.append("}<"+originst.getDeclaredField().getName());
		
		strBuff.append(">");
		return strBuff.toString();
	}

	@Override
	public void visit(IVisitor v) {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
