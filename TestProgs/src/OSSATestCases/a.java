package OSSATestCases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import OBJS.OSSAObject;
import OSSAInstructions.ArgPhiOSSAInstruction;
import OSSAInstructions.InvokeOSSAInstruction;
import OSSAInstructions.OutsideObjOSSAInstruction;
import OSSAInstructions.PutPhiOSSAInstruction;
import OSSAInstructions.RetPhiOSSAInstruction;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSACFG.ExceptionHandlerBasicBlock;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.intset.OrdinalSet;

public class a {
	/**
	 * First part of algorithm
	 * inserts empty put-phi instructions in input SSA Ir.
	 * TODO check for robustness of phi insertion especially with processed list.
	 */
	public void insertPutPhi(){
		SSAInstruction [] instructions = ir.getInstructions();
		for (int i = 0; i <= cfg.getMaxNumber(); i++) {
			BasicBlock bb = cfg.getNode(i);
		    int start = bb.getFirstInstructionIndex();
		    int end = bb.getLastInstructionIndex();
		    if (bb instanceof ExceptionHandlerBasicBlock) {
		      continue;
		    }
		    
		    for (int j = start; j <= end; j++) {
		        
		    	  if (instructions[j] != null) {
		                	
		        	SSAInstruction instructn = instructions[j];
		        	SSAPutInstruction putInstrctn;
		        	int defVn;
		        	if (instructn instanceof SSAPutInstruction) {
						putInstrctn = (SSAPutInstruction) instructn;
						defVn = putInstrctn.getUse(0);
					}
		        	else if(instructn instanceof SSANewInstruction) {
		        		defVn = ((SSANewInstruction)instructn).getDef();
		        	}
		        	else if (instructn instanceof SSAGetInstruction) {
		        		SSAGetInstruction getFieldInstrctn = (SSAGetInstruction) instructn;
						if(getFieldInstrctn.isStatic()) {
							defVn = getFieldInstrctn.getDef();
						}
						else continue;
		        	}
		        	else if(instructn instanceof SSAInvokeInstruction) {
		        		SSAInvokeInstruction ivinstrctn = (SSAInvokeInstruction) instructn;
		        		//for objects passed as arguments
		        		int noofparameters = ivinstrctn.getNumberOfUses();
		        		for(int k=0;k<noofparameters;k++){
							int param = ivinstrctn.getUse(k);
							TypeReference tr = findType(ir, param).getTypeReference();
							if(!tr.isPrimitiveType()){
								insertPutPhiHelper(bb, param);
							}
		        		}
		        		//for objects returned by function call
		        		if(ivinstrctn.hasDef()) {
		        			int defValueNumber = ivinstrctn.getDef();
							TypeReference tr = findType(ir, defValueNumber).getTypeReference();
							if(tr.isReferenceType()) {
								defVn = defValueNumber;
							}
							else 
								continue;
		        		}
		        		else
		        			continue;
		        			
		        	}
		        	else
		        		continue;
		        	
					insertPutPhiHelper(bb, defVn);	
					//}//if (instructn instanceof SSAPutInstruction)
		    	  }//if (instructions[j] != null)
		    }//for (int j = start; j <= end; j++)
		}//for (int i = 0; i <= cfg.getMaxNumber(); i++)
	}//insertputPhi()
	
	public void insertPutPhiHelper(BasicBlock bb, int defVn) {
		PointerKey ptrKeyDef = hm.getPointerKeyForLocal(node,defVn);
    	OrdinalSet<InstanceKey> ptsToDef = pa.getPointsToSet(ptrKeyDef);
    	HashSet<BasicBlock> worklist = new HashSet<BasicBlock>();
    	worklist.add(bb);
    	HashSet<BasicBlock> processed = new HashSet<BasicBlock>();
    	while(!worklist.isEmpty()) {
    		for(Iterator<BasicBlock>elems = worklist.iterator();elems.hasNext();) {
    			BasicBlock nxt = elems.next();
    			worklist.remove(nxt);
    			
    				for(Iterator<BasicBlock> domFrontiers= RDF.getDominanceFrontier(nxt);domFrontiers.hasNext();){
						BasicBlock d = domFrontiers.next();
						if(processed.contains(d))
							continue;
						processed.add(d);
						if(d.isExitBlock())
							continue;
						if(!handledPhisMap.containsKey(d))
							handledPhisMap.put(d, new HashMap<SSAInstruction, OrdinalSet<InstanceKey>>());
						for(Iterator<OrdinalSet<InstanceKey>>handledPhiElems = handledPhisMap.get(d).values().iterator(); handledPhiElems.hasNext();){
							OrdinalSet<InstanceKey>handledPhi = handledPhiElems.next();
							if(equateASIs(ptsToDef, handledPhi))
								continue;
						}
						
						PutPhiOSSAInstruction putPhiOSSAInstrctn ;
//						if (instructn instanceof SSAPutInstruction) 
							putPhiOSSAInstrctn = new PutPhiOSSAInstruction(ptsToDef, cfg.getPredNodeCount(d),findType(ir, defVn).getTypeReference(),false);
//						else
//							putPhiOSSAInstrctn = new PutPhiOSSAInstruction(ptsToDef, cfg.getPredNodeCount(d),findType(ir, defVn).getTypeReference(),true);
//							objectPutPhiInstructions.put(d, putPhiOSSAInstrctn);
						
						handledPhisMap.get(d).put(putPhiOSSAInstrctn, ptsToDef);
						
						if(!objectPutPhiInstructions.containsKey(d))
							objectPutPhiInstructions.put(d, new LinkedList<PutPhiOSSAInstruction>());
						objectPutPhiInstructions.get(d).add(putPhiOSSAInstrctn);
						
						if(!allocationPhiMap.containsKey(d))
							allocationPhiMap.put(d, new HashMap<InstanceKey, SSAInstruction>());
						for(Iterator<InstanceKey> ASIs = ptsToDef.iterator();ASIs.hasNext();){
							InstanceKey asi = ASIs.next();
							if(!allocationPhiMap.get(d).containsKey(asi))
								allocationPhiMap.get(d).put(asi, putPhiOSSAInstrctn);
						}
						
						worklist.add(d);
						//DONE insert putphis iteratively into next domfrontiers also.
					}
    			
    		}
			
    	}
	}
	
	
	
