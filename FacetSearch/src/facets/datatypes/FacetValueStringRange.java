package facets.datatypes;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import java.util.Set;

import org.openjena.atlas.lib.MapUtils;

import at.jku.rdfstats.hist.OrderedStringHistogram;
import at.jku.rdfstats.hist.builder.OrderedStringHistogramBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.util.OneToManyMap;

public class FacetValueStringRange extends FacetHistogram<String> {

	final private Node left;
	final private Node right;
	final private Integer binNumber;
	final private String leftvalue;
	final private String rightvalue;
	private Set<Node> entities;
	private Double fventropy = null;
	private Integer totalSubjects = null;

	public FacetValueStringRange(Node facet, String rangeleftvalue,
			String rangerightvalue, Node left, Node right, Integer binNumber,
			OrderedStringHistogramBuilder facethistogram, ClassType ct) {
		super(facet, facethistogram, ct);
		this.left = left;
		this.right = right;
		this.binNumber = binNumber;
		this.leftvalue = rangeleftvalue;
		this.rightvalue = rangerightvalue;
		fventropy = null;
		totalSubjects = null;
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
		if (binNumber >= facethistogrambuilder.getHistogram().getNumBins() - 2)
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
		String enclose = isRangeWithMax() ? "]]" : "]";

		String range = "[" + left.getLiteralValue() + ";"
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
		FacetValueStringRange other = (FacetValueStringRange) obj;
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
		OneToManyMap<String, Node> fromhistogram = facethistogrambuilder
				.getObjectSubjectNodeMap();
		Map<Integer, Integer> subjectcount = facethistogrambuilder
				.getHashedSubjectNodeCountMap();
		int subjects = 0;

		for (Entry<String, Node> entry : fromhistogram.entrySet()) {

			String object = entry.getKey();

			if (isRangeWithMax()) {

				if(!leftvalue.equals(rightvalue))
				
				{
				if ((object.compareTo(leftvalue) >= 0)
						&& (object.compareTo(rightvalue) <= 0)) {
					entities.add(entry.getValue());
					subjects += subjectcount.get(entry.getValue().hashCode());
				}
				
				}
				else {
					
					if (object.startsWith(leftvalue))
					 {
						entities.add(entry.getValue());
						subjects += subjectcount.get(entry.getValue().hashCode());
					}	
					
				}

			} else {

				if ((object.compareTo(leftvalue) >= 0)
						&& (object.compareTo(rightvalue) <= 0)) {
					entities.add(entry.getValue());
					subjects += subjectcount.get(entry.getValue().hashCode());
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

	private Double getFacetValueRangeEntropy() {
		if (fventropy != null)
			return fventropy;

		OrderedStringHistogram histogram = (OrderedStringHistogram) facethistogram;
		float relfreq;
		double entropy = 0.0;
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
	public Map<?, Integer> getFacetValueRangeData() {

		Map<Node, Integer> rangefacetvalues = new TreeMap<Node, Integer>(
				new Comparator<Node>() {

					@Override
					public int compare(Node o1, Node o2) {

						if (o1.equals(o2))
							return 0;
						else if (o1.toString().compareTo(o2.toString()) > 0)
							return 1;
						else
							return -1;

					};
				});

		OrderedStringHistogramBuilder builder = (OrderedStringHistogramBuilder) facethistogrambuilder;

		OneToManyMap<String, Node> fromhistogram = builder
				.getCachedObjectValues();
		Map<Integer, Integer> count = builder.getHashedNodeKeyNodeValueCount();

		for (Entry<String, Node> entry : fromhistogram.entrySet()) {

			String object = entry.getKey();

			if (isRangeWithMax()) {
                
				if(!leftvalue.equals(rightvalue))
				
				{
				if ((object.compareTo(leftvalue) >= 0)
						&& (object.compareTo(rightvalue) <= 0)) {
					MapUtils.increment(rangefacetvalues, entry.getValue(),
							count.get(entry.getValue().hashCode()));

				}
				}
				else {

					if (object.startsWith(leftvalue)) {
						MapUtils.increment(rangefacetvalues, entry.getValue(),
								count.get(entry.getValue().hashCode()));

					}

					
				}

			} else {

				if ((object.compareTo(leftvalue) >= 0)
						&& (object.compareTo(rightvalue) <= 0)) {

					MapUtils.increment(rangefacetvalues, entry.getValue(),
							count.get(entry.getValue().hashCode()));

				}

			}

		}

		return rangefacetvalues;
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
