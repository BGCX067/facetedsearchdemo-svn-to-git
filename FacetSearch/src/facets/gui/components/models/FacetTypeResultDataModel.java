package facets.gui.components.models;

import java.util.HashMap;
import java.util.Map;

import facets.datatypes.Facets;
import facets.gui.components.controller.FacetSearchController;

public class FacetTypeResultDataModel {
	private FacetSearchController facetsearchcontroller;
	private Map<String, Facets> classfacetstore;
	private Map<String, FacetSuggestionDataModel> classfacetsuggestiongenerator;

	public FacetTypeResultDataModel(FacetSearchController controller) {

		facetsearchcontroller = controller;
		classfacetstore = new HashMap<String, Facets>();
		classfacetsuggestiongenerator = new HashMap<String, FacetSuggestionDataModel>();
	}

	public void loadFacetTypeResultDataModel(String varclsname, Facets facets) {

		classfacetstore.put(varclsname, facets);
		FacetSuggestionDataModel sg = new FacetSuggestionDataModel(facets,
				facetsearchcontroller);
		classfacetsuggestiongenerator.put(varclsname, sg);

	}

	public FacetSuggestionDataModel getSuggestionGenerator(String varclsname) {

		return classfacetsuggestiongenerator.get(varclsname);

	}

	public void reset(){
		classfacetstore.clear();
		classfacetsuggestiongenerator.clear();
		
	}
	
}
