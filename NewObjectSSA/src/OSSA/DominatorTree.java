package OSSA;

import java.util.Iterator;

import com.ibm.wala.cfg.CFGSanitizer;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.dominators.DominanceFrontiers;

public class DominatorTree< T extends BasicBlock> {

	/**
	 * @param args
	 */
	
	public  SSACFG ssacfg;
	//public  ControlFlowGraph<I, T> cfg;
	public DominanceFrontiers<T> RFG;
	DominatorTree(SSACFG ssacfg1) {
		// TODO Auto-generated constructor stub
		ssacfg=ssacfg1;
		//cfg=(ControlFlowGraph)ssacfg;
	}
	
	public DominanceFrontiers<T> createDominanceFrontiers(){
		DominanceFrontiers<T> RDF= new DominanceFrontiers<T>((Graph<T>)ssacfg, (T)ssacfg.entry());
//		System.out.println("\n\n\nTHe dominance frontiers for cfg nodes are: "+RDF);
		for (int i = 0; i <= ssacfg.getMaxNumber(); i++) {
			BasicBlock bb = ssacfg.getNode(i);
			/*System.out.println("The dominance-frontier of basicblock bb"+bb.getNumber());
			for(Iterator<T>iterator=RDF.getDominanceFrontier((T) bb);iterator.hasNext();){
				BasicBlock dom = iterator.next();
				System.out.println("\n ->"+dom.getNumber());
			}*/
		}
		return RDF;
	}
	
	


}
