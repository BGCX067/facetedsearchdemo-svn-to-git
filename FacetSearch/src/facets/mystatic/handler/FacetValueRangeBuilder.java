package facets.mystatic.handler;

import java.util.Date;
import java.util.HashSet;

import java.util.Set;

import at.jku.rdfstats.ParseException;
import at.jku.rdfstats.RDFStatsConfiguration;
import at.jku.rdfstats.hist.BooleanHistogram;
import at.jku.rdfstats.hist.DateHistogram;
import at.jku.rdfstats.hist.DoubleHistogram;
import at.jku.rdfstats.hist.FloatHistogram;
import at.jku.rdfstats.hist.Histogram;
import at.jku.rdfstats.hist.IntegerHistogram;
import at.jku.rdfstats.hist.LongHistogram;
import at.jku.rdfstats.hist.OrderedStringHistogram;
import at.jku.rdfstats.hist.RDF2JavaMapper;
import at.jku.rdfstats.hist.URIHistogram;
import at.jku.rdfstats.hist.builder.BooleanHistogramBuilder;
import at.jku.rdfstats.hist.builder.DateHistogramBuilder;
import at.jku.rdfstats.hist.builder.DoubleHistogramBuilder;
import at.jku.rdfstats.hist.builder.FloatHistogramBuilder;
import at.jku.rdfstats.hist.builder.GenericSingleBinHistogramBuilder;
import at.jku.rdfstats.hist.builder.HistogramBuilder;
import at.jku.rdfstats.hist.builder.HistogramBuilderException;
import at.jku.rdfstats.hist.builder.HistogramBuilderFactory;
import at.jku.rdfstats.hist.builder.IntegerHistogramBuilder;
import at.jku.rdfstats.hist.builder.LongHistogramBuilder;
import at.jku.rdfstats.hist.builder.OrderedStringHistogramBuilder;
import at.jku.rdfstats.hist.builder.URIHistogramBuilder;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.expr.NodeValue;

import facets.datatypes.ClassType;
import facets.datatypes.FacetValueBlankRange;
import facets.datatypes.FacetValueDateRange;
import facets.datatypes.FacetValueNumberRange;
import facets.datatypes.FacetValueRange;
import facets.datatypes.FacetValueStringNumberRange;
import facets.datatypes.FacetValueStringRange;
import facets.datatypes.FacetValueURIRange;
import facets.gui.components.controller.FacetSearchController;
import facets.gui.components.controller.QueryConstructionController;

import facets.myconstants.DateUtil;
import facets.myconstants.FacetConstants;
import facets.myconstants.HelperFunctions;
import facets.myconstants.NumberUtil;

public class FacetValueRangeBuilder {

	private static FacetValueRangeBuilder fvaluehistogramrangebuilder = null;

	private static String facetvaluetype;
	private static String histogramClassName;
	private static Node currentfacet;

	//private static LiteralLabel facetvalueliterallabel;
	private static ClassType currentclasstype;
	private static FacetSearchController mcontroller;
	private static String varclsname = null;
	private static String fctvarname = null;

	private FacetValueRangeBuilder(FacetSearchController controller,
			ClassType ct) {
		mcontroller = controller;
		currentclasstype = ct;
		facetvaluetype = null;
		histogramClassName = null;
		currentfacet = null;
		varclsname = null;
		fctvarname = null;
	//	facetvalueliterallabel = null;

	}

	public static FacetValueRangeBuilder getInstance(
			FacetSearchController controller, ClassType ct) {
		if (fvaluehistogramrangebuilder != null) {
			mcontroller = controller;
			currentclasstype = ct;
			facetvaluetype = null;
			histogramClassName = null;
			currentfacet = null;
			varclsname = null;
			fctvarname = null;
		//	facetvalueliterallabel = null;

			return fvaluehistogramrangebuilder;
		}

		fvaluehistogramrangebuilder = new FacetValueRangeBuilder(controller, ct);
		return fvaluehistogramrangebuilder;
	}

