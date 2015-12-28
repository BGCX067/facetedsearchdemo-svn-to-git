package facets.mystatic.handler;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_NotOneOf;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.PatternVars;
import com.hp.hpl.jena.util.OneToManyMap;

import facets.datatypes.FacetValueDateRange;
import facets.datatypes.FacetValueNumberRange;
import facets.datatypes.FacetValueRange;
import facets.datatypes.FacetValueStringNumberRange;
import facets.datatypes.FacetValueStringRange;
import facets.gui.components.controller.QueryConstructionController;
import facets.gui.components.models.ClassTypeHistoryDataModel;
import facets.myconstants.HelperFunctions;

public class BasicPatternHandler {

	private static BasicPatternHandler mybp = null;

	/**
	 * To hold Join Triples for "Object" (In Subject - Predicate - Object )
	 * Resource (Class) Types
	 * 
	 * Keys are the objects variable class names and value is a triple with
	 * which it is joined.
	 * 
	 * Example: ?film rdf:type "film" ?film1 movie:actor ?actor1 ?actor1
	 * rdf:type "actor" here join triple map has key: actor1 Value: ?film1
	 * actor:actor ?actor1
	 */
	private OneToManyMap<String, Triple> objectjointriples = null;

	/**
	 * To hold Constraints variables on class types selected.
	 * 
	 * Example: key:film1 values: film1date, film1runtime key:actor1 values:
	 * actor1actor_name
	 * */

	private OneToManyMap<String, String> varclsnamefctvalname = null;

	/**
	 * To hold the constraint variable specific value or unknown value triple
	 * pattern
	 * 
	 * Example : key:actor1actor_name value: ?actor1actor_name movie:actor_name
	 * "Kevin Bacon"
	 */
	private Map<String, Triple> spcfctvalnametriple = null;

	/**
	 * To hold the constraint variable filter join triple, when user selects
	 * interval selection
	 * 
	 * Example : key: film1runtime value: ?film1 movie:runtime ?film1runtime
	 */
	private Map<String, Triple> clsfctvalnametriple = null;

	/**
	 * To hold the filter constraint, when user selects the interval selection.
	 * 
	 * Example: key: film1runtime value: Filter<myregex>(runtime, range values )
	 */

	private Map<String, ElementFilter> clsfacetvaluefilter = null;

	/**
	 * This map holds the base queries of classes visited.
	 * 
	 * Example: Key:film1 Value: select distinct ?film1 where { ?film1 rdf:type
	 * film }
	 * 
	 */

	private Map<String, String> classtypebasequery;

	private Deque<String> removedvar;

	private QueryConstructionController mcontroller;

	private BasicPatternHandler(QueryConstructionController controller) {
		mcontroller = controller;

		objectjointriples = new OneToManyMap<String, Triple>();
		varclsnamefctvalname = new OneToManyMap<String, String>();
		clsfctvalnametriple = new HashMap<String, Triple>();
		clsfacetvaluefilter = new HashMap<String, ElementFilter>();
		spcfctvalnametriple = new HashMap<String, Triple>();

		classtypebasequery = new HashMap<String, String>();
		removedvar = new ArrayDeque<String>();
	}

	public static BasicPatternHandler getInstance(
			QueryConstructionController controller) {

		if (mybp == null)
			mybp = new BasicPatternHandler(controller);

		return mybp;

	}

	public void reset() {

		objectjointriples.clear();
		varclsnamefctvalname.clear();
		clsfctvalnametriple.clear();
		clsfacetvaluefilter.clear();
		spcfctvalnametriple.clear();
		classtypebasequery.clear();
		removedvar.clear();

	}

	public void recordJoinTriplePaths(String newvarclsname, Triple tp) {

		objectjointriples.put(newvarclsname, tp);

	}

	public void recordSpecificTripleContraints(String varclsname,
			String fctvalname, Triple fctvaltriple) {

		if (!varclsnamefctvalname.containsValue(fctvalname))
			varclsnamefctvalname.put(varclsname, fctvalname);

		spcfctvalnametriple.put(fctvalname, fctvaltriple);

		if (clsfctvalnametriple.containsKey(fctvalname))
			clsfctvalnametriple.remove(fctvalname);

		if (clsfacetvaluefilter.containsKey(fctvalname))
			clsfacetvaluefilter.remove(fctvalname);

	}

