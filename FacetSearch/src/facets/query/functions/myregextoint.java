package facets.query.functions;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase4;

public class myregextoint extends FunctionBase4 {

	@Override
	public NodeValue exec(NodeValue objectv1, NodeValue leftv2,
			NodeValue rightv3, NodeValue boolv4) {

		String object = objectv1.asUnquotedString();
		String left = leftv2.asUnquotedString();
		String right = rightv3.asUnquotedString();

		boolean ismax = Boolean.parseBoolean(boolv4.asNode().getLiteralValue()
				.toString());

		Integer objectint=Integer.MIN_VALUE;
		try{
		objectint = Double.valueOf(object).intValue();
		}
		catch(NumberFormatException exception){
			
			return NodeValue.FALSE;
		}
		
		Integer leftint = Integer.valueOf(left);
		Integer rightint = Integer.valueOf(right);

		if (!ismax) {
			// conditions were >= , < 
			if ((objectint >= leftint) && (objectint < rightint)) {

				return NodeValue.TRUE;
			} else {

				return NodeValue.FALSE;

			}
		} else {

			if ((objectint >= leftint) && (objectint <= rightint)) {

				return NodeValue.TRUE;
			} else {

				return NodeValue.FALSE;

			}

		}

	}

}
