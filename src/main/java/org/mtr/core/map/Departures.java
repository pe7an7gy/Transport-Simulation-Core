package org.mtr.core.map;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import org.mtr.core.generated.map.DeparturesSchema;

public final class Departures extends DeparturesSchema {

	public Departures(long currentMillis, Object2ObjectAVLTreeMap<String, Long2ObjectAVLTreeMap<LongArrayList>> departures) {
		super(currentMillis);
		departures.forEach((routeIdHex, departuresForRoute) -> this.departures.add(new DeparturesByRoute(routeIdHex, departuresForRoute)));
	}
}
