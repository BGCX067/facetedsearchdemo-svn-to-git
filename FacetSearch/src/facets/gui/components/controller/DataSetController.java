package facets.gui.components.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.modify.request.QuadAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateDeleteWhere;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateRequest;

public class DataSetController {

	private static DataSetController datasetcontroller = null;

	private Dataset dataset;
	private File sourcefile;
	private FacetSearchController maincontroller;
	private List<Node> illegalnodes;

	private DataSetController(FacetSearchController mcontroller) {

		maincontroller = mcontroller;
		illegalnodes = new ArrayList<Node>();
	}

	public static DataSetController getInstance(FacetSearchController controller) {
		if (datasetcontroller != null)
		{	
			return datasetcontroller;

		}
		else {

			datasetcontroller = new DataSetController(controller);
			return datasetcontroller;
		}

	}

	public Dataset getDatasetInstance() {

		dataset = TDBFactory.createDataset(sourcefile.getAbsolutePath());

		return dataset;

	}

	public void addSourceFile(File source) {

		sourcefile = source;

	}

	public void addToupdateDelete(Node literal){
		illegalnodes.add(literal);
		  		
	}
	
	public void performDeleteIllegalNode(){
		
		if(!illegalnodes.isEmpty()){
			   
			Iterator<Node> itr =illegalnodes.iterator();
		
			while(itr.hasNext()){
			  
			 UpdateRequest request = new UpdateRequest();
			 QuadAcc datacc = new QuadAcc();
			  
			 datacc.addTriple(new Triple(Var.alloc("x") , Var.alloc("y"),itr.next()));
					 
			 UpdateDeleteWhere delete = new UpdateDeleteWhere(datacc);
			  
			 delete.output(new IndentedWriter(System.out));
			  
			 request.add(delete);

			 UpdateAction.execute(request, dataset);

			 }
		 illegalnodes.clear();
		}
			
	}
}