	public void recordTripleElementFilterConstraint(String varclsname,
			String fctvalname, Triple fctvaltriple, ElementFilter filter) {
        
				
		if (!varclsnamefctvalname.containsValue(fctvalname))
		varclsnamefctvalname.put(varclsname, fctvalname);
        
		if (filter != null) {
			clsfacetvaluefilter.put(fctvalname, filter);
			clsfctvalnametriple.put(fctvalname, fctvaltriple);
		    
		} else {

			clsfacetvaluefilter.remove(fctvalname);
			clsfctvalnametriple.put(fctvalname, fctvaltriple);
       }
       
	}

	public void recordBaseQuerySeen(String varclsname, String basequery) {

		classtypebasequery.put(varclsname, basequery);
	}

	public String removeFacetValueRecord(String fctvalname) {
		String varclsname = null;
		String removevarclsname = null;
		
		VariableHandler variablehandler = mcontroller.getVariableHandler();
		
		for (Entry<String, String> entry : varclsnamefctvalname.entrySet()) {

			if (entry.getValue().equals(fctvalname))
				
			{
				varclsname = entry.getKey();
				clsfctvalnametriple.remove(fctvalname);
				clsfacetvaluefilter.remove(fctvalname);
				spcfctvalnametriple.remove(fctvalname);
				varclsnamefctvalname.remove(varclsname, fctvalname);
				variablehandler.removeFacetValueVariable(fctvalname);
				break;
			}
		}
      
		
		
//		if(varclsname!=null) 
//		{
//        	varclsnamefctvalname.remove(varclsname, fctvalname);
//		    variablehandler.removeFacetValueVariable(fctvalname);
//		}
		
		

		/**
		 * To remove leaf Class type selected when the all the constraints on it
		 * are removed.
		 * 
		 * this is avoid adding addition joins to next steps as it gets slower..
		 * 
		 * example: film1 - acto1 - performance1 - performance_charactor -
		 * "Ethen";
		 * 
		 * Now when user removes the constraint "Ethen" then you are left with
		 * join triple patterns on actor1 and performance1, so next browsing
		 * steps should not include performance1.
		 * 
		 * In short: removing leaf class types from the history panel when there
		 * are no constraints on it.
		 */
		 removevarclsname = varclsname;

		if (!varclsnamefctvalname.containsKey(varclsname)
				&& !variablehandler.isRootName(varclsname)
				) {

			ClassTypeHistoryDataModel currentHistory = mcontroller
			.getClassTypeHistoryDataModel();

//			currentHistory.removeVarClsName(varclsname);
//			classtypebasequery.remove(varclsname);
			
			Triple toberemoved = objectjointriples.get(varclsname);
			
			Collection<Triple> joinpatterns = objectjointriples.values();
	       
            Iterator<Triple> joiniterator = joinpatterns.iterator();     			
            
            Var keyvarclsname = Var.alloc(varclsname);
            
            
            
            while(joiniterator.hasNext()){
            	
            	Triple t = joiniterator.next();
            	if(toberemoved.equals(t)) continue;
            	
            	if(t.objectMatches(keyvarclsname) || t.subjectMatches(keyvarclsname)) 
            	{
            		removevarclsname = null;
            	}
            	
            }
            if(removevarclsname!=null)    
            {	objectjointriples.remove(varclsname);
    			currentHistory.removeVarClsName(varclsname);
    			classtypebasequery.remove(varclsname);
           
            } 
//
//			String queryString = classtypebasequery.get(varclsname);
//
//			Query query = QueryFactory.create(queryString);
//			Element el = query.getQueryPattern();
//			Set<Var> allbasevariable = PatternVars.vars(el);
//
//			ClassTypeHistoryDataModel currentHistory = mcontroller
//					.getClassTypeHistoryDataModel();
//
//			if (currentHistory.isItLeafVarClsname(allbasevariable)) {
//				objectjointriples.remove(varclsname);
//				currentHistory.removeVarClsName(varclsname);
//				classtypebasequery.remove(varclsname);
//				leafclasstyperemove = true;
//			}
		}

		return removevarclsname;
		// TODO: to remove joins directly from gui ..

	}

	public String getBaseQuerySeen(String varclsname) {

		return classtypebasequery.get(varclsname);

	}

	public boolean hasJoinTP() {
		return !objectjointriples.isEmpty();
	}

	public boolean hasConstraintTP() {

		return !varclsnamefctvalname.isEmpty();
	}