	public Set<FacetValueRange> getFacetHistogramFacetValueRanges(Node facet,
			Node objectType) {

		currentfacet = facet;

		QueryConstructor constructor = mcontroller.getQueryConstructionController().getQueryContructor();

		varclsname = currentclasstype.getVarClsName();

		if (objectType.isURI()) {
			facetvaluetype = objectType.getURI();
		} else {
			facetvaluetype = objectType.getLiteralLexicalForm();
		}

		histogramClassName = HistogramBuilderFactory.getBuilderClass(
				facetvaluetype).getSimpleName();

		String query = constructor.getClassAsSubjectFacetValuesQuery(
				currentfacet.getURI(), currentclasstype.getVarClsName(),
				currentclasstype.getBaseQueryString());

		QueryExecution qexec = QueryExecutionFactory.create(query, mcontroller
				.getDataSetController().getDatasetInstance());

		ResultSet resultset = qexec.execSelect();
		Set<FacetValueRange> ranges = null;
        fctvarname = resultset.getResultVars().get(0);
		
		HistogramBuilder<?> histogrambuilder = buildHistogramForFacetValue(resultset);

		
		if (histogrambuilder instanceof OrderedStringHistogramBuilder) {
			OrderedStringHistogramBuilder builder = (OrderedStringHistogramBuilder) histogrambuilder;

			ranges = buildFacetValueRangeForString(builder);

		} else if (histogrambuilder instanceof URIHistogramBuilder) {

			URIHistogramBuilder builder = (URIHistogramBuilder) histogrambuilder;

			ranges = buildFacetValueRangeForURI(builder);

		} else if (histogrambuilder instanceof GenericSingleBinHistogramBuilder) {

			GenericSingleBinHistogramBuilder builder = (GenericSingleBinHistogramBuilder) histogrambuilder;

			ranges = buildFacetValueRangeForBlank(builder);
		} else {
			ranges = buildFacetValueRangeForLiteral(histogrambuilder);

		}

		qexec.close();
           
		mcontroller.getDataSetController().performDeleteIllegalNode();
		
		return ranges;

	}

	private Set<FacetValueRange> buildFacetValueRangeForBlank(
			GenericSingleBinHistogramBuilder histogrambuilder) {

		Set<FacetValueRange> ranges = new HashSet<FacetValueRange>();

		FacetValueRange facetvalue = new FacetValueBlankRange(currentfacet,
				null, null, null, null, 0, histogrambuilder, currentclasstype);

		ranges.add(facetvalue);
		return ranges;

	}

	private Set<FacetValueRange> buildFacetValueRangeForString(
			OrderedStringHistogramBuilder histogrambuilder) {

		Set<FacetValueRange> facetranges = new HashSet<FacetValueRange>();

		Histogram<String> histogram = histogrambuilder.getHistogram();

		OrderedStringHistogram ostrhistogram = (OrderedStringHistogram) histogram;
		OrderedStringHistogramBuilder ostrhistogrambuilder = histogrambuilder;

		int numBins = ostrhistogram.getNumBins();
		String[] labels = ostrhistogram.getLabels();

		String rangestart = labels[0];

		String rangeend = rangestart;

		if (numBins == 1) {
            
			if(ostrhistogram.getBinQuantity(ostrhistogram.getBinIndex(labels[0])) > 0){
			
			Node llabel = NodeValue.makeString(rangestart).asNode();
			Node rlabel = NodeValue.makeString(rangeend).asNode();

			FacetValueStringRange prange = new FacetValueStringRange(
					currentfacet, labels[0], labels[0], llabel, rlabel, 0,
					histogrambuilder, currentclasstype);

			facetranges.add(prange);
		    
			}
		} else {
			for (int i = 0; i < numBins - 1; i++) {
			   
				if(ostrhistogram.getBinQuantity(ostrhistogram.getBinIndex(labels[i])) > 0){
				
				FacetValueStringRange prange;
                
				Node llabel = NodeValue.makeString(labels[i]).asNode();
				Node rlabel = NodeValue.makeString(labels[i + 1]).asNode();
				prange = new FacetValueStringRange(currentfacet, labels[i],
						labels[i + 1], llabel, rlabel, i, ostrhistogrambuilder,
						currentclasstype);

				facetranges.add(prange);
				} 
			}

		}

		return facetranges;

	}

