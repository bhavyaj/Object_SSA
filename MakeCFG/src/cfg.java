import java.io.IOException;
import java.util.Iterator;

import javax.print.attribute.standard.Finishings;

import reachingdefanalysis.reachingdef;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.Util;
import com.ibm.wala.dataflow.graph.BitVectorSolver;
import com.ibm.wala.ide.ui.SWTTreeViewer;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.ref.ReferenceCleanser;
import com.ibm.wala.util.strings.StringStuff;
import com.ibm.wala.examples.analysis.dataflow.IntraprocReachingDefs;
import com.ibm.wala.examples.drivers.*;

public class cfg {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//buildcg_1(args[0]);
		CallGraph cg;
		cg = buildcg_1(args[0]);
		//System.out.println(cg.);
		IClassHierarchy cha=cg.getClassHierarchy();
		if (cg==null){
			System.out.println("NULL CG");
		}
		else
			//buildcg_2(args[1]);
			//ssa1(cg);
			ssa2(cha);		//can generate IR using cha caches also..ref.- com.wala.examples.analysis.constructAllIRs.java
//		cg.
	}
	
	public static void ssa1(CallGraph cg){
		try{
			
		for(Iterator<?> I = cg.getNodes(null).iterator(); I.hasNext();){
			CGNode n = (CGNode)I.next();
			if(n!=null){
			//ControlFlowGraph cfg=  n.getIR().getControlFlowGraph();
			//System.out.println(cfg.toString());
			///*
			//System.out.println(n.getMethod())
			System.err.println("\nIR of method: " + n.getMethod().getSignature());
			IR ir =n.getIR();
			if (ir == null)
			{
				System.err.println("No Instructions!");
				continue;
			}	
			
			int c=1;
			for(Iterator<SSAInstruction> i = ir.iterateAllInstructions(); i.hasNext(); ){
				SSAInstruction instructn = i.next();
				if (instructn instanceof SSAGetInstruction)
				{
					System.out.println("Get Instruction: " + instructn.toString());
				}
				else{
			
				System.err.println("Instrctn no. "+(c++)+":");
				System.out.println(instructn.toString());
				System.out.println(instructn.getDef());
				//if(instructn.getNumberOfUses()!=0)
					//System.out.println(instructn.getUse(2)) ;
				//else
					//System.out.println("No uses");
				System.out.println(instructn.hasDef());
				}
			}
			
			//*/
			}
		}	
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		catch(ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
	}
	
//	/*
	  public static void ssa2(IClassHierarchy cha){
			// Create a name representing the method whose IR we will visualize
		    ReferenceCleanser.registerClassHierarchy(cha);
		    AnalysisCache cache = new AnalysisCache();
		    ReferenceCleanser.registerCache(cache);
		    AnalysisOptions options = new AnalysisOptions();
		    IR ir=null;
		    System.out.print("building IRs...");
		    for (IClass klass : cha) {
		      for (IMethod method : klass.getDeclaredMethods()) {
		        wipeSoftCaches();
		        // construct an IR; it will be cached
		        
		        ir=cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
		        System.out.println("\n\nIR of method: "+method.getName()+method.getDeclaringClass());
		        if(ir==null)
		        		System.out.println("No ir for this method");
		        else{
		        		for(Iterator<?> i=ir.iterateAllInstructions();i.hasNext();){
		        			SSAInstruction instruct = (SSAInstruction) i.next();
		        			if (i instanceof SSAGetInstruction)
		    				{
		    					System.out.println("Get Instruction: " + instruct);
		    				}
		    				else
		    					System.out.println(instruct);
		        		}
		        	}
		   
		        	/*
		        	ExplodedControlFlowGraph ecfg = ExplodedControlFlowGraph.make(ir);
		          	reachingdef reachingDefs = new reachingdef(ecfg, cha);
		          	BitVectorSolver<IExplodedBasicBlock> solver = reachingDefs.analyze();
		          	*/
		     }
		   }
	}
	

	  /**
	   * Should we periodically clear out soft reference caches in an attempt to help the GC?
	   */
	  private final static boolean PERIODIC_WIPE_SOFT_CACHES = true;

	  /**
	   * Interval which defines the period to clear soft reference caches
	   */
	  private final static int WIPE_SOFT_CACHE_INTERVAL = 2500;

	  /**
	   * Counter for wiping soft caches
	   */
	  private static int wipeCount = 0;
	  
	  private static void wipeSoftCaches() {
		    if (PERIODIC_WIPE_SOFT_CACHES) {
		      wipeCount++;
		      if (wipeCount >= WIPE_SOFT_CACHE_INTERVAL) {
		        wipeCount = 0;
		        ReferenceCleanser.clearSoftCaches();
		      }
		    }
		  }
//	*/
	public static CallGraph buildcg_2(String jarFile){
		CallGraph cg=null;
		return cg;
		
	}
	
	
	
	public static CallGraph buildcg_1(String jarFile){
		
		try {
			//Create analysisscope
			AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarFile, FileProvider.getFile(com.ibm.wala.core.tests.callGraph.CallGraphTestUtil.REGRESSION_EXCLUSIONS));
			//AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarFile, null);
			//System.out.println(scope.toString());
			//Create cha
			ClassHierarchy cha = ClassHierarchy.make(scope);
			
			//Call Graph entrypoints
			Iterable<Entrypoint> e = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
			//Analysis options
			AnalysisOptions o = new AnalysisOptions(scope,e);
			//Make Call Graph
			System.out.print("Building Call Graph...");
			CallGraphBuilder builder = com.ibm.wala.ipa.callgraph.impl.Util.makeZeroCFABuilder(o, new AnalysisCache(), cha, scope);
			CallGraph cg = builder.makeCallGraph(o, null);
			
			System.out.println("done");
			//System.out.println(cg.toString());
			return cg;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(ClassHierarchyException e){
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CallGraphBuilderCancelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
		
	}
	

}
