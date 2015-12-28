package facets.gui.components.models;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;

import facets.datatypes.FacetIterator;
import facets.datatypes.FacetSuggestion;
import facets.datatypes.FacetValueRange;
import facets.datatypes.Facets;
import facets.gui.components.controller.FacetSearchController;
import facets.myconstants.FacetConstants;
import facets.myconstants.HelperFunctions;

public class FacetSuggestionDataModel {

	private final Facets facets;
	private final FacetSearchController facetsearchcontroller;

	private FacetIterator facetiterator;
	private ResultSet currentresultset;
	private Map<Integer, FacetSuggestion> suggestions;
	private int id = 0;
	private int counter = 0;
	private int lastdisplayed = 0;
	HelperFunctions helper = HelperFunctions.getInstance();
  
	private static boolean DEBUG = true;
    private boolean sorted = true; 
	private int method = 3;
	
	public FacetSuggestionDataModel(Facets po, FacetSearchController controller) {

		facets = po;
		facetsearchcontroller = controller;
		reset();
	}

	public boolean hasNextFacetSuggestion() {
         
		return facetiterator.hasNext();

	}

	public FacetSuggestion getPreviousFacetSuggestion() {

		if (suggestions.containsKey(--counter)) {
			lastdisplayed = counter;
			return suggestions.get(counter);
		}
		return null;
	}

	public FacetSuggestion getNextFacetSuggestion() {
		if (suggestions.containsKey(++counter)) {
			lastdisplayed = counter;
			return suggestions.get(counter);
		}

		return buildNextFacetSuggestion();
	}

	public FacetSuggestion getCurrentFacetSuggestion() {

		return suggestions.get(lastdisplayed);

	}

	private void reset() {
        if(sorted)  
		facetiterator = (FacetIterator) facets.sortedIterator();
        else
        facetiterator = (FacetIterator) facets.iterator();	
		
		currentresultset = facets.getClassType().getCurrentResultSet();
		suggestions = new HashMap<Integer, FacetSuggestion>();
		id = 0;
		counter = 0;
		lastdisplayed = 0;

	}

	private Set<Node> convertResultSettoSet(ResultSet rs) {
		ResultSetRewindable rewind = (ResultSetRewindable) rs;
		rewind.reset();
		Set<Node> inset = new HashSet<Node>();
		while (rewind.hasNext())
			inset.add(rewind.next().get(rewind.getResultVars().get(0)).asNode());
		return inset;
	}

	private FacetSuggestion buildNextFacetSuggestion() {

		Set<Node> uncoveredresults = convertResultSettoSet(currentresultset);
		Set<Node> coveredresults = new HashSet<Node>();
        
		FacetSuggestion newfacetsuggestion=null;;
		
		switch(method){
		case 1: newfacetsuggestion = 
				buildNextFacetSuggestionMethodOne(uncoveredresults, coveredresults);
		        break; 
		case 2: newfacetsuggestion =
				buildNextFacetSuggestionMethodTwo(uncoveredresults, coveredresults);	   
		        break;
		case 3: newfacetsuggestion = buildNextFacetSuggestionMethodThree(
				uncoveredresults, coveredresults);
		        break;
		}
		
//		FacetSuggestion newfacetsuggestion = buildNextFacetSuggestionMethodThree(
//				uncoveredresults, coveredresults);

//		FacetSuggestion newfacetsuggestion = buildNextFacetSuggestionMethodOne(
//				uncoveredresults, coveredresults);
		// FacetSuggestion newfacetsuggestion =
		// buildNextFacetSuggestionMethodTwo(uncoveredresults, coveredresults);

		suggestions.put(++id, newfacetsuggestion);
		counter = id;
		lastdisplayed = counter;
		return newfacetsuggestion;

	}