	private Set<FacetValueRange> buildFacetValueRangeForURI(
			HistogramBuilder<String> histogrambuilder) {

		Set<FacetValueRange> facetranges = new HashSet<FacetValueRange>();

		Histogram<String> histogram = histogrambuilder.getHistogram();

		URIHistogram urihistogram = (URIHistogram) histogram;
		int numBins = urihistogram.getNumBins();
		String labels[] = urihistogram.getLabels();
        
		if(urihistogram.getBinQuantity(urihistogram.getBinIndex(labels[0])) > 0){
		Node llabel = NodeValue.makeString(labels[0]).asNode();
		Node rlabel = NodeValue.makeString(labels[numBins - 1]).asNode();

		FacetValueURIRange prange = new FacetValueURIRange(currentfacet,
				labels[0], labels[numBins - 1], llabel, rlabel, 0,
				histogrambuilder, currentclasstype);

		facetranges.add(prange);
		}
		return facetranges;

//		String rangestart = labels[0];
//
//		String rangeend = rangestart;
//
//		if (numBins == 1) {
//            
//			if(urihistogram.getBinQuantity(urihistogram.getBinIndex(labels[0])) > 0){
//			
//			Node llabel = NodeValue.makeString(rangestart).asNode();
//			Node rlabel = NodeValue.makeString(rangeend).asNode();
//
//			FacetValueURIRange prange = new FacetValueURIRange(
//					currentfacet, labels[0], labels[0], llabel, rlabel, 0,
//					histogrambuilder, currentclasstype);
//
//			facetranges.add(prange);
//		    
//			}
//		} else {
//			for (int i = 0; i < numBins - 1; i++) {
//			   
//				if(urihistogram.getBinQuantity(urihistogram.getBinIndex(labels[i])) > 0){
//				
//				FacetValueURIRange prange;
//                
//				Node llabel = NodeValue.makeString(labels[i]).asNode();
//				Node rlabel = NodeValue.makeString(labels[i + 1]).asNode();
//				prange = new FacetValueURIRange(currentfacet, labels[i],
//						labels[i + 1], llabel, rlabel, i, histogrambuilder,
//						currentclasstype);
//
//				facetranges.add(prange);
//				} 
//			}
//
//		}
//
//		return facetranges;

	
	
	
	}

