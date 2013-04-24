package edu.umbc.cmsc628.geotagger;

public enum LocationType {
	STOP, SIGNAL, TRAFFIC, ACCIDENT, CONSTRUCTION, ALL;

	public static boolean isLocationType(String value) {
		for (LocationType e : LocationType.class.getEnumConstants()) {
			if (e.name().equals(value)) {
				return true;
			}
		}
		return false;
	}
}