	public boolean hasJoinTP(String varclsname) {
		if (objectjointriples.isEmpty())
			return false;
		return objectjointriples.containsKey(varclsname);

	}

	public boolean hasConstraints(String varclsname) {
		if (varclsnamefctvalname.isEmpty())
			return false;
		return varclsnamefctvalname.containsKey(varclsname);
	}

	public BasicPattern copyBaseQueryElementsBlock(Element el, BasicPattern bgp) {

		BasicPattern newbp = bgp;

		if (el instanceof ElementGroup) {
			for (Element elt : ((ElementGroup) el).getElements()) {
				if (elt instanceof ElementFilter) {
					continue;
				}

				if (elt instanceof ElementTriplesBlock) {
					ElementTriplesBlock etb = (ElementTriplesBlock) elt;

					BasicPattern oldbp = etb.getPattern();
					Iterator<Triple> itr = oldbp.iterator();

					while (itr.hasNext()) {

						newbp.add(itr.next());

					}
					continue;
				}

				if (elt instanceof ElementPathBlock) {

					ElementPathBlock epb = (ElementPathBlock) elt;

					ElementPathBlock epb2 = new ElementPathBlock();
					epb2.getPattern().addAll(epb.getPattern());
					PathBlock pb = epb2.getPattern();

					Iterator<TriplePath> tppath = pb.getList().iterator();

					while (tppath.hasNext()) {
						newbp.add(tppath.next().asTriple());

					}
					continue;
				}

			}
		}
		return newbp;
	}

	// public BasicPattern copyRootQueryJoinElementBlock(String varclsname,
	// BasicPattern bgp){
	//
	// BasicPattern newbp = bgp;
	//
	// ClassTypeHistoryDataModel currentHistory =
	// mcontroller.getClassTypeHistoryDataModel();
	//
	// if(!objectjointriples.isEmpty())
	// for(Entry<String, Triple> entry : objectjointriples.entrySet()){
	//
	// if(currentHistory.historyContains(entry.getKey()))
	// newbp.add(entry.getValue());
	//
	// }
	//
	//
	//
	// return newbp;
	// }
	//
	// public BasicPattern copyRootQueryConstraintElementBlock(String
	// varclsname, BasicPattern bgp){
	//
	// BasicPattern newbp = bgp;
	//
	// ClassTypeHistoryDataModel currentHistory =
	// mcontroller.getClassTypeHistoryDataModel();
	//
	// if(!varclsnamefctvalname.isEmpty())
	// for(Entry<String, String> entry: varclsnamefctvalname.entrySet()){
	//
	// if(currentHistory.historyContains(entry.getKey()))
	// {
	// if(spcfctvalnametriple.containsKey(entry.getValue()))
	// newbp.add(spcfctvalnametriple.get(entry.getValue()));
	// else{
	//
	// if(clsfctvalnametriple.containsKey(entry.getValue()) &&
	// clsfacetvaluefilter.containsKey(entry.getValue()))
	// newbp.add(clsfctvalnametriple.get(entry.getValue()));
	//
	// }
	// }
	//
	// }
	//
	//
	// return newbp;
	//
	// }

	// public ElementGroup copyRootQueryElementFilter(String varclsname,
	// ElementGroup eg){
	//
	// ElementGroup neweg = eg;
	//
	// ClassTypeHistoryDataModel currentHistory =
	// mcontroller.getClassTypeHistoryDataModel();
	//
	//
	// if(!varclsnamefctvalname.isEmpty())
	// for(Entry<String, String> entry: varclsnamefctvalname.entrySet()){
	//
	// if(currentHistory.historyContains(entry.getKey()))
	// {
	// if(!spcfctvalnametriple.containsKey(entry.getValue()))
	// {
	// if(clsfctvalnametriple.containsKey(entry.getValue()) &&
	// clsfacetvaluefilter.containsKey(entry.getValue()))
	// {
	//
	// neweg.addElementFilter(clsfacetvaluefilter.get(entry.getValue()));
	//
	// }
	//
	//
	// }
	// }
	//
	// }
	//
	//
	// return neweg;
	//
	//
	// }