	private Set<FacetValueRange> buildFacetValueRangeForLiteral(
			HistogramBuilder<?> histogrambuilder) {

		Set<FacetValueRange> facetranges = new HashSet<FacetValueRange>();

		Histogram<?> histogram = histogrambuilder.getHistogram();

		//String datatype = facetvalueliterallabel.getDatatypeURI();

		String datatype = facetvaluetype;

		if (histogram instanceof DateHistogram) {

			DateHistogram datehistogram = (DateHistogram) histogram;
			DateHistogramBuilder datehistogrambuilder = (DateHistogramBuilder) histogrambuilder;

			Date rangestart = datehistogram.getMin();

			HelperFunctions helper = HelperFunctions.getInstance();

			double binwidth = Math.ceil(datehistogram.getBinWidth());

			int numBins = datehistogram.getNumBins();

			for (int i = 0; i < numBins; i++) {

				Date rangeend = new Date(rangestart.getTime() + (long) binwidth);

				int binindex = datehistogram.getBinIndex(rangestart);
				if (datehistogram.getBinQuantity(binindex) > 0) {

					Node left = NodeValue.makeNode(helper.format(rangestart),
							null).asNode();

					Node right = NodeValue.makeNode(helper.format(rangeend),
							null).asNode();

					FacetValueDateRange prange = new FacetValueDateRange(
							currentfacet, rangestart, rangeend, left, right, i,
							datehistogrambuilder, currentclasstype);

					facetranges.add(prange);

				}

				rangestart = rangeend;
			}

			return facetranges;

		}

		else if (histogram instanceof IntegerHistogram) {

			IntegerHistogram inthistogram = (IntegerHistogram) histogram;
			IntegerHistogramBuilder inthistogrambuilder = (IntegerHistogramBuilder) histogrambuilder;

			Integer min = inthistogram.getMin();

			double binwidth = Math.ceil(inthistogram.getBinWidth());
			
			int numBins = inthistogram.getNumBins();
			Integer rangestart = min;

			for (int i = 0; i < numBins; i++) {

				Integer rangeend = rangestart + (int) binwidth;

				if (inthistogram.getBinQuantity(inthistogram
						.getBinIndex(rangestart)) > 0) {

					if (datatype != null) {
						
						Node left = NodeValue
								.makeInteger(rangestart.toString()).asNode();
						Node right = NodeValue.makeInteger(rangeend.toString())
								.asNode();
						FacetValueNumberRange<Integer> prange = new FacetValueNumberRange<Integer>(
								currentfacet, rangestart, rangeend, left,
								right, i, inthistogrambuilder, currentclasstype);
						facetranges.add(prange);
					} else {
						Node left = NodeValue.makeNode(rangestart.toString(),
								null).asNode();
						Node right = NodeValue.makeNode(rangeend.toString(),
								null).asNode();

						FacetValueStringNumberRange<Integer> prange = new FacetValueStringNumberRange<Integer>(
								currentfacet, rangestart, rangeend, left,
								right, i, inthistogrambuilder, currentclasstype);
						facetranges.add(prange);
					}
				}

				rangestart = rangeend;
			}
			return facetranges;
		} else if (histogram instanceof LongHistogram) {

			LongHistogram longhistogram = (LongHistogram) histogram;
			LongHistogramBuilder longhistogrambuilder = (LongHistogramBuilder) histogrambuilder;

			Long min = longhistogram.getMin();

			double binwidth = Math.ceil(longhistogram.getBinWidth());
			
			int numBins = longhistogram.getNumBins();
			Long rangestart = min;

			for (int i = 0; i < numBins; i++) {
				Long rangeend = rangestart + (long) binwidth;

				if (longhistogram.getBinQuantity(longhistogram
						.getBinIndex(rangestart)) > 0) {
					
					//TODO:  REMOVE DATA TYPE 
					Node left = NodeValue.makeNode(rangestart.toString(), null,
							datatype).asNode();
					Node right = NodeValue.makeNode(rangeend.toString(), null,
							datatype).asNode();

					FacetValueNumberRange<Long> prange = new FacetValueNumberRange<Long>(
							currentfacet, rangestart, rangeend, left, right, i,
							longhistogrambuilder, currentclasstype);

					facetranges.add(prange);
				}
				rangestart = rangeend;
			}

			return facetranges;
		} else if (histogram instanceof DoubleHistogram) {

			DoubleHistogram doublehistogram = (DoubleHistogram) histogram;
			DoubleHistogramBuilder doublehistogrambuilder = (DoubleHistogramBuilder) histogrambuilder;

			Double min = doublehistogram.getMin();

			double binwidth = Math.ceil(doublehistogram.getBinWidth());
			
			int numBins = doublehistogram.getNumBins();
			Double rangestart = min;

			for (int i = 0; i < numBins; i++) {

				Double rangeend = rangestart + binwidth;

				if (doublehistogram.getBinQuantity(doublehistogram
						.getBinIndex(rangestart)) > 0) {

					Node left = NodeValue.makeDouble(rangestart).asNode();

					Node right = NodeValue.makeDouble(rangeend).asNode();

					FacetValueNumberRange<Double> prange = new FacetValueNumberRange<Double>(
							currentfacet, rangestart, rangeend, left, right, i,
							doublehistogrambuilder, currentclasstype);

					facetranges.add(prange);
				}
				rangestart = rangeend;
			}
			return facetranges;
		} else if (histogram instanceof FloatHistogram) {

			FloatHistogram floathistogram = (FloatHistogram) histogram;
			FloatHistogramBuilder floathistogrambuilder = (FloatHistogramBuilder) histogrambuilder;

			Float min = floathistogram.getMin();

			double binwidth = Math.ceil(floathistogram.getBinWidth());
			
			int numBins = floathistogram.getNumBins();
			Float rangestart = min;

			for (int i = 0; i < numBins; i++) {

				Float rangeend = rangestart +(float) binwidth;
				if (floathistogram.getBinQuantity(floathistogram
						.getBinIndex(rangestart)) > 0) {
					Node left = NodeValue.makeFloat(rangestart).asNode();
					Node right = NodeValue.makeFloat(rangeend).asNode();
					FacetValueNumberRange<Float> prange = new FacetValueNumberRange<Float>(
							currentfacet, rangestart, rangeend, left, right, i,
							floathistogrambuilder, currentclasstype);

					facetranges.add(prange);

				}
				rangestart = rangeend;
			}
			return facetranges;
		}

		return facetranges;
	}

