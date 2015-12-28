package facets.gui.components.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.core.Var;

import facets.datatypes.ClassType;
import facets.datatypes.FacetValueRange;
import facets.datatypes.Facets;
import facets.gui.components.controller.FacetSearchController;

import facets.myconstants.FacetConstants;
import facets.mystatic.handler.BasicPatternHandler;
import facets.mystatic.handler.FacetSubjectHistogramBuilder;
import facets.mystatic.handler.FacetValueRangeBuilder;
import facets.mystatic.handler.QueryConstructor;
import facets.mystatic.handler.VariableHandler;

public class ClassTypeResultDataModel {

	private Map<String, ClassType> internalvarclsmap;

	private Map<String, String> initialclasses;
	private FacetSearchController facetsearchcontroller;

	private boolean DEBUG = FacetConstants.DEBUG;

	public ClassTypeResultDataModel(FacetSearchController controller) {

		facetsearchcontroller = controller;
		internalvarclsmap = new HashMap<String, ClassType>();
		initialclasses = new HashMap<String, String>();
	}

	public void reset(){
		internalvarclsmap.clear();
		
		
	}
	
	public List<String> loadInitialClassesFromDataset() {

		List<String> dataclasses = new ArrayList<String>();

		String queryString = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor()
				.loadInitialClassesFromDatasetQuery();

		Query q = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.create(q,
				facetsearchcontroller.getDataSetController()
						.getDatasetInstance());

		ResultSet rs = qexec.execSelect();

		String varname = rs.getResultVars().get(0);

		while (rs.hasNext()) {

			QuerySolution qs = rs.next();
			Node c = qs.get(varname).asNode();
			dataclasses.add(c.getLocalName());
			initialclasses.put(c.getLocalName(), c.getURI());
		}

		facetsearchcontroller.getQueryConstructionController()
				.getVariableHandler().setClassTypesofDataset(initialclasses);

		qexec.close();

		return dataclasses;
	}

	public List<String> constructRootClassType(String selectedtype) {

		VariableHandler variablehandler = facetsearchcontroller
				.getQueryConstructionController().getVariableHandler();
		BasicPatternHandler basichandler = facetsearchcontroller
				.getQueryConstructionController().getBasicPatternHandler();
		QueryConstructor queryhandler = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor();

//		variablehandler.reset();
//		basichandler.reset();

		/*
		 * create new variable name for new class type
		 */
		Var v1 = variablehandler.getNewRootVariableClassName(selectedtype);

		String varclsname = v1.getName();

		String newquery = queryhandler.getClassTypeCurrentResultSetQuery(
				varclsname, null);

		Query q = QueryFactory.create(newquery);

		QueryExecution qe = QueryExecutionFactory.create(q,
				facetsearchcontroller.getDataSetController()
						.getDatasetInstance());

		ResultSet rs = qe.execSelect();

		ResultSetRewindable resultSet = ResultSetFactory.makeRewindable(rs);

		ClassType ct = new ClassType(
				variablehandler.getClassTypeURI(varclsname), resultSet,
				varclsname); // root class
		ct.setBaseQueryString(newquery);

		/**
		 * TODO: check recording base query in basic pattern handler , no use as
		 * of now
		 */

		basichandler.recordBaseQuerySeen(varclsname, newquery);

		internalvarclsmap.put(varclsname, ct);

		/**
		 * TODO: implementation of KEYWORD SEARCH LOGIC
		 */
		List<String> classtypes = new ArrayList<String>();

		classtypes.add(varclsname);

		qe.close();

		return classtypes;
	}

	private void updateClassTypes(String varclsname, String basequery) {

		ClassType ct = null;

		String newquery = null;

		VariableHandler myh = facetsearchcontroller
				.getQueryConstructionController().getVariableHandler();
		BasicPatternHandler mybp = facetsearchcontroller
				.getQueryConstructionController().getBasicPatternHandler();
		QueryConstructor mqueryh = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor();

		if (!internalvarclsmap.containsKey(varclsname) && basequery != null) { // property
																				// referring
																				// to
																				// a
																				// class

			newquery = mqueryh.getClassTypeCurrentResultSetQuery(varclsname,
					basequery);

			Query q = QueryFactory.create(newquery);

			QueryExecution qe = QueryExecutionFactory.create(q,
					facetsearchcontroller.getDataSetController()
							.getDatasetInstance());

			ResultSet rs = qe.execSelect();

			ResultSetRewindable resultSet = ResultSetFactory.makeRewindable(rs);

			ct = new ClassType(myh.getClassTypeURI(varclsname), resultSet,
					varclsname);

			ct.setBaseQueryString(basequery);

			ct.setCuurentFacets(null);

			/**
			 * recording new base query seen
			 */
			mybp.recordBaseQuerySeen(varclsname, basequery);

			// for repeated clicking on class type to avoid repeated calculation

			ct.addCurrentQuery(newquery);

			internalvarclsmap.put(varclsname, ct);

			qe.close();
		} else {

			ct = internalvarclsmap.get(varclsname);
			
			// CHECK HERE 
			newquery = mqueryh.getClassTypeCurrentResultSetQuery(varclsname,
					basequery);

			Query q = QueryFactory.create(newquery);

			QueryExecution qe = QueryExecutionFactory.create(q,
					facetsearchcontroller.getDataSetController()
							.getDatasetInstance());

			ResultSet rs = qe.execSelect();

			ResultSetRewindable resultSet = ResultSetFactory.makeRewindable(rs);

			ct.setCurrentResultSet(resultSet);

			ct.setCuurentFacets(null);

			ct.addCurrentQuery(newquery);

			internalvarclsmap.put(varclsname, ct);

			qe.close();
		}

	}

