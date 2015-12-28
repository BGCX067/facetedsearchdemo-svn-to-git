package facets.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Facets {

	private final Set<FacetValueRange> originalfacetSet;

	private final ClassType currentclasstype;

	public Facets(ClassType ct) {

		originalfacetSet = new HashSet<FacetValueRange>();
		currentclasstype = ct;
	}

	public int facetsOriginalSize() {

		return originalfacetSet.size();
	}

	public ClassType getClassType() {

		return currentclasstype;

	}

	public void addFacetValueRange(FacetValueRange pv) {

		originalfacetSet.add(pv);

	}

	public void addallFacetValueRanges(Set<FacetValueRange> pvset) {

		originalfacetSet.addAll(pvset);

	}

	public Iterator<FacetValueRange> sortedIterator() {

		List<FacetValueRange> sortedfacets;

		sortedfacets = new ArrayList<FacetValueRange>(originalfacetSet);

		Collections.sort(sortedfacets, new FacetSortComparator());

		return new FacetIterator(sortedfacets);

	}

	public Iterator<FacetValueRange> iterator() {

		List<FacetValueRange> facets;

		facets = new ArrayList<FacetValueRange>(originalfacetSet);

		return new FacetIterator(facets);

	}

	@Override
	public String toString() {

		Iterator<FacetValueRange> iterator = sortedIterator();

		StringBuilder sb = new StringBuilder();

		while (iterator.hasNext())
			sb.append(iterator.next().writeDetailScoreOutput());

		return sb.toString();

	}

	class FacetSortComparator implements Comparator<FacetValueRange> {

		@Override
		public int compare(FacetValueRange o1, FacetValueRange o2) {

			if (o1.equals(o2))
				return 0;
			else if (o1.getFacetValueRangeTotalScore() > o2
					.getFacetValueRangeTotalScore())
				return -1;
			else if (o1.getFacetValueRangeTotalScore() < o2
					.getFacetValueRangeTotalScore())
				return 1;
			else
				return 0;
		}

	}

}
