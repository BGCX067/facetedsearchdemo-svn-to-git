package facets.gui.components.controller;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import com.hp.hpl.jena.query.ResultSetRewindable;

import facets.datatypes.FacetSubjectBlankRange;
import facets.datatypes.FacetSubjectURIRange;
import facets.datatypes.FacetSuggestion;
import facets.datatypes.FacetValueBlankRange;
import facets.datatypes.FacetValueRange;
import facets.datatypes.FacetValueStringRange;
import facets.datatypes.FacetValueURIRange;
import facets.datatypes.Facets;
import facets.gui.components.models.ClassTypeHistoryDataModel;
import facets.gui.components.models.ClassTypeResultDataModel;
import facets.gui.components.models.FacetTypeResultDataModel;
import facets.gui.components.models.FacetSuggestionDataModel;
import facets.gui.components.models.FacetValueRecordDataModel;
import facets.gui.components.models.QueryResultSetDataModel;
import facets.gui.components.view.FacetSearchView;
import facets.myconstants.HelperFunctions;

public class FacetSearchController {

	private static FacetSearchView mainmodelview;

	private DataSetController datasetHandler;

	private QueryConstructionController querycontroller;

	private static ClassTypeResultDataModel classtypedatamodel;

	private static ClassTypeHistoryDataModel classtypehistorymodel;

	private static FacetTypeResultDataModel facettypedatamodel;

	private static FacetValueRecordDataModel facetvaluedatamodel;

	private static QueryResultSetDataModel resultsetdatamodel;

	private Map<String, String> guivarclsname;

	private Map<Integer, FacetValueRange> tempguifacetnames;

	private Map<Integer, Object> tempguifacetvaluenames;

	private Map<String, String> tempguirecordfacetvaluenames;

	public FacetSearchController() {

		datasetHandler = DataSetController.getInstance(this);
		querycontroller = QueryConstructionController.getInstance(this);
		guivarclsname = new HashMap<String, String>();
		tempguifacetnames = new HashMap<Integer, FacetValueRange>();
		tempguifacetvaluenames = new HashMap<Integer, Object>();
		tempguirecordfacetvaluenames = new HashMap<String, String>();
	}

	private void controllerReset() {

		guivarclsname.clear();
		tempguifacetnames.clear();
		tempguifacetvaluenames.clear();
		tempguirecordfacetvaluenames.clear();
		querycontroller.reset();
		classtypedatamodel.reset();
		classtypehistorymodel.reset();
		facettypedatamodel.reset();
		facetvaluedatamodel.reset();

	}

	final public void registerClassTypeResultDataModel(
			ClassTypeResultDataModel classtypedatahandler) {

		classtypedatamodel = classtypedatahandler;

	}

	final public void registerClassTypeHistoryDataModel(
			ClassTypeHistoryDataModel classhistorydatamodel) {

		classtypehistorymodel = classhistorydatamodel;
	}

	final public void registerFacetTypeResultDataModel(
			FacetTypeResultDataModel facettypedatahandler) {
		facettypedatamodel = facettypedatahandler;
	}

	final public void registerFacetSearchView(FacetSearchView facetsearchview) {

		mainmodelview = facetsearchview;

	}

	final public void registerFacetValueRecordDataModel(
			FacetValueRecordDataModel facetvaluerangedatamodel) {

		facetvaluedatamodel = facetvaluerangedatamodel;

	}

	final public void registerQueryResultSetDataModel(
			QueryResultSetDataModel resultmodel) {

		resultsetdatamodel = resultmodel;

	}

	public DataSetController getDataSetController() {

		return datasetHandler;

	}

	public QueryConstructionController getQueryConstructionController() {

		return querycontroller;

	}

	public ClassTypeHistoryDataModel getClassTypeHistoryDataModel() {

		return classtypehistorymodel;

	}

	public void performDataFileUpload(File file) {

		datasetHandler.addSourceFile(file);

		List<String> entitytypes = classtypedatamodel
				.loadInitialClassesFromDataset();

		JComboBox combofill = mainmodelview.getTypeSelectionComboBox(true);

		combofill.setModel(new ListComboBoxModel<String>(entitytypes));
		combofill.setSelectedIndex(-1);
		combofill.setEnabled(true);
		combofill.setEditable(true);
		AutoCompleteDecorator.decorate(combofill);

	}

