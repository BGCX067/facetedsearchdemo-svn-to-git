package facets.myconstants;

public class PrefixHelper {

	/*
	 * dont know how to get automatically from data set;
	 */
	public static String getPrefixString() {

		String prefix = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX dbpedia: <http://dbpedia.org/property/>"
				+ "PREFIX skos:  <http://www.w3.org/2004/02/skos/core#>"
				+ "PREFIX oddlinker: <http://data.linkedmdb.org/resource/oddlinker/>"
				+ "PREFIX db: <http://data.linkedmdb.org/resource/>"
				+ "PREFIX movie: <http://data.linkedmdb.org/resource/movie/>"
				+ "PREFIX map: <file:/C:/d2r-server-0.4/mapping.n3#>"
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
				+ "PREFIX d2r: <http://sites.wiwiss.fu-berlin.de/suhl/bizer/d2r-server/config.rdf#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
				+ "PREFIX dc: <http://purl.org/dc/terms/>"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX fn:  <java:facet.>";

		return prefix;
	}

}
