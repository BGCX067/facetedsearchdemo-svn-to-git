package facets.datatypes;

import java.util.ArrayDeque;
import java.util.Deque;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.util.NodeUtils;

public class ClassType {

	private String cname;
	private String basequery;
	private Deque<String> queries;
	private String varclassname;
	private Facets allextendedfacets;
	private ResultSet resultSet;

	public ClassType(String cn, ResultSet result, String varclsname) {
		cname = cn;
		queries = new ArrayDeque<String>();
		resultSet = result;
		varclassname = varclsname;
	}

	public String getVarClsName() {
		return varclassname;
	}

	public Facets getCurrentFacets() {
		return allextendedfacets;
	}

	public void setCuurentFacets(Facets currentfacets) {
		allextendedfacets = currentfacets;
	}

	public String getBaseQueryString() {
		return basequery;
	}

	public void setBaseQueryString(String query) {

		basequery = query;
		queries.addFirst(query);
	}

	public void setCurrentResultSet(ResultSet result) {
		resultSet = result;

	}

	public void addCurrentQuery(String q) {

		queries.addFirst(q);

	}

	public String getCurrentQuery() {

		return queries.getFirst();

	}

	public ResultSet getCurrentResultSet() {

		return resultSet;

	}

	public Integer getCurrentResultSetSize() {

		return ResultSetFactory.makeRewindable(resultSet).size();

	}

	public String getItsURIName() {

		return cname;

	}

	public String getItsLocalName() {
		return NodeUtils.asNode(cname).getLocalName();
	}

	@Override
	public String toString() {

		return varclassname + "(" + getCurrentResultSetSize() + ")";
	}

}
