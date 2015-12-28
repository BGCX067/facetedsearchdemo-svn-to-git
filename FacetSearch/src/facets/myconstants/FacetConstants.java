package facets.myconstants;

import java.util.ArrayList;

public class FacetConstants {

	public final static int PREFERED_NUMBER_BINS = 10;

	public final static int PREFERED_DATE_BINS = 10;

	public final static int PREFERED_STRING_BINS = 10;

	public final static int PREFERED_URI_BINS = 10;

	public final static int SINGLE_BIN = 1;

	public final static int BOOLEAN_BIN = 2;
	
	public final static String DATE_HISTOGRAM_FORMAT = "yyyy"; //"yyyy-MM";

	public final static int PREFERED_SUGGESTION_COUNT = 10;

	public final static boolean DEBUG = false;

	public final static boolean FOR_DATE_STRING_CHECK = true;
	
	public final static boolean FOR_NUMBER_STRING_CHECK = false;
	
	public final static float QUANTITY_TO_CHECK = 0.00001f;
	
	public final static boolean TRY_CLEAN_THE_DATA = true;
	
	public final static String Var = "V";
	
	public final static ArrayList<String> facetexclude = new ArrayList<String>();
	static {
		facetexclude.add("http://data.linkedmdb.org/resource/movie/initial_release_date");
		facetexclude.add("http://purl.org/dc/terms/title");
		facetexclude.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	}

}