	private HistogramBuilder<?> buildHistogramForFacetValue(ResultSet resultset) {

		HistogramBuilder<?> builder = null;
		

		if (histogramClassName.equals("DateHistogramBuilder")) {

			builder = performaddFacetDateValue(resultset, true);

		} else if (histogramClassName.equals("IntegerHistogramBuilder")) {

			builder = performaddFacetIntegerValue(resultset, true);

		} else if (histogramClassName.equals("OrderedStringHistogramBuilder")) {

			builder = performaddFacetStringValue(resultset);

		}

		else if (histogramClassName.equals("URIHistogramBuilder")) {

			builder = new URIHistogramBuilder(
					RDFStatsConfiguration.getDefault(), facetvaluetype,
					FacetConstants.PREFERED_URI_BINS);

			while (resultset.hasNext()) {

				QuerySolution qs = resultset.next();
				Node object = qs.get(fctvarname).asNode();
				Node subject = qs.get(varclsname).asNode();

				try {
					builder.addNodeValue(object, subject);

				} catch (HistogramBuilderException e) {
					e.printStackTrace();
				}
			}
		} else if (histogramClassName
				.equals("GenericSingleBinHistogramBuilder")) {

			builder = new GenericSingleBinHistogramBuilder(
					RDFStatsConfiguration.getDefault(),
					XSDDatatype.XSDstring.getURI(), FacetConstants.SINGLE_BIN);

			while (resultset.hasNext()) {

				QuerySolution qs = resultset.next();
				Node object = qs.get(fctvarname).asNode();
				Node subject = qs.get(varclsname).asNode();

				try {

					builder.addNodeValue(object, subject);
				} catch (HistogramBuilderException e) {

					e.printStackTrace();
				}

			}

		} else if (histogramClassName.equals("LongHistogramBuilder")) {

			builder = new LongHistogramBuilder(
					RDFStatsConfiguration.getDefault(), facetvaluetype,
					FacetConstants.PREFERED_NUMBER_BINS);

			while (resultset.hasNext()) {

				QuerySolution qs = resultset.next();
				Node object = qs.get(fctvarname).asNode();
				Node subject = qs.get(varclsname).asNode();
				/*
				 * only considering for long type because integer and long as
				 * treated with long value.
				 */
//				if (facetvalueliterallabel == null)
//					facetvalueliterallabel = object.getLiteral();
				try {
					
					Long value = LongHistogram.parseNodeValueImpl(object);
     				//builder.addNodeValue(object, subject);
					((LongHistogramBuilder)builder).addValue(value, subject);
					
				} 
//				catch (HistogramBuilderException e) {
//					e.printStackTrace();
//				}
				catch(ParseException e){
					System.out.println(object.getLiteralValue().toString()+": Cannot Be Parsed To Long");	
				    			
					mcontroller.getDataSetController().addToupdateDelete(object);
					
				}
			}

		}

		else if (histogramClassName.equals("FloatHistogramBuilder")) {

			builder = new FloatHistogramBuilder(
					RDFStatsConfiguration.getDefault(), facetvaluetype,
					FacetConstants.PREFERED_NUMBER_BINS);

			while (resultset.hasNext()) {

				QuerySolution qs = resultset.next();
				Node object = qs.get(fctvarname).asNode();
				Node subject = qs.get(varclsname).asNode();
//				if (facetvalueliterallabel == null)
//					facetvalueliterallabel = object.getLiteral();
				try {
					
					Float value = FloatHistogram.parseNodeValueImpl(object);
					
					//builder.addNodeValue(object, subject);
                   ((FloatHistogramBuilder)builder).addValue(value, subject);
	               
				}
//				catch (HistogramBuilderException e) {
//					e.printStackTrace();
//				}
			    catch(ParseException e){
			    	System.out.println(object.getLiteralValue().toString()+": Cannot Be Parsed To Float");
			    	mcontroller.getDataSetController().addToupdateDelete(object);
			    	
			    }
			}
		} else if (histogramClassName.equals("DoubleHistogramBuilder")) {

			builder = new DoubleHistogramBuilder(
					RDFStatsConfiguration.getDefault(), facetvaluetype,
					FacetConstants.PREFERED_NUMBER_BINS);

			while (resultset.hasNext()) {

				QuerySolution qs = resultset.next();
				Node object = qs.get(fctvarname).asNode();
				Node subject = qs.get(varclsname).asNode();
//				if (facetvalueliterallabel == null)
//					facetvalueliterallabel = object.getLiteral();
				try {
					
					Double value = DoubleHistogram.parseNodeValueImpl(object);
					
					//builder.addNodeValue(object, subject);
					((DoubleHistogramBuilder)builder).addValue(value, subject);

				} 
//				catch (HistogramBuilderException e) {
//					e.printStackTrace();
//				}
				catch(ParseException e){
					System.out.println(object.getLiteralValue().toString()+": Cannot Be Parsed To Double");
					mcontroller.getDataSetController().addToupdateDelete(object);
					
				}
			}

		}
		else if (histogramClassName.equals("BooleanHistogramBuilder")) {

			builder = new BooleanHistogramBuilder(
					RDFStatsConfiguration.getDefault(), facetvaluetype,
					FacetConstants.BOOLEAN_BIN);

			while (resultset.hasNext()) {

				QuerySolution qs = resultset.next();
				Node object = qs.get(fctvarname).asNode();
				Node subject = qs.get(varclsname).asNode();
//				if (facetvalueliterallabel == null)
//					facetvalueliterallabel = object.getLiteral();
				try {
					boolean value = BooleanHistogram.parseNodeValueImpl(object);
					//builder.addNodeValue(object, subject);
                    ((BooleanHistogramBuilder)builder).addValue(value, subject);
                    
				} 
//				catch (HistogramBuilderException e) {
//					e.printStackTrace();
//				}
				catch(ParseException e){
					
					System.out.println(object.getLiteralValue().toString()+": Cannot Be Parsed To Boolean");
					mcontroller.getDataSetController().addToupdateDelete(object);
				}
			
			}

		}

		if(builder == null){
			
			
			System.out.println("EXCEPTION:SOMETHING WENT WRONG IN FACET VALUE HISTOGRAM BUILDER");
			
			
		}
		
		return builder;
	}

