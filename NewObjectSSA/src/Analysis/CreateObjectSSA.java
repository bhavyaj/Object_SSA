package Analysis;

import OSSA.CopyOfObjectSSA;
import OSSA.ObjectSSA;

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
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.collections.ArraySet;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.StringStuff;

public class CreateObjectSSA {

//	public static String methodsign = "OSSATestCases.HiddenArgCheck.test(LOSSATestCases/Sample;)V;";
	public static String methodsign = "TestProg4.test()V;";
//	public static String methodsign = "QuadTreeNode.gtEqualAdjNeighbor(I)LQuadTreeNode;";
	/**
	 * folder in which output files are to be stored
	 */
	public static  StringBuffer outputFiles = new StringBuffer("/home/bhavya/Acads/btp/NewObjectSSA/ObjectSSA/output/current/");
//	public static  StringBuffer outputFiles = new StringBuffer("/home/bhavya/Acads/btp/NewObjectSSA/ObjectSSA/output/jolden/bh/");
	/**
	 * jar file whose functions are to be analysed.
	 */
	public static String jarFile = "/home/bhavya/Acads/btp/NewObjectSSA/testprog.jar";
//	public static String jarFile = "/home/bhavya/Acads/btp/NewObjectSSA/ObjectSSA/output/jolden/perimeter/the.jar";
	
	public static ObjectSSA ossa;
	
	public static void main(String[] args) {
	try {
		AnalysisScope scope = AnalysisScopeReader
				.makeJavaBinaryAnalysisScope(
						jarFile,
						FileProvider
								.getFile(com.ibm.wala.core.tests.callGraph.CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		ClassHierarchy cha = ClassHierarchy.make(scope);
		Iterable<Entrypoint> e = Util.makeMainEntrypoints(scope, cha);
		AnalysisOptions options = new AnalysisOptions(scope, e);

//		findMethodSig(cha);

		// build the call graph

		com.ibm.wala.ipa.callgraph.CallGraphBuilder builder = Util
				.makeVanillaZeroOneCFABuilder(options, new AnalysisCache(),
						cha, scope, null, null);
		CallGraph cg = builder.makeCallGraph(options, null);

		// Pointer analysis
		PointerAnalysis pa = builder.getPointerAnalysis();
		BasicHeapGraph g = new BasicHeapGraph(pa, cg); // this is giving
														// runtime stack
														// overflow error
		HeapModel hm = g.getHeapModel();
		MethodReference mr0 = StringStuff.makeMethodReference(methodsign);
		IMethod m = cha.resolveMethod(mr0);
		CGNode node = cg.getNode(m, Everywhere.EVERYWHERE);

		IR ir = node.getIR();
		String psFiles = outputFiles.toString();
		String dotFiles = outputFiles.append("dottmps/").toString();
		(new pdfIr()).printIr(cha, ir, methodsign, psFiles, dotFiles);
//		CopyOfObjectSSA ossadummy = new CopyOfObjectSSA(cha, cg, pa, hm, m, ir);
		ossa = new ObjectSSA(cha, cg,  pa, hm, m, ir);//,psFiles, dotFiles);
		ossa.toString();
//		ossadummy.toString();
		System.out.println("Ossa:"+ossa);
		
//		System.out.println("\n\ndefuses"+ossa.DefUse.toString());
//		if(objectReachabilityAnalysisIntraProcedural(methodsign)==true)
//			System.out.println("Object is live!!");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * stores methodsignatures of methods in different classes in jar. 
	 * These signatures are then used to analyze corresponding methods.
	 * called only if uncommented in main function
	 * @param cha
	 */
	public static void findMethodSig(ClassHierarchy cha) {
		ArraySet<String> methodsigns = new ArraySet<String>();
		// String methodSig;
		for (IClass klass : cha) {
			if (klass.getReference().getClassLoader().getName().toString()
					.contentEquals("Application")) {
				System.out.println("\n\nklass:"
						+ klass
						+ "\nname "
						+ klass.getName().toString()
						+ " reference "
						+ klass.getReference().toString()
						+ "\nclassloader:"
						+ klass.getReference().getClassLoader().getName()
								.toString());
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
		System.out.println("Printing all the methods from ObjReachabilityAnalysis.findMethodSig():-\n\n"+methodsigns+"\n\n");
//		ObjReachabilityAnalysis.methodsigns=methodsigns;
	}
	
	
	
}
