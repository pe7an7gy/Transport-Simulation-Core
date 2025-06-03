package org.mtr.core.operation;

import org.mtr.core.data.Rail;
import org.mtr.core.generated.operation.SignalBlockUpdateSchema;
import org.mtr.core.serializer.ReaderBase;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongArrayList;

public final class SignalBlockUpdate extends SignalBlockUpdateSchema {

	public SignalBlockUpdate(Rail rail) {
		super(rail.getHexId());
		rail.iteratePreBlockedSignalColors(preBlockedSignalColors::add);
		rail.iterateCurrentlyBlockedSignalColors(currentlyBlockedSignalColors::add);
	}

	public SignalBlockUpdate(ReaderBase readerBase) {
		super(readerBase);
		updateData(readerBase);
	}

	public String getRailId() {
		return railId;
	}

	public LongArrayList getPreBlockedSignalColors() {
		return preBlockedSignalColors;
	}

	public LongArrayList getCurrentlyBlockedSignalColors() {
		return currentlyBlockedSignalColors;
	}
}
