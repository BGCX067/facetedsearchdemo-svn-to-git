package facets.myconstants;

import java.util.ArrayList;
import java.util.HashMap;

public class MyPriorPropertyConstant {

	// to load advance properties..

	public final static HashMap<String, String> myHashMap = new HashMap<String, String>();
	static {

	}

	public final static ArrayList<String> myArrayList = new ArrayList<String>();
	static {
		// myArrayList.add("actor");
		// myArrayList.add("genre");
		// myArrayList.add("director");

	}

	public static boolean contains(String propname) {

		if (myArrayList.contains(propname))
			return true;
		return false;

	}

}
