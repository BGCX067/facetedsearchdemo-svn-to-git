package facets.query.functions;

import at.jku.rdfstats.ParseException;
import at.jku.rdfstats.hist.RDF2JavaMapper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.update.UpdateAction;

public class getType extends FunctionBase1 {

	@Override
	public NodeValue exec(NodeValue v) {
		
	     Node node1 = v.getNode();
	     String type = null;
	     
	     
	     try{
	     if(node1!=null){
	      	 
	    	type = RDF2JavaMapper.getType(node1); 
	     
	     
	     }
	     else {
	    	 
	    	Node node2 = v.asNode(); 
	    	System.out.println("makeNode:"+node2);
	    	type = RDF2JavaMapper.getType(node2);
	    	System.out.print("withType:"+type);
	    	   	
	    	
	     }
	     
	     }
	     catch(ParseException e){
	    	 System.out.println("ParseException@getType:"+v);
	    	 
	     }
		
		
		
		
		return NodeValue.makeString(type);
	}

	
	
	
	
//	@Override
//	public NodeValue exec(NodeValue n) {
//
//		Node node = n.asNode();
//
//		try {
//			return NodeValue.makeNodeString(RDF2JavaMapper.getType(node));
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		return n;
//	}

}