	private DateHistogramBuilder performaddFacetDateValue(ResultSet resultset,
			boolean hasDataType) {
		DateHistogramBuilder builder = null;

		if (!hasDataType) {
			builder = new DateHistogramBuilder(
					RDFStatsConfiguration.getDefault(),
					XSDDatatype.XSDdate.getURI(),
					FacetConstants.PREFERED_DATE_BINS);
		} else {
			builder = new DateHistogramBuilder(
					RDFStatsConfiguration.getDefault(), facetvaluetype,
					FacetConstants.PREFERED_DATE_BINS);
		}

		HelperFunctions helper = HelperFunctions.getInstance();
		 

		while (resultset.hasNext()) {

			QuerySolution qs = resultset.next();
			Node object = qs.get(fctvarname).asNode();
			Node subject = qs.get(varclsname).asNode();
//			if (facetvalueliterallabel == null)
//				facetvalueliterallabel = object.getLiteral();
			try {

				 
				Date date = DateUtil.parse(object.getLiteralValue().toString());

 				Date newdate = helper.parse(helper.format(date));

				builder.addValue(newdate, subject);

			} catch (java.text.ParseException e) {

				 System.out.println("DateFormate Not Recognized and will be Ignored : "+object.getLiteralValue().toString());
			}
			catch(DatatypeFormatException e){
				
				System.out.println("Illegal DateFormate: "+object.toString());
				mcontroller.getDataSetController().addToupdateDelete(object);
				
			}
		}

		return builder;

	}