	public BasicPattern copyQueryJoinElementBlock(String varclsname,
			BasicPattern bgp, List<String> basevariables) {

		BasicPattern newbp = bgp;
		List<String> currbasevar = basevariables;

		ClassTypeHistoryDataModel currentHistory = mcontroller
				.getClassTypeHistoryDataModel();
		VariableHandler variablehandler = mcontroller.getVariableHandler();

		Iterator<String> overhistory = currentHistory
				.getAllSearchedClassHistoryIterator();

		// don't add joins which were already added in base query
		while (overhistory.hasNext()) {

			String history = overhistory.next();

			// root varclsname does not have join triple pattern because in
			// objectjointriples there wont be
			// triples with rootvarclsname as key
			if (!varclsname.equals(history)
					&& !variablehandler.isRootName(history)) {

				if (hasJoinTP(history) && !currbasevar.contains(history)) {
					Iterator<Triple> jitr = objectjointriples.getAll(history);

					while (jitr.hasNext()) {

						newbp.add(jitr.next());
					}
				}

			}

		}

		return newbp;
	}

	public BasicPattern copyQueryConstraintElementBlock(String varclsname,
			BasicPattern bgp, String facetvaluevariablename) {
		BasicPattern newbp = bgp;

		ClassTypeHistoryDataModel currentHistory = mcontroller
				.getClassTypeHistoryDataModel();

		Iterator<String> overhistory = currentHistory
				.getAllSearchedClassHistoryIterator();

		while (overhistory.hasNext()) {

			String history = overhistory.next();

			if (hasConstraints(history)) {
				Iterator<String> ctr = varclsnamefctvalname.getAll(history);

				while (ctr.hasNext()) {
					String fctvalname = ctr.next();
					if (!fctvalname.equals(facetvaluevariablename)) {
						if (spcfctvalnametriple.containsKey(fctvalname))
							newbp.add(spcfctvalnametriple.get(fctvalname));
						else {

							if (clsfctvalnametriple.containsKey(fctvalname))// &&
																			// clsfacetvaluefilter.containsKey(fctvalname))
								newbp.add(clsfctvalnametriple.get(fctvalname));

						}
					}
				}
			}

		}
		return newbp;
	}

	public ElementGroup copyQueryElementFilter(String varclsname,
			ElementGroup eg, String facetvaluevariablename) {
		ElementGroup neweg = eg;

		ClassTypeHistoryDataModel currentHistory = mcontroller
				.getClassTypeHistoryDataModel();

		Iterator<String> overhistory = currentHistory
				.getAllSearchedClassHistoryIterator();

		while (overhistory.hasNext()) {

			String history = overhistory.next();

			if (hasConstraints(history)) {
				Iterator<String> ctr = varclsnamefctvalname.getAll(history);

				while (ctr.hasNext()) {
					String fctvalname = ctr.next();
					if (!fctvalname.equals(facetvaluevariablename)) {
						if (!spcfctvalnametriple.containsKey(fctvalname))
							if (clsfctvalnametriple.containsKey(fctvalname)
									&& clsfacetvaluefilter
											.containsKey(fctvalname))
								neweg.addElementFilter(clsfacetvaluefilter
										.get(fctvalname));
					}

				}
			}

		}

		return neweg;
	}

