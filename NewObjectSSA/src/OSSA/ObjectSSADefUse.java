package OSSA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;


import OBJS.OSSAObject;


import com.ibm.wala.ssa.SSAInstruction;

public class ObjectSSADefUse {
	/**
	 * Hashtable containing all definitions of objects in Object-SSA.
	 * Get the definiton first corresponding to the object number and then the object version number(again a hash-table).
	 */
	public HashMap<OSSAObject, SSAInstruction>defs = new HashMap<OSSAObject, SSAInstruction>();
	/**
	 * Hashtable containing all uses of objects in Object-SSA.
	 * All version numbers of an object are treated as different objects.
	 * Get the uses first corresponding to the object number and then the object version number(again a hash-table).
	 * The instructions having use of a particular object are stored in a set.
	 */
	public HashMap<OSSAObject, HashSet<SSAInstruction>>uses = new HashMap<OSSAObject, HashSet<SSAInstruction>>();
	
	/**
	 * 
	 * @param obj the object whose new definition is found.
	 * @param instructn the instruction containing the new definition.
	 */
	public  void storeDef(OSSAObject obj, SSAInstruction instructn){
		//DONE store this object in defs
		if(!(defs.containsKey(obj)) && obj!=null)
			defs.put(obj, instructn);
		
		
		//also create use entry for the same object
//		if
	}
	
	public SSAInstruction getDef(OSSAObject obj){
		return defs.get(obj);
	}
	
	/**
	 * Stores use in {@link uses}. All the store uses are called in function {@link renameObjects}
	 * @param instructn
	 * @param obj
	 */
	public  void storeUse(OSSAObject obj, SSAInstruction instructn){
		//DONE write code
		if(obj==null)
			return;
		//This code runs with the assumption that objectStack contains the correct version number of the object.
		if(!(uses.containsKey(obj)))
			uses.put(obj, new HashSet<SSAInstruction>());
//		if(!(uses.get(obj).containsKey(versionno)))
//			uses.get(obj).put(versionno, new HashSet<SSAInstruction>());
		uses.get(obj).add(instructn);
		
	}
	
	public int numberOfUses(OSSAObject obj){
		return uses.get(obj).size();
	}
	
	public HashSet<SSAInstruction> getUses(OSSAObject obj){
		return uses.get(obj);
	}
	
	public String toString() {
		StringBuffer strbuff = new StringBuffer();
		strbuff.append("Defs: \n"+defs.toString());
		strbuff.append("\n\nUses: \n"+uses.toString());
		return strbuff.toString();
	}
}