	/*
	 * this listener is called when the user performs selection from combo box.
	 */

	public void performComboBoxSelection(String string) {

		/*
		 * contruct class data types for selected type or todo: keyword
		 */

		

		//classtypehistorymodel.clearAllSearchedClassTypeHistoryDataModel();

		//facetvaluedatamodel.clearAllFacetValueRecords();

		controllerReset();

		List<String> varclsnames = classtypedatamodel
				.constructRootClassType(string);
		
		DefaultListModel emptymodel = new DefaultListModel();
        DefaultTableModel emptytable = new DefaultTableModel();
        
		mainmodelview.getFacetTypeList(true).setModel(emptymodel);
		mainmodelview.getFacetValueList(true).setModel(emptymodel);
		mainmodelview.getRecordedFacetValueList(true).setModel(emptymodel);
		mainmodelview.getCurrentQueryResultSetTable(true).setModel(emptytable);

		performUpdateClassTypeResultDataModelView(varclsnames);

		boolean updatehistorypath = classtypehistorymodel
				.addCurrentSearchedClass(varclsnames.get(0));

		classtypehistorymodel.setCurrentSearchingClass(varclsnames.get(0));

		if (updatehistorypath) {
			performUpdateClassHistoryPathPanel(varclsnames.get(0));
		}

	}

	private void performUpdateClassTypeResultDataModelView(
			List<String> newvarclsname) {

		// String rootvarclsname =
		// querycontroller.getVariableHandler().getRootVariableClassName();

		JList classlistfill = mainmodelview.getClassTypeList(true);

		DefaultListModel newmodel = new DefaultListModel();

		Iterator<String> itr = newvarclsname.iterator();

		while (itr.hasNext()) {

			String varclsname = itr.next();

			Integer size = classtypedatamodel
					.getClassTypeResultSetSize(varclsname);

			String varclsnamecount = varclsname + "(" + size + ")";

			guivarclsname.put(varclsnamecount, varclsname);

			newmodel.addElement(varclsnamecount);

		}

		classlistfill.setModel(newmodel);

		classlistfill.setSelectedIndex(-1);

		classlistfill.setForeground(Color.RED);

		DefaultListModel emptymodel = new DefaultListModel();

		// mainmodelview.getFacetTypeList(true).setModel(emptymodel);
		mainmodelview.getFacetValueList(true).setModel(emptymodel);

	}

	/*
	 * When user browses the class type selected from the combo box
	 */

	public void performClassTypeSelection(Integer selectedindx,
			String externalvarclsname) {
		/*
		 * asking class data model to generate all the facets related to that
		 * class type ranked , with literal values as histogram ranges
		 */

		DefaultListModel emptymodel = new DefaultListModel();

		mainmodelview.getFacetTypeList(true).setModel(emptymodel);
		mainmodelview.getFacetValueList(true).setModel(emptymodel);

		String varclsname = guivarclsname.get(externalvarclsname);

		boolean newness = classtypedatamodel
				.availableNewClassTypeFacetDetails(varclsname);

		
		if (newness) {

			Facets facets = classtypedatamodel
					.generateClassTypeFacetDetails(varclsname);

			facettypedatamodel.loadFacetTypeResultDataModel(varclsname, facets);
            
			System.out.println(facets.toString());
		}
          
		
		
		
		
		FacetSuggestionDataModel generator = facettypedatamodel
				.getSuggestionGenerator(varclsname);

		FacetSuggestion suggestion = generator.getNextFacetSuggestion();

		performFacetSuggestionOperation(varclsname, suggestion);

	}

