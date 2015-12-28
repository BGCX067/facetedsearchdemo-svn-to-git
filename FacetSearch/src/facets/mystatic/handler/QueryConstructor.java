package facets.mystatic.handler;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

import java.util.Set;

import org.openjena.atlas.io.IndentedWriter;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.PatternVars;

import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.util.NodeUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import facets.datatypes.FacetValueRange;
import facets.gui.components.controller.QueryConstructionController;
import facets.myconstants.FacetConstants;

public class QueryConstructor {

	private static QueryConstructor queryconstructor = null;

	private QueryConstructionController mcontroller;

	private boolean DEBUG = FacetConstants.DEBUG;

	private QueryConstructor(QueryConstructionController qcontroller) {

		mcontroller = qcontroller;

		FunctionRegistry.get().put("java:facets.query.functions.getType",
				facets.query.functions.getType.class);
		FunctionRegistry.get().put("java:facets.query.functions.myregexdate",
				facets.query.functions.myregexdate.class);
		FunctionRegistry.get().put("java:facets.query.functions.myregexstr",
				facets.query.functions.myregexstr.class);
		FunctionRegistry.get().put("java:facets.query.functions.myregextoint",
				facets.query.functions.myregextoint.class);

		FunctionRegistry.get().put("java:facets.query.functions.mysingledate",
				facets.query.functions.mysingledate.class);
		FunctionRegistry.get().put("java:facets.query.functions.mysinglestr",
				facets.query.functions.mysinglestr.class);
		FunctionRegistry.get().put("java:facets.query.functions.mysingleint",
				facets.query.functions.mysingleint.class);

	}

	public static QueryConstructor getInstance(
			QueryConstructionController maincontroller) {

		if (queryconstructor != null)
			return queryconstructor;
		else {

			queryconstructor = new QueryConstructor(maincontroller);
			return queryconstructor;
		}

	}

	public String loadInitialClassesFromDatasetQuery() {

		Query newquery = QueryFactory.create();

		newquery.setQuerySelectType();

		Var instances = Var.alloc("instances");
		Var classes = Var.alloc("classes");

		BasicPattern bp = new BasicPattern();

		Triple t1 = new Triple(instances, RDF.type.asNode(), classes);

		bp.add(t1);

		newquery.setQueryPattern(new ElementTriplesBlock(bp));

		newquery.setDistinct(true);

		newquery.addResultVar(classes);

		newquery = QueryFactory.create(newquery);

		System.out
				.println("\n\n---------------------------------------------------------------------------------");
		newquery.serialize(new IndentedWriter(System.out));

		return newquery.toString();

	}

	public String getClassTypeCurrentResultSetQuery(String varclsname,
			String basequery) {

		VariableHandler mvarh = mcontroller.getVariableHandler();
		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();

		if (basequery == null && mvarh.isRootName(varclsname)) // root
		{

			String rootvarclsname = varclsname;

			Var root = Var.alloc(rootvarclsname);

			Node rootnode = NodeUtils.asNode(mvarh
					.getClassTypeURI(rootvarclsname));

			Query newquery = QueryFactory.create();
			newquery.setQuerySelectType();

			BasicPattern bp = new BasicPattern();

			Triple t1 = new Triple(root, RDF.type.asNode(), rootnode); // root
																		// node

			bp.add(t1);

			newquery.setDistinct(true);
			newquery.setQueryPattern(new ElementTriplesBlock(bp));

			newquery.addResultVar(root);

			newquery = QueryFactory.create(newquery);
            if(DEBUG) 
			{System.out
					.println("\n\n------------------------------------------------------------------------------------");
			newquery.serialize(new IndentedWriter(System.out));
			}
			return newquery.toString();

		} else {

			Query newquery = QueryFactory.create();
			newquery.setQuerySelectType();

			Query oldsyn = QueryFactory.create(basequery);

			Element el = oldsyn.getQueryPattern();
			BasicPattern newbp = new BasicPattern();

			ElementGroup newelgroup = new ElementGroup();

			Set<Var> variableset = PatternVars.vars(el);
			Iterator<Var> bvitr = variableset.iterator();
			List<String> currbasevar = new ArrayList<String>();
			while (bvitr.hasNext())
				currbasevar.add(bvitr.next().getName());

			newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

			if (mybp.hasConstraintTP() || mybp.hasJoinTP()) {

				newbp = mybp.copyQueryJoinElementBlock(varclsname, newbp,
						currbasevar);

				newbp = mybp.copyQueryConstraintElementBlock(varclsname, newbp,
						null);

				newelgroup = mybp.copyQueryElementFilter(varclsname,
						newelgroup, null);

			}

			Var var = Var.alloc(varclsname);

			newelgroup.addElement(new ElementTriplesBlock(newbp));

			newquery.setQueryPattern(newelgroup);

			newquery.setDistinct(true);

			newquery.addResultVar(var);

			/**
			 * TODO: was trying to project only root variable number
			 * 
			 */
			// newsyn.addResultVar(mvarh.getRootVariableClassName());

			newquery = QueryFactory.create(newquery);
			System.out
					.println("\n\n-----------------------------------------------------------------------------------------------------");
			newquery.serialize(new IndentedWriter(System.out));

			return newquery.toString();

		}

	}

