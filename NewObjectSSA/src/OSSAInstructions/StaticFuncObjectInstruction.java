package OSSAInstructions;

import OBJS.OSSAObject;

import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SymbolTable;

public class StaticFuncObjectInstruction extends SSAInstruction{

//	public SSAInstruction originst;
	public OSSAObject nwobj;
	
	public StaticFuncObjectInstruction(OSSAObject nwobj) {
		this.nwobj = nwobj;
//		this.originst = originst;
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
		StringBuffer strbuff = new StringBuffer(nwobj+" = StaticfuncObj()");
		return strbuff.toString();
	}

	@Override
	public void visit(IVisitor v) {
		// TODO Auto-generated method stub
		
	}

}
