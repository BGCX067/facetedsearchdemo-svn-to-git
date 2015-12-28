package facets.datatypes;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import at.jku.rdfstats.hist.GenericSingleBinHistogram;
import at.jku.rdfstats.hist.builder.HistogramBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.util.OneToManyMap;

public class FacetValueBlankRange extends FacetHistogram<Object> {

	final private Node left;
	final private Node right;
	final private Object leftvalue;
	final private Object rightvalue;
	private Set<Node> entities;
	private Double fventropy = null;
	private Integer totalSubjects = null;

	public FacetValueBlankRange(Node facet, Object rangeleftvalue,
			Object rangerightvalue, Node left, Node right, Integer binNumber,
			HistogramBuilder<Object> facethistogram, ClassType ct) {

		super(facet, facethistogram, ct);

		this.left = left;
		this.right = right;
		this.leftvalue = rangeleftvalue;
		this.rightvalue = rangerightvalue;
		fventropy = null;
		totalSubjects = null;
	}

	@Override
	public ClassType getParentClassType() {

		return facetparent;
	}

	@Override
	public String toString() {
		String count = null;
		if (entities != null)
			count = "(" + entities.size() + ")";
		else
			count = "(--)";

		return facet.getLocalName() + count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facet == null) ? 0 : facet.hashCode());
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result
				+ ((leftvalue == null) ? 0 : leftvalue.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		result = prime * result
				+ ((rightvalue == null) ? 0 : rightvalue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FacetValueBlankRange other = (FacetValueBlankRange) obj;
		if (facet == null) {
			if (other.facet != null)
				return false;
		} else if (!facet.equals(other.facet))
			return false;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (leftvalue == null) {
			if (other.leftvalue != null)
				return false;
		} else if (!leftvalue.equals(other.leftvalue))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		if (rightvalue == null) {
			if (other.rightvalue != null)
				return false;
		} else if (!rightvalue.equals(other.rightvalue))
			return false;
		return true;
	}

	protected double getFacetSubjectEntitySetScore() {
		int U_e_i = getFacetSubjectEntitySet().size();

		//return (double) si / totalSubjects;
		//return (double) si / facetparent.getCurrentResultSetSize();

		return (double) U_e_i / facetparent.getCurrentResultSetSize();
		
		//return (double) totalSubjects / facetparent.getCurrentResultSetSize();
	}

	@Override
	public Set<Node> getFacetSubjectEntitySet() {

		if (entities != null && totalSubjects != null)
			return entities;

		OneToManyMap<Object, Node> fromHistogram = facethistogrambuilder
				.getObjectSubjectNodeMap();
		entities = (Set<Node>) fromHistogram.values();
		Collection<Integer> valuecount = facethistogrambuilder
				.getHashedSubjectNodeCountMap().values();
		int subjects = 0;
		Iterator<Integer> subjectcount = valuecount.iterator();
		while (subjectcount.hasNext()) {
			subjects += subjectcount.next();
		}
		totalSubjects = subjects;
		return entities;

	}

	@Override
	public Node getFacetNode() {
		return facet;
	}

	private Double getFacetValueRangeEntropy() {

		if (fventropy != null)
			return fventropy;

		double entropy = 0.0d;
		GenericSingleBinHistogram histogram = (GenericSingleBinHistogram) facethistogram;

		float relfreq = histogram.getBinQuantityRelative(0);
		;

		if (relfreq != 0.0)
			entropy = relfreq * Math.log(1.0 / relfreq);

		fventropy = entropy;

		return fventropy;

	}

	@Override
	public Double getFacetValueRangeTotalScore() {

		// Double score = getHistogramWeight() * getHistogramEntropy() *
		// getFacetSubjectEntitySetScore();

		// Double score = getHistogramJaccardWeight() * getHistogramEntropy() *
		// getFacetSubjectEntitySetScore();

		// Double normalize = Math.log(facethistogram.getTotalValues());

		// Double score = getDiscountingFactor() *
		// (getHistogramEntropy()/normalize) + ((double)1.0 -
		// getDiscountingFactor()) * getFacetSubjectEntitySetScore();

		// Double score = getDiscountingFactor() * (
		// (getHistogramEntropy()/normalize) + getFacetSubjectEntitySetScore()
		// );

		// Double score = getDiscountingFactor() *
		// (getHistogramEntropy()/normalize) * getFacetSubjectEntitySetScore() ;

		// Double score = getDiscountingFactor2() * getHistogramEntropy() *
		// getFacetSubjectEntitySetScore();

		// Double score = getFacetValueRangeEntropy();

		// Double score = (( getDiscountingFactor2() *
		// getHistogramJaccardWeight() ) + (1.0 - getDiscountingFactor2()) *
		// (getHistogramEntropy()/normalize) ) *
		// getFacetSubjectEntitySetScore();

//		Double score = getHistogramWeigthedEntropyScore()
//		* getFacetSubjectEntitySetScore();

       //Double score = getHistogramWeigthedEntropyScore();

		Double score = (getDiscountingLamdaFactor() * getFacetSubjectEntitySetScore()) 
			      + ((1 - getDiscountingLamdaFactor()) * getHistogramEntropy());
		
return score;
}

//private Double getHistogramWeigthedEntropyScore(){
//
//Double score = getDiscountingLamdaFactor() * getHistogramJaccardWeight() +
//(1.0 - getDiscountingLamdaFactor()) * getHistogramEntropy() * getFacetSubjectEntitySetScore();
//return score;
//}

	@Override
	public Map<?, Integer> getFacetValueRangeData() {
		return null;
	}

	@Override
	public String writeShortScoreOutput() {

		StringBuilder sb = new StringBuilder();

		sb.append("\n").append(facetparent.toString()).append("/").append("->")
				.append(toString()).append("/")
				.append(getFacetValueRangeTotalScore()).append("\n");

		return sb.toString();

	}

	@Override
	public String writeDetailScoreOutput() {

		StringBuilder sb = new StringBuilder(300);
		String CAMMA = "; ";
		String NLINE = "\n";
		String LINE = "\n--------------------------------------------------------------------";

		int idx = facethistogram.getBinIndex(leftvalue);

		sb.append(LINE)
				.append(writeShortScoreOutput())
				.append("bins:" + facethistogram.getNumBins())
				.append(CAMMA)
				.append("Idx i:" + idx)
				.append(CAMMA)
				.append("binFreq:" + facethistogram.getBinQuantity(idx))
				.append(CAMMA)
				.append("binFreqRelative:"
						+ facethistogram.getBinQuantityRelative(idx))
				.append(CAMMA)
				.append(NLINE)
				.append("Total FV:" + facethistogram.getTotalValues())
				.append(CAMMA)
				.append("Unique FV:" + facethistogram.getDistinctValues())
				.append(CAMMA)
				.append(NLINE)
				.append("Total_S_i:" + totalSubjects)
				.append(CAMMA)
				.append("Unique_S_i:" + getFacetSubjectEntitySet().size())
				.append(CAMMA)
				.append(NLINE)
				.append("Lamda:" + getDiscountingLamdaFactor())
				.append(CAMMA)
				.append("weight_JW:" + getHistogramJaccardWeight())
				.append(CAMMA)
				.append("weight_W:"+ getHistogramWeight())
				.append(CAMMA)
				.append(NLINE)
				.append("entropy_f:" + getHistogramEntropy())
				.append(CAMMA)
				.append("weighted_entropy:"
						+ getHistogramWeigthedEntropyScore()).append(CAMMA)
				.append("Cover_fv_i:" + getFacetSubjectEntitySetScore())
				.append(CAMMA).append(NLINE)
				.append("Score:" + getFacetValueRangeTotalScore());

		return sb.toString();

	}

	@Override
	public boolean isEntityAsObject() {

		return false;
	}

}
