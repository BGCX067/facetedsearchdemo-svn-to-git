package facets.gui.components.controller;

import facets.gui.components.models.ClassTypeHistoryDataModel;
import facets.mystatic.handler.BasicPatternHandler;
import facets.mystatic.handler.QueryConstructor;
import facets.mystatic.handler.VariableHandler;

public class QueryConstructionController {

	private static QueryConstructionController queryconstructor = null;

	private FacetSearchController maincontroller;

	private QueryConstructor query;

	private BasicPatternHandler basicpattern;

	private VariableHandler variable;

	private QueryConstructionController(FacetSearchController controller) {

		maincontroller = controller;
		query = QueryConstructor.getInstance(this);
		variable = VariableHandler.getInstance(this);
		basicpattern = BasicPatternHandler.getInstance(this);
	}

	public static QueryConstructionController getInstance(
			FacetSearchController controller) {

		if (queryconstructor != null)
			return queryconstructor;

		else {

			queryconstructor = new QueryConstructionController(controller);
			return queryconstructor;

		}

	}

	public QueryConstructor getQueryContructor() {

		return query;

	}

	public BasicPatternHandler getBasicPatternHandler() {

		return basicpattern;

	}

	public VariableHandler getVariableHandler() {

		return variable;
	}

	public void reset(){
		basicpattern.reset();
		variable.reset();
	
	}
//	public DataSetController getDataSetController() {
//
//		return maincontroller.getDataSetController();
//
//	}
//
	public ClassTypeHistoryDataModel getClassTypeHistoryDataModel() {

		return maincontroller.getClassTypeHistoryDataModel();

	}

}