	private FacetSuggestion buildNextFacetSuggestionMethodOne(
			Set<Node> uncovered, Set<Node> covered) {

		/**
		 * 1
		 * 
		 * This sequence of steps , gets the facets sorted based on score, and
		 * in order to check the coverage of Result Set, it first picks the top
		 * most FacetValueRange and then checks next best possible covering
		 * FacetValueRange in the result set. These steps are iterated until all
		 * the results are covered or when the facet list is empty.
		 * 
		 * 
		 * However the problem here is it is very expensive, because after
		 * consider first FacetValueRange it needs to traverse through all the
		 * facets values ranges leading to calling subject entity query for all
		 * the facet values ranges even though if user is not interested in all
		 * facets.
		 */

		Set<Node> uncoveredresults = uncovered;
		Set<Node> coveredresults = covered;

		FacetSuggestion newfacetsuggestion = new FacetSuggestion();
		facetiterator.resetIterator();

		boolean consideringfirst = true;
		int localcounter = 0;
		int currentsize = ((ResultSetRewindable) currentresultset).size();

//		while (!uncoveredresults.isEmpty() && facetiterator.hasNext()
//				&& (coveredresults.size() < currentsize)
//				&& localcounter < FacetConstants.PREFERED_SUGGESTION_COUNT)
		while (facetiterator.hasNext()
					&& localcounter < FacetConstants.PREFERED_SUGGESTION_COUNT)
		{

			if (consideringfirst) {

				FacetValueRange facetFirst = facetiterator.next();

				Set<Node> entities = facetFirst.getFacetSubjectEntitySet();
				if (entities.size() <= 0)
					continue;

				coveredresults = helper.getUnion(coveredresults, entities);
				uncoveredresults = helper.getDifference(uncoveredresults,
						coveredresults);

				facetiterator.remove(facetFirst);
				facetiterator.resetIterator();

				localcounter++;
				newfacetsuggestion.addSuggestion(facetFirst);

				consideringfirst = false;
				continue;

			}

			FacetValueRange facet = getNextBestFacetValueRange(
					new HashSet<Node>(uncoveredresults), new HashSet<Node>(
							coveredresults));

			if (facet == null)
				break;

			coveredresults = helper.getUnion(coveredresults,
					facet.getFacetSubjectEntitySet());
			uncoveredresults = helper.getDifference(uncoveredresults,
					coveredresults);

			facetiterator.remove(facet);
			facetiterator.resetIterator();

			localcounter++;
			newfacetsuggestion.addSuggestion(facet);

			consideringfirst = true;

		}

		return newfacetsuggestion;

	}

	private FacetSuggestion buildNextFacetSuggestionMethodTwo(
			Set<Node> uncovered, Set<Node> covered) {
		/**
		 * 2:
		 * 
		 * This sequence of steps first gets the facets in sorted order, and
		 * then directly checks for first best possible facet value range, not
		 * giving importance to the first facet value ranges which where sorted
		 * based on the score.These steps are iterated until all the results are
		 * covered or when the facet list is empty.
		 * 
		 * However even this sequences of steps has to call class query for all
		 * facet value ranges making it slow, to get first suggestion.
		 * 
		 */
		Set<Node> uncoveredresults = uncovered;
		Set<Node> coveredresults = covered;

		FacetSuggestion newfacetsuggestion = new FacetSuggestion();
		facetiterator.resetIterator();
		int currentsize = ((ResultSetRewindable) currentresultset).size();
		int localcounter = 0;

//		while (!uncoveredresults.isEmpty() && facetiterator.hasNext()
//				&& (coveredresults.size() < currentsize)
//				&& localcounter < FacetConstants.PREFERED_SUGGESTION_COUNT) 
		while (facetiterator.hasNext()
				&& localcounter < FacetConstants.PREFERED_SUGGESTION_COUNT)
				{		
			FacetValueRange facet = getNextBestFacetValueRange(
					new HashSet<Node>(uncoveredresults), new HashSet<Node>(
							coveredresults));

			if (facet == null)
				break;

			coveredresults = helper.getUnion(coveredresults,
					facet.getFacetSubjectEntitySet());
			uncoveredresults = helper.getDifference(uncoveredresults,
					coveredresults);

			facetiterator.remove(facet);
			facetiterator.resetIterator();

			localcounter++;
			newfacetsuggestion.addSuggestion(facet);

		}

		return newfacetsuggestion;

	}

