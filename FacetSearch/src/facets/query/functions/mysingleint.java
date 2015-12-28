package facets.query.functions;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

public class mysingleint extends FunctionBase2 {

	@Override
	public NodeValue exec(NodeValue objectv1, NodeValue single) {

		String object = objectv1.asUnquotedString();
		String singlevalue = single.asUnquotedString();

		Integer objectint = Double.valueOf(object).intValue();
		Integer singleint = Integer.valueOf(singlevalue);
		// conditions not proper.. not accurate   
		if (objectint >= singleint && objectint <= singleint) {

			return NodeValue.TRUE;
		} else {

			return NodeValue.FALSE;

		}
	}

}
