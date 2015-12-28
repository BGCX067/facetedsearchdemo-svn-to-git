package facets.mystatic.handler;

import facets.datatypes.ClassType;
import facets.gui.components.controller.QueryConstructionController;

public class EntitiesExtractor {

	private QueryConstructionController mcontroller;
	private ClassType classtype;

	private EntitiesExtractor(QueryConstructionController controller,
			ClassType ct) {

		mcontroller = controller;
		classtype = ct;

	}

	public static EntitiesExtractor getInstance(
			QueryConstructionController controller, ClassType ct) {

		return new EntitiesExtractor(controller, ct);

	}

	// public ResultSet getFacetValueRangeResultSet(FacetValueRange
	// facetvaluerange){
	//
	// QueryConstructor queryconstruct = mcontroller.getQueryContructor();
	//
	//
	// String query =
	// queryconstruct.getFacetValueRangeCurrentResultSetQuery(classtype.getVarClsName(),
	// classtype.getBaseQueryString(), facetvaluerange);
	//
	// QueryExecution qexec = QueryExecutionFactory.create(query,
	// mcontroller.getDataSetController().getDatasetInstance());
	//
	// ResultSet resultset = qexec.execSelect();
	//
	// ResultSetRewindable resultsetrewind =
	// ResultSetFactory.makeRewindable(resultset);
	//
	//
	// qexec.close();
	//
	//
	// return resultsetrewind;
	// }

}