	private FacetSuggestion buildNextFacetSuggestionMethodThree(
			Set<Node> uncovered, Set<Node> covered) {

		/**
		 * 3:
		 * 
		 * This sequence of steps goes through facets in sorted order of their
		 * score, and checks for coverage of the result set and store the facet
		 * value ranges into the facet suggestion. These steps are iterated
		 * until all the results are covered or when the facet list is empty.
		 * 
		 * Or When you need only specific number of facets at each iteration,
		 * provide a parameter and stop the loading of facet value ranges into
		 * the facet suggestion when it reaches that number.
		 * 
		 */

		Set<Node> uncoveredresults = uncovered;
		Set<Node> coveredresults = covered;

		FacetSuggestion newfacetsuggestion = new FacetSuggestion();
		facetiterator.resetIterator();
		int currentsize = ((ResultSetRewindable) currentresultset).size();
		int localcounter = 0;

//		while (!uncoveredresults.isEmpty() && facetiterator.hasNext()
//				&& (coveredresults.size() < currentsize)
//				&& localcounter < FacetConstants.PREFERED_SUGGESTION_COUNT) 
		while (facetiterator.hasNext()
				&& localcounter < FacetConstants.PREFERED_SUGGESTION_COUNT)		
		{
			FacetValueRange facet = facetiterator.next();

			Set<Node> entities = facet.getFacetSubjectEntitySet();
			if (entities.size() <= 0) {
				continue;
			}

			coveredresults = helper.getUnion(coveredresults, entities);
			uncoveredresults = helper.getDifference(uncoveredresults,
					coveredresults);

			newfacetsuggestion.addSuggestion(facet);
			facetiterator.remove();

			facetiterator.resetIterator();

			localcounter++;
		}

		return newfacetsuggestion;

	}

	private FacetValueRange getNextBestFacetValueRange(
			Set<Node> uncoveredresults, Set<Node> coveredresults) {

		double thisscore = 0d;
		double uncoveredresultscore = 0d;
		double coveredresultscore = 0d;
		double facetscore = 0d;
		double overlapscore = 0d;

		Map<Double, FacetValueRange> facetscores = new TreeMap<Double, FacetValueRange>(
				new Comparator<Double>() {
					@Override
					public int compare(Double o1, Double o2) {
						if (o1 == o2)
							return 0;
						else if (o1 > o2)
							return -1;
						else
							return 1;
					}
				});

		while (facetiterator.hasNext()) {

			FacetValueRange currentfacet = facetiterator.next();

			Set<Node> entities = currentfacet.getFacetSubjectEntitySet();

			if (entities.size() <= 0)
				continue;

			uncoveredresultscore = (double) helper.getDifference(
					uncoveredresults, entities).size() + 1;
			coveredresultscore = (double) helper.getDifference(entities,
					coveredresults).size() + 1;
			facetscore = currentfacet.getFacetValueRangeTotalScore();
			overlapscore = coveredresultscore / uncoveredresultscore;

			thisscore = facetscore * overlapscore;

			facetscores.put(thisscore, currentfacet);

		}
		FacetValueRange topfacet = null;
        if(!facetscores.isEmpty())
		topfacet = facetscores.entrySet().iterator().next().getValue(); 

//		FacetValueRange topfacet = null;
//		boolean assigned = false;
//		for (Entry<Double, FacetValueRange> entry : facetscores.entrySet()) {
//
//			if (!assigned) {
//				topfacet = entry.getValue();
//				assigned = true;
//
//			}
//			if (!DEBUG && assigned)
//				break;
//
//			System.out.println(entry.getValue().toString() + ":"
//					+ entry.getKey());
//		}

		return topfacet;

	}

}