	public String getClassAsSubjectFacetsQuery(String varclsname,
			String basequery) {

		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();

		Var fpredicate = Var.alloc("fpredicate");
		Var object = Var.alloc("object");
		Var otype = Var.alloc("otype");

		Var main = Var.alloc(varclsname);

		ExprList f4object = new ExprList();
		f4object.add(new ExprVar(object));

		Expr typefunction = new E_Function(
				"java:facets.query.functions.getType", f4object);

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);
		Element el = oldsyn.getQueryPattern();
		BasicPattern newbp = new BasicPattern();
		ElementGroup newelgroup = new ElementGroup();

		Set<Var> variableset = PatternVars.vars(el);
		Iterator<Var> bvitr = variableset.iterator();
		List<String> currbasevar = new ArrayList<String>();
		while (bvitr.hasNext())
			currbasevar.add(bvitr.next().getName());

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		if (mybp.hasConstraintTP() || mybp.hasJoinTP()) {

			newbp = mybp.copyQueryJoinElementBlock(varclsname, newbp,
					currbasevar);

			newbp = mybp.copyQueryConstraintElementBlock(varclsname, newbp,
					null);

			newelgroup = mybp.copyQueryElementFilter(varclsname, newelgroup,
					null);

		}

		newbp.add(new Triple(main, fpredicate, object));

		newelgroup.addElement(new ElementTriplesBlock(newbp));
		// group.addElementFilter(mybp.getFilter(fpredicate,
		// "http://data.linkedmdb.org/resource/"));
//		newelgroup.addElementFilter(mybp.getFilter(fpredicate,
//				"http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));

		newquery.setQueryPattern(newelgroup);

		newquery.addGroupBy(fpredicate);
		newquery.addGroupBy(otype, typefunction);

		newquery.addResultVar(fpredicate);
		newquery.addResultVar(otype);

		newquery = QueryFactory.create(newquery);

		System.out
				.println("\n\n--------------------------------------------------------------------------");
		newquery.serialize(new IndentedWriter(System.out));

		return newquery.toString();

	}

	public String getClassAsObjectFacetsQuery(String varclsname,
			String basequery) {

		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();

		Var bpredicate = Var.alloc("bpredicate");
		Var subject = Var.alloc("subject");
		Var stype = Var.alloc("stype");

		Var main = Var.alloc(varclsname);

		ExprList f4subject = new ExprList();
		f4subject.add(new ExprVar(subject));

		Expr typefunction = new E_Function(
				"java:facets.query.functions.getType", f4subject);

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);
		Element el = oldsyn.getQueryPattern();
		BasicPattern newbp = new BasicPattern();
		ElementGroup newelgroup = new ElementGroup();

		Set<Var> variableset = PatternVars.vars(el);
		Iterator<Var> bvitr = variableset.iterator();
		List<String> currbasevar = new ArrayList<String>();
		while (bvitr.hasNext())
			currbasevar.add(bvitr.next().getName());

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		if (mybp.hasConstraintTP() || mybp.hasJoinTP()) {

			newbp = mybp.copyQueryJoinElementBlock(varclsname, newbp,
					currbasevar);

			newbp = mybp.copyQueryConstraintElementBlock(varclsname, newbp,
					null);

			newelgroup = mybp.copyQueryElementFilter(varclsname, newelgroup,
					null);

		}

		newbp.add(new Triple(subject, bpredicate, main));

		newelgroup.addElement(new ElementTriplesBlock(newbp));