	private IntegerHistogramBuilder performaddFacetIntegerValue(
			ResultSet resultset, boolean hasDataTypeURI) {

		IntegerHistogramBuilder builder = null;

		if (!hasDataTypeURI) {
			builder = new IntegerHistogramBuilder(
					RDFStatsConfiguration.getDefault(),
					XSDDatatype.XSDint.getURI(),
					FacetConstants.PREFERED_NUMBER_BINS);
		} else {
			builder = new IntegerHistogramBuilder(
					RDFStatsConfiguration.getDefault(), facetvaluetype,
					FacetConstants.PREFERED_NUMBER_BINS);
		}

		while (resultset.hasNext()) {

			QuerySolution qs = resultset.next();
			Node object = qs.get(fctvarname).asNode();
			Node subject = qs.get(varclsname).asNode();
//			if (facetvalueliterallabel == null)
//				facetvalueliterallabel = object.getLiteral();
            try{
			Integer parsedToint = Double.valueOf(
					object.getLiteralValue().toString()).intValue();
			builder.addValue(parsedToint, subject);

            }
            catch(NumberFormatException exception){
            	
            System.out.println(object.getLiteralValue().toString()+": Cannot Be Parsed To Integer");	
            mcontroller.getDataSetController().addToupdateDelete(object);
            }
		}
		return builder;

	}

