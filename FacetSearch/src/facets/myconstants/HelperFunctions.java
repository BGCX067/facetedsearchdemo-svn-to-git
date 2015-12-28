package facets.myconstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;

public class HelperFunctions {
	private static HelperFunctions helper = null;

	private SimpleDateFormat simple;

	private HelperFunctions() {

		simple = new SimpleDateFormat(FacetConstants.DATE_HISTOGRAM_FORMAT);
	}

	public static HelperFunctions getInstance() {
		if (helper != null)
			return helper;
		helper = new HelperFunctions();
		return helper;

	}

	public Set<Node> getIntersection(Set<Node> pv1, Set<Node> pv2) {

		Set<Node> intersection = new HashSet<Node>(pv1);

		intersection.retainAll(pv2);

		return intersection;
	}

	public Set<Node> getIntersection(List<Set<Node>> propvalue) {

		Set<Node> intersection = new HashSet<Node>(propvalue.get(0));

		Iterator<Set<Node>> itr = propvalue.iterator();

		while (itr.hasNext()) {

			intersection.retainAll(itr.next());
		}

		return intersection;
	}

	public Set<Node> getUnion(Set<Node> pv1, Set<Node> pv2) {

		Set<Node> union = new HashSet<Node>(pv1);
		union.addAll(pv2);

		return union;
	}

	public Set<Node> getUnion(List<Set<Node>> propvalue) {

		Set<Node> union = new HashSet<Node>(propvalue.get(0));

		Iterator<Set<Node>> itr = propvalue.iterator();

		while (itr.hasNext()) {

			union.addAll(itr.next());
		}

		return union;
	}

	public Set<Node> getDifference(Set<Node> pv1, Set<Node> pv2) {

		Set<Node> difference = new HashSet<Node>(pv1);

		difference.removeAll(pv2);

		return difference;
	}

	public float overlapMeasure(Set<Node> pv1, Set<Node> pv2) {

		return getIntersection(pv1, pv2).size() / getUnion(pv1, pv2).size();

	}

	public Date parse(String parseddate) throws ParseException {

		return simple.parse(parseddate);
	}

	public String format(Date parseddate) {

		return simple.format(parseddate);

	}

}
