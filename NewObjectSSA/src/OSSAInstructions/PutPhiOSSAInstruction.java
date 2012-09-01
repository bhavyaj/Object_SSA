package OSSAInstructions;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.crypto.KeySelector.Purpose;

import OBJS.OSSAObject;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * PutPhiOSSAInstruction 
 * right now it implements only first part of algorithm. i.e. insertion of put-phis
 * @author yash
 *
 */
public class PutPhiOSSAInstruction extends SSAInstruction{
	/*
	 * Object defined by this put-phi instruction
	 */
	public OSSAObject defObj;
	/**
	 * no. of predecessors of basic block contaning this putphi = no of args of this putphi
	 */
	int noArgs;
	/**
	 * arguments of putphi, for abstracat objects even args will have args. 
	 * Inner hashmap stores those args along with the basicblock from which they come.
	 */
	public HashMap<InstanceKey,HashMap<BasicBlock, OSSAObject>> putPhiArgs = new HashMap<InstanceKey, HashMap<BasicBlock,OSSAObject>>();
	
	/**
	 * ptsTo set of object created by this putphi
	 */
	public OrdinalSet<InstanceKey>ptsTo;
	
	/*
	 * 
	 */
	public TypeReference concreteType;
	
	public Boolean isCreatePhi = false;
	/**
	 * 
	 * @param ptsTo
	 * @param predCount
	 * 
	 * ASI is allocation site identifier
	 */
	public PutPhiOSSAInstruction(OrdinalSet<InstanceKey>ptsTo, int predCount, TypeReference concreteType, Boolean isCreatePhi){
		this.noArgs = predCount;
		this.ptsTo = ptsTo;
		for(Iterator<InstanceKey> ASIs = ptsTo.iterator();ASIs.hasNext();){
			InstanceKey ASI = ASIs.next();
			putPhiArgs.put(ASI, new HashMap<BasicBlock, OSSAObject>());
		}
		this.concreteType = concreteType;
		this.isCreatePhi = isCreatePhi;
		
	}
	
	/**
	 * constructor just to create empty arrays
	 */
	public PutPhiOSSAInstruction() {
		
	}
	/**
	 * currently will return in args only the ASIs
	 * @return string representation of put-phi instruction
	 */
	public String toString(){
		StringBuffer strbuff = new StringBuffer();
		strbuff.append(defObj);
		if(isCreatePhi)
			strbuff.append(" = Object-CreatePhi(");
		else
			strbuff.append(" = Object-PutPhi(");
		for(Iterator<InstanceKey>argsinstances = putPhiArgs.keySet().iterator();argsinstances.hasNext();){
			InstanceKey argsInstanceKey = argsinstances.next();
			strbuff.append("[");
			for(Iterator<OSSAObject>asiargs = putPhiArgs.get(argsInstanceKey).values().iterator();asiargs.hasNext();) {
				OSSAObject nxtarg = asiargs.next();
				strbuff.append(nxtarg+", ");
			}
			strbuff.deleteCharAt((strbuff.length())-2);
			strbuff.append("]"+argsInstanceKey.getConcreteType().getName()+", ");
		}
		strbuff.deleteCharAt((strbuff.length())-2); 	//deletes last ','
		strbuff.append(")");
		return strbuff.toString();
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
