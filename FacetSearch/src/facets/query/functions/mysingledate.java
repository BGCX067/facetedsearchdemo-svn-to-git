package facets.query.functions;

import java.text.ParseException;
import java.util.Date;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

import facets.myconstants.DateUtil;
import facets.myconstants.HelperFunctions;

public class mysingledate extends FunctionBase2 {

	@Override
	public NodeValue exec(NodeValue objectv1, NodeValue singlev2) {

		String object = objectv1.asUnquotedString();
		if (DateUtil.determineDateFormat(object) == null)
			return NodeValue.FALSE;
		String single = singlev2.asUnquotedString();

		HelperFunctions helper = HelperFunctions.getInstance();

		try {

			Date objectdate = DateUtil.parse(object);
			Date newobjectdate = helper.parse(helper.format(objectdate));

			long objecttime = newobjectdate.getTime();
			Date singledate = DateUtil.parse(single);
			long singletime = singledate.getTime();

			if (objecttime == singletime) {
             
				return NodeValue.TRUE;
			} 
//				else {
//				//System.out.println(objectv1.asString()+"      "+newobjectdate+"        "+singledate);
//				return NodeValue.FALSE;
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
