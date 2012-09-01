import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.ws.handler.MessageContext.Scope;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACache;
import com.ibm.wala.ssa.SSAComparisonInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.collections.IteratorUtil;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.ref.ReferenceCleanser;
import com.ibm.wala.util.strings.StringStuff;

//####are my comments- Yash

public class SparseAnalysis {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassHierarchyException 
	 */
	public static void main(String[] args) throws IOException, ClassHierarchyException {
		try{
			// TODO Auto-generated method stub
			AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(args[0], FileProvider.getFile(com.ibm.wala.core.tests.callGraph.CallGraphTestUtil.REGRESSION_EXCLUSIONS));
			ClassHierarchy cha = ClassHierarchy.make(scope);
			Iterable<Entrypoint> e = Util.makeMainEntrypoints(scope, cha);
			AnalysisOptions options = new AnalysisOptions(scope,e);
			
			
			//####Loop for finding the method signature of desired method..in this case Sample.a()I--class sample, method name a, return type int
			/*
			for(IClass klass:cha){
				//System.out.println("name "+klass.getName().toString()+" reference "+klass.getReference().toString());
				if(klass.getName().toString().equalsIgnoreCase("LSample"))
					for(IMethod  method:klass.getAllMethods())
						System.out.println("method ref "+method.getReference()+" method sig "+method.getSignature());
			}
			*/
			
			//####Code taken from PDFWalaIR to make the ir for a particula method
			//MethodReference
			MethodReference mr = StringStuff.makeMethodReference("Sample.a()I");
			IMethod m = cha.resolveMethod(mr);			
			if (m == null) {
		       Assertions.UNREACHABLE("could not resolve " + mr);
		    }
		    
			options.getSSAOptions().setPiNodePolicy(SSAOptions.getAllBuiltInPiNodes());
		    AnalysisCache cache = new AnalysisCache();
		    IR ir = cache.getSSACache().findOrCreateIR(m, Everywhere.EVERYWHERE, options.getSSAOptions());
		    if (ir == null) {
		    	Assertions.UNREACHABLE("Null IR for " + m);
		    }
		    ///*
		    System.out.println(ir.toString()+"\n"+"\n");

		    int c = 1,noofdefs=0,noofuses=0;
		    
		 //#### Create the worklist, worklist contatins the statements which are difining some live variables
		    Set worklist = new HashSet();
		    //defstmts hashmap stores the statements corresponding to definitions
		    HashMap defstmts = new HashMap();
		    //####Intitalize the worklist along with printing the ssa form on console
		    //####Initial worklist contains all print instructions, conditionals(for being extra cautious) and return stmts
			for (Iterator<?> i = ir.iterateAllInstructions(); i.hasNext();) {
				SSAInstruction instrcn = (SSAInstruction) i.next();
				
				System.out.print(c++ + " : ");

				if (instrcn instanceof SSAGetInstruction)
				{
					System.out.println("Get Instruction: " + instrcn);
				}
				else if (instrcn instanceof SSAPutInstruction)
				{
					System.out.println("Put Instruction: " + instrcn);
				}
				else if  (instrcn instanceof SSABinaryOpInstruction)
					System.out.println("Binaryop Instruction: " + instrcn);
				else if  (instrcn instanceof SSAGotoInstruction)
					System.out.println("SSAGoto Instruction: " + instrcn);
				else if  (instrcn instanceof SSAReturnInstruction){
					System.out.println("Return Instruction: " + instrcn);
					worklist.add(instrcn);
				}
				else if  (instrcn instanceof SSAComparisonInstruction)
					System.out.println("SSAComparison Instruction: " + instrcn);
				else if  (instrcn instanceof SSAConditionalBranchInstruction){
					System.out.println("SSAConditionalBranch Instruction: " + instrcn);
					worklist.add(instrcn);
				}
				else 
					System.out.println(instrcn.toString());
				
				noofdefs=instrcn.getNumberOfDefs();
				noofuses=instrcn.getNumberOfUses();
				for(int j=0;j<noofdefs;j++){
					System.out.println("def "+j+" "+instrcn.getDef(j));
					defstmts.put(instrcn.getDef(j), instrcn);
				}
				for(int j=0;j<noofuses;j++)
					System.out.println("use "+j+" "+instrcn.getUse(j));	
				
				System.out.println("\n");
			}
			System.out.println("worklist:\n"+worklist);
			
			System.out.println("\ndefstmts:\n"+defstmts);
			
			//*/
			
			//####Set the initial set of live variables which are used or defined in conditionals and return statements
			Set livevariables = new HashSet();
			Iterator it = worklist.iterator();
			while(it.hasNext()){
				SSAInstruction instrcn = (SSAInstruction)it.next();
				noofdefs=instrcn.getNumberOfDefs();
				noofuses=instrcn.getNumberOfUses();
				for(int j=0;j<noofdefs;j++){
					livevariables.add(instrcn.getDef(j));
					//System.out.println("def "+instrcn.getDef(j));
				}
				for(int j=0;j<noofuses;j++){
					livevariables.add(instrcn.getUse(j));
					//System.out.println("use "+instrcn.getUse(j));
				}
			}
			System.out.println(livevariables);
			
			//#correct this code..rest all is cool..probably :P
			it = worklist.iterator();
			while(it.hasNext()){
				System.out.println("while loop executes ");//+it.next());
				SSAInstruction instrcn = (SSAInstruction)it.next();
				if(instrcn==null)
					continue;
				worklist.remove(instrcn);
				//System.out.println(worklist);//+instrcn.toString());
				noofdefs=instrcn.getNumberOfDefs();
				noofuses=instrcn.getNumberOfUses();
				for(int i=0;i<noofdefs;i++){
					livevariables.add(instrcn.getDef(i));
				}
				
				for(int j=0;j<noofuses;j++){
					System.out.println("Worklist: "+worklist);
					//if(!livevariables.contains(instrcn.getUse(j))){
						livevariables.add(instrcn.getUse(j));
						worklist.add(defstmts.get(instrcn.getUse(j)));
					//}
					/*
					for(int k=0;k<noofdefs;k++)
						if(!livevariables.contains(instrcn.getDef(k))){
							livevariables.add(instrcn.getDef(k));
							//for(int k=0;k<noofuses;k++){
							
							
							//}
						}
					*/
					
				}
				it=worklist.iterator();
			}
			
			
			System.out.println("final output livevariables"+livevariables);
			//####SSA construction and IClass and IMethod knowing code part taken from construct all irs
		    // register class hierarchy and AnalysisCache with the reference cleanser, so that their soft references are appropriately wiped
			/*
		    ReferenceCleanser.registerClassHierarchy(cha);
		    AnalysisCache cache = new AnalysisCache();
		    ReferenceCleanser.registerCache(cache);
		    System.out.print("building IRs...");
		    for (IClass klass : cha) {
		    	System.err.println(klass.getName().toString());
		    	for (IMethod method : klass.getDeclaredMethods()) {
		    		System.out.println(method.getSignature());
		    	}
		      ///*
		    	for (IMethod method : klass.getDeclaredMethods()) {
		        wipeSoftCaches();
		        System.out.println(method.getReference().toString());
		        // construct an IR; it will be cached
		        IR ir = cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
		        //if(ir!=null)
		        	//System.err.println(ir.toString());
		      }
		      //
		    }
			*/
		    /*
		    System.out.println("done making ir for method "+ mr.getName().toString());
		    
		    
		    
		 //####OLD and WRONG Live variable analysis..
//		    liveAnalysis(ir);
		    DefUse du = new DefUse(ir);
		    
		    int maxvn = ir.getSymbolTable().getMaxValueNumber();
		    Boolean live[] =  new Boolean[maxvn];
		    //System.out.println(du.getNumberOfUses(ir.));
		    for(int i =1;i<=maxvn;i++){
		    	live[i-1]=false;
		    	if(du.getNumberOfUses(i)>0)
		    		live[i-1]=true;
		    }
		    for(int i=0; i<maxvn;i++){
		    	System.out.println("v"+(i+1)+"is live: "+live[i].toString());
		    	//if(live[i-1]==true)
		    		//System.out.println(du.getDef(i-1).toString()+"is live");
		    	//else
		    		//System.out.println(du.getDef(i).toString()+"is not live");
		    }
			for (Iterator<?> i = ir.iterateAllInstructions(); i.hasNext();) {
				SSAInstruction instrcn = (SSAInstruction) i.next();
				instrcn.getDef();
		    	
		    }
		    
			*/
		}catch(Throwable t){
			t.printStackTrace();
		}
		
		
	}


	
}
