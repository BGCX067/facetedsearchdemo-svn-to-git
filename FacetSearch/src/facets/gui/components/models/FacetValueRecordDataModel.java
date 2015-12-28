package facets.gui.components.models;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import facets.datatypes.FacetValueNumberRange;
import facets.datatypes.FacetValueRange;
import facets.datatypes.FacetValueStringRange;
import facets.datatypes.FacetValueURIRange;
import facets.gui.components.controller.FacetSearchController;
import facets.mystatic.handler.BasicPatternHandler;

import facets.mystatic.handler.VariableHandler;

public class FacetValueRecordDataModel {

	private FacetSearchController facetsearchcontroller;

	private FacetValueRange currentfacetvaluerange = null;

	private Map<String, Triple> fctvalnametriple;

	private Map<String, ElementFilter> fctvalnamefilter;

	private String currentvarclsname = null;

	public FacetValueRecordDataModel(FacetSearchController controller) {

		facetsearchcontroller = controller;

		fctvalnametriple = new HashMap<String, Triple>();

		fctvalnamefilter = new HashMap<String, ElementFilter>();

	}

	public void setCurrentFacetValueRange(String varclsname,
			FacetValueRange facetvaluerange) {
		currentvarclsname = varclsname;
		currentfacetvaluerange = facetvaluerange;
	}

	public void reset() {

		currentfacetvaluerange = null;
		currentvarclsname = null;
		fctvalnametriple.clear();
		fctvalnamefilter.clear();
		        
	}

	public String performFacetValueIntervalSelection() {

		if (currentfacetvaluerange == null)
			return null;
		else {

			if (currentvarclsname != currentfacetvaluerange
					.getParentClassType().getVarClsName())
				return null;

			VariableHandler myh = facetsearchcontroller
					.getQueryConstructionController().getVariableHandler();
			BasicPatternHandler mybp = facetsearchcontroller
					.getQueryConstructionController().getBasicPatternHandler();

			Node facet = currentfacetvaluerange.getFacetNode();

			Var fvvar = myh.getFacetValueVariable(currentvarclsname,
					facet.getLocalName());

			ElementFilter filter = mybp.getElementFilter(fvvar,
					currentfacetvaluerange);

			Triple filtertriple = new Triple(Var.alloc(currentvarclsname),
					facet, fvvar);

			mybp.recordTripleElementFilterConstraint(currentvarclsname,
					fvvar.getName(), filtertriple, filter);

			fctvalnametriple.put(fvvar.getName(), filtertriple);

			fctvalnamefilter.put(fvvar.getName(), filter);

			return fvvar.getName();

		}

	}

	public String performFacetValueSpecificSelection(Object value) {
		if (currentfacetvaluerange == null)
			return null;
		else {
			if (currentvarclsname != currentfacetvaluerange
					.getParentClassType().getVarClsName())
				return null;

			VariableHandler myh = facetsearchcontroller
					.getQueryConstructionController().getVariableHandler();
			BasicPatternHandler mybp = facetsearchcontroller
					.getQueryConstructionController().getBasicPatternHandler();

			Node facet = currentfacetvaluerange.getFacetNode();

			Var fvvar = myh.getFacetValueVariable(currentvarclsname,
					facet.getLocalName());

			if (currentfacetvaluerange instanceof FacetValueNumberRange<?>) {

				FacetValueNumberRange<?> range = (FacetValueNumberRange<?>) currentfacetvaluerange;

				LiteralLabel label = range.getLeftNodeValue().getLiteral();

				Node specific = NodeValue.makeNode(value.toString(),
						label.language(), label.getDatatypeURI()).asNode();

				Triple specifictriple = new Triple(
						Var.alloc(currentvarclsname), facet, specific);

				mybp.recordSpecificTripleContraints(currentvarclsname,
						fvvar.getName(), specifictriple);

				fctvalnametriple.put(fvvar.getName(), specifictriple);

				if (fctvalnamefilter.containsKey(fvvar.getName()))
					fctvalnamefilter.remove(fvvar.getName());

				return fvvar.getName();
			} else if (currentfacetvaluerange instanceof FacetValueStringRange) {

				FacetValueStringRange range = (FacetValueStringRange) currentfacetvaluerange;

				Node literal = (Node) value;

				Triple specifictriple = new Triple(
						Var.alloc(currentvarclsname), facet, literal);

				mybp.recordSpecificTripleContraints(currentvarclsname,
						fvvar.getName(), specifictriple);

				fctvalnametriple.put(fvvar.getName(), specifictriple);

				if (fctvalnamefilter.containsKey(fvvar.getName()))
					fctvalnamefilter.remove(fvvar.getName());

				return fvvar.getName();

			}
			else if (currentfacetvaluerange instanceof FacetValueURIRange) {

				

				Node literal = (Node) value;

				Triple specifictriple = new Triple(
						Var.alloc(currentvarclsname), facet, literal);

				mybp.recordSpecificTripleContraints(currentvarclsname,
						fvvar.getName(), specifictriple);

				fctvalnametriple.put(fvvar.getName(), specifictriple);

				if (fctvalnamefilter.containsKey(fvvar.getName()))
					fctvalnamefilter.remove(fvvar.getName());

				return fvvar.getName();

			}
			
			else {

				ElementFilter filter = mybp.getSingleElementFilter(fvvar,
						currentfacetvaluerange, value);

				Triple filtertriple = new Triple(Var.alloc(currentvarclsname),
						facet, fvvar);

				mybp.recordTripleElementFilterConstraint(currentvarclsname,
						fvvar.getName(), filtertriple, filter);

				fctvalnametriple.put(fvvar.getName(), filtertriple);

				fctvalnamefilter.put(fvvar.getName(), filter);

				return fvvar.getName();
			}

		}
	}

	public String performFacetValueUnknownSelection() {

		if (currentfacetvaluerange == null)
			return null;
		else {
			if (currentvarclsname != currentfacetvaluerange
					.getParentClassType().getVarClsName())
				return null;

			VariableHandler myh = facetsearchcontroller
					.getQueryConstructionController().getVariableHandler();
			BasicPatternHandler mybp = facetsearchcontroller
					.getQueryConstructionController().getBasicPatternHandler();

			Node facet = currentfacetvaluerange.getFacetNode();

			Var fvuknwvar = myh.getFacetValueUnknownVariable(currentvarclsname,
					facet.getLocalName());

			Triple specifictriple = new Triple(Var.alloc(currentvarclsname),
					facet, fvuknwvar);

			mybp.recordTripleElementFilterConstraint(currentvarclsname,
					fvuknwvar.getName(), specifictriple, null);

			fctvalnametriple.put(fvuknwvar.getName(), specifictriple);

			if (fctvalnamefilter.containsKey(fvuknwvar.getName()))
				fctvalnamefilter.remove(fvuknwvar.getName());

			return fvuknwvar.getName();
		}

	}

	public String getStringRecordToDisplay(String fctvalname) {

		if (fctvalnametriple.containsKey(fctvalname)
				&& !fctvalnamefilter.containsKey(fctvalname))
			return fctvalnametriple.get(fctvalname).toString();

		else {

			if (fctvalnamefilter.containsKey(fctvalname))
				return fctvalnamefilter.get(fctvalname).getExpr().toString();

			else
				return null;
		}

	}

	public String removeRecordedFacetValue(String fctvalname) {

		fctvalnametriple.remove(fctvalname);
		fctvalnamefilter.remove(fctvalname);

		BasicPatternHandler mybp = facetsearchcontroller
				.getQueryConstructionController().getBasicPatternHandler();

		return mybp.removeFacetValueRecord(fctvalname);

	}

}
