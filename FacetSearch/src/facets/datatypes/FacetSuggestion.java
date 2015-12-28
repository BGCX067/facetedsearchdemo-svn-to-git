package facets.datatypes;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class FacetSuggestion {

	private Collection<FacetValueRange> propertyvalue;

	public FacetSuggestion() {
		propertyvalue = new TreeSet<FacetValueRange>(new SuggestionComparator());
	}

	public void addSuggestion(FacetValueRange suggestion) {

		propertyvalue.add(suggestion);

	}

	public int size() {

		return propertyvalue.size();

	}

	public Iterator<FacetValueRange> getSuggestionIterator() {
		return propertyvalue.iterator();
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder(100);

		Iterator<FacetValueRange> iterator = propertyvalue.iterator();
		while (iterator.hasNext()) {

			sb.append(iterator.next().writeShortScoreOutput());

		}
		sb.append("\n");
		return sb.toString();
	}

	class SuggestionComparator implements Comparator<FacetValueRange> {

		@Override
		public int compare(FacetValueRange o1, FacetValueRange o2) {

			if (o1.getFacetValueRangeTotalScore() >= o2
					.getFacetValueRangeTotalScore())
				return -1;
			else if (o1.getFacetValueRangeTotalScore() < o2
					.getFacetValueRangeTotalScore())
				return 1;
			return 0;
		}

	}
}
