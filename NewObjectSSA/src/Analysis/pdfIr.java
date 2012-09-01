package Analysis;

import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.viz.PDFViewUtil;

public class pdfIr {

	public  StringBuffer psFile;// = new StringBuffer("/home/yash/Workspaces/workspace/ObjectSSA/output/TestInterproc.test()V/");
	public  StringBuffer dotFile;// = new StringBuffer("/home/yash/Workspaces/workspace/ObjectSSA/output/TestInterproc.test()V/");
	public static String dotExe = "/usr/bin/dot";
	public static String gvExe = "/usr/bin/acroread";
	
	/**
	 * Complete all the output file names
	 */
	public  void createFileNames(String methodName){
		psFile./*append(methodName).append("/").*/append(methodName).append("ir.pdf");
		dotFile./*append(methodName).append("/").*/append(methodName).append("temp.dt");
//		System.out.println(psFile+"\n"+dotFile);
		//psFile.
//		title.concat(methodName);
	}
	
	public  void printIr(ClassHierarchy cha, IR ir, String methodName, String psFile, String dotFile){
		try {
			this.psFile = new StringBuffer(psFile);
			this.dotFile = new StringBuffer(dotFile);
			methodName = refactor(methodName);
			createFileNames(methodName);
			PDFViewUtil.ghostviewIR(cha, ir, this.psFile.toString(), this.dotFile.toString(), dotExe, gvExe);
		} catch (WalaException e) {
			// NOTHINGTODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
	
	/**
	 * replaces '/' with '-' in methodName
	 * @param methodName
	 * @return
	 */
	public static String refactor(String methodName){
		return methodName.replace('/', '-');
	}
}
