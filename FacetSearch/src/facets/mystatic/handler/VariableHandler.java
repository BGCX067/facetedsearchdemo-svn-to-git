package facets.mystatic.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openjena.atlas.lib.MapUtils;


import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.util.OneToManyMap;

import facets.gui.components.controller.QueryConstructionController;
import facets.myconstants.FacetConstants;

public class VariableHandler {

	private static VariableHandler variables = null;
	/**
	 * This map contains short class type names as keys and value as counter
	 * which increments to create multiple class type variables of same name
	 * 
	 * Example : class type : actor ; variable class names
	 * (varclsnameshortclsname) actor1, actor2, actor3 ..
	 */
	private Map<String, Integer> shortclsnamecounter; // shortclassname -->
														// Counts

	/**
	 * This map contains short class type names as keys and repective URI names
	 * as values
	 * 
	 * Example : actor: http://linkedmdb/..../actor
	 */

	private Map<String, String> shortclsnameuris; // shortclassname -->
													// shortclsnameuris

	/**
	 * In order to enter multiple contrains of same class type on current
	 * searching class type
	 * 
	 * Example: ?film1 movie:actor ?actor1 ?film1 movie:actor ?actor2
	 * 
	 * This map holds data, with key as: actor and Value: actor1, actor2, actor3
	 */
	private OneToManyMap<String, String> shortclsnamevarclsname;

	private OneToManyMap<String, String> shortblknamevarclsname;

	private Set<String> unknownvariables;

	/**
	 * A root variable class name
	 */
	private String rootvarclsname = null;

	private int blanknamecounter = 0;

	private QueryConstructionController mcontroller;
	
	private Pattern p = Pattern.compile("[\\p{Punct}\\p{Space}]");
   
	private VariableHandler(QueryConstructionController controller) {
		mcontroller = controller;
		shortclsnamecounter = new HashMap<String, Integer>();
		shortclsnamevarclsname = new OneToManyMap<String, String>();
		shortblknamevarclsname = new OneToManyMap<String, String>();
		blanknamecounter = 0;
		unknownvariables = new HashSet<String>();

	}

	public static VariableHandler getInstance(
			QueryConstructionController controller) {

		if (variables == null) {
			variables = new VariableHandler(controller);
		}

		return variables;
	}

	/**
	 * resets all the previous entries.
	 */
	public void reset() {

		shortclsnamecounter.clear();
		shortclsnamevarclsname.clear();
		shortblknamevarclsname.clear();
		unknownvariables.clear();
		blanknamecounter = 0;
		rootvarclsname = null;

	}

	public void setClassTypesofDataset(Map<String, String> map) {
		shortclsnameuris = map;
	}

	public Var getNewRootVariableClassName(String strclsname) {

		/**
		 * clearing all the previous searching ..
		 */
		reset();
        Matcher m = p.matcher(strclsname);
        String clsname = strclsname;
		System.out.println("------------------------>"+strclsname);
		
        if(m.find())
        {
        	clsname = m.replaceAll("");
        	System.out.println("Match Found and Replaced with:"+clsname);
        }
        else{
        	
        	System.out.println("No Match Found for: "+p.pattern());
        }
		
        MapUtils.increment(shortclsnamecounter, strclsname);

		String varclsname = FacetConstants.Var + clsname + shortclsnamecounter.get(strclsname);
		Var root = Var.alloc(varclsname);
		shortclsnamevarclsname.put(strclsname, root.getName());
		rootvarclsname = root.getName();
		return root;

	}

	public String getRootVariableClassName() {
		return rootvarclsname;
	}

	public boolean isRootName(String varclsname) {

		return rootvarclsname.equals(varclsname);

	}

	public String getClassTypeURI(String varclsname) {

		for (Entry<String, String> entry : shortclsnamevarclsname.entrySet()) {

			if (entry.getValue().equals(varclsname))
				return shortclsnameuris.get(entry.getKey());

		}

		return null;

	}

	public Var getNewVariableClassName(String strclsname) {

		 Matcher m = p.matcher(strclsname);
	        String clsname = strclsname;
			System.out.println("------------------------>"+strclsname);
			
	        if(m.find())
	        {
	        	clsname = m.replaceAll("");
	        	System.out.println("Match Found and Replaced with:"+clsname);
	        }
	        else{
	        	
	        	System.out.println("No Match Found for: "+p.pattern());
	        }
		
		if (!shortclsnamevarclsname.containsKey(strclsname))
			shortclsnamecounter.put(strclsname, 0);

		MapUtils.increment(shortclsnamecounter, strclsname);

		String varclsname = FacetConstants.Var+clsname + shortclsnamecounter.get(strclsname);

		Var var = Var.alloc(varclsname);

		shortclsnamevarclsname.put(strclsname, var.getName());

		return var;

	}

	public Iterator<String> getAllVariableClassNames() {

		return shortclsnamevarclsname.values().iterator();
	}

	public Set<String> getAllBlankVariableClassNames() {
		Set<String> blk = new HashSet<String>();

		Iterator<String> itr = shortblknamevarclsname.values().iterator();

		while (itr.hasNext()) {

			blk.add(itr.next());

		}

		return blk;
	}

	public Var getNewVariableBlankName(String varclsname, String strblkname) {

		String name = varclsname + "_" + strblkname + (++blanknamecounter);

		shortblknamevarclsname.put(strblkname, name);

		Var var = Var.alloc(name);

		return var;
	}

	public Var getFacetValueVariable(String varclsname, String shortfctname) {

		if (shortclsnamevarclsname.containsValue(varclsname)
				|| shortblknamevarclsname.containsValue(varclsname))
			return Var.alloc(varclsname + "_" + shortfctname);
		else
			return null;
	}

	public Var getFacetValueUnknownVariable(String varclsname,
			String shortfctname) {

		if (!shortclsnamevarclsname.containsValue(varclsname))
			return null;

		String uknw = varclsname + "_" + shortfctname;

		unknownvariables.add(uknw);

		return Var.alloc(uknw);

	}

	public Iterator<String> getFacetValueUnknownVariables() {
		return unknownvariables.iterator();
	}
   
	public void removeFacetValueVariable(String fctvalvar){
		
		unknownvariables.remove(fctvalvar);
	}
}
