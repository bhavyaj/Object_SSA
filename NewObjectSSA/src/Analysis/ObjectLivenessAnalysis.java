package Analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import OBJS.OSSAObject;
import OSSA.ObjectSSA;
import OSSAInstructions.ArgPhiOSSAInstruction;
import OSSAInstructions.DPhiOSSAInstruction;
import OSSAInstructions.GetFieldOSSAInstruction;
import OSSAInstructions.InvokeOSSAInstruction;
import OSSAInstructions.PutFieldOSSAInstruction;
import OSSAInstructions.PutPhiOSSAInstruction;
import OSSAInstructions.RetPhiOSSAInstruction;



import com.ibm.wala.analysis.pointers.BasicHeapGraph;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.shrikeBT.Instruction;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.ArraySet;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.GraphReachability;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.StringStuff;

public class ObjectLivenessAnalysis {
	
	public static ArraySet<String> methodsigns;

	/**
	 * folder in which output files are to be stored
	 */
	public static  StringBuffer outputFiles = new StringBuffer("/home/yash/Workspaces/workspace/ObjectSSA/output/current/");
//	public static  StringBuffer outputFiles = new StringBuffer("/home/yash/Workspaces/workspace/ObjectSSA/output/jolden/bh/");
	/**
	 * jar file whose functions are to be analysed.
	 */
//	public static String jarFile = "/home/yash/Workspaces/workspace/ObjectSSA/output/TestProgs/the.jar";
	public static String jarFile = "/home/yash/Workspaces/workspace/ObjectSSA/output/jolden/voronoi/the.jar";

	/*
	 * minimum number of instructions in SSA of method for it to be analyzed.3
	 */
	public static int noofinstructionbound = 0;

	/**
	 * Set classname to null for running analysis over all the class in jar
	 */
//	public static String classname = "LOSSATestCases/TestOSSA4";// null; 
	public static String classname = null;
	/**
	 * stores method signatures and ossa for all methods in all classes in {@link jarFile}
	 */
	public static HashMap<String, ObjectSSA> ossas = new HashMap<String, ObjectSSA>();
	
	/**
	 * list of methods and corresponding arguments which are to be analysed.
	 *//*
	public static HashMap<String, HashSet<OSSAObject>> methodsToBeAnalyzed = new HashMap<String, HashSet<OSSAObject>>();
*/
	/**
	 * stores liveness of args whose liveness is already checked
	 */
	public static HashMap<ObjToBeChecked, Boolean>argObjsisLive = new HashMap<ObjToBeChecked, Boolean>();
	/**
	 * set of all the instrumented checks in code where analysis is to be done.
	 */
	public static HashSet<ObjToBeChecked> objsToBeChecked = new HashSet<ObjToBeChecked>();
	/**
	 * liveness status of checked objs
	 */
	public static HashMap<ObjToBeChecked, Boolean>checkedObjLiveness = new HashMap<ObjToBeChecked, Boolean>();
	