//		newelgroup.addElementFilter(mybp.getFilter(bpredicate,
//				"http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));

		newquery.setQueryPattern(newelgroup);

		newquery.addGroupBy(bpredicate);
		newquery.addGroupBy(stype, typefunction);

		newquery.addResultVar(bpredicate);
		newquery.addResultVar(stype);

		newquery = QueryFactory.create(newquery);

		System.out
				.println("\n\n--------------------------------------------------------------------------");
		newquery.serialize(new IndentedWriter(System.out));

		return newquery.toString();

	}

	public String getClassAsSubjectFacetValuesQuery(String faceturi,
			String varclsname, String basequery) {

		VariableHandler mvarh = mcontroller.getVariableHandler();
		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();

		Var main = Var.alloc(varclsname);
		Var object = mvarh.getFacetValueVariable(varclsname, NodeUtils.asNode(faceturi).getLocalName());
		//Var object = Var.alloc("object");
		// Var root = Var.alloc(mvarh.getRootVariableClassName());

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);

		Element el = oldsyn.getQueryPattern();
		BasicPattern newbp = new BasicPattern();

		ElementGroup newelgroup = new ElementGroup();

		Set<Var> variableset = PatternVars.vars(el);
		Iterator<Var> bvitr = variableset.iterator();
		List<String> currbasevar = new ArrayList<String>();
		while (bvitr.hasNext())
			currbasevar.add(bvitr.next().getName());

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		if (mybp.hasConstraintTP() || mybp.hasJoinTP()) {

			newbp = mybp.copyQueryJoinElementBlock(varclsname, newbp,
					currbasevar);

			newbp = mybp.copyQueryConstraintElementBlock(varclsname, newbp,
					null);

			newelgroup = mybp.copyQueryElementFilter(varclsname, newelgroup,
					null);

		}

		newbp.add(new Triple(main, NodeUtils.asNode(faceturi), object));

		newelgroup.addElement(new ElementTriplesBlock(newbp));

		newquery.setQueryPattern(newelgroup);

		newquery.addResultVar(object);

		newquery.addResultVar(main);

		// newquery.addResultVar(root);

		newquery = QueryFactory.create(newquery);

		if (DEBUG) {

			System.out
					.println("\n\n------------------------------------------------------------------------------------");
			newquery.serialize(new IndentedWriter(System.out));

		}

		return newquery.toString();

	}

	public String getClassAsObjectFacetValuesQuery(String faceturi,
			String varclsname, String basequery) {

		// VariableHandler mvarh = mcontroller.getVariableHandler();
		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();

		Var main = Var.alloc(varclsname);
		Var subject = Var.alloc("subject");
		// Var root = Var.alloc(mvarh.getRootVariableClassName());

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);

		Element el = oldsyn.getQueryPattern();
		BasicPattern newbp = new BasicPattern();

		ElementGroup newelgroup = new ElementGroup();

		Set<Var> variableset = PatternVars.vars(el);
		Iterator<Var> bvitr = variableset.iterator();
		List<String> currbasevar = new ArrayList<String>();
		while (bvitr.hasNext())
			currbasevar.add(bvitr.next().getName());

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		if (mybp.hasConstraintTP() || mybp.hasJoinTP()) {

			newbp = mybp.copyQueryJoinElementBlock(varclsname, newbp,
					currbasevar);

			newbp = mybp.copyQueryConstraintElementBlock(varclsname, newbp,
					null);

			newelgroup = mybp.copyQueryElementFilter(varclsname, newelgroup,
					null);

		}

		newbp.add(new Triple(subject, NodeUtils.asNode(faceturi), main));

		newelgroup.addElement(new ElementTriplesBlock(newbp));

		newquery.setQueryPattern(newelgroup);

		newquery.addResultVar(subject);

		newquery.addResultVar(main);

		// newquery.addResultVar(root);

		newquery = QueryFactory.create(newquery);

		if (DEBUG) {

			System.out
					.println("\n\n------------------------------------------------------------------------------------");
			newquery.serialize(new IndentedWriter(System.out));

		}

		return newquery.toString();

	}

	public String getClassAsSubjectFacetValueTypeCheckQuery(String varclsname,
			String basequery, FacetValueRange facetvaluerange) {

		VariableHandler mvarh = mcontroller.getVariableHandler();
		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();

		Var main = Var.alloc(varclsname);
		Var classtype = Var.alloc("fvalclstype");

		Var object = mvarh.getFacetValueVariable(varclsname, facetvaluerange
				.getFacetNode().getLocalName());

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);

		Element el = oldsyn.getQueryPattern();

		BasicPattern newbp = new BasicPattern();
		
		ElementGroup newelgroup = new ElementGroup();

		Set<Var> variableset = PatternVars.vars(el);
		Iterator<Var> bvitr = variableset.iterator();
		List<String> currbasevar = new ArrayList<String>();
		while (bvitr.hasNext())
			currbasevar.add(bvitr.next().getName());

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		if (mybp.hasConstraintTP() || mybp.hasJoinTP()) {

			newbp = mybp.copyQueryJoinElementBlock(varclsname, newbp,
					currbasevar);

			newbp = mybp.copyQueryConstraintElementBlock(varclsname, newbp,
					null);

			newelgroup = mybp.copyQueryElementFilter(varclsname, newelgroup,
					null);

		}
		
		Triple t1 = new Triple(main, facetvaluerange.getFacetNode(), object);
		Triple t2 = new Triple(object, RDF.type.asNode(), classtype);

		newbp.add(t1);
		newbp.add(t2);

		newelgroup.addElement(new ElementTriplesBlock(newbp));

		newquery.setQueryPattern(newelgroup);
		
		newquery.addResultVar(classtype);

		newquery.setDistinct(true);

		newquery = QueryFactory.create(newquery);

		//if (DEBUG) {
			System.out
					.println("\n\n CHECK for CLASS----------------------------------------------------------------------------");
			newquery.serialize(new IndentedWriter(System.out));
		//}

		return newquery.toString();

	}

	public String getClassAsObjectFacetValueTypeCheckQuery(String varclsname,
			String basequery, FacetValueRange facetvaluerange) {

		VariableHandler mvarh = mcontroller.getVariableHandler();
		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();

		Var main = Var.alloc(varclsname);
		Var classtype = Var.alloc("fvalclstype");

		Var subject = mvarh.getFacetValueVariable(varclsname, facetvaluerange
				.getFacetNode().getLocalName());

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);

		Element el = oldsyn.getQueryPattern();

		BasicPattern newbp = new BasicPattern();

		ElementGroup newelgroup = new ElementGroup();

		Set<Var> variableset = PatternVars.vars(el);
		Iterator<Var> bvitr = variableset.iterator();
		List<String> currbasevar = new ArrayList<String>();
		while (bvitr.hasNext())
			currbasevar.add(bvitr.next().getName());

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		if (mybp.hasConstraintTP() || mybp.hasJoinTP()) {

			newbp = mybp.copyQueryJoinElementBlock(varclsname, newbp,
					currbasevar);

			newbp = mybp.copyQueryConstraintElementBlock(varclsname, newbp,
					null);

			newelgroup = mybp.copyQueryElementFilter(varclsname, newelgroup,
					null);

		}

		Triple t1 = new Triple(subject, facetvaluerange.getFacetNode(), main);
		Triple t2 = new Triple(subject, RDF.type.asNode(), classtype);

		newbp.add(t1);
		newbp.add(t2);

		newelgroup.addElement(new ElementTriplesBlock(newbp));

		newquery.setQueryPattern(newelgroup);

		newquery.addResultVar(classtype);

		newquery.setDistinct(true);

		newquery = QueryFactory.create(newquery);

		//if (DEBUG) {
			System.out
					.println("\n\n CHECK for CLASS----------------------------------------------------------------------------");
			newquery.serialize(new IndentedWriter(System.out));
		//}

		return newquery.toString();

	}

	public String getClassAsSubjectNewClassTypeBaseQuery(String newvarclsname,
			String newvarclsuri, String varclsname, String basequery,
			FacetValueRange facetvaluerange) {

		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();
		// VariableHandler mvarh = mcontroller.getVariableHandler();

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);

		Element el = oldsyn.getQueryPattern();

		BasicPattern newbp = new BasicPattern();

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		Var main = Var.alloc(varclsname);

		Var newclsname = Var.alloc(newvarclsname);

		Triple t1 = new Triple(main, facetvaluerange.getFacetNode(), newclsname);
		Triple t2 = new Triple(newclsname, RDF.type.asNode(),
				NodeUtils.asNode(newvarclsuri));

		/**
		 * adding joins
		 */
		mybp.recordJoinTriplePaths(newclsname.getName(), t1);

		newbp.add(t1);
		newbp.add(t2);

		newquery.setQueryPattern(new ElementTriplesBlock(newbp));

		// TODO: changed
		newquery.addResultVar(newclsname);

		// newquery.addResultVar(mvarh.getRootVariableClassName());
		newquery.setDistinct(true);

		newquery = QueryFactory.create(newquery);

		System.out
				.println("\n\nNew BaseQuery----------------------------------------------------------------------------");
		newquery.serialize(new IndentedWriter(System.out));

		return newquery.toString();

	}

	public String getClassAsObjectNewClassTypeBaseQuery(String newvarclsname,
			String newvarclsuri, String varclsname, String basequery,
			FacetValueRange facetvaluerange) {

		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();
		// VariableHandler mvarh = mcontroller.getVariableHandler();

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);

		Element el = oldsyn.getQueryPattern();

		BasicPattern newbp = new BasicPattern();

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		Var main = Var.alloc(varclsname);

		Var newclsname = Var.alloc(newvarclsname);

		Triple t1 = new Triple(newclsname, facetvaluerange.getFacetNode(), main);
		Triple t2 = new Triple(newclsname, RDF.type.asNode(),
				NodeUtils.asNode(newvarclsuri));

		/**
		 * adding joins
		 */
		mybp.recordJoinTriplePaths(newclsname.getName(), t1);

		newbp.add(t1);
		newbp.add(t2);

		newquery.setQueryPattern(new ElementTriplesBlock(newbp));

		// TODO: changed to get only the root instances
		newquery.addResultVar(newclsname);
		// newquery.addResultVar(mvarh.getRootVariableClassName());
		newquery.setDistinct(true);

		newquery = QueryFactory.create(newquery);

		System.out
				.println("\n\nNew BaseQuery----------------------------------------------------------------------------");
		newquery.serialize(new IndentedWriter(System.out));

		return newquery.toString();

	}

	public String getClassAsSubjectNewBlankTypeBaseQuery(String varblkname,
			String varclsname, String basequery, FacetValueRange facetvaluerange) {

		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();
		// VariableHandler mvarh = mcontroller.getVariableHandler();

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);

		Element el = oldsyn.getQueryPattern();

		BasicPattern newbp = new BasicPattern();

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		Var main = Var.alloc(varclsname);

		Var newblkname = Var.alloc(varblkname);

		/**
		 * no type triple
		 */
		Triple t1 = new Triple(main, facetvaluerange.getFacetNode(), newblkname);

		/**
		 * adding joins
		 */
		mybp.recordJoinTriplePaths(newblkname.getName(), t1);

		newbp.add(t1);

		newquery.setQueryPattern(new ElementTriplesBlock(newbp));

		// TODO: changed
		newquery.addResultVar(newblkname);
		// newquery.addResultVar(mvarh.getRootVariableClassName());
		newquery.setDistinct(true);

		newquery = QueryFactory.create(newquery);

		System.out
				.println("\n\nNew BaseQuery----------------------------------------------------------------------------");
		newquery.serialize(new IndentedWriter(System.out));

		return newquery.toString();

	}

	public String getClassAsObjectNewBlankTypeBaseQuery(String varblkname,
			String varclsname, String basequery, FacetValueRange facetvaluerange) {

		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();
		// VariableHandler mvarh = mcontroller.getVariableHandler();

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);

		Element el = oldsyn.getQueryPattern();

		BasicPattern newbp = new BasicPattern();

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		Var main = Var.alloc(varclsname);

		Var newblkname = Var.alloc(varblkname);

		/**
		 * no type triple
		 */
		Triple t1 = new Triple(newblkname, facetvaluerange.getFacetNode(), main);

		/**
		 * adding joins
		 */
		mybp.recordJoinTriplePaths(newblkname.getName(), t1);

		newbp.add(t1);

		newquery.setQueryPattern(new ElementTriplesBlock(newbp));

		// TODO: changed
		newquery.addResultVar(newblkname);
		// newquery.addResultVar(mvarh.getRootVariableClassName());
		newquery.setDistinct(true);

		newquery = QueryFactory.create(newquery);

		System.out
				.println("\n\nNew BaseQuery----------------------------------------------------------------------------");
		newquery.serialize(new IndentedWriter(System.out));

		return newquery.toString();

	}

	public String getCurrentStatusResultSetQuery(String varclsname,
			String basequery, Iterator<String> currentvarclsnames) {

		VariableHandler mvarh = mcontroller.getVariableHandler();
		BasicPatternHandler mybp = mcontroller.getBasicPatternHandler();

		// TODO: changed to get only the root instances
		Var main = Var.alloc(varclsname);

		Var main_label = Var.alloc(varclsname + "_label");

		Query newquery = QueryFactory.create();
		newquery.setQuerySelectType();

		Query oldsyn = QueryFactory.create(basequery);

		Element el = oldsyn.getQueryPattern();

		BasicPattern newbp = new BasicPattern();

		ElementGroup newelgroup = new ElementGroup();

		Set<Var> variableset = PatternVars.vars(el);
		Iterator<Var> bvitr = variableset.iterator();
		List<String> currbasevar = new ArrayList<String>();
		while (bvitr.hasNext())
			currbasevar.add(bvitr.next().getName());

		newbp = mybp.copyBaseQueryElementsBlock(el, newbp);

		if (mybp.hasConstraintTP() || mybp.hasJoinTP()) {

			newbp = mybp.copyQueryJoinElementBlock(varclsname, newbp,
					currbasevar);

			newbp = mybp.copyQueryConstraintElementBlock(varclsname, newbp,
					null);

			newelgroup = mybp.copyQueryElementFilter(varclsname, newelgroup,
					null);

		}

		Triple selected = new Triple(main, RDFS.label.asNode(), main_label);
		newbp.add(selected);

		Triple history = null;
		String name = null;
		List<Var> projections = new ArrayList<Var>();
		Var v;

		Iterator<String> classes = currentvarclsnames;
		Set<String> blkvariables = mvarh.getAllBlankVariableClassNames();

		while (classes.hasNext()) {

			name = classes.next();

			if (!name.equals(varclsname) && !blkvariables.contains(name)) {
				v = Var.alloc(name + "_label");

				history = new Triple(Var.alloc(name), RDFS.label.asNode(), v);

				newbp.add(history);

				projections.add(v);

			}

		}

		newelgroup.addElement(new ElementTriplesBlock(newbp));

		newquery.setQueryPattern(newelgroup);

		newquery.addResultVar(main_label);

		Iterator<String> uknwitr = mvarh.getFacetValueUnknownVariables();

		while (uknwitr.hasNext()) {

			newquery.addResultVar(Var.alloc(uknwitr.next()));

		}

		Iterator<Var> clssitr = projections.iterator();

		while (clssitr.hasNext()) {

			newquery.addResultVar(clssitr.next());
		}

		newquery.setDistinct(true);

		newquery = QueryFactory.create(newquery);

		if (DEBUG) {
			System.out
					.println("\n\n------------------------------------------------------------------------------------");
			newquery.serialize(new IndentedWriter(System.out));
		}

		return newquery.toString();

	}

}
