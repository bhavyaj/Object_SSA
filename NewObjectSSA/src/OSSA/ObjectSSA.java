package OSSA;

import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import OBJS.OSSAObject;
import OSSAInstructions.ArgPhiOSSAInstruction;
import OSSAInstructions.CreateOSSAInstruction;
import OSSAInstructions.DPhiOSSAInstruction;
import OSSAInstructions.FuncOSSAInstruction;
import OSSAInstructions.GetFieldOSSAInstruction;
import OSSAInstructions.InvokeOSSAInstruction;
import OSSAInstructions.OutsideObjOSSAInstruction;
import OSSAInstructions.PutFieldOSSAInstruction;
import OSSAInstructions.PutPhiOSSAInstruction;
import OSSAInstructions.RetPhiOSSAInstruction;
import OSSAInstructions.ReturnOSSAInstruction;

import com.ibm.wala.analysis.typeInference.TypeAbstraction;
import com.ibm.wala.analysis.typeInference.TypeInference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.model.java.lang.reflect.Array;
import com.ibm.wala.shrikeBT.NewInstruction;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAIndirectionData;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSACFG.ExceptionHandlerBasicBlock;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.ArraySet;
import com.ibm.wala.util.graph.dominators.DominanceFrontiers;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableMapping;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.OrdinalSetMapping;
import com.ibm.wala.util.strings.StringStuff;

public class ObjectSSA {

	/**
	 * Version no assigned to next definition of OSSAObject in ObjectSSA
	 */
	public int objectVersionNoNext = 1;
	/**
	 * The call site identifier for function calls. 
	 * might be used to link function call phis with function call OSSA statements
	 */
	public int funcCallSiteNoNext = 1;
	public  ClassHierarchy cha;
	public IR ir;
	
	/**
	 * SSACFG for the ir.
	 */
	public SSACFG cfg;
	/**
	 * Pointer analysis OSSAObject for getting the heap instances. 
	 */
	public  PointerAnalysis pa;
	
	/**
	 * the heap model corresponding to pointer-analysis OSSAObject pa. Used for getting the heap instances.
	 */
	public  HeapModel hm;
	
	public CGNode node;
	
	public ObjectSSADefUse DefUse = new ObjectSSADefUse();
	
	/*
	 * if a static object is being accessed
	 */
	public boolean staticAcces = false;
	
	/**
	 * if hidden arguments(arguments not directly passed but referred by link of references from passed args.) are accessed
	 */
	public boolean hiddenArgAccess = false;
	
	/**
	 * an empty object representing the nullobj in ossa.
	 */
	public static OSSAObject nullobj = new OSSAObject(0);
	/**
	 * RDF contains dominance frontiers of all basic blocks
	 */
	public  DominanceFrontiers<BasicBlock> RDF;
	public  String methodName;
	public IMethod method;
	public static String methodNameStatic;
	public static ArraySet<String> methodSignSetStatic;
	
	public static HashMap<String,ArrayList<InstanceKey>> HiddenargToAsi = new HashMap<String,ArrayList<InstanceKey>>(); 
	public static HashMap<String,ArrayList<SSAInstruction>> HiddenargToInstruction = new HashMap<String,ArrayList<SSAInstruction>>();
	public static HashMap<SSAInvokeInstruction, ArraySet<String>> SSAInvokeToMethodList = new HashMap<SSAInvokeInstruction, ArraySet<String>>();
	public static HashMap<String,ArrayList<InstanceKey>> HiddenReturnToAsi = new HashMap<String,ArrayList<InstanceKey>>(); 
	
