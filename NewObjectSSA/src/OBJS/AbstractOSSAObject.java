package OBJS;

import java.util.HashMap;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * for the abstract objects contaning more than one ASIs in there pointsTo set.
 * Most probably not need it at all. Use stack and instructions structure instead.
 * TODO check need
 * 
 * @author yash
 *
 */
public class AbstractOSSAObject extends OSSAObject{
	
	public HashMap<InstanceKey, OSSAObject> containedObj = new HashMap<InstanceKey, OSSAObject>();
	public AbstractOSSAObject(int VersionNo, TypeReference concreteType,
			OrdinalSet<InstanceKey> ptsTo) {
		super(VersionNo, concreteType, ptsTo);
		// TODO Auto-generated constructor stub
	}

}
