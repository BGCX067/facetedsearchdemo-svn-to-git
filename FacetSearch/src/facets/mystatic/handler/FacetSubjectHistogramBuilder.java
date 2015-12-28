package facets.mystatic.handler;

import java.util.HashSet;
import java.util.Set;

import at.jku.rdfstats.RDFStatsConfiguration;
import at.jku.rdfstats.hist.GenericSingleBinHistogram;
import at.jku.rdfstats.hist.Histogram;
import at.jku.rdfstats.hist.URIHistogram;
import at.jku.rdfstats.hist.builder.GenericSingleBinHistogramBuilder;
import at.jku.rdfstats.hist.builder.HistogramBuilder;
import at.jku.rdfstats.hist.builder.HistogramBuilderException;
import at.jku.rdfstats.hist.builder.HistogramBuilderFactory;
import at.jku.rdfstats.hist.builder.URIHistogramBuilder;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.expr.NodeValue;

import facets.datatypes.ClassType;
import facets.datatypes.FacetSubjectBlankRange;
import facets.datatypes.FacetSubjectURIRange;
import facets.datatypes.FacetValueRange;
import facets.gui.components.controller.FacetSearchController;
import facets.gui.components.controller.QueryConstructionController;
import facets.myconstants.FacetConstants;

public class FacetSubjectHistogramBuilder {

	private static FacetSubjectHistogramBuilder fsubjecthistogrambuilder = null;

	private static ClassType currentclasstype;
	private static FacetSearchController mcontroller;
	private static String facetsubjecttype;
	private static String histogramClassName;
	private static Node currentfacet;
	private static String varclsname;

	private FacetSubjectHistogramBuilder(
			FacetSearchController controller, ClassType ct) {
		mcontroller = controller;
		currentclasstype = ct;
		histogramClassName = null;
		facetsubjecttype = null;
		currentfacet = null;
		varclsname = null;
	}

	public static FacetSubjectHistogramBuilder getInstance(
			FacetSearchController controller, ClassType ct) {
		if (fsubjecthistogrambuilder != null) {
			mcontroller = controller;
			currentclasstype = ct;
			histogramClassName = null;
			facetsubjecttype = null;
			currentfacet = null;
			varclsname = null;
			return fsubjecthistogrambuilder;
		}

		fsubjecthistogrambuilder = new FacetSubjectHistogramBuilder(controller,
				ct);
		return fsubjecthistogrambuilder;
	}

	public Set<FacetValueRange> getInwardFacetHistogramFacetValueRanges(
			Node facet, Node subjecttype) {

		currentfacet = facet;

		varclsname = currentclasstype.getVarClsName();

		if (subjecttype.isURI()) {
			facetsubjecttype = subjecttype.getURI();
		} else {
			facetsubjecttype = subjecttype.getLiteralLexicalForm();
		}

		histogramClassName = HistogramBuilderFactory.getBuilderClass(
				facetsubjecttype).getSimpleName();

		String query = mcontroller.getQueryConstructionController().getQueryContructor()
				.getClassAsObjectFacetValuesQuery(currentfacet.getURI(),
						currentclasstype.getVarClsName(),
						currentclasstype.getBaseQueryString());

		QueryExecution qexec = QueryExecutionFactory.create(query, mcontroller
				.getDataSetController().getDatasetInstance());

		ResultSet resultset = qexec.execSelect();
		Set<FacetValueRange> ranges = null;

		HistogramBuilder<?> histogrambuilder = buildHistogramForFacetSubjectValue(resultset);

		if (histogrambuilder instanceof URIHistogramBuilder) {

			URIHistogramBuilder builder = (URIHistogramBuilder) histogrambuilder;

			ranges = buildFacetSubjectRangeForURI(builder);

		} else if (histogrambuilder instanceof GenericSingleBinHistogram) {

			GenericSingleBinHistogramBuilder builder = (GenericSingleBinHistogramBuilder) histogrambuilder;

			ranges = buildFacetSubjectRangeForBlank(builder);
		}

		return ranges;
	}

	private Set<FacetValueRange> buildFacetSubjectRangeForURI(
			HistogramBuilder<String> histogrambuilder) {

		Set<FacetValueRange> facetranges = new HashSet<FacetValueRange>();

		URIHistogramBuilder builder = (URIHistogramBuilder) histogrambuilder;
			
		Histogram<String> histogram = histogrambuilder.getHistogram();
         
		
		URIHistogram urihistogram = (URIHistogram) histogram;
		int numBins = urihistogram.getNumBins();
		String labels[] = urihistogram.getLabels();

		Node llabel = NodeValue.makeString(labels[0]).asNode();
		Node rlabel = NodeValue.makeString(labels[numBins - 1]).asNode();

		FacetSubjectURIRange prange = new FacetSubjectURIRange(currentfacet,
				labels[0], labels[numBins - 1], llabel, rlabel, 0,
				histogrambuilder, currentclasstype);

		facetranges.add(prange);

		builder.makenull();
		
		return facetranges;

	}

	private Set<FacetValueRange> buildFacetSubjectRangeForBlank(
			GenericSingleBinHistogramBuilder histogrambuilder) {

		Set<FacetValueRange> ranges = new HashSet<FacetValueRange>();

		FacetSubjectBlankRange facetsubject = new FacetSubjectBlankRange(
				currentfacet, null, null, null, null, 0, histogrambuilder,
				currentclasstype);

		ranges.add(facetsubject);
		return ranges;

	}

	private HistogramBuilder<?> buildHistogramForFacetSubjectValue(
			ResultSet resultset) {

		HistogramBuilder<?> builder = null;

		if (histogramClassName.equals("URIHistogramBuilder")) {

			builder = new URIHistogramBuilder(
					RDFStatsConfiguration.getDefault(), facetsubjecttype,
					FacetConstants.PREFERED_URI_BINS);

			while (resultset.hasNext()) {

				QuerySolution qs = resultset.next();
				Node subjecturi = qs.get("subject").asNode();
				Node main = qs.get(varclsname).asNode();

				try {
					builder.addNodeValue(subjecturi, main); //-- original
					//builder.addNodeValue(main, subjecturi);

				} catch (HistogramBuilderException e) {
					e.printStackTrace();
				}
			}
		} else if (histogramClassName.equals("GenericSingleBinHistogram")) {

			builder = new GenericSingleBinHistogramBuilder(
					RDFStatsConfiguration.getDefault(),
					XSDDatatype.XSDstring.getURI(), FacetConstants.SINGLE_BIN);

			while (resultset.hasNext()) {

				QuerySolution qs = resultset.next();
				Node subjecturi = qs.get("subject").asNode();
				Node main = qs.get(varclsname).asNode();

				try {

					builder.addNodeValue(subjecturi, main);
					//builder.addNodeValue(main,subjecturi);
				
				} catch (HistogramBuilderException e) {

					e.printStackTrace();
				}

			}

		}

		return builder;
	}

}
