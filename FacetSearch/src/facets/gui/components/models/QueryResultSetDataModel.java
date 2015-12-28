package facets.gui.components.models;

import java.util.Iterator;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;

import facets.gui.components.controller.FacetSearchController;
import facets.mystatic.handler.BasicPatternHandler;
import facets.mystatic.handler.QueryConstructor;

public class QueryResultSetDataModel {

	private FacetSearchController facetsearchcontroller;

	public QueryResultSetDataModel(FacetSearchController controller) {

		facetsearchcontroller = controller;

	}

	public ResultSet getShowCurrentStatusResultSet(String varclsname,
			Iterator<String> clsnames) {

		BasicPatternHandler basich = facetsearchcontroller
				.getQueryConstructionController().getBasicPatternHandler();
		QueryConstructor queryconstruct = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor();

		String basequery = basich.getBaseQuerySeen(varclsname);

		String resultquery = queryconstruct.getCurrentStatusResultSetQuery(
				varclsname, basequery, clsnames);

		QueryExecution qexec = QueryExecutionFactory.create(resultquery,
				facetsearchcontroller.getDataSetController()
						.getDatasetInstance());

		ResultSet resultset = qexec.execSelect();
		ResultSetRewindable rewind = ResultSetFactory.makeRewindable(resultset);

		// dont close if result set is not rewindable
		qexec.close();

		return rewind;

	}

	public String getShowCurrentStatusResultQuery(String varclsname,
			Iterator<String> clsnames) {

		BasicPatternHandler basich = facetsearchcontroller
				.getQueryConstructionController().getBasicPatternHandler();
		QueryConstructor queryconstruct = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor();

		String basequery = basich.getBaseQuerySeen(varclsname);

		String resultquery = queryconstruct.getCurrentStatusResultSetQuery(
				varclsname, basequery, clsnames);

		return resultquery;

	}

}