	if(instructn instanceof SSAInvokeInstruction ){
		
		SSAInvokeInstruction ivinstrctn = (SSAInvokeInstruction) instructn;
		if(ivinstrctn.getDeclaredTarget().getName().toString().contains("println"))
			continue;
		//args of function call
		int noofparameters = ivinstrctn.getNumberOfUses();
		ArrayList<HashMap<InstanceKey, OSSAObject>> args = new ArrayList<HashMap<InstanceKey,OSSAObject>>();
		ArrayList<OSSAObject> changedArgs = new ArrayList<OSSAObject>();
		ArrayList<ArgPhiOSSAInstruction> argPhiInstrctns = new ArrayList<ArgPhiOSSAInstruction>();
//		InvokeOSSAInstruction invokeOSSAInstrctn = new InvokeOSSAInstruction(ivinstrctn, defObj, args, changedArgs);
		for(int k=0;k<noofparameters;k++){
			int param = ivinstrctn.getUse(k);
			TypeReference tr = findType(ir, param).getTypeReference();
			if(!tr.isPrimitiveType()){
				PointerKey ptrKey = hm.getPointerKeyForLocal(node, param);
				OrdinalSet<InstanceKey> ptsTo = pa.getPointsToSet(ptrKey);
				args.add(getUseObjs(ptsTo));
//				OSSAObject retObj = createObj(tr, ptsTo);
				//TODO Check Insert argphis()
				ArgPhiOSSAInstruction nwArgPhi = createArgPhi(tr, ptsTo, args.get(k));
				argPhiInstrctns.add(nwArgPhi);
				changedArgs.add(nwArgPhi.defObj);
//				invokeOSSAInstrctn.argsarray[k] = 
			}
			else {
				args.add(null);
				changedArgs.add(null);
				argPhiInstrctns.add(null);
			}
		}
		
		// function call returns some value or object.
		OSSAObject defObj = null;
		LinkedList<OutsideObjOSSAInstruction> outsideObjInstructions = new LinkedList<OutsideObjOSSAInstruction>();
		RetPhiOSSAInstruction retPhi =null;
		if(ivinstrctn.hasDef()) {
			int defValueNumber = ivinstrctn.getDef();
			TypeReference tr = findType(ir, defValueNumber).getTypeReference();
			if(tr.isReferenceType()) {
    			PointerKey ptrDef = hm.getPointerKeyForLocal(node, defValueNumber);
				OrdinalSet<InstanceKey> ptsTo = pa.getPointsToSet(ptrDef);
				//TODO Insert retPhi()
				if(ptsTo.size()==1) {
					for(Iterator<InstanceKey> ASIs = ptsTo.iterator();ASIs.hasNext();) {
						InstanceKey asi = ASIs.next();
						if(!objStack.containsKey(asi)) {
							OSSAObject newObj = new OSSAObject(objectVersionNoNext++, tr, ptsTo);
							objStack.put( asi, new Stack<OSSAObject>());
//											asi.getConcreteType().
							OutsideObjOSSAInstruction outsideObjInstrctn = new OutsideObjOSSAInstruction(newObj);
							DefUse.storeDef(newObj, outsideObjInstrctn);
							outsideObjInstructions.add(outsideObjInstrctn);
							objStack.get(asi).push(newObj);	
						}
						defObj = createObj(tr, ptsTo);
					}
				}
				boolean insertRetPhi=false;
				InstanceKey prevASI , newASI = null;
				OSSAObject contaningObj;
				for(Iterator<InstanceKey> ASIs = ptsTo.iterator();ASIs.hasNext();) {
					InstanceKey asi = ASIs.next();
					if(!objStack.containsKey(asi)) {
						OSSAObject newObj = new OSSAObject(objectVersionNoNext++, tr, ptsTo);
						objStack.put( asi, new Stack<OSSAObject>());
//								asi.getConcreteType().
						OutsideObjOSSAInstruction outsideObjInstrctn = new OutsideObjOSSAInstruction(newObj);
						DefUse.storeDef(newObj, outsideObjInstrctn);
						outsideObjInstructions.add(outsideObjInstrctn);
						objStack.get(asi).push(newObj);	
					}
					
					prevASI = newASI;
					newASI = asi;
					contaningObj =objStack.get(newASI).peek();
					if(prevASI==null)
						continue;		
					 
					if(contaningObj.equals(objStack.get(prevASI).peek())) {
						defObj = contaningObj;
						continue;
					}
					else {
						insertRetPhi=true;
						break;
					}							
				}
				
				RetPhiOSSAInstruction retphi=null;
				if(insertRetPhi) {
					retPhi = createRetPhi(tr, ptsTo,outsideObjInstructions);
					defObj = retPhi.defObj;
				}
//				else {
//					defObj =  
//				}
											
			}
		}//if(ivinstrctn.hasDef()) {
		
		InvokeOSSAInstruction invokeOSSAInstrctn = new InvokeOSSAInstruction(ivinstrctn, defObj, args, changedArgs, argPhiInstrctns, outsideObjInstructions, retPhi);
		DefUse.storeDef(defObj, invokeOSSAInstrctn);
		for(int i=0;i<noofparameters;i++) {
			HashMap<InstanceKey, OSSAObject>arg = args.get(i);
			if(arg!=null) {
				for(Iterator<OSSAObject>objs = arg.values().iterator();objs.hasNext();) {
					DefUse.storeUse(objs.next(), invokeOSSAInstrctn);
				}
			}
			
		}
		
		
		
		instrMap.put(ivinstrctn,invokeOSSAInstrctn);
		
	}//if(instructn instanceof SSAInvokeInstruction ){
	
	
	
	
	
}

