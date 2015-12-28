package facets.myconstants;

import java.util.Scanner;

public class NumberUtil {

	public static boolean isNumeric(String inputData) {

		Object check = numericType(inputData);

		if (check != null)
			return true;
		else
			return false;

		// return inputData.matches("[-+]?\\d+(\\.\\d+)?");
	}

	private static Object numericType(String inputData) {

		Scanner sc = new Scanner(inputData);

		if (sc.hasNextInt())
			return Integer.valueOf(inputData);
		else if (sc.hasNextBigInteger())
			return Long.valueOf(inputData);
		else if (sc.hasNextLong())
			return Long.valueOf(inputData);
		else if (sc.hasNextDouble())
			return Double.valueOf(inputData);
		else if (sc.hasNextFloat())
			return Float.valueOf(inputData);
		

		return null;
	}

	public static Object getNumericType(String inputData) {

		return numericType(inputData);

	}

}
