package dev.velix.imperat.util;

public final class TypeUtility {

	public static boolean isInteger(String string) {
		if(string == null)return false;
		try {
			Integer.parseInt(string);
			return true;
		}catch (NumberFormatException ex) {
			return false;
		}
	}

	public static boolean isBoolean(String string) {
		if(string == null)return false;
		return Boolean.parseBoolean(string);
	}

	public static boolean isDouble(String str) {
		if (str == null) return false;
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isLong(String str) {
		if (str == null) return false;
		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