	/**
	 * Constructor for ObjectSSA. Generates separate OSSA for separate methods
	 * @param cha {@link ClassHierarchy}
	 * @param ir {@link IR}
	 * @param pa {@link PointerAnalysis}
	 * @param hm {@link HeapModel}
	 * @param cg {@link CallGraph}
	 * @param m {@link IMethod}
	 */
	public ObjectSSA(ClassHierarchy cha, CallGraph cg, PointerAnalysis pa, HeapModel hm, IMethod m, IR ir, ArraySet<String> methodsignSet){//, String psFiles, String dotFiles){
		try{
			this.cha = cha;
			this.cfg= ir.getControlFlowGraph();
			this.ir = ir;
			this.pa = pa;
			this.hm = hm;
			this.method = m;
			this.methodName = method.getSignature();
			this.node = cg.getNode(method, Everywhere.EVERYWHERE);
			methodNameStatic = method.getSignature();
			methodSignSetStatic = methodsignSet;
//			this.psFiles = psFiles;
//			this.dotFiles = dotFiles;
			instrMap = new HashMap<SSAInstruction, SSAInstruction>();
			RDF = (new DominatorTree< BasicBlock>(cfg)).createDominanceFrontiers();		//RDF contains dominancefrontiers
//			System.out.println("\n\n"+RDF+"\n\n");
//			CGNode node = cg.getNode(method, Everywhere.EVERYWHERE);
//			IR ir = node.getIR();
//			System.out.println("IR for method:"+methodName+"\n"+ir.toString());
//			(new pdfIr()).printIr(cha, ir, methodName,psFiles, dotFiles);
//			translator(ir,node);
//			
			//Calculate no of phis in SSA IR
			noOfCtrPhisinSSA = 0;
			for(Iterator<SSAInstruction>phiinstrctns = (Iterator<SSAInstruction>) ir.iteratePhis();phiinstrctns.hasNext();) {
				phiinstrctns.next();
				noOfCtrPhisinSSA++;
			}
			noOfPutPhis = 0 ;
			noOfDPhis = 0;
			noOfRetPhis = 0;
			noOfReturns = 0;
			noOfArgPhis = 0;
			populateHiddenArgsandReturns(cha, cg, hm, pa, methodsignSet); // populating hidden arguments and hidden returns
			insertPutPhi();
			rename();
			populateOSSAInstructions();
			noOfCtrPhisinOSSA = ctrlPhiInstructions.size();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Stores ctrl-Phis for all scalars(not references) in basic bolck, These control phi instructios are taken directly from WALA Ir.
	 */
	public HashMap<BasicBlock, LinkedList<SSAPhiInstruction>> ctrlPhiInstructions = new HashMap<BasicBlock, LinkedList<SSAPhiInstruction>>();
	
	/**
	 * Stores Putphis for all objects in corresponding basic-blocks.
	 * MIght be redundant with handledPhismap
	 */
//	public  Hashtable<BasicBlock, HashMap<OSSAObject,PutPhiOSSAInstruction>> objectPutPhiInstructions = new Hashtable<BasicBlock, HashMap<OSSAObject,PutPhiOSSAInstruction>>();
	public HashMap<BasicBlock, LinkedList<PutPhiOSSAInstruction>> objectPutPhiInstructions = new HashMap<BasicBlock, LinkedList<PutPhiOSSAInstruction>>();
	/**
	 * Instruction label mapped to all pointsTO set defined through this phi. 
	 * Stored for each basic-block contaning putφ.
	 * Might be redundant with objectputphiinstructions
	 * TODO check for redundancy
	 */
	public HashMap<BasicBlock, HashMap<SSAInstruction, OrdinalSet<InstanceKey>>> handledPhisMap =  new HashMap<BasicBlock, HashMap<SSAInstruction,OrdinalSet<InstanceKey>>>();
	
	/**
	 * Allocation Site of OSSAObject mapped to put-φ instruction label. 
	 * Stored for each basic-block contaning put-φs.
	 */
	public HashMap<BasicBlock, HashMap<InstanceKey, SSAInstruction>> allocationPhiMap = new HashMap<BasicBlock, HashMap<InstanceKey,SSAInstruction>>();
	
	
	public int noOfPutPhis;
	public int noOfDPhis;
	public int noOfCtrPhisinOSSA;
	public int noOfCtrPhisinSSA;
	public int noOfArgPhis;
	public static int noOfRetPhis;
	public static int noOfReturns;
	
	
	public static void populateHiddenArgsandReturns(ClassHierarchy cha, CallGraph cg, HeapModel hm, PointerAnalysis pa, ArraySet<String> methodsignSet)
	{

		//populate hiddenargtoasi and hiddenargtoinstruction
		
		for(Iterator<String> methodSignIter = methodsignSet.iterator();methodSignIter.hasNext();){
			 String methodSignElems = methodSignIter.next();
			 MethodReference mr0 = StringStuff.makeMethodReference(methodSignElems);
			 IMethod m = cha.resolveMethod(mr0);
			 CGNode node = cg.getNode(m, Everywhere.EVERYWHERE);
			 if(node != null)
			 {
				 IR ir = node.getIR();
				 
				 SSACFG cfg= ir.getControlFlowGraph();
				 SSAInstruction [] instructions = ir.getInstructions();
				 if(HiddenargToAsi.get(methodSignElems)==null){
					 HiddenargToAsi.put(methodSignElems, new ArrayList<InstanceKey>());
				 }
				 if(HiddenargToInstruction.get(methodSignElems)==null){
					 HiddenargToInstruction.put(methodSignElems, new ArrayList<SSAInstruction>());	 
				 }
				 
				 for (int i = 0; i <= cfg.getMaxNumber(); i++) {
					 BasicBlock bb_1 = cfg.getNode(i);
					 int start_1 = bb_1.getFirstInstructionIndex();
					 int end_1 = bb_1.getLastInstructionIndex();
					 if (bb_1 instanceof ExceptionHandlerBasicBlock) {
						 continue;
					 }
				    
					 for (int j = start_1; j <= end_1; j++) {
				        
						 if (instructions[j] != null) {
							 SSAInstruction instructn_1 = instructions[j];
							 if(instructn_1 instanceof SSAInvokeInstruction){
								 SSAInvokeInstruction invokeinstructn = (SSAInvokeInstruction) instructn_1;
								 if(invokeinstructn.getDeclaredTarget().getName().toString().contains("println"))
										continue;
								 String temp_methodsign =  invokeinstructn.getDeclaredTarget().getSignature();
								 // check here for noofuses and noofparams
								 int no_of_params = invokeinstructn.getNumberOfUses();
								 for(int k=0;k<no_of_params;k++){
									 int parameter = invokeinstructn.getUse(k);
										TypeReference tr_1 = findTypeParameter(ir, parameter).getTypeReference();
										if(tr_1!=null&&!tr_1.isPrimitiveType()){
											PointerKey pointerKey = hm.getPointerKeyForLocal(node, parameter);
											OrdinalSet<InstanceKey> pointsTo = pa.getPointsToSet(pointerKey);
											for(Iterator<InstanceKey> instanceIter = pointsTo.iterator(); instanceIter.hasNext();){
												InstanceKey instanceElems = instanceIter.next();
												if(HiddenargToAsi.get(temp_methodsign) == null){
													HiddenargToAsi.put(temp_methodsign,new ArrayList<InstanceKey>());
													HiddenargToInstruction.put(temp_methodsign,new ArrayList<SSAInstruction>());
												}
												//if(!HiddenargToAsi.get(temp_methodsign).contains(instanceElems)){
													HiddenargToAsi.get(temp_methodsign).add(instanceElems);
													
													HiddenargToInstruction.get(temp_methodsign).add(invokeinstructn);
												//}
											}
										}
								 }
							 }
						 }
						 
					 }
				 }
			 }
				 //added for inserting arguments in hashmap in 1st phase
			}
			
			{
				 String methodSignElems = methodNameStatic;
				 MethodReference mr0 = StringStuff.makeMethodReference(methodSignElems);
				 IMethod m = cha.resolveMethod(mr0);
				 CGNode node = cg.getNode(m, Everywhere.EVERYWHERE);
				 if(node != null)
				 {
					 IR ir = node.getIR();
					 
					 SSACFG cfg= ir.getControlFlowGraph();
					 SSAInstruction [] instructions = ir.getInstructions();
					 
					 
					 for (int i = 0; i <= cfg.getMaxNumber(); i++) {
						 BasicBlock bb_1 = cfg.getNode(i);
						 int start_1 = bb_1.getFirstInstructionIndex();
						 int end_1 = bb_1.getLastInstructionIndex();
						 if (bb_1 instanceof ExceptionHandlerBasicBlock) {
							 continue;
						 }
					    
						 for (int j = start_1; j <= end_1; j++) {
					        
							 if (instructions[j] != null) {
								 SSAInstruction instructn_1 = instructions[j];
								 if(instructn_1 instanceof SSAInvokeInstruction){
									 SSAInvokeInstruction invokeinstructn = (SSAInvokeInstruction) instructn_1;
									 if(invokeinstructn.getDeclaredTarget().getName().toString().contains("println"))
											continue;
									 String temp_methodsign =  invokeinstructn.getDeclaredTarget().getSignature();
									 ArraySet<String> worklistMethodsign = new ArraySet<String>();
									 
									 if(methodsignSet.contains(temp_methodsign))
									 {
										 //worklistMethodsign.add(temp_methodsign);
										 //worklistHiddenArgs(cha, cg, hm, pa, temp_methodsign, worklistMethodsign, methodsignSet);
										 ArrayList<String> visitedMethod = new ArrayList<String>();
										 //dfsHiddenArgs(cha,cg,hm,pa,temp_methodsign,temp_methodsign,methodsignSet, visitedMethod);
										 //SSAInvokeToMethodList.put(invokeinstructn, worklistMethodsign);
										 ArrayList<String> visitedReturnMethod = new ArrayList<String>();
										 HiddenReturnToAsi.put(temp_methodsign,new ArrayList<InstanceKey>());
										 dfsHiddenReturns(cha,cg,hm,pa,temp_methodsign,temp_methodsign,methodsignSet, visitedReturnMethod);
								     }
								}
							 }
						 }
					 }
				 }
			}
			
	}
	
	
	public static void dfsHiddenArgs(ClassHierarchy cha, CallGraph cg, HeapModel hm, PointerAnalysis pa,String rootMethodSign, String methodsign, ArraySet<String> methodsignSet, ArrayList<String> visitedMethod)
	{
		 visitedMethod.add(methodsign);
		 ArrayList<String> tempWorklistMethodsign = new ArrayList<String>();
		 MethodReference mr0 = StringStuff.makeMethodReference(methodsign);
		 IMethod m = cha.resolveMethod(mr0);
		 CGNode node = cg.getNode(m, Everywhere.EVERYWHERE);
		 if(node != null)
		 {
			 IR ir = node.getIR();
			 
			 SSACFG cfg= ir.getControlFlowGraph();
			 SSAInstruction [] instructions = ir.getInstructions();
			 
			 for (int i = 0; i <= cfg.getMaxNumber(); i++) {
				 BasicBlock bb_1 = cfg.getNode(i);
				 int start_1 = bb_1.getFirstInstructionIndex();
				 int end_1 = bb_1.getLastInstructionIndex();
				 if (bb_1 instanceof ExceptionHandlerBasicBlock) {
					 continue;
				 }
			    
				 for (int j = start_1; j <= end_1; j++) {
					 if (instructions[j] != null) {
						 SSAInstruction instructn_1 = instructions[j];
						 if(instructn_1 instanceof SSAInvokeInstruction){
							 SSAInvokeInstruction invokeinstructn = (SSAInvokeInstruction) instructn_1;
							 if(invokeinstructn.getDeclaredTarget().getName().toString().contains("println"))
									continue;
							 else
							 {
								 String temp_methodsign =  invokeinstructn.getDeclaredTarget().getSignature();
								 if(methodsignSet.contains(temp_methodsign))
								 {
									 tempWorklistMethodsign.add(temp_methodsign);
								 }
							 }
						 }
						 if(instructn_1 instanceof SSAGetInstruction)
						 {
							 SSAGetInstruction getFieldInstrct = (SSAGetInstruction) instructn_1;
							 ////// check //////
							 if(!getFieldInstrct.isStatic())
							 {
								 //OrdinalSet<InstanceKey> containerPtsTo = pa.getPointsToSet(containerPtrKey);
								 FieldReference field = getFieldInstrct.getDeclaredField();
								 if(field.getFieldType().isReferenceType()) {
									 int fieldValueNumber = getFieldInstrct.getDef();
									 PointerKey fieldPtrKey = hm.getPointerKeyForLocal(node,fieldValueNumber);
									 OrdinalSet<InstanceKey> fieldPtsTo = pa.getPointsToSet(fieldPtrKey);
									 int getvariableValueNo = getFieldInstrct.getUse(1);
									 ////check here..,.needs to be confirmed for getUse(1)
									 if(getvariableValueNo <= 0)
									 {
										 System.out.println("instruction is "+getFieldInstrct.toString()+" and methodsignature is "+methodsign);
									 }
									 PointerKey varibalePtrKey = hm.getPointerKeyForLocal(node, getvariableValueNo);
									 OrdinalSet<InstanceKey> variablePtsTo = pa.getPointsToSet(varibalePtrKey);
									 boolean flag = false;
									 for(Iterator<InstanceKey> variablePointsToIter= variablePtsTo.iterator();variablePointsToIter.hasNext();){
										 InstanceKey variablePointsToIterElems = variablePointsToIter.next();
										 if(HiddenargToAsi.get(rootMethodSign).contains(variablePointsToIterElems)){
											 flag = true;
										 }
										 
									 }
									 //// iterator on field asi....check
									 if(flag){
										 for(Iterator<InstanceKey> fieldPointsToIterator = fieldPtsTo.iterator();fieldPointsToIterator.hasNext();){
											 InstanceKey fieldPointsToElems = fieldPointsToIterator.next();
											 HiddenargToAsi.get(rootMethodSign).add(fieldPointsToElems);
											 HiddenargToInstruction.get(rootMethodSign).add(getFieldInstrct);
										 }
										 
									 }
								 }
							 }
						 }
					 }
				 }
			 }
		 
			 if(!tempWorklistMethodsign.isEmpty())
			 {
				 for(Iterator<String> workListMethodIter = tempWorklistMethodsign.iterator(); workListMethodIter.hasNext();)
				 {
					 String workListMethodElem = workListMethodIter.next();
					 if(!visitedMethod.contains(workListMethodElem))
					 {
						 dfsHiddenArgs(cha,cg,hm,pa,rootMethodSign,workListMethodElem,methodsignSet,visitedMethod);
					 }
				 }
			 }
		 }
		 
	}
	
	
	
	
	public static void dfsHiddenReturns(ClassHierarchy cha, CallGraph cg, HeapModel hm, PointerAnalysis pa,String rootMethodSign, String methodsign, ArraySet<String> methodsignSet, ArrayList<String> visitedReturnMethod)
	{
		 visitedReturnMethod.add(methodsign);
		 ArrayList<InstanceKey> tempHiddenReturn = new ArrayList<InstanceKey>();
		 ArrayList<String> tempWorklistMethodsign = new ArrayList<String>();
		 MethodReference mr0 = StringStuff.makeMethodReference(methodsign);
		 IMethod m = cha.resolveMethod(mr0);
		 CGNode node = cg.getNode(m, Everywhere.EVERYWHERE);
		 if(node != null)
		 {
			 IR ir = node.getIR();
			 
			 SSACFG cfg= ir.getControlFlowGraph();
			 SSAInstruction [] instructions = ir.getInstructions();
			 
			 for (int i = 0; i <= cfg.getMaxNumber(); i++) {
				 BasicBlock bb_1 = cfg.getNode(i);
				 int start_1 = bb_1.getFirstInstructionIndex();
				 int end_1 = bb_1.getLastInstructionIndex();
				 if (bb_1 instanceof ExceptionHandlerBasicBlock) {
					 continue;
				 }
			    
				 for (int j = start_1; j <= end_1; j++) {
			        
					 if (instructions[j] != null) {
						 SSAInstruction instructn_1 = instructions[j];
						 if(instructn_1 instanceof SSANewInstruction){
							 SSANewInstruction invokeinstructn = (SSANewInstruction) instructn_1;
							 //if(invokeinstructn.getDeclaredTarget().getName().toString().contains("println"))
							//		continue;
							 //if(invokeinstructn.getDeclaredTarget().getName().toString().contains("init"))
							 //{
								 int newVal = invokeinstructn.getDef();
								 PointerKey point_key = hm.getPointerKeyForLocal(node, newVal);
								 OrdinalSet<InstanceKey> ptsTo_Def = pa.getPointsToSet(point_key);
								 tempHiddenReturn.addAll(OrdinalSet.toCollection(ptsTo_Def));
								 /*
								 int no_of_uses = invokeinstructn.getNumberOfUses();
								 if(no_of_uses<1)
								 {
								 	 continue;
								 }
								 int parameter = invokeinstructn.getUse(0);
								 TypeReference tr_1 = findTypeParameter(ir, parameter).getTypeReference();
								 if(tr_1!=null&&!tr_1.isPrimitiveType()){
									PointerKey pointerKey = hm.getPointerKeyForLocal(node, parameter);
									OrdinalSet<InstanceKey> pointsTo = pa.getPointsToSet(pointerKey);
									boolean flag = false;
									for(Iterator<InstanceKey> instanceIter = pointsTo.iterator(); instanceIter.hasNext();){
										InstanceKey instanceElems = instanceIter.next();
										if(flag) continue;
										if(HiddenargToAsi.get(rootMethodSign).contains(instanceElems) || HiddenReturnToAsi.get(rootMethodSign).contains(instanceElems)){
											for(Iterator<InstanceKey> defIter = ptsTo_Def.iterator(); defIter.hasNext();)
											{
												InstanceKey defIterElem = defIter.next();
												HiddenReturnToAsi.get(rootMethodSign).add(defIterElem);	
											}
											flag = true;
											break;
										}
									}
								 }
								 */
							 }
							 if(instructn_1 instanceof SSAInvokeInstruction)
							 {
								 SSAInvokeInstruction invokeinstructn_1 = (SSAInvokeInstruction) instructn_1;
								 String temp_methodsign =  invokeinstructn_1.getDeclaredTarget().getSignature();
								 if(invokeinstructn_1.getDeclaredTarget().getName().toString().contains("println"))
										continue;
								 if(methodsignSet.contains(temp_methodsign))
								 {
									 tempWorklistMethodsign.add(temp_methodsign);
								 }
							 }
						}
					 }
				 }
		 
			 for (int i = 0; i <= cfg.getMaxNumber(); i++) {
				 BasicBlock bb_1 = cfg.getNode(i);
				 int start_1 = bb_1.getFirstInstructionIndex();
				 int end_1 = bb_1.getLastInstructionIndex();
				 if (bb_1 instanceof ExceptionHandlerBasicBlock) {
					 continue;
				 }
			    
				 for (int j = start_1; j <= end_1; j++) {
			        
					 if (instructions[j] != null)
					 {
						 SSAInstruction instructn_1 = instructions[j];
						 if(instructn_1 instanceof SSAPutInstruction)
						 {
							 SSAPutInstruction invokeinstructn = (SSAPutInstruction) instructn_1;
							 if(!invokeinstructn.isStatic())
							 {
								 int tempVar0 = invokeinstructn.getUse(0);
								 int tempVar1 = invokeinstructn.getUse(1);
								 PointerKey point_key1 = hm.getPointerKeyForLocal(node, tempVar1);
								 OrdinalSet<InstanceKey> ptsTo_Def1 = pa.getPointsToSet(point_key1);
								 int tempVar2 = invokeinstructn.getUse(2);
								 PointerKey point_key2 = hm.getPointerKeyForLocal(node, tempVar2);
								 OrdinalSet<InstanceKey> ptsTo_Def2 = pa.getPointsToSet(point_key2);
								 boolean flag1 =false;
								 boolean flag2 = false;
								 for(Iterator<InstanceKey> pointsToIter2 = ptsTo_Def2.iterator();pointsToIter2.hasNext();)
								 {
									 InstanceKey pointsToElems2 = pointsToIter2.next();
									 if(tempHiddenReturn.contains(pointsToElems2)){
										 flag1 =true;
										 break;
									 }
								 }
								 if(flag1)
								 {
									 
									 PointerKey point_key0 = hm.getPointerKeyForLocal(node, tempVar0);
									 OrdinalSet<InstanceKey> ptsTo_Def0 = pa.getPointsToSet(point_key0);
									 for(Iterator<InstanceKey> ptsToDefIter0 = ptsTo_Def0.iterator();ptsToDefIter0.hasNext();)
									 {
										 InstanceKey ptsToDefElem0 = ptsToDefIter0.next();
										 if(HiddenargToAsi.get(rootMethodSign).contains(ptsToDefElem0) )
										 {
											 flag2 = true;
											 break;
										 }
										 else if(!HiddenReturnToAsi.get(rootMethodSign).isEmpty())
										 {
											 if(HiddenReturnToAsi.get(rootMethodSign).contains(ptsToDefElem0))
											 {
												 flag2=true;
												 break;
											 }
										 }						 
									 }
									 if(flag2)
									 {
										 for(Iterator<InstanceKey> pts_To_Iter1 = ptsTo_Def1.iterator();pts_To_Iter1.hasNext();){
											 InstanceKey pts_To_Elem = pts_To_Iter1.next();
											 HiddenReturnToAsi.get(rootMethodSign).add(pts_To_Elem);
											 noOfRetPhis++;
										 }
										 //HiddenReturnToAsi.get(rootMethodSign).addAll(OrdinalSet.toCollection(ptsTo_Def1));
										 
									 }
								 }
								 
							 }
							 
							 
						 }
					 }
				 }
			 }
			 if(!tempWorklistMethodsign.isEmpty())
			 {
				 for(Iterator<String> workListMethodIter = tempWorklistMethodsign.iterator(); workListMethodIter.hasNext();)
				 {
					 String workListMethodElem = workListMethodIter.next();
					 if(!visitedReturnMethod.contains(workListMethodElem))
					 {
						 dfsHiddenReturns(cha,cg,hm,pa,rootMethodSign,workListMethodElem,methodsignSet,visitedReturnMethod);
				 	 }
				 }
				 
			 }
		 }
	}
			 
									 
							
	
	public static void worklistHiddenArgs(ClassHierarchy cha, CallGraph cg, HeapModel hm, PointerAnalysis pa, String methodsign,ArraySet<String> worklistMethodsign, ArraySet<String> methodsignSet){
		ArraySet<String> tempWorklistMethodsign = new ArraySet<String>();
		MethodReference mr0 = StringStuff.makeMethodReference(methodsign);
		 IMethod m = cha.resolveMethod(mr0);
		 CGNode node = cg.getNode(m, Everywhere.EVERYWHERE);
		 if(node != null)
		 {
			 IR ir = node.getIR();
			 
			 SSACFG cfg= ir.getControlFlowGraph();
			 SSAInstruction [] instructions = ir.getInstructions();
			 
			 for (int i = 0; i <= cfg.getMaxNumber(); i++) {
				 BasicBlock bb_1 = cfg.getNode(i);
				 int start_1 = bb_1.getFirstInstructionIndex();
				 int end_1 = bb_1.getLastInstructionIndex();
				 if (bb_1 instanceof ExceptionHandlerBasicBlock) {
					 continue;
				 }
			    
				 for (int j = start_1; j <= end_1; j++) {
			        
					 if (instructions[j] != null) {
						 SSAInstruction instructn_1 = instructions[j];
						 if(instructn_1 instanceof SSAInvokeInstruction){
							 SSAInvokeInstruction invokeinstructn = (SSAInvokeInstruction) instructn_1;
							 if(invokeinstructn.getDeclaredTarget().getName().toString().contains("println"))
									continue;
							 
							 String temp_methodsign =  invokeinstructn.getDeclaredTarget().getSignature();
							 if(methodsignSet.contains(temp_methodsign))
							 {
								 tempWorklistMethodsign.add(temp_methodsign);
							 }
						 }
					 }
				 }
							 
			 }
			 if(!tempWorklistMethodsign.isEmpty()){
				 worklistMethodsign.addAll(tempWorklistMethodsign);
				 for(Iterator<String> methodsignIter = tempWorklistMethodsign.iterator();methodsignIter.hasNext();){
					 String methodsignElems = methodsignIter.next();
					 worklistHiddenArgs(cha,cg, hm, pa, methodsignElems ,worklistMethodsign, methodsignSet);
				 }
			 }
			 
			 
		 }
	}
		
	
	
	public static TypeAbstraction findTypeParameter (IR ir, int valueNumber){
		boolean doPrimitives = true; // infer types for primitive vars?
	    TypeInference ti = TypeInference.make(ir, doPrimitives);
	    TypeAbstraction type = ti.getType(valueNumber);
	    return type;
	}
	
	
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
							if(tr!=null&&!tr.isPrimitiveType()){
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
    		HashSet<BasicBlock> worklist2 =worklist;
    		for(Iterator<BasicBlock>elems = worklist.iterator();elems.hasNext();) {
    			BasicBlock nxt = elems.next();
    			worklist2.remove(nxt);
    			
    				for(Iterator<BasicBlock> domFrontiers= RDF.getDominanceFrontier(nxt);domFrontiers.hasNext();){
						BasicBlock d = domFrontiers.next();
						if(processed.contains(d))
							continue;
						processed.add(d);
						if(d.isExitBlock())
							continue;
						if(!handledPhisMap.containsKey(d))
							handledPhisMap.put(d, new HashMap<SSAInstruction, OrdinalSet<InstanceKey>>());
						HashMap<BasicBlock, HashMap<SSAInstruction, OrdinalSet<InstanceKey>>> handledPhisMapTemp =  new HashMap<BasicBlock, HashMap<SSAInstruction,OrdinalSet<InstanceKey>>>();
						for(Iterator<BasicBlock> block = handledPhisMap.keySet().iterator();block.hasNext();)
						{
							BasicBlock basicblock = block.next();
							HashMap<SSAInstruction, OrdinalSet<InstanceKey>> tempHashMap = new HashMap<SSAInstruction, OrdinalSet<InstanceKey>>();
							for(Iterator<SSAInstruction> instructIter = handledPhisMap.get(basicblock).keySet().iterator();instructIter.hasNext();)
							{
								SSAInstruction instruct = instructIter.next();
								//OrdinalSet<InstanceKey> asi = handledPhisMap.get(block).get(instruct);
								
								tempHashMap.put(instruct,handledPhisMap.get(basicblock).get(instruct) );
							}
							handledPhisMapTemp.put(basicblock, tempHashMap);
						}
						for(Iterator<OrdinalSet<InstanceKey>>handledPhiElems = handledPhisMapTemp.get(d).values().iterator(); handledPhiElems.hasNext();)
						{	
							OrdinalSet<InstanceKey>handledPhi = handledPhiElems.next();
							//search for each elements in ptsdef that it exists in handledphi....
							
							OrdinalSet<InstanceKey> handledIntersect = OrdinalSet.intersect(ptsToDef , handledPhi);
							//if ptsdef and handledphi has common element, then scan for the instruction that has common element and remove the corresponding argument from the instruction
							if(!handledIntersect.isEmpty())	{
								Set<SSAInstruction> handledPhisMapSet=handledPhisMapTemp.get(d).keySet();
								SSAInstruction instruction ;
								Iterator<SSAInstruction> SSAInstructionTemp = handledPhisMapSet.iterator();
								instruction = SSAInstructionTemp.next();
								
								
								for(Iterator<SSAInstruction> SSAInstructionElems = handledPhisMapSet.iterator();SSAInstructionElems.hasNext();){
									SSAInstruction SSAInstructionElemsNext=SSAInstructionElems.next();
									
									if(handledPhisMapTemp.get(d).get(SSAInstructionElemsNext) == handledPhi){
										instruction = SSAInstructionElemsNext;
										break;
									}
								}
								
								
								Collection<InstanceKey> handledIntersectCollection = OrdinalSet.toCollection(handledIntersect);
								Collection<InstanceKey> handledPhiCollection = OrdinalSet.toCollection(handledPhi);
								Collection<InstanceKey> handledPhiRemaining = handledPhiCollection;
								for(Iterator<InstanceKey> handledPhiCollectionElems = handledIntersectCollection.iterator(); handledPhiCollectionElems.hasNext(); ){
									InstanceKey handledPhiCollectionElemsNext= handledPhiCollectionElems.next();
									handledPhiRemaining.remove(handledPhiCollectionElemsNext);
								}
								//modified handledphi by creating handledphitemp which is modified version of handledphi and then removing the instruction and handledphi from handledphimap hash map and then inserting instruction and handledphitemp in the hashmap 
								
								
								OrdinalSet<InstanceKey> handledPhiTemp = OrdinalSet.toOrdinalSet(handledPhiRemaining, handledPhi.getMapping());
								//handledPhisMap.get(d).remove(instruction);
												
								
								//PutPhiOSSAInstruction phInstruction = (PutPhiOSSAInstruction) instruction;
								for(Iterator<InstanceKey> phInstrIter= handledIntersect.iterator(); phInstrIter.hasNext();)
								{
									InstanceKey phInstrASI = phInstrIter.next();	
									((PutPhiOSSAInstruction)instruction).putPhiArgs.remove(phInstrASI);
								}
								
								((PutPhiOSSAInstruction)instruction).ptsTo = handledPhiTemp;
								
								handledPhisMap.get(d).put(instruction, handledPhiTemp);
								
								for(Iterator<InstanceKey> AllocationElems = handledIntersect.iterator() ; AllocationElems.hasNext();){
									InstanceKey AllocationElemsNext = AllocationElems.next();
									allocationPhiMap.get(d).remove(AllocationElemsNext);
								}
								
								for(Iterator<InstanceKey> allocationElemsIter = handledPhiTemp.iterator() ; allocationElemsIter.hasNext();){
									InstanceKey allocationElemsIterNext = allocationElemsIter.next();
									allocationPhiMap.get(d).put(allocationElemsIterNext,((PutPhiOSSAInstruction)instruction) );
								}

								
								
								if(handledPhiRemaining.isEmpty()){
									this.noOfPutPhis--;	
									handledPhisMap.get(d).remove(instruction);
									objectPutPhiInstructions.get(d).remove(instruction);
								}
								
							}
							//}
						
						//	if(equateASIs(ptsToDef, handledPhi))
						//		continue;
						}
						
						PutPhiOSSAInstruction putPhiOSSAInstrctn ;
						this.noOfPutPhis++;
//						if (instructn instanceof SSAPutInstruction) 
							putPhiOSSAInstrctn = new PutPhiOSSAInstruction(ptsToDef, cfg.getPredNodeCount(d),findType(ir, defVn).getTypeReference(),false);
//						else
//							putPhiOSSAInstrctn = new PutPhiOSSAInstruction(ptsToDef, cfg.getPredNodeCount(d),findType(ir, defVn).getTypeReference(),true);
						
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
						
						worklist2.add(d);
						//DONE insert putphis iteratively into next domfrontiers also.
					}
    			elems = worklist2.iterator();
    		}
			
    	}
	}
	
	/*
	 * simple naming for asilist
	 * not yet used anywhere though. Full name is used everywhere, bad display.
	 */
	public ArrayList<InstanceKey> ASIlist = new ArrayList<InstanceKey>();
	
	/**
	 * HashTable for storing translated OSSAinstructions corresponding to WALA IR SSA instructions.
	 * When there is no scalar instruction then new OSSAinstruction is stored against the same OSSAinstruction.
	 * THe new SSAinstruction is created for example when we have phis because of definitions created by put instructions and for d-phi and u-phi instructions
	 * Also control-phis for objects are @deprecated.
	 */
	public  HashMap<SSAInstruction, SSAInstruction> instrMap; 

	/**
	 * Stacks for different allocation-sites, each stack ASI has only oneelement in its pointsTo set
	 * but OrdinalSet just for ease of implementation from walaIR. 
	 */
	public HashMap<InstanceKey, Stack<OSSAObject>> objStack = new HashMap<InstanceKey, Stack<OSSAObject>>();
	public void rename() {
		renameArgs();
		renameBody(cfg.entry());
	}
	public FuncOSSAInstruction funcossainstrctn;
	/**
	 * Creates Objects for arguments to the function
	 */
	public void renameArgs() {
		int noofparams = ir.getNumberOfParameters();
		funcossainstrctn= new FuncOSSAInstruction(noofparams,ir.getMethod().getName().toString());//getSignature());
		for(int j = 0;j<noofparams;j++){
			int paramvalueno = ir.getParameter(j);
			funcossainstrctn.vns[j]=paramvalueno;
			TypeReference paramtype = ir.getParameterType(j);
			funcossainstrctn.paramtypes.put(paramvalueno, paramtype);
			if(!paramtype.isPrimitiveType()){
				PointerKey ptrKey = hm.getPointerKeyForLocal(node,paramvalueno);
				OrdinalSet<InstanceKey> ptsTo = pa.getPointsToSet(ptrKey);
				OSSAObject nwobj = createObj(paramtype, ptsTo);	
				funcossainstrctn.args[j] = nwobj;
//				funcossainstrctn.argObjs.put(paramvalueno, nwobj);
				DefUse.storeDef(nwobj, funcossainstrctn);
				
			}
		}
	}
	
	/*
	 * Map to store static objects in analysis class.
	 */
	public HashMap<InstanceKey, OSSAObject>staticObjs = new HashMap<InstanceKey, OSSAObject>();
	
	/**
	 * Implements second part of algorithm. Called after put-phi insertions
	 * All heap objects are identified and then renamed.
	 * Also translates simple SSA IR into ObjectSSA IR.
	 */
	public void renameBody(BasicBlock bb){
		
		SSAInstruction [] instructns = ir.getInstructions();
		
		//TODO Check- Store the current state of objectStack
		HashMap<InstanceKey, OSSAObject>stackview =  new HashMap<InstanceKey, OSSAObject>();
		for(Iterator<InstanceKey>stackASIs= objStack.keySet().iterator();stackASIs.hasNext();) {
			InstanceKey asi = stackASIs.next();
			if(!objStack.get(asi).isEmpty())
			stackview.put(asi, objStack.get(asi).peek());
		}
		
		
		int start = bb.getFirstInstructionIndex();
		int end = bb.getLastInstructionIndex();
		
		/**
		 * Control-phis for scalars
		 */
		for (Iterator<SSAPhiInstruction> it = bb.iteratePhis(); it.hasNext();) {
			SSAPhiInstruction phiInstructn= (SSAPhiInstruction) it.next();
	    	int valueNumber = phiInstructn.getDef();
	    	if(findType(ir, valueNumber).getTypeReference().isPrimitiveType()) {
	    		if(!ctrlPhiInstructions.containsKey(bb))
					ctrlPhiInstructions.put(bb, new LinkedList<SSAPhiInstruction>());
				ctrlPhiInstructions.get(bb).add(phiInstructn);
	    	}
	    	 
		}
		
		/**
		 * DONE Iterate through putphis before other instructions in basicblock. 
		 */
		if(objectPutPhiInstructions.containsKey(bb)){
			for(Iterator<PutPhiOSSAInstruction>putphiinstrctns = objectPutPhiInstructions.get(bb).iterator();putphiinstrctns.hasNext();) {
				PutPhiOSSAInstruction putphiinstrctn = putphiinstrctns.next();
				for(Iterator<InstanceKey>ASIs = putphiinstrctn.putPhiArgs.keySet().iterator();ASIs.hasNext();) {
					InstanceKey asi = ASIs.next();
					if(putphiinstrctn.putPhiArgs.get(asi).isEmpty()) {
						if(objStack.get(asi).isEmpty())
						{
							System.out.println("error");
						}
						putphiinstrctn.putPhiArgs.get(asi).put(bb,objStack.get(asi).peek());
						DefUse.storeUse(objStack.get(asi).peek(), putphiinstrctn);
					}
				}
				OSSAObject newPutPhiObj = createObj(putphiinstrctn.concreteType, putphiinstrctn.ptsTo);
				putphiinstrctn.defObj =  newPutPhiObj;
				DefUse.storeDef(newPutPhiObj, putphiinstrctn);
				
			}
		}
			
		/*
		 * for(Iterator<ObjectPhiInstruction> objphiinstrctns = iterateObjPhis(bb);objphiinstrctns.hasNext();){
	    	  ObjectPhiInstruction objphiinstrctn = objphiinstrctns.next();
	    	  objphiinstrctn.defVersionNo = objphiinstrctn.defObj.VersionNo++;
	    	  objectStack.get(objphiinstrctn.defObj).push(objphiinstrctn.defVersionNo);
	    	  storeDef(objphiinstrctn.defObj, objphiinstrctn);
	    }
		*/
		
		
		
		for (int j = start; j <= end; j++) {
	        
	    	  if (instructns[j] != null) {
	                	
	        	SSAInstruction instructn = instructns[j];
//	        	SSAInstruction ossainstrctn = instrMap.get(instructn);
//	        	if(ossainstrctn==null||instructn.toString().contentEquals(ossainstrctn.toString()))
//	        		continue;
	        	if(instructn==null)
	        		continue;
	        	//TODO If possible handle all the uses of object in any type of instruction here only.
	        	int noofuses = instructn.getNumberOfUses();
	        	
	        	
	        	//TODO handle different types of instructions
	        	if (instructn instanceof SSANewInstruction) {
					SSANewInstruction new_instrctn = (SSANewInstruction) instructn;
					int new_valueNumber = new_instrctn.getDef();
					int temp = new_instrctn.getNumberOfUses();
					int temp2 = new_instrctn.getNumberOfDefs();
					PointerKey ptrKey = hm.getPointerKeyForLocal(node,
							new_valueNumber);
					OrdinalSet<InstanceKey> ptsTo = pa.getPointsToSet(ptrKey);
					assert (ptsTo!=null && ptsTo.size()==1):"New statement without allocation site identifier";
					OSSAObject nwobj = createObj(new_instrctn.getConcreteType(), ptsTo);
					/*//Create Stack
					for(Iterator<InstanceKey>ASIs = ptsTo.iterator();ASIs.hasNext();) {
						InstanceKey asi = ASIs.next();
						objStack.put( asi, new Stack<OSSAObject>());
						//Create Object and insert into Stack
						nwobj= new OSSAObject(objectVersionNoNext++, new_instrctn
										.getConcreteType(), ptsTo);
						objStack.get(asi).push(nwobj);
						//Create CreateOSSAInstruction and store it in instrMap.
						
					}*/
					CreateOSSAInstruction newCreateOSSAInstrctn = new CreateOSSAInstruction(new_instrctn, nwobj);
					instrMap.put(new_instrctn, newCreateOSSAInstrctn);
					DefUse.storeDef(nwobj, newCreateOSSAInstrctn);
					
					continue;
				}
	        	//DONE Complete PUT and Get Instructions, what if used object has same objects on all stacks. 
	        	//and check for correct object version  numbering
	        	if (instructn instanceof SSAPutInstruction) {
					SSAPutInstruction putFieldInstrctn = (SSAPutInstruction) instructn;
					PutFieldOSSAInstruction  putOSSAinstrctn;
					if(putFieldInstrctn.isStatic()) {
						int defvn = putFieldInstrctn.getDef();
						staticAcces =true;
						OSSAObject staticobj=nullobj;
						if(defvn!=-1) {
							PointerKey defPtrKey = hm.getPointerKeyForLocal(node,
									defvn);
							OrdinalSet<InstanceKey> defPtsTo = pa.getPointsToSet(defPtrKey);
							for(Iterator<InstanceKey>ASIs = defPtsTo.iterator();ASIs.hasNext();) {
								InstanceKey asi = ASIs.next();
								if(!staticObjs.containsKey(asi)) {
									staticobj = new OSSAObject(objectVersionNoNext++, asi.getConcreteType().getReference(), asi);
									staticObjs.put(asi, staticobj);
								}
								/*if(!objStack.containsKey(asi)) {
									objStack.put(asi, new Stack<OSSAObject>());
									staticobj = new OSSAObject(objectVersionNoNext++, asi.getConcreteType().getReference(), asi);
									objStack.get(asi).push(staticobj);
									//ASIList not yet used
									ASIlist.add(asi);
								}*/
								staticobj = staticObjs.get(asi);
							}
							assert(staticobj!=null):"StaticObject is null; some error";
						}
						FieldReference field = putFieldInstrctn.getDeclaredField();
						
						if(field.getFieldType().isReferenceType()) {
							int fieldValueNumber = putFieldInstrctn.getUse(1);
							PointerKey fieldPtrKey = hm.getPointerKeyForLocal(node,
									fieldValueNumber);
							OrdinalSet<InstanceKey> fieldPtsTo = pa.getPointsToSet(fieldPtrKey);
							boolean temphiddenarg=hiddenArgAccess;
							putOSSAinstrctn = new PutFieldOSSAInstruction(putFieldInstrctn, staticobj, getUseObjs(fieldPtsTo));
							if(!temphiddenarg&&hiddenArgAccess)
								hiddenArgAccess=false;
						}
						else {
							putOSSAinstrctn = new PutFieldOSSAInstruction(putFieldInstrctn, staticobj, null);
						}
//						putOSSAinstrctn = new PutFieldOSSAInstruction(putFieldInstrctn, staticobj);
						DefUse.storeDef(staticobj, putOSSAinstrctn);
//						DefUse.storeUse(contaningObj, putOSSAinstrctn);
						if(putOSSAinstrctn.fieldRefObjs!=null)
						for(Iterator<OSSAObject>useobjs = putOSSAinstrctn.fieldRefObjs.values().iterator();useobjs.hasNext();) {
							OSSAObject useobj = useobjs.next();
							DefUse.storeUse(useobj, putOSSAinstrctn);
						}
						instrMap.put(putFieldInstrctn, putOSSAinstrctn);
						continue;
						
					}
					int def_ValueNumber = putFieldInstrctn.getUse(0);
					//Check if object has only one or more allocation sites associated with it.
					//put dphis in those cases
					PointerKey ptrKey = hm.getPointerKeyForLocal(node,
							def_ValueNumber);
					OrdinalSet<InstanceKey> ptsTo = pa.getPointsToSet(ptrKey);
					InstanceKey prevASI=null;
					InstanceKey newASI=null;
					OSSAObject contaningObj = null;
					Boolean insertDphi = false;
					//Check if all ASIs are same, then no need to insert dphi.
					for(Iterator<InstanceKey> ASIs = ptsTo.iterator();ASIs.hasNext();) {
						prevASI = newASI;
						newASI = ASIs.next();
						contaningObj =objStack.get(newASI).peek();
						if(prevASI==null)
							continue;		
						 
						if(contaningObj.equals(objStack.get(prevASI).peek())) {
							
							continue;
						}
						else {
							insertDphi=true;
							break;
						}							
					}
					DPhiOSSAInstruction dphi=null;
					if(insertDphi) {
						//TODO insert Dphi
						dphi = createDPhi(findType(ir, def_ValueNumber).getTypeReference(),ptsTo);
						contaningObj = dphi.defObj;
						DefUse.storeDef(contaningObj, dphi);
					}
					
					OSSAObject defObj;
					if(ptsTo.isEmpty())
						defObj=nullobj;
					else
					//Make LHS and RHS Objects.
					defObj = createObj(contaningObj.classType, ptsTo);
					
					
					
					FieldReference field = putFieldInstrctn.getDeclaredField();
					
					if(field.getFieldType().isReferenceType()) {
						int fieldValueNumber = putFieldInstrctn.getUse(1);
						PointerKey fieldPtrKey = hm.getPointerKeyForLocal(node,
								fieldValueNumber);
						OrdinalSet<InstanceKey> fieldPtsTo = pa.getPointsToSet(fieldPtrKey);
						boolean temphiddenarg=hiddenArgAccess;
						putOSSAinstrctn = new PutFieldOSSAInstruction(putFieldInstrctn, defObj, contaningObj, getUseObjs(fieldPtsTo), dphi);
						if(!temphiddenarg&&hiddenArgAccess)
							hiddenArgAccess=false;
					}
					else {
						putOSSAinstrctn = new PutFieldOSSAInstruction(putFieldInstrctn, defObj, contaningObj, dphi);
					}
					DefUse.storeDef(defObj, putOSSAinstrctn);
					DefUse.storeUse(contaningObj, putOSSAinstrctn);
					if(putOSSAinstrctn.fieldRefObjs!=null)
					for(Iterator<OSSAObject>useobjs = putOSSAinstrctn.fieldRefObjs.values().iterator();useobjs.hasNext();) {
						OSSAObject useobj = useobjs.next();
						DefUse.storeUse(useobj, putOSSAinstrctn);
					}
					instrMap.put(putFieldInstrctn, putOSSAinstrctn);
					
					continue;
				}
	        	
	        	if (instructn instanceof SSAGetInstruction) {
					SSAGetInstruction getFieldInstrctn = (SSAGetInstruction) instructn;
					GetFieldOSSAInstruction  getOSSAinstrctn;
					if(getFieldInstrctn.isStatic()) {
						int defvn = getFieldInstrctn.getDef();
						staticAcces =true;
						OSSAObject staticobj=nullobj;
//						if(findType(ir, defvn).getTypeReference().is)
						PointerKey defPtrKey = hm.getPointerKeyForLocal(node,
								defvn);
						OrdinalSet<InstanceKey> defPtsTo = pa.getPointsToSet(defPtrKey);
						for(Iterator<InstanceKey>ASIs = defPtsTo.iterator();ASIs.hasNext();) {
							InstanceKey asi = ASIs.next();
							if(!staticObjs.containsKey(asi)) {
								staticobj = new OSSAObject(objectVersionNoNext++, asi.getConcreteType().getReference(), asi);
								staticObjs.put(asi, staticobj);
							}
							/*if(!objStack.containsKey(asi)) {
								objStack.put(asi, new Stack<OSSAObject>());
								staticobj = new OSSAObject(objectVersionNoNext++, asi.getConcreteType().getReference(), asi);
								objStack.get(asi).push(staticobj);
								//ASIList not yet used
								ASIlist.add(asi);
							}*/
							staticobj = staticObjs.get(asi);
						}
						assert(staticobj!=null):"StaticObject is null; some error";
						getOSSAinstrctn = new GetFieldOSSAInstruction(getFieldInstrctn, staticobj);
						instrMap.put(getFieldInstrctn, getOSSAinstrctn);
						continue;
						
					}
						
					int containerValueNumber = getFieldInstrctn.getUse(1);
					PointerKey containerPtrKey = hm.getPointerKeyForLocal(node,
							containerValueNumber);
					OrdinalSet<InstanceKey> containerPtsTo = pa.getPointsToSet(containerPtrKey);
					////loook hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
					
					FieldReference field = getFieldInstrctn.getDeclaredField();
					
					if(field.getFieldType().isReferenceType()) {
						int fieldValueNumber = getFieldInstrctn.getDef();
						PointerKey fieldPtrKey = hm.getPointerKeyForLocal(node,
								fieldValueNumber);
						OrdinalSet<InstanceKey> fieldPtsTo = pa.getPointsToSet(fieldPtrKey);
						getOSSAinstrctn = new GetFieldOSSAInstruction(getFieldInstrctn, getUseObjs(containerPtsTo), getUseObjs(fieldPtsTo));
					}
					else {
						getOSSAinstrctn = new GetFieldOSSAInstruction(getFieldInstrctn, getUseObjs(containerPtsTo));
					}
					for(Iterator<OSSAObject>useobjs = getOSSAinstrctn.containerObjs.values().iterator();useobjs.hasNext();) {
						OSSAObject useobj = useobjs.next();
						DefUse.storeUse(useobj, getOSSAinstrctn);
					}
					if(getOSSAinstrctn.fieldRefObjs!=null)
						for(Iterator<OSSAObject>useobjs = getOSSAinstrctn.fieldRefObjs.values().iterator();useobjs.hasNext();) {
							OSSAObject useobj = useobjs.next();
							DefUse.storeUse(useobj, getOSSAinstrctn);
						}
					instrMap.put(getFieldInstrctn, getOSSAinstrctn);
					continue;
				}
	        	
	        	if (instructn instanceof SSAReturnInstruction){
	        		SSAReturnInstruction retInstrctn = (SSAReturnInstruction)instructn;
	        		int valueNumber = retInstrctn.getResult();
	        		
	        		if(valueNumber == -1||findType(ir, valueNumber)==TypeAbstraction.TOP){
	        			instrMap.put(retInstrctn, retInstrctn);
	        			noOfReturns++;
	        			noOfRetPhis++;
	        			continue;
	        		}
	        		// objnumbers++;
	        		
	        		if(findType(ir, valueNumber).getTypeReference().isReferenceType()) {
		        		PointerKey ptrKey = hm.getPointerKeyForLocal(node,
		        				valueNumber);
		        		OrdinalSet<InstanceKey> ptsTo = pa.getPointsToSet(ptrKey);
		        		boolean temphiddenarg=hiddenArgAccess;
		        		ReturnOSSAInstruction retOSSAInstrctn = new ReturnOSSAInstruction(retInstrctn, getUseObjs(ptsTo));
		        		if(!temphiddenarg&&hiddenArgAccess)
							hiddenArgAccess=false;
		        		instrMap.put(retInstrctn, retOSSAInstrctn);
		        		for(Iterator<OSSAObject>useobjs = retOSSAInstrctn.retObj.values().iterator();useobjs.hasNext();) {
							OSSAObject useobj = useobjs.next();
							DefUse.storeUse(useobj, retOSSAInstrctn);
						}
	        		}
	        		continue;
	        	}
	        	
	        	//TODO check the invoke ossa instruction logic.MUST****
	        	if(instructn instanceof SSAInvokeInstruction ){
					
					SSAInvokeInstruction ivinstrctn = (SSAInvokeInstruction) instructn;
					String mthdSignTarget = ivinstrctn.getDeclaredTarget().getSignature();
					if(!methodSignSetStatic.contains(mthdSignTarget) || ivinstrctn.getDeclaredTarget().getName().toString().contains("println"))
						continue;
					//args of function call
					
					int noofparameters = ivinstrctn.getNumberOfUses();
					ArrayList<HashMap<InstanceKey, OSSAObject>> args = new ArrayList<HashMap<InstanceKey,OSSAObject>>();
					ArrayList<OSSAObject> changedArgs = new ArrayList<OSSAObject>();
					ArrayList<ArgPhiOSSAInstruction> argPhiInstrctns = new ArrayList<ArgPhiOSSAInstruction>();
//					InvokeOSSAInstruction invokeOSSAInstrctn = new InvokeOSSAInstruction(ivinstrctn, defObj, args, changedArgs);
					//ArraySet<String> tempWorkList = new ArraySet<String>();
					//tempWorkList.addAll(SSAInvokeToMethodList.get(ivinstrctn));
					
					ArrayList<InstanceKey> argsList = new ArrayList<InstanceKey>();
					argsList.addAll(HiddenargToAsi.get(mthdSignTarget));
					/*
					for(Iterator<String> methdsignIter = tempWorkList.iterator();methdsignIter.hasNext();)
					{
						String methdsignElems = methdsignIter.next();
						argsList.addAll(HiddenargToAsi.get(methdsignElems));
					}
					*/
					ArrayList<InstanceKey> argsListTemp = new ArrayList<InstanceKey>();
					argsListTemp.addAll(argsList);
	        	
					for(Iterator<InstanceKey> asiIter = argsListTemp.iterator(); asiIter.hasNext();)
					{
						InstanceKey asiElems = asiIter.next();
						
						if(!objStack.containsKey(asiElems) || objStack.get(asiElems).isEmpty() || objStack.get(asiElems) == null) //// check here whether objstack.get(asiElems) can be empty or not
						{
							argsList.remove(asiElems);
						}
						else
						{
							//this.noOfArgPhis++;
						}
						
					}
					
					//ArraySet<InstanceKey> ArgPhiAsi = new ArraySet<InstanceKey>();
					ArraySet<ArraySet<InstanceKey>> ArgsAsiSet = new ArraySet<ArraySet<InstanceKey>>();
					for(Iterator<InstanceKey> asiIterator = argsList.iterator(); asiIterator.hasNext();)
					{
						InstanceKey asiElement = asiIterator.next();
						ArraySet<InstanceKey> tempArray = new ArraySet<InstanceKey>();
						tempArray.add(asiElement);
						ArgsAsiSet.add(tempArray);
					}
					for(Iterator<InstanceKey> asiIter1 = argsList.iterator(); asiIter1.hasNext();)
					{
						InstanceKey asiElems1 = asiIter1.next();
						for(Iterator<InstanceKey> asiIter2 = argsList.iterator(); asiIter2.hasNext();)
						{
							InstanceKey asiElems2 = asiIter2.next();
							if(objStack.get(asiElems1).peek() == objStack.get(asiElems2).peek())
							{
								ArraySet<InstanceKey> tempArrayAsi1 = new ArraySet<InstanceKey>();
								ArraySet<InstanceKey> tempArrayAsi2 = new ArraySet<InstanceKey>();
								for(Iterator<ArraySet<InstanceKey>> arrayAsiIter = ArgsAsiSet.iterator(); arrayAsiIter.hasNext();)
								{
									ArraySet<InstanceKey> arrayAsiELems = arrayAsiIter.next();
									if(arrayAsiELems.contains(asiElems1))
									{
										tempArrayAsi1 = arrayAsiELems;
									}
									if(arrayAsiELems.contains(asiElems2))
									{
										tempArrayAsi2 = arrayAsiELems;
									}
								}
								if(tempArrayAsi1 != tempArrayAsi2)
								{
									ArgsAsiSet.remove(tempArrayAsi2);
									ArgsAsiSet.remove(tempArrayAsi1);
									tempArrayAsi1.addAll(tempArrayAsi2);
									ArgsAsiSet.add(tempArrayAsi1);
								}
							}
						}
					}
						
					for(Iterator<ArraySet<InstanceKey>> argsAsiSetIterator = ArgsAsiSet.iterator(); argsAsiSetIterator.hasNext();)
					{
						ArraySet<InstanceKey> argsAsiSetElem = argsAsiSetIterator.next();
						OSSAObject tempObject = objStack.get(argsAsiSetElem.get(0)).peek();
						TypeReference tr = tempObject.classType;
						
						//Collection<InstanceKey> ptsToCollection = OrdinalSet.toCollection(tempObject.allocationSites);
						//ptsToCollection.removeAll(ptsToCollection);
						//OrdinalSet<InstanceKey> tempOrdinalSet = OrdinalSet.toOrdinalSet(ptsToCollection, tempObject.allocationSites.getMapping());
						//OrdinalSet<InstanceKey> tempOrdinalSet = OrdinalSet.empty();
						//MutableMapping<InstanceKey> mut_mapping = (MutableMapping<InstanceKey>) tempObject.allocationSites.getMapping();
						//OrdinalSetMapping<InstanceKey> newMapping = tempOrdinalSet.getMapping();
						//OrdinalSetMapping<InstanceKey> newMapping;
						/*
						OrdinalSet<InstanceKey> ptsTo = null;
						boolean initial = true;
						for(Iterator<InstanceKey> inkeyIter = argsAsiSetElem.iterator();inkeyIter.hasNext();)
						{
							InstanceKey inkeyElem = inkeyIter.next();
							for(int i = objStack.get(inkeyElem).size()-1 ; i>=0; i--)
							{
								OSSAObject currentObj = objStack.get(inkeyElem).elementAt(i);
								if(currentObj.ptsTo != null)
								{
									if(initial)
									{
										mut_mapping = (MutableMapping<InstanceKey>) currentObj.allocationSites.getMapping();
										int tempIndex = mut_mapping.getMappedIndex(inkeyElem);
										ptsTo = mut_mapping.makeSingleton(tempIndex);
										initial = false;
									}
									else
									{
										mut_mapping = (MutableMapping<InstanceKey>) currentObj.allocationSites.getMapping();
										int tempIndex = mut_mapping.getMappedIndex(inkeyElem);
										OrdinalSet<InstanceKey> tempINkey = mut_mapping.makeSingleton(tempIndex);
										ptsTo = OrdinalSet.unify(ptsTo, tempINkey);
									}
									break;
								}
							}
							//ptsToCollection.add(inkeyElem);
							//newMapping.add(inkeyElem);
						}
						
						//OrdinalSet<InstanceKey> ptsTo = OrdinalSet.toOrdinalSet(ptsToCollection, newMapping);
						*/
						OrdinalSet<InstanceKey> ptsTo = tempObject.allocationSites;
						if(tempObject.outSideObj != null && tempObject.outSideObj == true)
						{
							System.out.print(" ");
							noOfArgPhis++;
							continue;
						}
						if(ptsTo == null)
						{
							System.out.print(" ");
							noOfArgPhis++;
							continue;
						}
						HashMap<InstanceKey, OSSAObject> argsObjs = getUseObjs(ptsTo);
						args.add(argsObjs);
						ArgPhiOSSAInstruction nwArgPhi = createArgPhi(tr, ptsTo, argsObjs);
						changedArgs.add(nwArgPhi.defObj);
						argPhiInstrctns.add(nwArgPhi);
						this.noOfArgPhis++;
					}
					/*
					if(instructn instanceof SSAInvokeInstruction ){
					
					SSAInvokeInstruction ivinstrctn = (SSAInvokeInstruction) instructn;
					if(ivinstrctn.getDeclaredTarget().getName().toString().contains("println"))
						continue;
					//args of function call
					int noofparameters = ivinstrctn.getNumberOfUses();
					ArrayList<HashMap<InstanceKey, OSSAObject>> args = new ArrayList<HashMap<InstanceKey,OSSAObject>>();
					ArrayList<OSSAObject> changedArgs = new ArrayList<OSSAObject>();
					ArrayList<ArgPhiOSSAInstruction> argPhiInstrctns = new ArrayList<ArgPhiOSSAInstruction>();
//					InvokeOSSAInstruction invokeOSSAInstrctn = new InvokeOSSAInstruction(ivinstrctn, defObj, args, changedArgs);
					for(int k=0;k<noofparameters;k++){
						int param = ivinstrctn.getUse(k);
						TypeReference tr = findType(ir, param).getTypeReference();
						if(tr!=null&&!tr.isPrimitiveType()){
							PointerKey ptrKey = hm.getPointerKeyForLocal(node, param);
							OrdinalSet<InstanceKey> ptsTo = pa.getPointsToSet(ptrKey);
							boolean temphiddenarg=hiddenArgAccess;
							args.add(getUseObjs(ptsTo));
							if(!temphiddenarg&&hiddenArgAccess)
								hiddenArgAccess=false;
//							OSSAObject retObj = createObj(tr, ptsTo);
							//TODO Check Insert argphis()
							ArgPhiOSSAInstruction nwArgPhi = createArgPhi(tr, ptsTo, args.get(k));
							argPhiInstrctns.add(nwArgPhi);
							changedArgs.add(nwArgPhi.defObj);
//							invokeOSSAInstrctn.argsarray[k] = 
						}
						else {
							args.add(null);
							changedArgs.add(null);
							argPhiInstrctns.add(null);
						}
					}

					
					/*
					for(int k=0;k<noofparameters;k++){
						int param = ivinstrctn.getUse(k);
						TypeReference tr = findType(ir, param).getTypeReference();
						if(tr!=null&&!tr.isPrimitiveType()){
							PointerKey ptrKey = hm.getPointerKeyForLocal(node, param);
							OrdinalSet<InstanceKey> ptsTo = pa.getPointsToSet(ptrKey);
							boolean temphiddenarg=hiddenArgAccess;
							args.add(getUseObjs(ptsTo));
							if(!temphiddenarg&&hiddenArgAccess)
								hiddenArgAccess=false;
//							OSSAObject retObj = createObj(tr, ptsTo);
							//TODO Check Insert argphis()
							ArgPhiOSSAInstruction nwArgPhi = createArgPhi(tr, ptsTo, args.get(k));
							argPhiInstrctns.add(nwArgPhi);
							changedArgs.add(nwArgPhi.defObj);
//							invokeOSSAInstrctn.argsarray[k] = 
						}
						else {
							args.add(null);
							changedArgs.add(null);
							argPhiInstrctns.add(null);
						}
					}
					*/
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
										defObj = new OSSAObject(objectVersionNoNext++, tr, ptsTo);
										objStack.put( asi, new Stack<OSSAObject>());
	//											asi.getConcreteType().
										OutsideObjOSSAInstruction outsideObjInstrctn = new OutsideObjOSSAInstruction(defObj);
										DefUse.storeDef(defObj, outsideObjInstrctn);
										outsideObjInstructions.add(outsideObjInstrctn);
										objStack.get(asi).push(defObj);	
									}
									else
										defObj = createObj(tr, ptsTo);
								}
							}
							boolean insertRetPhi=false;
							InstanceKey prevASI , newASI = null;
							OSSAObject contaningObj;
							for(Iterator<InstanceKey> ASIs = ptsTo.iterator();ASIs.hasNext();) {
								InstanceKey asi = ASIs.next();
								if(!objStack.containsKey(asi)||objStack.get(asi).isEmpty()) {
									/*never executed??
									 * if(ivinstrctn.isStatic()) {
										OSSAObject newstaticObj = new OSSAObject(objectVersionNoNext++, tr, ptsTo);
										//DefUse.storeDef(newObj, outsideObjInstrctn);
										objStack.get(asi).push(newstaticObj);
										stackview.put(asi, objStack.get(asi).peek());
									}*/
									
									OSSAObject newObj = new OSSAObject(objectVersionNoNext++, tr, ptsTo);
									if(!objStack.containsKey(asi))
										objStack.put( asi, new Stack<OSSAObject>());
//											asi.getConcreteType().
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
//									defObj = contaningObj;
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
								noOfRetPhis++;
								noOfReturns++;
							}
							else {
								defObj =  createObj(tr, ptsTo);
							}
														
						}
					}//if(ivinstrctn.hasDef()) {
					
					InvokeOSSAInstruction invokeOSSAInstrctn = new InvokeOSSAInstruction(ivinstrctn, defObj, args, changedArgs, argPhiInstrctns, outsideObjInstructions, retPhi);
					DefUse.storeDef(defObj, invokeOSSAInstrctn);
					ArrayList<HashMap<InstanceKey, OSSAObject>> args_original = new ArrayList<HashMap<InstanceKey,OSSAObject>>();
					int count_params = 0;
					for(int i=0;i<noofparameters;i++) {
						int param = ivinstrctn.getUse(i);
						TypeReference tr = findType(ir, param).getTypeReference();
						if(tr!=null&&!tr.isPrimitiveType()){
							PointerKey ptrKey = hm.getPointerKeyForLocal(node, param);
							OrdinalSet<InstanceKey> ptsTo = pa.getPointsToSet(ptrKey);
							args_original.add(getUseObjs(ptsTo));
							count_params ++;
						}
					}
					for(int i=0;i<count_params;i++) {
						HashMap<InstanceKey, OSSAObject>arg = args_original.get(i);
						if(arg!=null) {
							for(Iterator<OSSAObject>objs = arg.values().iterator();objs.hasNext();) {
								DefUse.storeUse(objs.next(), invokeOSSAInstrctn);
							}
						}
						
					}
					
					
					
					instrMap.put(ivinstrctn,invokeOSSAInstrctn);
					
					
	        	}//if(instructn instanceof SSAInvokeInstruction ){
	        		        	
	        	
	    	 }//if (instructns[j] != null)
		}//for (int j = start; j <= end; j++) 
		
		//Populate Putphis only pre-fixed asi components
		for(Iterator<ISSABasicBlock> succiterator = cfg.getSuccNodes(bb);succiterator.hasNext();) {
			BasicBlock succ = (BasicBlock) succiterator.next();
			HashMap<InstanceKey, SSAInstruction> allocatedPhis = allocationPhiMap.get(succ);
			if(allocatedPhis==null||allocatedPhis.isEmpty())
				continue;
			for(Iterator<InstanceKey>ASIs = allocatedPhis.keySet().iterator();ASIs.hasNext();) {
				InstanceKey asi = ASIs.next();
				PutPhiOSSAInstruction pphiinstr = ((PutPhiOSSAInstruction)allocatedPhis.get(asi));
				OSSAObject argobj=nullobj;
				if(objStack.containsKey(asi)&&!objStack.get(asi).isEmpty())
					argobj = objStack.get(asi).peek();
				pphiinstr.putPhiArgs.get(asi).put(bb, argobj);
				DefUse.storeUse(argobj, pphiinstr);
			}
			
		}
		
		//Making the recursive call for renaming objects to all children of current basic-block.
		for(Iterator<BasicBlock> domchildren = RDF.dominatorTree().getSuccNodes(bb);domchildren.hasNext();){
			BasicBlock child = domchildren.next();
			renameBody(child);
		}
		
		//DONE  Check Restore stack to older state before getting into this basic block
		for(Iterator<InstanceKey>stackASIs = objStack.keySet().iterator();stackASIs.hasNext();) {
			InstanceKey asi = stackASIs.next();
			OSSAObject oldTop = null;
			if(stackview.containsKey(asi))
				oldTop = stackview.get(asi);
			Stack<OSSAObject>singleObjStack = objStack.get(asi);
			if(oldTop==null) {
				singleObjStack.clear();
			}
			else {
				while(!oldTop.equals(singleObjStack.peek())) {
					singleObjStack.pop();
				}
			}
		}
	}
	
	/**
	 * 
	 * @param concreteType Type of object being created.
	 * @param ptsTo ASIs pointed by this object
	 * @return new OSSAObject
	 * Also updates stacks corresponding to ptsTo set
	 */
	public  OSSAObject createObj(TypeReference concreteType, OrdinalSet<InstanceKey> ptsTo) {
		OSSAObject newObj = new OSSAObject(objectVersionNoNext++, concreteType, ptsTo);
		for(Iterator<InstanceKey>ASIs = ptsTo.iterator();ASIs.hasNext();) {
			InstanceKey asi = ASIs.next();
			if(!objStack.containsKey(asi))
				objStack.put( asi, new Stack<OSSAObject>());
//				asi.getConcreteType().
			
			objStack.get(asi).push(newObj);			
		}
		return newObj;
	}
	/**
	 * creates dphi instructions for putfield instructions.
	 * @param concreteType the type of putfield instruction.
	 * @param ptsTo ASIs which are merged through this dphi.
	 * @return dphi instruction
	 */
	private DPhiOSSAInstruction createDPhi(TypeReference concreteType, OrdinalSet<InstanceKey> ptsTo) {
		HashMap<InstanceKey, OSSAObject> argObjs= new HashMap<InstanceKey, OSSAObject>();
		for(Iterator<InstanceKey>ASIs = ptsTo.iterator();ASIs.hasNext();) {
			InstanceKey asi = ASIs.next();
			argObjs.put(asi, objStack.get(asi).peek());
		}
		OSSAObject defObj = createObj(concreteType, ptsTo);
		DPhiOSSAInstruction  newDPhiInstrctn = new DPhiOSSAInstruction(defObj, argObjs);
		noOfDPhis++;
		for(Iterator<OSSAObject>argobjs = argObjs.values().iterator();argobjs.hasNext();) {
			OSSAObject argobj = argobjs.next();
			DefUse.storeUse(argobj, newDPhiInstrctn);
		}
		DefUse.storeDef(defObj, newDPhiInstrctn);
		return newDPhiInstrctn;
	}
	
	ArgPhiOSSAInstruction createArgPhi(TypeReference concreteType, OrdinalSet<InstanceKey> ptsTo, HashMap<InstanceKey, OSSAObject>argsObjs) {
		OSSAObject defObj = createObj(concreteType, ptsTo);
		ArgPhiOSSAInstruction newArgPhiInstrctn = new ArgPhiOSSAInstruction(defObj, argsObjs);
		for(Iterator<OSSAObject>argobjs = argsObjs.values().iterator();argobjs.hasNext();) {
			OSSAObject argobj = argobjs.next();
			DefUse.storeUse(argobj, newArgPhiInstrctn);
		}
		DefUse.storeDef(defObj, newArgPhiInstrctn);
		return newArgPhiInstrctn;
	}
	
	RetPhiOSSAInstruction createRetPhi(TypeReference concreteType, OrdinalSet<InstanceKey> ptsTo, LinkedList<OutsideObjOSSAInstruction> outsideObjInstructions) {//, HashMap<InstanceKey, OSSAObject>argsObjs) {
		HashMap<InstanceKey, OSSAObject> argsObjs= new HashMap<InstanceKey, OSSAObject>();
	//	LinkedList<OutsideObjOSSAInstruction> outsideObjInstructions = new LinkedList<OutsideObjOSSAInstruction>();
		for(Iterator<InstanceKey>ASIs = ptsTo.iterator();ASIs.hasNext();) {
			InstanceKey asi = ASIs.next();
			if(!objStack.containsKey(asi)||objStack.get(asi).isEmpty()) {
				OSSAObject newObj = new OSSAObject(objectVersionNoNext++, concreteType, ptsTo);
				if(!objStack.containsKey(asi))
					objStack.put( asi, new Stack<OSSAObject>());
//						asi.getConcreteType().
//				OutsideObjOSSAInstruction outsideObjInstrctn = new OutsideObjOSSAInstruction(newObj);
//				DefUse.storeDef(newObj, outsideObjInstrctn);
//				outsideObjInstructions.add(outsideObjInstrctn);
				objStack.get(asi).push(newObj);	
			}
				argsObjs.put(asi, objStack.get(asi).peek());
			
		}
		OSSAObject defObj = createObj(concreteType, ptsTo);
		RetPhiOSSAInstruction newRetPhiInstrctn = new RetPhiOSSAInstruction(defObj, argsObjs,outsideObjInstructions);
		for(Iterator<OSSAObject>argobjs = argsObjs.values().iterator();argobjs.hasNext();) {
			OSSAObject argobj = argobjs.next();
			DefUse.storeUse(argobj, newRetPhiInstrctn);
		}
		DefUse.storeDef(defObj, newRetPhiInstrctn);
		return newRetPhiInstrctn;
	}

	/**
	 * Helper function to fill in RHS uses in a instructino
	 * @param ptsTo
	 * @return
	 */
	private HashMap<InstanceKey, OSSAObject> getUseObjs(OrdinalSet<InstanceKey>ptsTo){
		HashMap<InstanceKey, OSSAObject> retMap = new HashMap<InstanceKey, OSSAObject>();
		for(Iterator<InstanceKey>ASIs = ptsTo.iterator();ASIs.hasNext();) {
			InstanceKey asi = ASIs.next();
			/*
			 * If ptsto set has asi defined outside thiss function, than we need to create stack and object corresponding to that object.
			 */
			if(asi!=null&&(!objStack.containsKey(asi)||objStack.get(asi).isEmpty())) {
				OSSAObject outObj =  new OSSAObject(objectVersionNoNext++, asi.getConcreteType().getReference(), asi);
				if(!objStack.containsKey(asi)) {
					objStack.put( asi, new Stack<OSSAObject>());
					hiddenArgAccess=true;
//					asi.getConcreteType().
				}
				objStack.get(asi).push(outObj);		
				
				outObj.outSideObj = true;
			}
			if(objStack.containsKey(asi))
				retMap.put(asi, objStack.get(asi).peek());
			else
				retMap.put(asi, nullobj);
		}
		return retMap;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		SSAInstruction[] instructions = ir.getInstructions();
		result.append("THE OBJECT SSA:-\n\n");
		result.append(funcossainstrctn);
		if(staticAcces) {
			result.append("\n"+"Static object access, OSSA might create side-effects\n");
		}
		if(hiddenArgAccess) {
			result.append("\n"+"hidden arg object access, OSSA might create side-effects\n");
		}
		for(int i=0; i<=cfg.getMaxNumber();i++) {
			BasicBlock bb =cfg.getBasicBlock(i);
			int start = bb.getFirstInstructionIndex();
			int end = bb.getLastInstructionIndex();
			result.append("BB").append(bb.getNumber());
		    result.append("\n");
		    if(ctrlPhiInstructions.containsKey(bb))
			    for(Iterator<SSAPhiInstruction>ctrlphiinstrctns = ctrlPhiInstructions.get(bb).iterator();ctrlphiinstrctns.hasNext();) {
			    	SSAPhiInstruction ctrlphiinstrctn = ctrlphiinstrctns.next();
			    	result.append("           " + ctrlphiinstrctn.toString()).append("\n");
			    }
		    
		    if(objectPutPhiInstructions.containsKey(bb))
			    for(Iterator<PutPhiOSSAInstruction>putphiinstrctns = objectPutPhiInstructions.get(bb).iterator();putphiinstrctns.hasNext();) {
			    	PutPhiOSSAInstruction putphiinstrctn = putphiinstrctns.next();
			    	result.append("           " + putphiinstrctn.toString()).append("\n");
			    }
		  //Ignore this "if"- for exception blocks in source java code.
		      if (bb instanceof ExceptionHandlerBasicBlock) {
		        ExceptionHandlerBasicBlock ebb = (ExceptionHandlerBasicBlock) bb;
		        SSAGetCaughtExceptionInstruction s = ebb.getCatchInstruction();
		        if (s != null) {
		          result.append("           " + s.toString()).append("\n");
		        } else {
		          result.append("           " + " No catch instruction. Unreachable?\n");
		        }
		      }
		     
		      //print other OSSA instructions from instrMap
		      for (int j = start; j <= end; j++) {
		        
		    	  if (instructions[j] != null) {
		    		  SSAInstruction instructn = instructions[j];
		    		  StringBuffer x;
		    		  if(instrMap.containsKey(instructn)){
		    			  SSAInstruction ossainstructn = instrMap.get(instructn);
		    			  if (ossainstructn instanceof PutFieldOSSAInstruction) {
							PutFieldOSSAInstruction putossainstrctn = (PutFieldOSSAInstruction) ossainstructn;
							x= new StringBuffer(putossainstrctn.dphi+"\n");
							x.append(j + "  "+putossainstrctn.toString());
		    			  }
		    			  else
		    				  x = new StringBuffer(j + "  "+ossainstructn.toString());
		    		  }
		    		  else
		    			  x = new StringBuffer(j + "   "+ instructn.toString());
		    		  StringStuff.padWithSpaces(x, 45);
		    		  result.append(x);
		    		  result.append("\n");
		    	  }
		      }//for (int j = start; j <= end; j++)
		}//for(int i=0; i<=cfg.getMaxNumber();i++)
		result.append("\nNumber of Put Phis "+noOfPutPhis);
		result.append("\nNumber of D Phis "+noOfDPhis);
		result.append("\nNumber of ctrlphis in OSSA for scalars "+ noOfCtrPhisinOSSA);
		result.append("\nNumber of ctrlphis in SSA for scalars "+ noOfCtrPhisinSSA);
//		System.out.println(result);
		
		return result.toString();
	}
	
	/**
	 * ASI--Allocation Site Identifier, basically the pointsTo set.
	 * @param asi1
	 * @param asi2
	 * @return True if both the ordinalsets contaning allocation sites have exactly same elements.
	 */
	public Boolean equateASIs(OrdinalSet<InstanceKey> asi1,OrdinalSet<InstanceKey>asi2){
		Collection<InstanceKey>asi1Collection = OrdinalSet.toCollection(asi1);
		Collection<InstanceKey>asi2Collection = OrdinalSet.toCollection(asi2);
		if(asi1Collection.containsAll(asi2Collection))
			if(asi2Collection.containsAll(asi1Collection))
				return true;
		return false;
	}
	
	/**
	 * contains only normal instructinos and not ssa control phi, pi and ossa dphi ,put-phi instructions.
	 */
	private SSAInstruction [] ossainstructions;
	/**
	 * populates ossainstructions array. contains only normal ossa instructinos and no phis or funcossa instructions.
	 */
	public void populateOSSAInstructions() {
		SSAInstruction [] instructions = ir.getInstructions();
		ossainstructions = new SSAInstruction[instructions.length];
		for(int i=0;i<instructions.length;i++) {
			ossainstructions[i] = instrMap.get(instructions[i]);
		}
	}
	/**
	   * Returns the normal instructions. Does not include {@link SSAPhiInstruction}, {@link SSAPiInstruction}, or
	   * {@link SSAGetCaughtExceptionInstruction}s, or OSSA putphi, dphi instruyctinons which are currently managed by {@link BasicBlock}. Entries in the returned array
	   * might be null.
	   * 
	   * This may go away someday.
	   */
	public SSAInstruction[] getOSSAInstructions() {
	    return ossainstructions;
	  }
	/**
	 * TODO handle the case of phis also.
	 * @param instrctn whose basic-block is to be found out.
	 * @return 
	 */
	
	public BasicBlock getBasicBlockForOSSAInstruction(SSAInstruction instrctn) {
		int i=0;
		/**
		 * if it is funcOssainstructino then return first basicblock
		 */
		if(instrctn.toString().contentEquals(funcossainstrctn.toString()))
			cfg.entry();
//			return (BasicBlock)ir.getBasicBlockForInstruction(ir.getInstructions()[1]);
		
		if(ossainstructions==null)
		{
			System.out.println("null error: "+ ossainstructions);
		}
		for(i =0; i<ossainstructions.length;i++) {
			if(ossainstructions[i]==instrctn)
				break;
		}
		assert(i<=ossainstructions.length):"assert error in ObjectSSA";
		if(i>=ossainstructions.length)
			return cfg.entry();
//			return (BasicBlock) ir.getBasicBlockForInstruction(ir.getInstructions()[1]);
		return (BasicBlock) ir.getBasicBlockForInstruction(ir.getInstructions()[i]);
	}
	
	
	public int noofinstrctns() {
		return ossainstructions.length;
	}
	/**
	 * 
	 * @param ir
	 * @param valueNumber whose type is returned 
	 * @return type of valuenumber in ir.
	 */
	public static TypeAbstraction findType (IR ir, int valueNumber){
		boolean doPrimitives = true; // infer types for primitive vars?
	    TypeInference ti = TypeInference.make(ir, doPrimitives);
	    TypeAbstraction type = ti.getType(valueNumber);
	    return type;
	}
}//class ObjectSSA 
