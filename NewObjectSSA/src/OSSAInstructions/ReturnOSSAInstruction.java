package OSSAInstructions;

import java.util.HashMap;
import java.util.Iterator;

import OBJS.OSSAObject;


import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAReturnInstruction;

public class ReturnOSSAInstruction extends SSAReturnInstruction{

	public SSAReturnInstruction origReturnInstrctn;
	public HashMap<InstanceKey,OSSAObject> retObj;
	
	public ReturnOSSAInstruction(SSAReturnInstruction ssareturninstrctn, HashMap<InstanceKey,OSSAObject> retobj) {
		this.origReturnInstrctn = ssareturninstrctn;
		this.retObj = retobj;
	}
	
	public ReturnOSSAInstruction(){
		this.origReturnInstrctn=null;
	}
	
	public String toString() {
		if(retObj==null)
			return origReturnInstrctn.toString();
		StringBuffer retStr = new StringBuffer();
		retStr.append("Return {");
		for(Iterator<OSSAObject>objs = retObj.values().iterator();objs.hasNext();) {
			retStr.append(objs.next()+", ");
		}
		retStr.deleteCharAt((retStr.length())-2); 	//deletes last ','
		retStr.append("}");
		return retStr.toString();
	}
	
}
