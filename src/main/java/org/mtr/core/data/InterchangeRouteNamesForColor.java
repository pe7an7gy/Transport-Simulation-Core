package org.mtr.core.data;

import org.mtr.core.generated.data.InterchangeRouteNamesForColorSchema;
import org.mtr.core.serializer.ReaderBase;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.function.Consumer;

public final class InterchangeRouteNamesForColor extends InterchangeRouteNamesForColorSchema implements Comparable<InterchangeRouteNamesForColor> {

	public InterchangeRouteNamesForColor(long color) {
		super(color);
	}

	public InterchangeRouteNamesForColor(ReaderBase readerBase) {
		super(readerBase);
		updateData(readerBase);
	}

	public void forEach(Consumer<String> consumer) {
		Collections.sort(routeNames);
		routeNames.forEach(consumer);
	}

	int getColor() {
		return (int) (color & 0xFFFFFF);
	}

	void addRouteName(String routeName) {
		if (!routeNames.contains(routeName)) {
			routeNames.add(routeName);
		}
	}

	void addRouteNames(ObjectArrayList<String> routeNames) {
		this.routeNames.addAll(routeNames);
	}

	@Override
	public int compareTo(InterchangeRouteNamesForColor interchangeRouteNamesForColor) {
		return Long.compare(color, interchangeRouteNamesForColor.color);
	}
}