	public String updateForFacetValueBlankTypeAsSubject(String varclsname,
			FacetValueRange facetvalue) {

		VariableHandler myh = facetsearchcontroller
				.getQueryConstructionController().getVariableHandler();

		QueryConstructor mqueryh = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor();

		String currentbasequery = internalvarclsmap.get(varclsname)
				.getBaseQueryString();

		Var newvarclsname = myh.getNewVariableBlankName(varclsname, "blank");

		String newbasequery = mqueryh.getClassAsSubjectNewBlankTypeBaseQuery(
				newvarclsname.getName(), varclsname, currentbasequery,
				facetvalue);

		updateClassTypes(newvarclsname.getName(), newbasequery);

		return newvarclsname.getName();
	}

	public String updateForFacetValueBlankTypeAsObject(String varclsname,
			FacetValueRange facetvalue) {

		VariableHandler myh = facetsearchcontroller
				.getQueryConstructionController().getVariableHandler();

		QueryConstructor mqueryh = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor();

		String currentbasequery = internalvarclsmap.get(varclsname)
				.getBaseQueryString();

		Var newvarclsname = myh.getNewVariableBlankName(varclsname, "blank");

		String newbasequery = mqueryh.getClassAsSubjectNewBlankTypeBaseQuery(
				newvarclsname.getName(), varclsname, currentbasequery,
				facetvalue);

		updateClassTypes(newvarclsname.getName(), newbasequery);

		return newvarclsname.getName();
	}

	public List<String> checkForFacetValueClassTypeAsSubject(String varclsname,
			FacetValueRange facetvalue) {

		VariableHandler myh = facetsearchcontroller
				.getQueryConstructionController().getVariableHandler();

		QueryConstructor mqueryh = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor();

		String currentbasequery = internalvarclsmap.get(varclsname)
				.getBaseQueryString();

		String query = mqueryh.getClassAsSubjectFacetValueTypeCheckQuery(
				varclsname, currentbasequery, facetvalue);

		QueryExecution qexec = QueryExecutionFactory.create(query,
				facetsearchcontroller.getDataSetController()
						.getDatasetInstance());

		ResultSet resultset = qexec.execSelect();
		ResultSetRewindable rewind = ResultSetFactory.makeRewindable(resultset);
		
		ResultSetFormatter.out(System.out, rewind);
		
		rewind.reset();
		List<String> newvarclsnamelist = null;
		if (rewind.size() > 0) {

			newvarclsnamelist = new ArrayList<String>();

			while (rewind.hasNext()) {

				Node uriclsname = rewind.next()
						.get(rewind.getResultVars().get(0)).asNode();

				Var newvarclsname = myh.getNewVariableClassName(uriclsname
						.getLocalName());

				String newbasequery = mqueryh
						.getClassAsSubjectNewClassTypeBaseQuery(
								newvarclsname.getName(), uriclsname.getURI(),
								varclsname, currentbasequery, facetvalue);

				updateClassTypes(newvarclsname.getName(), newbasequery);

				newvarclsnamelist.add(newvarclsname.getName());

			}

		}

		return newvarclsnamelist;
	}

	public List<String> checkForFacetValueClassTypeAsObject(String varclsname,
			FacetValueRange facetvalue) {

		VariableHandler myh = facetsearchcontroller
				.getQueryConstructionController().getVariableHandler();

		QueryConstructor mqueryh = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor();

		String currentbasequery = internalvarclsmap.get(varclsname)
				.getBaseQueryString();

		String query = mqueryh.getClassAsObjectFacetValueTypeCheckQuery(
				varclsname, currentbasequery, facetvalue);

		QueryExecution qexec = QueryExecutionFactory.create(query,
				facetsearchcontroller.getDataSetController()
						.getDatasetInstance());

		ResultSet resultset = qexec.execSelect();
		ResultSetRewindable rewind = ResultSetFactory.makeRewindable(resultset);
		ResultSetFormatter.out(System.out, rewind);
		
		rewind.reset();
		List<String> newvarclsnamelist = null;
		if (rewind.size() > 0) {

			newvarclsnamelist = new ArrayList<String>();

			while (rewind.hasNext()) {

				Node uriclsname = rewind.next()
						.get(rewind.getResultVars().get(0)).asNode();

				Var newvarclsname = myh.getNewVariableClassName(uriclsname
						.getLocalName());

				String newbasequery = mqueryh
						.getClassAsObjectNewClassTypeBaseQuery(
								newvarclsname.getName(), uriclsname.getURI(),
								varclsname, currentbasequery, facetvalue);

				updateClassTypes(newvarclsname.getName(), newbasequery);

				newvarclsnamelist.add(newvarclsname.getName());

			}

		}

		return newvarclsnamelist;
	}