	private HistogramBuilder<?> performaddFacetStringValue(ResultSet resultset) {

		OrderedStringHistogramBuilder builder = null;

		ResultSetRewindable result = ResultSetFactory.makeRewindable(resultset);

		if(FacetConstants.FOR_DATE_STRING_CHECK){
			
		    if(checkForStringToBeDataTypes(result))	
			  return performaddFacetDateValue(result, false);		
		}
		else if(FacetConstants.FOR_NUMBER_STRING_CHECK){
			
			if(checkForStringToBeNumberTypes(result)){
				return performaddFacetIntegerValue(result, false);	
			}
		}
				
		
		builder = new OrderedStringHistogramBuilder(
				RDFStatsConfiguration.getDefault(), facetvaluetype,
				FacetConstants.PREFERED_STRING_BINS);

		result.reset();

		while (result.hasNext()) {

			QuerySolution qs = result.next();
			Node object = qs.get(fctvarname).asNode();
			Node subject = qs.get(varclsname).asNode();
			try {
				builder.addNodeValue(object, subject);
			} catch (HistogramBuilderException e) {
				e.printStackTrace();
			}
		}
		return builder;

	
//		if (result.hasNext()) {
//
//			QuerySolution s = result.next();
//			Node obj = s.get("object").asNode();
//
////			if (facetvalueliterallabel == null)
////				facetvalueliterallabel = obj.getLiteral();
//
//			Object parsednode = null;
//
//			try {
//				parsednode = RDF2JavaMapper.parseNodeValue(obj);
//			} catch (ParseException e1) {
//
//				e1.printStackTrace();
//			}
//
//			if (DateUtil.isValidDate((String) parsednode)) {
//
//				result.reset();
//
//				return performaddFacetDateValue(result, false);
//
//			} 
//				else if (NumberUtil.isNumeric((String) parsednode)) {
//
//				Object typeparsednode = NumberUtil
//						.getNumericType((String) parsednode);
//
//				if (typeparsednode instanceof Boolean) {
//					// TODO:handle it
//				} else {
//
//					result.reset();
//
//					return performaddFacetIntegerValue(result, false);
//				}
//
//			} 
//			else
//
//			{
//				builder = new OrderedStringHistogramBuilder(
//						RDFStatsConfiguration.getDefault(), facetvaluetype,
//						FacetConstants.PREFERED_STRING_BINS);
//
//				result.reset();
//
//				while (result.hasNext()) {
//
//					QuerySolution qs = result.next();
//					Node object = qs.get("object").asNode();
//					Node subject = qs.get(varclsname).asNode();
//					try {
//						builder.addNodeValue(object, subject);
//					} catch (HistogramBuilderException e) {
//						e.printStackTrace();
//					}
//				}
//
//				return builder;
//			}
//
//		}
//		return builder;

	}

	/*
	 * STUPID CHECK .. WORSE CASE IS WHEN IT HAPPENS TO CHECK ALL THE RESULT SET SIZE
	 * THIS WAS JUST DONE BECAUSE OF DIFFERENCE BETWEEN DATASETS
	 * 
	 * LinkedMDB
	 * DBPedia
	 * 
	 * BETTER CASE IS TO JUST TREAT THE VALUES AS STRING .. AND BUILD ORDERED STRING HISTOGRAMS
	 * 
	 */
	private boolean checkForStringToBeDataTypes(ResultSetRewindable result) {
		
		int size = result.size();
		
		int quantity_to_check = (int) ( size * FacetConstants.QUANTITY_TO_CHECK); 
		
		int counter = -1;
		
		int iTiSDate = -1;
		
				
		while(result.hasNext()){
			
			QuerySolution s = result.next();
			Node obj = s.get(fctvarname).asNode();

			if (DateUtil.isValidDate((String)obj.getLiteralValue())){
						iTiSDate++;
			}
			
			counter++;
			if(counter>= quantity_to_check){
				break;
			}
			
		}
		result.reset();
		if(iTiSDate >= counter) return true;
		else return false;
		
	}

	
    private boolean checkForStringToBeNumberTypes(ResultSetRewindable result) {
		
		int size = result.size();
		
		int quantity_to_check = (int) ( size * FacetConstants.QUANTITY_TO_CHECK); 
		
		int counter = -1;
		
		int iTiSNumber = -1;
		
				
		while(result.hasNext()){
			
			QuerySolution s = result.next();
			Node obj = s.get(fctvarname).asNode();

			if (NumberUtil.isNumeric((String) obj.getLiteralValue())){
						iTiSNumber++;
			}
			
			counter++;
			if(counter>= quantity_to_check){
				break;
			}
			
		}
		
		result.reset();
		if(iTiSNumber >= counter) return true;
		else return false;
		
	}
	
}
