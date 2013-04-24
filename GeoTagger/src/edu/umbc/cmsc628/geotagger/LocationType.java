package edu.umbc.cmsc628.geotagger;

public enum LocationType {
	stop, signal, traffic, accident, construction, all;

	public static boolean isLocationType(String value) {
		for (LocationType e : LocationType.class.getEnumConstants()) {
			if (e.name().equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}
}
