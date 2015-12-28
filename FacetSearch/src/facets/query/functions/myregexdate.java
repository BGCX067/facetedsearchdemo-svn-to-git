package facets.query.functions;

import java.text.ParseException;
import java.util.Date;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase4;

import facets.myconstants.DateUtil;
import facets.myconstants.HelperFunctions;

public class myregexdate extends FunctionBase4 {

	@Override
	public NodeValue exec(NodeValue objectv1, NodeValue leftv2,
			NodeValue rightv3, NodeValue boolv4) {

		String object = objectv1.asUnquotedString();
		if (DateUtil.determineDateFormat(object) == null)
			return NodeValue.FALSE;
		String left = leftv2.asUnquotedString();
		String right = rightv3.asUnquotedString();

		boolean ismax = Boolean.parseBoolean(boolv4.asNode().getLiteralValue()
				.toString());

		HelperFunctions helper = HelperFunctions.getInstance();

		try {

			// SimpleDateFormat parsedate = new SimpleDateFormat("yyyy-MM");
			Date objectdate = DateUtil.parse(object);
			Date newobjectdate = helper.parse(helper.format(objectdate));
          		
            
			long objecttime = newobjectdate.getTime() ;//+  1 * 24 * 60 * 60 * 1000;
			Date leftdate = DateUtil.parse(left);
			long lefttime = leftdate.getTime();
			Date rightdate = DateUtil.parse(right);
			long righttime = rightdate.getTime();

			if (ismax) {

				if (objecttime >= lefttime
						&& objecttime <= righttime) {
                   return NodeValue.TRUE;
				}

			} else {

				if (objecttime >= lefttime
						&& objecttime <= righttime) {
					return NodeValue.TRUE;
				}

			}
			
			
//			if (!ismax) {
//                //TODO: // conditions were >= , < 
//				if ((objecttime >= lefttime) && (objecttime <= righttime)) {
//
//					return NodeValue.TRUE;
//				} else {
//					// check what happens with the date type? will empty string
//					// work?
//					return NodeValue.FALSE;
//
//				}
//			} else {
//
//				if ((objecttime >= lefttime) && (objecttime <= righttime)) {
//
//					return NodeValue.TRUE;
//				} else {
//
//					return NodeValue.FALSE;
//
//				}
//
//			}

		} catch (ParseException pe) {

			System.out.println("IN REGEX FUNCTION UNKNOWN DATE FORMAT:"
					+ object);
			pe.printStackTrace();

		}

		return NodeValue.FALSE;
	}

}
