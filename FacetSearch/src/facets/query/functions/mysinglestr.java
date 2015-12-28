package facets.query.functions;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

public class mysinglestr extends FunctionBase2 {

	@Override
	public NodeValue exec(NodeValue objectv1, NodeValue single) {

		String object = objectv1.asUnquotedString();
		String singlevalue = single.asUnquotedString();

		if (object.startsWith(singlevalue)) {

			return NodeValue.TRUE;
		} else {

			return NodeValue.FALSE;

		}

	}

}
