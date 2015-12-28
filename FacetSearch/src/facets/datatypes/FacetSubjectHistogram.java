package facets.datatypes;

import at.jku.rdfstats.hist.GenericSingleBinHistogram;
import at.jku.rdfstats.hist.Histogram;
import at.jku.rdfstats.hist.URIHistogram;
import at.jku.rdfstats.hist.builder.HistogramBuilder;

import com.hp.hpl.jena.graph.Node;

public abstract class FacetSubjectHistogram<NATIVE> implements FacetValueRange {

	protected final HistogramBuilder<NATIVE> facethistogrambuilder;
	protected final Node facet;
	protected final ClassType facetparent;
	protected final Histogram<NATIVE> facethistogram;
	protected Double hJaccardWeight = null;
	protected Double hEntropy = null;
	 protected Double hWeight = null;
	// protected Double discounting = null;
	protected Double lamda = null;

	public FacetSubjectHistogram(Node f,
			HistogramBuilder<NATIVE> histogrambuilder, ClassType ct) {
		facet = f;
		facethistogrambuilder = histogrambuilder;
		facetparent = ct;
		facethistogram = histogrambuilder.getHistogram();
		hJaccardWeight = null;

		hEntropy = null;
		hWeight = null;
		// discounting = null;
		lamda = null;
	}

	protected Double getHistogramJaccardWeight() {

		if (hJaccardWeight != null)
			return hJaccardWeight;

		int N = facetparent.getCurrentResultSetSize();
		int n = facethistogrambuilder.getHistogram().getDistinctValues();
		hJaccardWeight = Math
				.log((Math.abs((N - n)) + 1.0) / (1.0 + N));

		return hJaccardWeight;
	}

	
	 protected Double getHistogramWeight() {
	 if (hWeight != null)
	 return hWeight;
	
	 int N = facetparent.getCurrentResultSetSize();
	 int n = facethistogrambuilder.getHistogram().getDistinctValues();
	
	 hWeight = Math.log(n / (double)N );
	
	 return hWeight;
	
	 }
	//
	// protected Double getDiscountingFactor() {
	// if (discounting != null)
	// return discounting;
	// int N = facethistogrambuilder.getHistogram().getTotalValues();
	// int n = facethistogrambuilder.getHistogram().getDistinctValues();
	//
	// discounting = (N - n) / (double) (N + n);
	//
	// return discounting;
	// }

	protected Double getDiscountingLamdaFactor() {
		if (lamda != null)
			return lamda;
		//int N = facetparent.getCurrentResultSetSize();
		int N = facethistogrambuilder.getHistogram().getTotalValues();
		int n = facethistogrambuilder.getHistogram().getDistinctValues();

		lamda = ((double) n / N);
         
		//lamda = 0.2d;
		
		return lamda;

	}

	protected Double getHistogramEntropy() {

		if (hEntropy != null)
			return hEntropy;

		double entropy = 0.0d;

		if (facethistogram instanceof GenericSingleBinHistogram) {

			GenericSingleBinHistogram histogram = (GenericSingleBinHistogram) facethistogram;

			float relfreq = histogram.getBinQuantityRelative(0);
			;

			if (relfreq > 0.0)
				entropy = relfreq * Math.log(1.0 / relfreq);

			return entropy;

		} else if (facethistogram instanceof URIHistogram) {

			URIHistogram histogram = (URIHistogram) facethistogram;

			int numBins = histogram.getNumBins();

			String[] labels = histogram.getLabels();

			float relfreq;

			for (int i = 0; i < numBins; i++) {

				relfreq = histogram.getBinQuantityRelative(histogram
						.getBinIndex(labels[i]));

				if (relfreq <= 0.0)
					continue;

				entropy += relfreq * Math.log(1.0 / relfreq);

			}

		}

		hEntropy = entropy;
		return hEntropy;

	}

	protected Double getHistogramWeigthedEntropyScore() {

		return (getDiscountingLamdaFactor() * getHistogramJaccardWeight())
				+ (1.0 - getDiscountingLamdaFactor()) * getHistogramEntropy();
		
	//	return getHistogramEntropy();

	}

}