	private void performFacetSuggestionOperation(String varclsname,
			FacetSuggestion suggestion) {

		System.out.println(suggestion.toString());

		DefaultListModel newmodel = new DefaultListModel();

		tempguifacetnames.clear();
		int count = 0;

		Iterator<FacetValueRange> facetsranges = suggestion
				.getSuggestionIterator();

		while (facetsranges.hasNext()) {

			try {

				FacetValueRange fvr = facetsranges.next();
				String prefix = fvr.isEntityAsObject() ? "<-" : "->";
				String str = prefix + fvr.toString();

				tempguifacetnames.put(count++, fvr);

				newmodel.addElement(str);

			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}

		JList facetlistfill = mainmodelview.getFacetTypeList(true);

		facetlistfill.setModel(newmodel);

		facetlistfill.setSelectedIndex(-1);

		facetlistfill.setForeground(Color.BLACK);

		boolean updatehistorypath = classtypehistorymodel
				.addCurrentSearchedClass(varclsname);

		classtypehistorymodel.setCurrentSearchingClass(varclsname);

		if (updatehistorypath) {
			performUpdateClassHistoryPathPanel(varclsname);
		}

		DefaultListModel emptymodel = new DefaultListModel();

		JList facetvaluelistfill = mainmodelview.getFacetValueList(true);

		facetvaluelistfill.setModel(emptymodel);

	}

	private void performUpdateClassHistoryPathPanel(String varclsname) {

		JPanel history = mainmodelview.getHistoryPanelDataView(true);
		history.setAutoscrolls(true);

		history.removeAll();
		ButtonGroup buttongroup = new ButtonGroup();
		HistoryClassPathSelectionListerner listerner = new HistoryClassPathSelectionListerner();
		Iterator<String> historyclasses = classtypehistorymodel
				.getAllSearchedClassHistoryIterator();

		while (historyclasses.hasNext()) {
			String cls = historyclasses.next();
			JRadioButton jradio = new JRadioButton(cls);
			jradio.addActionListener(listerner);
			if (cls.equals(varclsname))
				jradio.setSelected(true);
			buttongroup.add(jradio);
			history.add(jradio);

		}

		history.revalidate();
		history.repaint();

	}

	public void performFacetTypeSelection(Integer selectedidx, String string) {
		String varclsname = classtypehistorymodel.getCurrentSearchingClass();

				
		performFacetValueOperation(varclsname,
				tempguifacetnames.get(selectedidx));

	}

	private void performFacetValueOperation(String varclsname,
			FacetValueRange facetvalue) {

		facetvaluedatamodel.setCurrentFacetValueRange(varclsname, facetvalue);

		List<String> newvarclsname = null;
		if (facetvalue instanceof FacetValueBlankRange) {

			String newvarblkname = classtypedatamodel
					.updateForFacetValueBlankTypeAsSubject(varclsname,
							facetvalue);

			Integer size = classtypedatamodel
					.getClassTypeResultSetSize(newvarblkname);

			String varblknamecount = newvarblkname + "(" + size + ")";

			guivarclsname.put(varblknamecount, newvarblkname);

			List<String> tempblkname = new ArrayList<String>();
			tempblkname.add(newvarblkname);

			performUpdateClassTypeResultDataModelView(tempblkname);

			performClassTypeSelection(0, varblknamecount);

		} else if (facetvalue instanceof FacetSubjectBlankRange) {

			String newvarblkname = classtypedatamodel
					.updateForFacetValueBlankTypeAsObject(varclsname,
							facetvalue);

			Integer size = classtypedatamodel
					.getClassTypeResultSetSize(newvarblkname);

			String varblknamecount = newvarblkname + "(" + size + ")";

			guivarclsname.put(varblknamecount, newvarblkname);

			List<String> tempblkname = new ArrayList<String>();
			tempblkname.add(newvarblkname);

			performUpdateClassTypeResultDataModelView(tempblkname);

			performClassTypeSelection(0, varblknamecount);

		} else if (facetvalue instanceof FacetSubjectURIRange) {

			newvarclsname = classtypedatamodel
					.checkForFacetValueClassTypeAsObject(varclsname, facetvalue);
			if (newvarclsname != null)
				performUpdateClassTypeResultDataModelView(newvarclsname);
			else {

			}
		} else if (facetvalue instanceof FacetValueURIRange) {
            FacetValueURIRange urirange = (FacetValueURIRange) facetvalue;
            
			newvarclsname = classtypedatamodel
					.checkForFacetValueClassTypeAsSubject(varclsname,
							facetvalue);
			if (newvarclsname != null)
			{	performUpdateClassTypeResultDataModelView(newvarclsname);
			    urirange.makeNull();    
			}
			else {
                    
				performFacetValueURIStringOperation(facetvalue);
				
			}

		} else if (facetvalue instanceof FacetValueStringRange) {

			performFacetValueLiteralStringOperation(facetvalue);

		}

		else {

			performFacetValueLiteralOperation(facetvalue);

		}

	}

	private void performFacetValueURIStringOperation(
			FacetValueRange facetvalue) {

		Map<?, Integer> facetvaluedata = facetvalue.getFacetValueRangeData();

		String values = "";

		tempguifacetvaluenames.clear();
		int count = 0;
		DefaultListModel newmodel = new DefaultListModel();

		for (Entry<?, Integer> entry : facetvaluedata.entrySet()) {

			if (entry.getKey() instanceof Node) {

				Node n = (Node) entry.getKey();
				if (n.isLiteral())
					values = n.getLiteralValue() + "(" + entry.getValue() + ")";
				if (n.isURI())
					values = n.getURI();
			}
			tempguifacetvaluenames.put(count++, entry.getKey());
			newmodel.addElement(values);
			// System.out.println(values);
		}

		JList facetvaluefill = mainmodelview.getFacetValueList(true);

		facetvaluefill.setModel(newmodel);

		facetvaluefill.setSelectedIndex(-1);

		facetvaluefill.setForeground(Color.GREEN);

	}
	
	
	private void performFacetValueLiteralStringOperation(
			FacetValueRange facetvalue) {

		Map<?, Integer> facetvaluedata = facetvalue.getFacetValueRangeData();

		String values = "";

		tempguifacetvaluenames.clear();
		int count = 0;
		DefaultListModel newmodel = new DefaultListModel();

		for (Entry<?, Integer> entry : facetvaluedata.entrySet()) {

			if (entry.getKey() instanceof Node) {

				Node n = (Node) entry.getKey();
				if (n.isLiteral())
					values = n.getLiteralValue() + "(" + entry.getValue() + ")";
				if (n.isURI())
					values = n.getURI();
			}
			tempguifacetvaluenames.put(count++, entry.getKey());
			newmodel.addElement(values);
			// System.out.println(values);
		}

		JList facetvaluefill = mainmodelview.getFacetValueList(true);

		facetvaluefill.setModel(newmodel);

		facetvaluefill.setSelectedIndex(-1);

		facetvaluefill.setForeground(Color.GREEN);

	}

	private void performFacetValueLiteralOperation(FacetValueRange facetvalue) {

		HelperFunctions helper = HelperFunctions.getInstance();

		DefaultListModel newmodel = new DefaultListModel();
		Map<?, Integer> facetvaluedata = facetvalue.getFacetValueRangeData();

		String values = "";

		tempguifacetvaluenames.clear();
		int count = 0;

		for (Entry<?, Integer> entry : facetvaluedata.entrySet()) {

			if (entry.getKey() instanceof Date) {
				values = helper.format((Date) entry.getKey()) + "("
						+ entry.getValue() + ")";
			} else {
				values = entry.getKey().toString() + "(" + entry.getValue()
						+ ")";
				;
			}
			tempguifacetvaluenames.put(count++, entry.getKey());
			newmodel.addElement(values);
			// System.out.println(values);
		}

		JList facetvaluefill = mainmodelview.getFacetValueList(true);

		facetvaluefill.setModel(newmodel);

		facetvaluefill.setSelectedIndex(-1);

		facetvaluefill.setForeground(Color.GREEN);

	}

	public void previousActionPerformed() {
		String varclsname = classtypehistorymodel.getCurrentSearchingClass();
		if (varclsname != null) {

			FacetSuggestionDataModel suggestion = facettypedatamodel
					.getSuggestionGenerator(varclsname);
			FacetSuggestion fsug = suggestion.getPreviousFacetSuggestion();
			if (fsug != null) {
				performFacetSuggestionOperation(varclsname, fsug);
			}
		}
	}

	public void nextActionPerformed() {
		String varclsname = classtypehistorymodel.getCurrentSearchingClass();
		if (varclsname != null) {

			FacetSuggestionDataModel suggestion = facettypedatamodel
					.getSuggestionGenerator(varclsname);
			FacetSuggestion fsug = suggestion.getNextFacetSuggestion();
			if (fsug != null) {
				performFacetSuggestionOperation(varclsname, fsug);
			}

		}

	}

	public void performFacetValueIntervalSelection() {
		String varclsname = classtypehistorymodel.getCurrentSearchingClass();

		String fctvalname = facetvaluedatamodel
				.performFacetValueIntervalSelection();
		

		JList list = mainmodelview.getRecordedFacetValueList(true);
	
		DefaultListModel newmodel = new DefaultListModel();
		
		if (fctvalname != null) {
            
			
			String display = facetvaluedatamodel
					.getStringRecordToDisplay(fctvalname);
			
			tempguirecordfacetvaluenames.put(fctvalname, display);
						
			Iterator<String> iterator = tempguirecordfacetvaluenames.values().iterator();
				
			while(iterator.hasNext())
			{
			    newmodel.addElement(iterator.next());
				
			}
          
			list.setModel(newmodel);
			
			list.setSelectedIndex(-1);

			list.setForeground(Color.BLUE);

			performRecordFacetValueClassTypeChanges(varclsname);
			            

		}

	}

	private void performRecordFacetValueClassTypeChanges(String varclsname) {

		/**
		 * update.....
		 */
		classtypedatamodel.updateClassTypeResultSet(varclsname);

		JList classlistfill = mainmodelview.getClassTypeList(true);

		DefaultListModel newmodel = new DefaultListModel();

		Integer size = classtypedatamodel.getClassTypeResultSetSize(varclsname);

		String varclsnamecount = varclsname + "(" + size + ")";

		guivarclsname.put(varclsnamecount, varclsname);

		newmodel.addElement(varclsnamecount);

		classlistfill.setModel(newmodel);

		classlistfill.setSelectedIndex(-1);

		classlistfill.setForeground(Color.RED);

		boolean updatehistorypath = classtypehistorymodel
				.addCurrentSearchedClass(varclsname);

		classtypehistorymodel.setCurrentSearchingClass(varclsname);

		if (updatehistorypath) {
			performUpdateClassHistoryPathPanel(varclsname);
		}

		DefaultListModel emptymodel = new DefaultListModel();

		JList facetlistfill = mainmodelview.getFacetTypeList(true);

		facetlistfill.setModel(emptymodel);

		JList facetvaluefill = mainmodelview.getFacetValueList(true);

		facetvaluefill.setModel(emptymodel);

	}

	public void performFacetValueSpecificSelection(Integer selectedidx) {

		String varclsname = classtypehistorymodel.getCurrentSearchingClass();

		String fctvalname = facetvaluedatamodel
				.performFacetValueSpecificSelection(tempguifacetvaluenames
						.get(selectedidx));
		
		
		JList list = mainmodelview.getRecordedFacetValueList(true);
   
	  	DefaultListModel newmodel = new DefaultListModel();
	  	
		if (fctvalname != null) {
              
			
			String display = facetvaluedatamodel
					.getStringRecordToDisplay(fctvalname);
	
			
			tempguirecordfacetvaluenames.put(fctvalname, display);
						
			Iterator<String> iterator = tempguirecordfacetvaluenames.values().iterator();
				
			while(iterator.hasNext())
			{
			    newmodel.addElement(iterator.next());
				
			}

			list.setModel(newmodel);

			list.setSelectedIndex(-1);

			list.setForeground(Color.BLUE);

			performRecordFacetValueClassTypeChanges(varclsname);

		}

	}

	public void performRemoveRecordedFacetValue(Object selectedvalue) {

		//String varclsname = classtypehistorymodel.getCurrentSearchingClass();

		String fctvalname =  null;

		for(Entry<String, String> entry:tempguirecordfacetvaluenames.entrySet()){
			
			if(entry.getValue().equals((String) selectedvalue)){
				
				fctvalname = entry.getKey();
			}
			
		}
		
		
		String rootvarclsname = classtypehistorymodel
				.getCurrentRootSearchingClass();

		if(fctvalname!=null)
		{
		String wasleafvarclsname = facetvaluedatamodel
				.removeRecordedFacetValue(fctvalname);

		JList list = mainmodelview.getRecordedFacetValueList(true);

		DefaultListModel newmodel = new DefaultListModel();
		
		tempguirecordfacetvaluenames.remove(fctvalname);
		
		Iterator<String> iterator = tempguirecordfacetvaluenames.values().iterator();
		
		while(iterator.hasNext())
		{
		    newmodel.addElement(iterator.next());
			
		}

		list.setModel(newmodel);

		list.setSelectedIndex(-1);

		list.setForeground(Color.BLUE);

		/**
		 * remove
		 */
		if (wasleafvarclsname!=null) {

			classtypedatamodel.removeClassType(wasleafvarclsname);
			performRecordFacetValueClassTypeChanges(rootvarclsname);

			performUpdateClassHistoryPathPanel(rootvarclsname);

		} else {

			performRecordFacetValueClassTypeChanges(wasleafvarclsname);
		}
        
		}
	}

	public void performFacetValueUnknownSelection() {

		String varclsname = classtypehistorymodel.getCurrentSearchingClass();

		String fctvalname = facetvaluedatamodel
				.performFacetValueUnknownSelection();

		JList list = mainmodelview.getRecordedFacetValueList(true);

	    DefaultListModel newmodel = new DefaultListModel();
	  	
		if (fctvalname != null) {
              
			
			String display = facetvaluedatamodel
					.getStringRecordToDisplay(fctvalname);
	
			
			tempguirecordfacetvaluenames.put(fctvalname, display);
						
			Iterator<String> iterator = tempguirecordfacetvaluenames.values().iterator();
				
			while(iterator.hasNext())
			{
			    newmodel.addElement(iterator.next());
				
			}

			list.setModel(newmodel);
			
			list.setSelectedIndex(-1);

			list.setForeground(Color.BLUE);

			performRecordFacetValueClassTypeChanges(varclsname);

		}

	}

	public void performShowCurrentStatusResultSet() {

		String varclsname = classtypehistorymodel.getCurrentSearchingClass();

		ResultSet result = resultsetdatamodel.getShowCurrentStatusResultSet(
				varclsname,
				classtypehistorymodel.getAllSearchedClassHistoryIterator());
		ResultSetRewindable rewind = (ResultSetRewindable) result;

		// ResultSetFormatter.out(System.out, rewind);
		rewind.reset();

		int rows = rewind.size();

		Object[] cols = rewind.getResultVars().toArray();

		Object[][] tabledata = new Object[rows][cols.length];

		rewind.reset();

		for (int i = 0; i < rows && rewind.hasNext(); i++) {
			QuerySolution sol = rewind.next();

			for (int j = 0; j < cols.length; j++) {

				tabledata[i][j] = sol.get((String) cols[j]);

			}

		}

		DefaultTableModel newtable = new DefaultTableModel(tabledata, cols);

		JTable table = mainmodelview.getCurrentQueryResultSetTable(true);

		table.setModel(newtable);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setAutoscrolls(true);
		table.revalidate();
		table.repaint();

	}

	public void performShowCurrentStatusResultQuery() {

		String varclsname = classtypehistorymodel.getCurrentSearchingClass();

		String query = resultsetdatamodel.getShowCurrentStatusResultQuery(
				varclsname,
				classtypehistorymodel.getAllSearchedClassHistoryIterator());

		System.out.println(query);

	}

	class HistoryClassPathSelectionListerner implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			JRadioButton jrb = (JRadioButton) e.getSource();

			String selected = jrb.getText();
			classtypehistorymodel.setCurrentSearchingClass(selected);

			performShowCurrentStatusResultSet();
			List<String> varclsname = new ArrayList<String>();
			varclsname.add(selected);

			performUpdateClassTypeResultDataModelView(varclsname);
		}

	}

}