private class class2{
	while(!worklist.isEmpty()) {
		for(Iterator<BasicBlock>elems = worklist.iterator();elems.hasNext();) {
			BasicBlock nxt = elems.next();
			worklist.remove(nxt);
			
				for(Iterator<BasicBlock> domFrontiers= RDF.getDominanceFrontier(nxt);domFrontiers.hasNext();){
					BasicBlock d = domFrontiers.next();
					if(processed.contains(d))
						continue;
					processed.add(d);
					if(d.isExitBlock())
						continue;
					if(!handledPhisMap.containsKey(d))
						handledPhisMap.put(d, new HashMap<SSAInstruction, OrdinalSet<InstanceKey>>());
					for(Iterator<OrdinalSet<InstanceKey>>handledPhiElems = handledPhisMap.get(d).values().iterator(); handledPhiElems.hasNext();){
						OrdinalSet<InstanceKey>handledPhi = handledPhiElems.next();
						if(equateASIs(ptsToDef, handledPhi))
							continue;
					}
					
					PutPhiOSSAInstruction putPhiOSSAInstrctn ;
					if (instructn instanceof SSAPutInstruction) 
						putPhiOSSAInstrctn = new PutPhiOSSAInstruction(ptsToDef, cfg.getPredNodeCount(d),findType(ir, defVn).getTypeReference(),false);
					else
						putPhiOSSAInstrctn = new PutPhiOSSAInstruction(ptsToDef, cfg.getPredNodeCount(d),findType(ir, defVn).getTypeReference(),true);
//							objectPutPhiInstructions.put(d, putPhiOSSAInstrctn);
					
					handledPhisMap.get(d).put(putPhiOSSAInstrctn, ptsToDef);
					
					if(!objectPutPhiInstructions.containsKey(d))
						objectPutPhiInstructions.put(d, new LinkedList<PutPhiOSSAInstruction>());
					objectPutPhiInstructions.get(d).add(putPhiOSSAInstrctn);
					
					if(!allocationPhiMap.containsKey(d))
						allocationPhiMap.put(d, new HashMap<InstanceKey, SSAInstruction>());
					for(Iterator<InstanceKey> ASIs = ptsToDef.iterator();ASIs.hasNext();){
						InstanceKey asi = ASIs.next();
						if(!allocationPhiMap.get(d).containsKey(asi))
							allocationPhiMap.get(d).put(asi, putPhiOSSAInstrctn);
					}
					
					worklist.add(d);
					//DONE insert putphis iteratively into next domfrontiers also.
				}
			
		}
		
	}
}









