package reachingdefanalysis;

import java.util.ArrayList;
import java.util.Map;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.dataflow.graph.BitVectorFramework;
import com.ibm.wala.dataflow.graph.BitVectorIdentity;
import com.ibm.wala.dataflow.graph.BitVectorKillGen;
import com.ibm.wala.dataflow.graph.BitVectorSolver;
import com.ibm.wala.dataflow.graph.BitVectorUnion;
import com.ibm.wala.dataflow.graph.ITransferFunctionProvider;
import com.ibm.wala.fixpoint.BitVectorVariable;
import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.ObjectArrayMapping;
import com.ibm.wala.util.intset.BitVector;
import com.ibm.wala.util.intset.OrdinalSetMapping;

public class reachingdef {
	private static IClassHierarchy cha;
	private static ExplodedControlFlowGraph ecfg;
	/**
	 * maps each static field to the numbers of the statements (in {@link #putInstrNumbering}) that define it; used for kills in flow
	 * functions
	 */
    private final Map<IField, BitVector> staticField2DefStatements = HashMapFactory.make();
    /**
     * maps the index of a putstatic IR instruction to a more compact numbering for use in bitvectors
     */
    private final OrdinalSetMapping<Integer> putInstrNumbering;
	
    public reachingdef(ExplodedControlFlowGraph ecfg, IClassHierarchy cha) {
        this.ecfg = ecfg;
        this.cha = cha;
        this.putInstrNumbering = numberPutStatics();
      }

      /**
       * generate a numbering of the putstatic instructions
       */
      private OrdinalSetMapping<Integer> numberPutStatics() {
        ArrayList<Integer> putInstrs = new ArrayList<Integer>();
        IR ir = ecfg.getIR();
        SSAInstruction[] instructions = ir.getInstructions();
        for (int i = 0; i < instructions.length; i++) {
          SSAInstruction instruction = instructions[i];
          if (instruction instanceof SSAPutInstruction && ((SSAPutInstruction) instruction).isStatic()) {
            SSAPutInstruction putInstr = (SSAPutInstruction) instruction;
            // instrNum is the number that will be assigned to this putstatic
            int instrNum = putInstrs.size();
            putInstrs.add(i);
            // also update the mapping of static fields to def'ing statements
            IField field = cha.resolveField(putInstr.getDeclaredField());
            assert field != null;
            BitVector bv = staticField2DefStatements.get(field);
            if (bv == null) {
              bv = new BitVector();
              staticField2DefStatements.put(field, bv);
            }
            bv.set(instrNum);
          }
        }
        return new ObjectArrayMapping<Integer>(putInstrs.toArray(new Integer[putInstrs.size()]));
      }


/*
* @param <T> type of node in the graph 
* @param <V> type of abstract states computed 
* in ITransferFunctionProvider<T, v extends IVariable>
* 
*/
    private class reachindefTransferfunc implements ITransferFunctionProvider<IExplodedBasicBlock, BitVectorVariable>{

		@Override
		public UnaryOperator<BitVectorVariable> getEdgeTransferFunction(
				IExplodedBasicBlock src, IExplodedBasicBlock dst) {
			// TODO Auto-generated method stub
			//return null;
			//not required therefore throw exception
			throw new UnsupportedOperationException("no unary operation reqd");
		}
	
		@Override
		public AbstractMeetOperator<BitVectorVariable> getMeetOperator() {
			// TODO Auto-generated method stub
			//meet operator for reaching def is union
			return BitVectorUnion.instance();
		}
	
		@Override
		public UnaryOperator<BitVectorVariable> getNodeTransferFunction(
				IExplodedBasicBlock node) {
			// TODO Auto-generated method stub
			//return null;
		      SSAInstruction instruction = node.getInstruction();
		      int instructionIndex = node.getFirstInstructionIndex();
		      if (instruction instanceof SSAPutInstruction && ((SSAPutInstruction) instruction).isStatic()) {
		        // kill all defs of the same static field, and gen this instruction
		        final SSAPutInstruction putInstr = (SSAPutInstruction) instruction;
		        final IField field = cha.resolveField(putInstr.getDeclaredField());
		        assert field != null;
		        BitVector kill = staticField2DefStatements.get(field);
		        BitVector gen = new BitVector();
		        gen.set(putInstrNumbering.getMappedIndex(instructionIndex));
		        return new BitVectorKillGen(kill, gen);
		      } else {
		        // identity function for non-putstatic instructions
		        return BitVectorIdentity.instance();
		      }
		}
	
		@Override
		public boolean hasEdgeTransferFunctions() {
			// TODO Auto-generated method stub
			//Only node transfer functions reqd for intraprocedural analysis?
			return false;
		}
	
		@Override
		public boolean hasNodeTransferFunctions() {
			// TODO Auto-generated method stub
			return true;
		}
		
	}
    
    /**
     * run the analysis
     * 
     * @return the solver used for the analysis, which contains the analysis result
     */
    public BitVectorSolver<IExplodedBasicBlock> analyze() {
      // the framework describes the dataflow problem, in particular the underlying graph and the transfer functions
      BitVectorFramework<IExplodedBasicBlock, Integer> framework = new BitVectorFramework<IExplodedBasicBlock, Integer>(ecfg,
          new reachindefTransferfunc(), putInstrNumbering);
      BitVectorSolver<IExplodedBasicBlock> solver = new BitVectorSolver<IExplodedBasicBlock>(framework);
      try {
        solver.solve(null);
      } catch (CancelException e) {
        // this shouldn't happen
        assert false;
      }
//      if (VERBOSE) {
        for (IExplodedBasicBlock ebb : ecfg) {
          System.out.println(ebb);
          System.out.println(ebb.getInstruction());
          System.out.println(solver.getIn(ebb));
          System.out.println(solver.getOut(ebb));
        }
//      }
      return solver;
    }
}