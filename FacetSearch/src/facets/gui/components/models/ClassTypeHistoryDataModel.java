package facets.gui.components.models;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.sparql.core.Var;

import facets.gui.components.controller.FacetSearchController;

public class ClassTypeHistoryDataModel {

	private FacetSearchController facetsearchcontroller;

	private Set<String> currentclasshistory;
	private String currentvarclsname;

	public ClassTypeHistoryDataModel(FacetSearchController controller) {

		facetsearchcontroller = controller;

		currentclasshistory = new HashSet<String>();

	}

	public void reset(){
		currentclasshistory.clear();
		currentvarclsname = null;
	}
	
	public void setCurrentSearchingClass(String varclsname) {

		currentvarclsname = varclsname;

	}

	public String getCurrentRootSearchingClass() {
		return facetsearchcontroller.getQueryConstructionController()
				.getVariableHandler().getRootVariableClassName();

	}

	public String getCurrentSearchingClass() {

		return currentvarclsname;

	}

	public boolean addCurrentSearchedClass(String varclsname) {

		return currentclasshistory.add(varclsname);
	}

	public Iterator<String> getAllSearchedClassHistoryIterator() {

		return currentclasshistory.iterator();
	}

//	public void clearAllSearchedClassTypeHistoryDataModel() {
//
//		currentclasshistory.clear();
//		currentvarclsname = null;
//	}

	public boolean historyContains(String varclsname) {

		return currentclasshistory.contains(varclsname);
	}

	public boolean removeVarClsName(String varclsname) {
		return currentclasshistory.remove(varclsname);
	}

//	public boolean isItLeafVarClsname(Set<Var> allvarclsname) {
//
//		Iterator<Var> variables = allvarclsname.iterator();
//		boolean isleaf = true;
//		String varclsname = null;
//		/**
//		 * go through all the base query pattern variables and if it contains
//		 * all the variables from the history panel thinking that its a leaf
//		 * then or else it is not.
//		 */
//
//		while (variables.hasNext()) {
//			varclsname = variables.next().getName();
//
//			if (!currentclasshistory.contains(varclsname))
//				isleaf = false;
//
//		}
//
//		return isleaf;
//
//	}

}
