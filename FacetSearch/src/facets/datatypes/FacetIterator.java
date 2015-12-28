package facets.datatypes;

import java.util.Iterator;
import java.util.List;

public class FacetIterator implements Iterator<FacetValueRange> {

	private final List<FacetValueRange> sortedfacets;
	private int currentIndex;

	public FacetIterator(List<FacetValueRange> facets) {
		sortedfacets = facets;
		currentIndex = 0;
	}

	public void remove(FacetValueRange toremove) {
		currentIndex = sortedfacets.indexOf(toremove);
		sortedfacets.remove(currentIndex--);
	}

	@Override
	public boolean hasNext() {
		if (currentIndex >= sortedfacets.size()) {
			return false;
		} else {
			return true;
		}
	}

	public void resetIterator() {
		currentIndex = 0;
	}

	public int currentIndexValue() {
		return currentIndex;
	}

	@Override
	public FacetValueRange next() {
		return sortedfacets.get(currentIndex++);
	}

	@Override
	public void remove() {
		sortedfacets.remove(--currentIndex);
	}

}
