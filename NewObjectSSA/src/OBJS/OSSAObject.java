package OBJS;

import java.util.LinkedList;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.intset.OrdinalSet;

public class OSSAObject {
	/**
	 * set of instancekeys on heap representing this object
	 */
	public OrdinalSet<InstanceKey> ptsTo;	
	/**
	 * type of class of this object
	 */
	public TypeReference classType;				
	/**
	 * Object Version no assigned at time of renaming. 
	 * Can't make final, since some of them are inserted at time of put-phi insertion.
	 */
	public  int objectVersionNo;
	
	public Boolean outSideObj;
	/*
	 * Constructor for null object.
	 */
	public OSSAObject(int vn) {
		objectVersionNo=0;
	}
	
	/*
	 * for OSSA Objects containing only one allocation site and not points to set
	 * created mainly to accomodate the outside objs whose stack needs to be created in current functino
	 */
	public InstanceKey singleAllocationSite;
	
	public OSSAObject(int VersionNo, TypeReference concreteType, InstanceKey allocationsite){
		objectVersionNo = VersionNo;
		classType = concreteType;
		this.singleAllocationSite = allocationsite;
	}
	
	/**	
	 * Stores different allocation sites this object can be pointing to.
	 * Might not require this as finally Object class might store only concrete objects 
	 * and abstract objects are found out through checking points to set and stack tops.
	 */
	public OrdinalSet<InstanceKey> allocationSites;
	
	public OSSAObject(int VersionNo, TypeReference concreteType, OrdinalSet<InstanceKey>ptsTo){
		objectVersionNo = VersionNo;
		classType = concreteType;
		this.allocationSites = ptsTo;
	}
	
	public Boolean isAbstract(){
		if(1==allocationSites.size())
			return false;
		else
			return true;
	}
	
	public Boolean equals(OSSAObject otherObj) {
		if(this.objectVersionNo==otherObj.objectVersionNo)
			return true;
		else 
			return false;
	}
	
	public String toString() {
		StringBuffer strBuff = new StringBuffer();
		if(objectVersionNo==0) {
			return "NullObject";
		}
		strBuff.append("O"+objectVersionNo);
		return strBuff.toString();
	}
	
	/**
	 * To get only one object from corresponding to ikey from an abstract object
	 * @param ikey
	 * @return
	 */
	public String toString(InstanceKey ikey) {
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("O_"+objectVersionNo);
		if(ptsTo.contains(ikey))
			strBuff.append("["+ikey+"]");
		else {
			System.err.println("instance Key"+ikey+"does not belong to object"+this.toString());
			return null;
		}
		return strBuff.toString();
	}
}