	// public BasicPattern completeOperationToAddConstraintsJoins(String
	// varclsname, BasicPattern bgp, Set<Var> basevariables){
	// BasicPattern newbp = bgp;
	// String addedjoinvar = varclsname;
	// Iterator<Var> bvitr = basevariables.iterator();
	// // List<String> currbasevar = new ArrayList<String>();
	// while(bvitr.hasNext()) currbasevar.add(bvitr.next().getName());
	//
	// //IMPORTANT: CONFUSED IF I SHOULD AGAIN ADD JOIN PATTERN HERE, IF YES
	// THINK OF ALTERNATIVE
	// // if(hasJoinTP(varclsname)){
	// // Iterator<Triple> jitr = objectjointriples.getAll(varclsname);
	// //
	// // while(jitr.hasNext()){
	// //
	// // newbp.add(jitr.next());
	// // }
	// // }
	//
	// ClassTypeHistoryDataModel currentHistory =
	// mcontroller.getClassTypeHistoryDataModel();
	//
	// if(hasCListItem(varclsname)){
	// Iterator<Triple> ctr = facetvaluetriple.getAll(varclsname);
	//
	// while(ctr.hasNext()){
	//
	// newbp.add(ctr.next());
	// }
	// }
	//
	//
	// Iterator<String> overhistory =
	// currentHistory.getAllSearchedClassHistoryIterator();
	//
	// while(overhistory.hasNext()){
	//
	// String history = overhistory.next();
	//
	// if(!history.equals(addedjoinvar)){
	//
	// //don't add joins which were already added
	// //history varclsname has join but not seen in base variables of query,
	// then add thoughs joins
	// //in this way you can go to number several levels down ... until you
	// reach a literal level
	// //note this crashes the system as it takes lots of data.. In short too
	// many variables in
	// // query pattern without any constraints is a problem
	//
	// if(hasJoinTP(history) && !currbasevar.contains(history)){
	// Iterator<Triple> jitr = objectjointriples.getAll(history);
	//
	// while(jitr.hasNext()){
	//
	// newbp.add(jitr.next());
	// }
	// }
	//
	// if(hasCListItem(history)){
	// Iterator<Triple> ctr = facetvaluetriple.getAll(history);
	//
	// while(ctr.hasNext()){
	//
	// newbp.add(ctr.next());
	// }
	// }
	//
	// }
	//
	// }
	//
	// return newbp;
	// }
	//
	//
	// public BasicPattern completeOperation4(String varclsname, BasicPattern
	// bgp, Set<Var> basevariables){
	// BasicPattern newbp = bgp;
	// Iterator<Var> basevaritr = basevariables.iterator();
	//
	// if(hasCListItem(varclsname)){
	// Iterator<Triple> ctr = facetvaluetriple.getAll(varclsname);
	//
	// while(ctr.hasNext()){
	//
	// newbp.add(ctr.next());
	// }
	// }
	//
	// while(basevaritr.hasNext()){
	//
	// String varname = basevaritr.next().getName();
	// if(!varname.equals(varclsname)){
	//
	//
	// if(hasCListItem(varname)){
	//
	// Iterator<Triple> ctr = facetvaluetriple.getAll(varname);
	//
	// while(ctr.hasNext()){
	//
	// newbp.add(ctr.next());
	// }
	// }
	// }
	//
	// }
	//
	//
	//
	// return newbp;
	// }

	public ElementFilter getDateSpecificSelectionFilter(Var varclsfctobjname) {

		Var objectvar = varclsfctobjname;

		return null;

	}

	public ElementFilter getElementFilter(Var varclsfctobjname,
			FacetValueRange facetvaluerange) {

		Var objectvar = varclsfctobjname;

		if (facetvaluerange instanceof FacetValueDateRange) {
			FacetValueDateRange range = (FacetValueDateRange) facetvaluerange;
			ExprList exprlist = new ExprList();
			Expr objectexpr = new ExprVar(objectvar);
			Expr leftexpr = NodeValue.makeNode(range.getLeftNodeValue());
			Expr rightexpr = NodeValue.makeNode(range.getRightNodeValue());
			Expr boolexpr = NodeValue.makeBoolean(range.isRangeWithMax());

			exprlist.add(objectexpr);
			exprlist.add(leftexpr);
			exprlist.add(rightexpr);
			exprlist.add(boolexpr);

			Expr regexfunction = new E_Function(
					"java:facets.query.functions.myregexdate", exprlist);

			return new ElementFilter(regexfunction);
		}

		else if (facetvaluerange instanceof FacetValueNumberRange<?>) {

			FacetValueNumberRange<?> range = (FacetValueNumberRange<?>) facetvaluerange;

			Expr and = null;

			if (!range.isRangeWithMax()) {

				Expr greaterequal = new E_GreaterThanOrEqual(new ExprVar(
						objectvar),
						NodeValue.makeNode(range.getLeftNodeValue()));
				Expr lessthan = new E_LessThan(new ExprVar(objectvar),
						NodeValue.makeNode(range.getRightNodeValue()));

				and = new E_LogicalAnd(greaterequal, lessthan);

			} else {
				Expr greaterequal = new E_GreaterThanOrEqual(new ExprVar(
						objectvar),
						NodeValue.makeNode(range.getLeftNodeValue()));
				Expr lessequal = new E_LessThanOrEqual(new ExprVar(objectvar),
						NodeValue.makeNode(range.getRightNodeValue()));

				and = new E_LogicalAnd(greaterequal, lessequal);
			}
			return new ElementFilter(and);
		}

		else if (facetvaluerange instanceof FacetValueStringRange) {
			FacetValueStringRange range = (FacetValueStringRange) facetvaluerange;
			ExprList exprlist = new ExprList();
			Expr objectexpr = new ExprVar(objectvar);
			Expr leftexpr = NodeValue.makeNode(range.getLeftNodeValue());
			Expr rightexpr = NodeValue.makeNode(range.getRightNodeValue());
			Expr boolexpr = NodeValue.makeBoolean(range.isRangeWithMax());

			exprlist.add(objectexpr);
			exprlist.add(leftexpr);
			exprlist.add(rightexpr);
			exprlist.add(boolexpr);

			Expr regexfunction = new E_Function(
					"java:facets.query.functions.myregexstr", exprlist);

			return new ElementFilter(regexfunction);

		}

		else if (facetvaluerange instanceof FacetValueStringNumberRange<?>) {

			FacetValueStringNumberRange<?> range = (FacetValueStringNumberRange<?>) facetvaluerange;
			ExprList exprlist = new ExprList();
			Expr objectexpr = new ExprVar(objectvar);
			Expr leftexpr = NodeValue.makeNode(range.getLeftNodeValue());
			Expr rightexpr = NodeValue.makeNode(range.getRightNodeValue());
			Expr boolexpr = NodeValue.makeBoolean(range.isRangeWithMax());

			exprlist.add(objectexpr);
			exprlist.add(leftexpr);
			exprlist.add(rightexpr);
			exprlist.add(boolexpr);

			Expr regexfunction = new E_Function(
					"java:facets.query.functions.myregextoint", exprlist);

			return new ElementFilter(regexfunction);

		}

		return null;

	}

