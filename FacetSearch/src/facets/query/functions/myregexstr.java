package facets.query.functions;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase4;

public class myregexstr extends FunctionBase4 {

	@Override
	public NodeValue exec(NodeValue objectv1, NodeValue leftv2,
			NodeValue rightv3, NodeValue boolv4) {

		String object = objectv1.asUnquotedString();
		String left = leftv2.asUnquotedString();
		String right = rightv3.asUnquotedString();

		boolean ismax = Boolean.parseBoolean(boolv4.asNode().getLiteralValue()
				.toString());

		if (ismax) {

			if(!left.equals(right))
			
			{
			if ((object.compareTo(left) >= 0)
					&& (object.compareTo(right) <= 0)) {
			     return NodeValue.TRUE;	
			}
			
			}
			else {
				
				if (object.startsWith(left))
				 {
				   return NodeValue.TRUE;
				}	
				
			}

		} else {

			if ((object.compareTo(left) >= 0)
					&& (object.compareTo(right) <= 0)) {
                return NodeValue.TRUE;
			}

		}

	   return NodeValue.FALSE;
	}
	 
	
		
//		if (!ismax) {
//            // conditions were >= , < 
//			if ((object.compareTo(left) >= 0) && (object.compareTo(right) <= 0)) {
//
//				return NodeValue.TRUE;
//			} else {
//
//				return NodeValue.FALSE;
//
//			}
//		} else {
//			
//            if(!left.equals(right)){
//			
//            if ((object.compareTo(left) >= 0) && (object.compareTo(right) <= 0)) {
//
//				return NodeValue.TRUE;
//			} else {
//
//				return NodeValue.FALSE;
//
//			}
//                
//            }
//            else {
//            	
//            	if (object.startsWith(left)) {
//
//    				return NodeValue.TRUE;
//    			} else {
//
//    				return NodeValue.FALSE;
//
//    			}
//            	
//            }
//            
//		}

//	}

}
