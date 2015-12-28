package facets.datatypes;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.openjena.atlas.lib.MapUtils;

import at.jku.rdfstats.hist.DateHistogram;
import at.jku.rdfstats.hist.builder.DateHistogramBuilder;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.util.OneToManyMap;

public class FacetValueDateRange extends FacetHistogram<Date> {

	final private Node left;
	final private Node right;
	final private Integer binNumber;
	final private Date leftvalue;
	final private Date rightvalue;
	private Set<Node> entities;
	private Double fventropy = null;
	private Integer totalSubjects = null;

	public FacetValueDateRange(Node facet, Date rangeleftvalue,
			Date rangerightvalue, Node left, Node right, Integer binNumber,
			DateHistogramBuilder facethistogram, ClassType ct) {

		super(facet, facethistogram, ct);

		this.leftvalue = rangeleftvalue;
		this.rightvalue = rangerightvalue;
		this.left = left;
		this.right = right;
		this.binNumber = binNumber;
		fventropy = null;

	}

	public Node getLeftNodeValue() {
		return left;
	}

	public Node getRightNodeValue() {
		return right;
	}

	public boolean isRangeWithMin() {

		if (binNumber == 0)
			return true;
		else
			return false;
	}

	public boolean isRangeWithMax() {
		if (binNumber == facethistogrambuilder.getHistogram().getNumBins() - 1)
			return true;
		else
			return false;
	}

	@Override
	public String toString() {

		String count = null;
		if (entities != null)
			count = "(" + entities.size() + ")";
		else
			count = "(--)";

		String enclose = (isRangeWithMax()) ? "]]" : "]";

		String range = " [ " + left.getLiteralValue() + ";"
				+ right.getLiteralValue() + enclose;

		return facet.getLocalName() + count + range;
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
		FacetValueDateRange other = (FacetValueDateRange) obj;
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
		return (double) U_e_i / facetparent.getCurrentResultSetSize();
		
		//		return (double) totalSubjects / facetparent.getCurrentResultSetSize();

	}

	@Override
	public Set<Node> getFacetSubjectEntitySet() {
		if (entities != null && totalSubjects != null) {
			return entities;
		}
		entities = new HashSet<Node>();
		OneToManyMap<Date, Node> fromhistogram = facethistogrambuilder
				.getObjectSubjectNodeMap();
		Map<Integer, Integer> subjectcount = facethistogrambuilder
				.getHashedSubjectNodeCountMap();

		int subjects = 0;

		for (java.util.Map.Entry<Date, Node> entry : fromhistogram.entrySet()) {

			long entrytime = entry.getKey().getTime();
			Node entryvalue = entry.getValue();
           //Conditions not accurate 
			if (isRangeWithMax()) {

				if (entrytime >= leftvalue.getTime()
						&& entrytime <= rightvalue.getTime()) {
					entities.add(entryvalue);
					subjects += subjectcount.get(entryvalue.hashCode());
				}

			} else {

				if (entrytime >= leftvalue.getTime()
						&& entrytime <= rightvalue.getTime()) {
					entities.add(entryvalue);
					subjects += subjectcount.get(entryvalue.hashCode());
				}

			}

		}
		totalSubjects = subjects;
		return entities;
	}

	@Override
	public ClassType getParentClassType() {

		return facetparent;
	}

	@Override
	public Node getFacetNode() {

		return facet;
	}

	@Override
	public Map<?, Integer> getFacetValueRangeData() {

		OneToManyMap<Date, Node> fromhistogram = facethistogrambuilder
				.getObjectSubjectNodeMap();
		Map<Integer, Integer> count = facethistogrambuilder
				.getHashedSubjectNodeCountMap();

		Map<Date, Integer> rangefacetvalues = new TreeMap<Date, Integer>();

		for (Entry<Date, Node> entry : fromhistogram.entrySet()) {

			long entrytime = entry.getKey().getTime();

			if (isRangeWithMax()) {

				if (entrytime >= leftvalue.getTime()
						&& entrytime <= rightvalue.getTime()) {

					MapUtils.increment(rangefacetvalues, entry.getKey());
			
				}

			} else {

				if (entrytime >= leftvalue.getTime()
						&& entrytime <= rightvalue.getTime()) {

					MapUtils.increment(rangefacetvalues, entry.getKey());
				}

			}

		}

		return rangefacetvalues;
	}

	private Double getFacetValueRangeEntropy() {

		if (fventropy != null)
			return fventropy;

		DateHistogram histogram = (DateHistogram) facethistogram;

		float relfreq;
		double entropy = 0.0d;

		relfreq = histogram.getBinQuantityRelative(histogram
				.getBinIndex(leftvalue));

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
		Double score = (getDiscountingLamdaFactor() * getFacetSubjectEntitySetScore()) 
			      + ((1 - getDiscountingLamdaFactor()) * getHistogramEntropy());

//Double score = getHistogramWeigthedEntropyScore();

return score;
}

//private Double getHistogramWeigthedEntropyScore(){
//
//Double score = getDiscountingLamdaFactor() * getHistogramJaccardWeight() +
//(1.0 - getDiscountingLamdaFactor()) * getHistogramEntropy() * getFacetSubjectEntitySetScore();
//return score;
//}

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