	public ElementFilter getSingleElementFilter(Var varclsfctobjname,
			FacetValueRange facetvaluerange, Object value) {

		Var objectvar = varclsfctobjname;

		if (facetvaluerange instanceof FacetValueDateRange) {
			FacetValueDateRange range = (FacetValueDateRange) facetvaluerange;
			ExprList exprlist = new ExprList();
			Expr objectexpr = new ExprVar(objectvar);
			HelperFunctions helper = HelperFunctions.getInstance();
			LiteralLabel label = range.getLeftNodeValue().getLiteral();
			Expr singlevalue = NodeValue.makeNode(helper.format((Date) value),
					label.language(), label.getDatatypeURI());

			exprlist.add(objectexpr);
			exprlist.add(singlevalue);

			Expr regexfunction = new E_Function(
					"java:facets.query.functions.mysingledate", exprlist);

			return new ElementFilter(regexfunction);
		}

		else if (facetvaluerange instanceof FacetValueStringRange) {
			FacetValueStringRange range = (FacetValueStringRange) facetvaluerange;
			ExprList exprlist = new ExprList();
			Expr objectexpr = new ExprVar(objectvar);
			LiteralLabel label = range.getLeftNodeValue().getLiteral();

			Expr singlevalue = NodeValue.makeNode(value.toString(),
					label.language(), label.getDatatypeURI());

			exprlist.add(objectexpr);
			exprlist.add(singlevalue);

			Expr regexfunction = new E_Function(
					"java:facets.query.functions.mysinglestr", exprlist);

			return new ElementFilter(regexfunction);

		}

		else if (facetvaluerange instanceof FacetValueStringNumberRange<?>) {

			FacetValueStringNumberRange<?> range = (FacetValueStringNumberRange<?>) facetvaluerange;
			ExprList exprlist = new ExprList();
			Expr objectexpr = new ExprVar(objectvar);
			LiteralLabel label = range.getLeftNodeValue().getLiteral();
			Expr singlevalue = NodeValue.makeNode(value.toString(),
					label.language(), label.getDatatypeURI());

			exprlist.add(objectexpr);
			exprlist.add(singlevalue);

			Expr regexfunction = new E_Function(
					"java:facets.query.functions.mysingleint", exprlist);

			return new ElementFilter(regexfunction);

		}

		return null;

	}

	public ElementFilter getFilter(Var p, String tofilter) {

		Expr str = new E_Str(new ExprVar(p));
		Expr string1 = NodeValue.makeString(tofilter);
		ExprList exprlist = new ExprList();
		exprlist.add(string1);

		// E_Regex regex = new E_Regex(str, tofilter, "i");

		Expr notoneof = new E_NotOneOf(str, exprlist);

		// ElementFilter efilter = new ElementFilter(regex);

		ElementFilter efilter = new ElementFilter(notoneof);

		return efilter;

	}
}