	public static Boolean checkInterProcedural = true;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			getOSSAs();
//			findChecks();	
			putChecks();
			System.out.println(objsToBeChecked.size()+" Objs to be checked: ");//+objsToBeChecked);
			for(Iterator<ObjToBeChecked>objstobecheckediterator = objsToBeChecked.iterator();objstobecheckediterator.hasNext();) {
				ObjToBeChecked objtbc = objstobecheckediterator.next();
				if(checkedObjLiveness.containsKey(objtbc)) {
					continue;
				}
				
				//TODO CHECK
//				if(argObjsisLive.containsKey(objtbc)) {
//					objtbc.liveness=argObjsisLive.get(objtbc);
//					checkedObjLiveness.put(objtbc, objtbc.liveness);
//				}
				checkedObjLiveness.put(objtbc, checkLiveness(objtbc));
//				objstobecheckediterator = objsToBeChecked.iterator();
			}
			int live=0,notlive=0,total=checkedObjLiveness.size();
			System.out.println("LIveness of objs checked: \n"+checkedObjLiveness);
			for(Iterator<Boolean>liveness = checkedObjLiveness.values().iterator();liveness.hasNext();) {
				Boolean liive = liveness.next();
				if(liive)
					live++;
				else 
					notlive++;
			}
			System.out.println(instrumentedfunccalls.size()+" instrumentedfunccalls: ");//+instrumentedfunccalls);
			System.out.println("Total Objects checked = "+total);
			System.out.println(live+" objects live");
			System.out.println(notlive+ " objects not live");
			System.out.println("NO of put-phis: "+totalputphis);
			System.out.println("NO of d-phis: "+totaldphis);
			System.out.println("NO of ossac-phis: "+totalossacphis);
			System.out.println("NO of ssac-phis: "+totalssacphis);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to find and store all instrumented checkpoints in ossa into objsToBeAnalyzed
	 * TODO implement this method
	 */
	public static void findChecks() {
		try {
		for(Iterator<ObjectSSA>ossaiterator = ossas.values().iterator();ossaiterator.hasNext();) {
			ObjectSSA ossa = ossaiterator.next();
			for(Iterator<SSAInstruction>ossainstrctns = ossa.instrMap.values().iterator();ossainstrctns.hasNext();) {
				SSAInstruction ossainstrctn = ossainstrctns.next();
				int ObjVersionNotobechecked=-1;
				if (ossainstrctn instanceof InvokeOSSAInstruction) {
					InvokeOSSAInstruction invokeinstrctn = (InvokeOSSAInstruction) ossainstrctn;
					if(invokeinstrctn.origInstrctn.getCallSite().getDeclaredTarget().getName().toString().compareTo("check")==0) {
						String s = ossa.ir.getSymbolTable().getValue(invokeinstrctn.origInstrctn.getUse(0)).toString().substring(1);
		    			ObjVersionNotobechecked = Integer.parseInt(s.trim());
		    			assert(ObjVersionNotobechecked!=-1):"Invalid object value number to be analysed";

						OSSAObject objToBeChecked = getObjFromValueNo(ossa,ObjVersionNotobechecked);
						ObjToBeChecked otbc = new ObjToBeChecked();
						otbc.add(objToBeChecked, ossa.ir.getMethod().getSignature(), ossainstrctn);
						objsToBeChecked.add(otbc);
					}
				}
				
//				assert(ObjValueNotobechecked!=-1):"Invalid object value number to be analysed";
			}//for(Iterator<SSAInstruction>ossainstrctns = ossa.instrMap.values().iterator();ossainstrctns.hasNext();)
			
			
		}//for(Iterator<ObjectSSA>ossaiterator = ossas.values().iterator();ossaiterator.hasNext();)
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void putChecks() {
		for(Iterator<ObjectSSA>ossaiterator = ossas.values().iterator();ossaiterator.hasNext();) {
			ObjectSSA ossa = ossaiterator.next();
			for(Iterator<SSAInstruction>ossainstrctns = ossa.instrMap.values().iterator();ossainstrctns.hasNext();) {
				SSAInstruction ossainstrctn = ossainstrctns.next();
				int ObjVersionNotobechecked=-1;
				if (ossainstrctn instanceof InvokeOSSAInstruction) {
					InvokeOSSAInstruction ivossainstrctn = (InvokeOSSAInstruction)ossainstrctn;
					if(instrumentedfunccalls.contains(ivossainstrctn.origInstrctn.getDeclaredTarget().getSignature())) {
						for(int i=0; i <ivossainstrctn.args.size();i++) {
							if(ivossainstrctn.args.get(i)==null)
								continue;
							for(Iterator<OSSAObject>objs=ivossainstrctn.args.get(i).values().iterator();objs.hasNext();) {
								OSSAObject objToBeChecked = objs.next();
								ObjToBeChecked otbc = new ObjToBeChecked();
								otbc.add(objToBeChecked, ossa.ir.getMethod().getSignature(), ossainstrctn);
								objsToBeChecked.add(otbc);
							}
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * @param objToBeChecked details of object to be checked for liveness
	 * @return true if obj is live in given method, false otherwise.
	 * TODO testing and benchmark running
	 */
	public static Boolean checkLiveness(ObjToBeChecked objToBeChecked) {
		HashSet<OSSAObject> worklist = new HashSet<OSSAObject>();
		HashSet<OSSAObject> checked= new HashSet<OSSAObject>();
		ObjectSSA ossa = ossas.get(objToBeChecked.checkPointMethod);
		BasicBlock checkInstructionBB = ossa.getBasicBlockForOSSAInstruction(objToBeChecked.checkPointInstruction);
		//Graphreachability for cfg.
	    GraphReachability<ISSABasicBlock> graphrchblty = new GraphReachability<ISSABasicBlock>(ossa.cfg, new Filter(){@Override public boolean accepts(Object o) {return true;}});
//	    System.err.println("grphrchblty"+graphrchblty);
	    try {
			graphrchblty.solve(null);
		} catch (CancelException e) {
			e.printStackTrace();
		}
		worklist.add(objToBeChecked.obj);
		while(!worklist.isEmpty()) {
			OSSAObject checkobj = (OSSAObject)worklist.toArray()[0];
	    	worklist.remove(checkobj);
	    	checked.add(checkobj);
			HashSet<SSAInstruction>uses= ossa.DefUse.uses.get(checkobj);
			if(uses==null||uses.isEmpty())
				return false;
			for(Iterator<SSAInstruction>useInstructions = uses.iterator(); useInstructions.hasNext();) {
				SSAInstruction useinstruction = useInstructions.next();
				//If useinsrtuction is one of the phis 
				
				if (useinstruction instanceof PutPhiOSSAInstruction) {
					PutPhiOSSAInstruction putphiinstruct = (PutPhiOSSAInstruction) useinstruction;
					if(!checked.contains(putphiinstruct.defObj)) {
						worklist.add(putphiinstruct.defObj);
						objToBeChecked.liveUses.add(putphiinstruct);
						continue;
					}					
//					objToBeChecked.liveness = true;
//					objToBeChecked.					
				}
				if (useinstruction instanceof DPhiOSSAInstruction) {
					DPhiOSSAInstruction dphiinstruct = (DPhiOSSAInstruction) useinstruction;
					if(!checked.contains(dphiinstruct.defObj)) {
						worklist.add(dphiinstruct.defObj);
						objToBeChecked.liveUses.add(dphiinstruct);
						continue;
					}					
//					objToBeChecked.liveness = true;
//					objToBeChecked.					
				}
				if (useinstruction instanceof ArgPhiOSSAInstruction) {
					ArgPhiOSSAInstruction dphiinstruct = (ArgPhiOSSAInstruction) useinstruction;
					if(!checked.contains(dphiinstruct.defObj)) {
						worklist.add(dphiinstruct.defObj);
						objToBeChecked.liveUses.add(dphiinstruct);
						continue;
					}					
//					objToBeChecked.liveness = true;
//					objToBeChecked.					
				}
				if (useinstruction instanceof RetPhiOSSAInstruction) {
					RetPhiOSSAInstruction dphiinstruct = (RetPhiOSSAInstruction) useinstruction;
					if(!checked.contains(dphiinstruct.defObj)) {
						worklist.add(dphiinstruct.defObj);
						objToBeChecked.liveUses.add(dphiinstruct);
						continue;
					}					
//					objToBeChecked.liveness = true;
//					objToBeChecked.					
				}
				
				
				
				BasicBlock useInstructionBB = ossa.getBasicBlockForOSSAInstruction(useinstruction);
				if(!graphrchblty.getReachableSet(checkInstructionBB).contains(useInstructionBB))
					continue;
				if (useinstruction instanceof PutFieldOSSAInstruction) {
					PutFieldOSSAInstruction putfieldinstruct = (PutFieldOSSAInstruction) useinstruction;
					objToBeChecked.liveUses.add(putfieldinstruct);
					objToBeChecked.liveness = true;
//					objToBeChecked.					
					continue;
				}
				if (useinstruction instanceof GetFieldOSSAInstruction) {
					GetFieldOSSAInstruction putfieldinstruct = (GetFieldOSSAInstruction) useinstruction;
					objToBeChecked.liveUses.add(putfieldinstruct);
					objToBeChecked.liveness = true;
//					objToBeChecked.					
					continue;
				}
				
				if (useinstruction instanceof InvokeOSSAInstruction) {
					InvokeOSSAInstruction invokeInstruction = (InvokeOSSAInstruction) useinstruction;
					String invokedMethod = invokeInstruction.origInstrctn.getDeclaredTarget().getSignature();
					if(ossas.get(invokedMethod)==null) {
						continue;
					}
					
					if(!checkInterProcedural) {
						objToBeChecked.liveUses.add(invokeInstruction);
						objToBeChecked.liveness = true;
						continue;
					}
					//TODO probable breakpoint
					else {
						//TODO Interprocedural
						//invokeInstruction.args.
						ObjToBeChecked nwobjtobechkd = new ObjToBeChecked();
						
						int noofparams = invokeInstruction.args.size();
						for(int i=0;i<noofparams;i++) {
							HashMap<InstanceKey, OSSAObject>argi = invokeInstruction.args.get(i);
							if(argi!=null&&argi.containsValue(checkobj)) {
//								System.out.println("New object to be checked instruction:"+ossas.get(invokedMethod).funcossainstrctn);
								nwobjtobechkd.add(ossas.get(invokedMethod).funcossainstrctn.args[i], invokedMethod, ossas.get(invokedMethod).funcossainstrctn);
								if(argObjsisLive.containsKey(nwobjtobechkd)) {
									if(argObjsisLive.get(nwobjtobechkd)!=null) {
										objToBeChecked.liveness = argObjsisLive.get(nwobjtobechkd);
										if(objToBeChecked.isLive())
											objToBeChecked.liveUses.add(invokeInstruction);
									}
								}
								else {
									//This assignment to end recursion.
									argObjsisLive.put(nwobjtobechkd, null);
									objToBeChecked.liveness = checkLiveness(nwobjtobechkd);
									argObjsisLive.put(nwobjtobechkd, objToBeChecked.liveness);
									if(objToBeChecked.isLive())
										objToBeChecked.liveUses.add(invokeInstruction);
								}
							}
						}
//						nwobjtobechkd.add(obj, checkPointMethod, checkPointInstruction)
						
					}
					continue;
				}
				//no rtphiinstructinos as yet now
				if (useinstruction instanceof RetPhiOSSAInstruction) {
//					retPhiOSSAInstruction retphiinstruction = (retPhiOSSAInstruction) useinstruction;
					/*objToBeChecked.liveUses.add(retfieldinstruct);
					objToBeChecked.liveness = true;
					continue;*/
				}


			}
		}
		if(objToBeChecked.liveness==null) {
			objToBeChecked.liveness=false;
			
		}
		
		return objToBeChecked.liveness;
	}
	
	
	/**
	 * @param ossa
	 * @param objversionno
	 * @return Object having object version no objversionno in OSSA ossa. 
	 */
	static OSSAObject getObjFromValueNo(ObjectSSA ossa, int objversionno) {
		for(Iterator<OSSAObject>objs = ossa.DefUse.defs.keySet().iterator();objs.hasNext();) {
			OSSAObject obj= objs.next();
			if(obj.objectVersionNo==objversionno)
				return obj;
		}
		return null;
	}
	
	public static int totalputphis=0;
	public static int totaldphis=0;
	public static int totalossacphis=0;
	public static int totalssacphis=0;
	/*
	 * Method signatures of method having more than 30 ssa instrctns. 
	 * Func calls to these methods will be instrumented with checks for argument objects
	 */
	public static HashSet<String> instrumentedfunccalls;
	/**
	 * Method to generate OSSA and stores in ossas
	 * DONE check and correct.
	 */
	public static void getOSSAs() {
		try{
			AnalysisScope scope = AnalysisScopeReader		
			.makeJavaBinaryAnalysisScope(
					jarFile,
					FileProvider
							.getFile(com.ibm.wala.core.tests.callGraph.CallGraphTestUtil.REGRESSION_EXCLUSIONS));
			ClassHierarchy cha = ClassHierarchy.make(scope);
			Iterable<Entrypoint> e = Util.makeMainEntrypoints(scope, cha);
			AnalysisOptions options = new AnalysisOptions(scope, e);
			
			//DONE- if want analysis of specific class use findMethodSigforclass function
			//findMethodSig for all classes.
			//if(classname==null)
			//	findMethodSig(cha);
			//else
				findMethodSigforclass(cha);
			// //
			// build the call graph
			
			com.ibm.wala.ipa.callgraph.CallGraphBuilder builder = Util
					.makeVanillaZeroOneCFABuilder(options, new AnalysisCache(),
							cha, scope, null, null);
			CallGraph cg = builder.makeCallGraph(options, null);
			
			// Pointer analysis
			PointerAnalysis pa = builder.getPointerAnalysis();
			BasicHeapGraph g = new BasicHeapGraph(pa, cg); 
			HeapModel hm = g.getHeapModel();
			instrumentedfunccalls = new HashSet<String>();
			
			String psFiles = outputFiles.toString();
			String dotFiles = outputFiles.append("dottmps/").toString();
			for(Iterator<String> methods = methodsigns.iterator();methods.hasNext();){			
				String methodsign = methods.next();
				MethodReference mr0 = StringStuff.makeMethodReference(methodsign);
				IMethod m = cha.resolveMethod(mr0);
				CGNode node = cg.getNode(m, Everywhere.EVERYWHERE);
			
				if(node!=null){
//					System.err.println("\nCreating OSSA for method:- "+methodsign+"\n\n");
					IR ir = node.getIR();
					System.out.println("\nCreating OSSA for method:- "+methodsign+"\n\n");
					ObjectSSA ossa = new ObjectSSA(cha, cg,  pa, hm, m, ir);
					ossas.put(methodsign, ossa);
					totalputphis += ossa.noOfPutPhis;
					totaldphis += ossa.noOfDPhis;
					totalossacphis += ossa.noOfCtrPhisinOSSA;
					totalssacphis += ossa.noOfCtrPhisinSSA;
					if(ossa.ir.getInstructions().length>noofinstructionbound)
						instrumentedfunccalls.add(methodsign);
					System.out.println(ossa.toString());
										
				}			
				
			}
			System.out.println(instrumentedfunccalls.size()+" instrumentedfunccalls: ");//+instrumentedfunccalls);
		}catch (Exception e) {
			e.printStackTrace();
			}
	}
	
	
	
	/**
	 * 
	 * @param cha
	 */	
	public static void findMethodSigforclass(ClassHierarchy cha){
		ArraySet<String> methodsigns = new ArraySet<String>();
		// String methodSig;
		for (IClass klass : cha) {
			if (klass.getReference().getClassLoader().getName().toString()
					.contentEquals("Application")) {
				/*System.out.println("\n\nklass:"
						+ klass
						+ "\nname "
						+ klass.getName().toString()
						+ " reference "
						+ klass.getReference().toString()
						+ "\nclassloader:"
						+ klass.getReference().getClassLoader().getName()
								.toString());*/
				if(classname!=null && klass.getName().toString().contentEquals(classname)) {
					for (IMethod method : klass.getAllMethods())
						if (method.getReference().getDeclaringClass()
								.getClassLoader().getName().toString()
								.contentEquals("Application")){
							/*System.out.println("\nmethod ref "
									+ method.getReference().toString()
									+ " \nmethod sig " + method.getSignature());*/
							methodsigns.add(method.getSignature());
						}
				}
				if(classname==null) {
					for (IMethod method : klass.getAllMethods())
						if (method.getReference().getDeclaringClass()
								.getClassLoader().getName().toString()
								.contentEquals("Application")){
							/*System.out.println("\nmethod ref "
									+ method.getReference().toString()
									+ " \nmethod sig " + method.getSignature());*/
							methodsigns.add(method.getSignature());
						}
				}
			}
		}
		System.out.println("Printing all the methods from ObjReachabilityAnalysis.findMethodSig():-\n\n"+methodsigns+"\n\n");
		ObjectLivenessAnalysis.methodsigns=methodsigns;

	}
	
	
	
}
class ObjToBeChecked{
	/**
	 * obj to be checked
	 */
	public OSSAObject obj;
//	public String checkPointMethod;
	/**
	 * Signature of method in which obj is to be checked
	 */
	public String checkPointMethod;
	/** 
	 * instructino in method in which obj is to be checked
	 */
	public SSAInstruction checkPointInstruction;
	public HashSet<SSAInstruction> liveUses = new HashSet<SSAInstruction>();
	public Boolean liveness;
	
	public void add(OSSAObject obj, String checkPointMethod, SSAInstruction checkPointInstruction) {
		this.obj = obj;
		this.checkPointMethod= checkPointMethod;
		this.checkPointInstruction = checkPointInstruction;
//		this.liveInstructions = getUses(obj, checkPointMethod);
	}
	
	public String toString() {
		StringBuffer strbuff = new StringBuffer();
		strbuff.append("\nObj: "+obj+" in method: "+checkPointMethod);//+" in instruction: "+checkPointInstruction);
		return strbuff.toString();
	}
	
	public HashSet<SSAInstruction>getUses(OSSAObject obj, IMethod method){
		return null;
	}
	
	public Boolean isLive(){
		return liveness;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((checkPointMethod == null) ? 0 : checkPointMethod.hashCode());
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjToBeChecked other = (ObjToBeChecked) obj;
		if (checkPointMethod == null) {
			if (other.checkPointMethod != null)
				return false;
		} else if (!checkPointMethod.equals(other.checkPointMethod))
			return false;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		return true;
	}

	//TODO probable breakpoint
	public Boolean equals(ObjToBeChecked compareObj) {
		return (this.toString().equals(compareObj.toString()));
	}
}