	public Integer getClassTypeResultSetSize(String varclsname) {

		ClassType ct1 = internalvarclsmap.get(varclsname);
		updateClassTypes(varclsname, ct1.getBaseQueryString());

		return internalvarclsmap.get(varclsname).getCurrentResultSetSize();

	}

	public void removeClassType(String varclsname) {

		VariableHandler myh = facetsearchcontroller
				.getQueryConstructionController().getVariableHandler();
		if (!myh.getRootVariableClassName().equals(varclsname))
			internalvarclsmap.remove(varclsname);

	}

	public boolean availableNewClassTypeFacetDetails(String varclsname) {

		ClassType ct = internalvarclsmap.get(varclsname);

		QueryConstructor queryhandler = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor();

		String currentquery = queryhandler.getClassTypeCurrentResultSetQuery(
				varclsname, ct.getBaseQueryString());

		String lastsavedquery = ct.getCurrentQuery();
		// to handle when user clicks class type many times
		if (currentquery.equals(lastsavedquery)
				&& ct.getCurrentFacets() != null) {
			return false;
		}
		return true;

	}

	public void updateClassTypeResultSet(String varclsname) {

		ClassType ct = internalvarclsmap.get(varclsname);

		updateClassTypes(varclsname, ct.getBaseQueryString());

	}

	public Facets generateClassTypeFacetDetails(String varclsname) {

		ClassType ct = internalvarclsmap.get(varclsname);

		QueryConstructor queryhandler = facetsearchcontroller
				.getQueryConstructionController().getQueryContructor();

	
			
		String queryString = queryhandler.getClassAsSubjectFacetsQuery(
				varclsname, ct.getBaseQueryString());

		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.create(query,
				facetsearchcontroller.getDataSetController()
						.getDatasetInstance());

		
			ResultSet rs1 = qexec.execSelect();

			ResultSetRewindable rs = ResultSetFactory.makeRewindable(rs1);
            
			if(DEBUG)
			ResultSetFormatter.out(System.out, rs);

			Facets currentclassfacets = new Facets(ct);

			FacetValueRangeBuilder facetrangebuilder = FacetValueRangeBuilder
					.getInstance(facetsearchcontroller,
							 ct);
			FacetSubjectHistogramBuilder facetsubjectbuilder = FacetSubjectHistogramBuilder
					.getInstance(facetsearchcontroller, ct);

			for (; rs.hasNext();) {

				QuerySolution qs = rs.next();

				Node facet = qs.get("fpredicate").asNode();

				Node facetvaluetype = qs.get("otype").asNode();

				if (FacetConstants.facetexclude.contains(facet.getURI()))
					continue;

				Set<FacetValueRange> literalfacets = facetrangebuilder
						.getFacetHistogramFacetValueRanges(facet,
								facetvaluetype);

				currentclassfacets.addallFacetValueRanges(literalfacets);

			}
				
		String queryStringInward = queryhandler.getClassAsObjectFacetsQuery(
				varclsname, ct.getBaseQueryString());

		Query queryInward = QueryFactory.create(queryStringInward);

		QueryExecution qexecinward = QueryExecutionFactory.create(queryInward,
				facetsearchcontroller.getDataSetController()
						.getDatasetInstance());

		

			ResultSet rs2 = qexecinward.execSelect();

			ResultSetRewindable rsw = ResultSetFactory.makeRewindable(rs2);
			
			if(DEBUG)
			ResultSetFormatter.out(System.out, rsw);

			for (; rsw.hasNext();) {

				QuerySolution qs = rsw.next();

				Node facet = qs.get("bpredicate").asNode();

				Node facetvaluetype = qs.get("stype").asNode();

				if (FacetConstants.facetexclude.contains(facet.getURI()))
					continue;

				Set<FacetValueRange> classorblkfacets = facetsubjectbuilder
						.getInwardFacetHistogramFacetValueRanges(facet,
								facetvaluetype);

				currentclassfacets.addallFacetValueRanges(classorblkfacets);

			}

		ct.setCuurentFacets(currentclassfacets);

		qexec.close();
		qexecinward.close();
		
		facetsearchcontroller.getDataSetController().performDeleteIllegalNode();

		return ct.getCurrentFacets();

	}

}
