package facets.datatypes;

import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;

public interface FacetValueRange {

	public Set<Node> getFacetSubjectEntitySet();

	public Node getFacetNode();

	public Map<?, Integer> getFacetValueRangeData();

	public Double getFacetValueRangeTotalScore();

	public ClassType getParentClassType();

	public boolean isEntityAsObject();

	public String writeDetailScoreOutput();

	public String writeShortScoreOutput();

	@Override
	public String toString();

}
