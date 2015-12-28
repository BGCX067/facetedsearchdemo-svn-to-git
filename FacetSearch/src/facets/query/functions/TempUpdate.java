package facets.query.functions;

import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.modify.request.QuadAcc;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.Target;
import com.hp.hpl.jena.sparql.modify.request.UpdateClear;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataDelete;
import com.hp.hpl.jena.sparql.modify.request.UpdateDeleteWhere;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop;
import com.hp.hpl.jena.sparql.modify.request.UpdateModify;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.util.NodeUtils;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateRequest;

public class TempUpdate {
 
	
	public static void main(String args[]){
	 
		String directory = "/home/ashaik/MyDatabases/DBPedia2";
		Dataset dataset = TDBFactory.createDataset(directory) ; 
	    //Model model = dataset.getDefaultModel();
	   // model.remove(Var.alloc("x"), Var.alloc("y"), NodeValue.makeNodeDate("1957-11-31").asNode());
	    UpdateRequest request = new UpdateRequest();
	    QuadAcc quadtriple = new QuadAcc();
	  	
	    //Triple tp = new Triple(Var.alloc("x") , Var.alloc("y"),NodeValue.makeNodeDate("1957-11-31").asNode());
	    //quadtriple.addTriple(tp);
	    //Triple tp2 = new Triple(Var.alloc("a") , Var.alloc("b"),NodeValue.makeNodeDate("1926-02-29").asNode());
	    //quadtriple.addTriple(tp2);
	    
//	    quadtriple.addTriple(new Triple(Var.alloc("x1") , Var.alloc("y1"),NodeValue.makeNodeDate("2009-06-31").asNode()));
//	    quadtriple.addTriple(new Triple(Var.alloc("x2") , Var.alloc("y2"),NodeValue.makeNodeDate("2009-09-31").asNode()));
//	    quadtriple.addTriple(new Triple(Var.alloc("x3") , Var.alloc("y3"),NodeValue.makeNodeDate("2009-11-31").asNode()));
//	    quadtriple.addTriple(new Triple(Var.alloc("x") , Var.alloc("y"),NodeValue.makeNodeDate("2010-06-31").asNode()));
//	    quadtriple.addTriple(new Triple(Var.alloc("x") , Var.alloc("y"),NodeValue.makeNodeDate("2009-06-31").asNode()));
//	    quadtriple.addTriple(new Triple(Var.alloc("x") , Var.alloc("y"),NodeValue.makeNodeDate("2008-06-31").asNode()));
//	                                 
	    
   
   //"9223372036854775807"^^http://www.w3.org/2001/XMLSchema#nonNegativeInteger
	 //   WARN [AWT-EventQueue-0] (Log.java:63) - Datatype format exception: "2009-06-31"^^xsd:date
	 //   WARN [AWT-EventQueue-0] (Log.java:63) - Datatype format exception: "2009-09-31"^^xsd:date
	 //   WARN [AWT-EventQueue-0] (Log.java:63) - Datatype format exception: "2009-09-31"^^xsd:date
	 //   WARN [AWT-EventQueue-0] (Log.java:63) - Datatype format exception: "2010-09-31"^^xsd:date
	 //   WARN [AWT-EventQueue-0] (Log.java:63) - Datatype format exception: "2009-11-31"^^xsd:date
	 //   WARN [AWT-EventQueue-0] (Log.java:63) - Datatype format exception: "2010-06-31"^^xsd:date
	 //   WARN [AWT-EventQueue-0] (Log.java:63) - Datatype format exception: "2009-11-31"^^xsd:date
	 //   WARN [AWT-EventQueue-0] (Log.java:63) - Datatype format exception: "2009-11-31"^^xsd:date
	 //   WARN [AWT-EventQueue-0] (Log.java:63) - Datatype format exception: "2008-06-31"^^xsd:date
	    
	    
	 NodeValue nv = NodeValue.makeNode("9223372036854775807", null, "http://www.w3.org/2001/XMLSchema#nonNegativeInteger");   
	    
	 
	 
	 // String query = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> SELECT ?x ?y WHERE { ?x ?y \"9223372036854775807\"^^xsd:nonNegativeInteger. }";
	    
	 String query = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> SELECT ?x ?y WHERE { ?x ?y \"2009-06-31\"^^xsd:date. }";
	 
	 QueryExecution exec = QueryExecutionFactory.create(query, dataset);
	    
	  ResultSet rs = exec.execSelect();  
	 
	  ResultSetFormatter.out(System.out, rs);
	  
	  QuadAcc datacc = new QuadAcc();
	  //datacc.addTriple(0, new Triple(Var.alloc("x") , Var.alloc("y") ,nv.asNode()));
	 
	  datacc.addTriple(0, new Triple(Var.alloc("x") , Var.alloc("y"),NodeValue.makeNodeDate("2009-06-31").asNode()));
	  
	  //UpdateDataDelete datadelete = new UpdateDataDelete(datacc);
	  
	  
	  //datadelete.output(new IndentedWriter(System.out));
	  
	  //request.add(datadelete);
	  
	  UpdateDeleteWhere delete = new UpdateDeleteWhere(datacc);
	  
	  delete.output(new IndentedWriter(System.out));
	  
	  request.add(delete);
//	    
	  UpdateAction.execute(request, dataset);

	  String query2 = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> SELECT ?x ?y WHERE { ?x ?y \"9223372036854775807\"^^xsd:nonNegativeInteger. }";
	    
	  QueryExecution exec2 = QueryExecutionFactory.create(query2, dataset);
	  
	  ResultSet rs2 = exec2.execSelect();  
		 
	  ResultSetFormatter.out(System.out, rs2);
	  
	}



}
