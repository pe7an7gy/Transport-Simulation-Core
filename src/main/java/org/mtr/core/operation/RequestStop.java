package org.mtr.core.operation;

import org.mtr.core.data.Siding;
import org.mtr.core.data.Vehicle;
import org.mtr.core.generated.operation.RequestStopSchema;
import org.mtr.core.serializer.ReaderBase;
import org.mtr.core.simulation.Simulator;

public final class RequestStop extends RequestStopSchema {

	public RequestStop(long sidingId, long vehicleId) {
		super(sidingId, vehicleId);
	}

	public RequestStop(ReaderBase readerBase) {
		super(readerBase);
		updateData(readerBase);
	}

	public void requestStop(Simulator simulator) {
		final Siding siding = simulator.sidingIdMap.get(sidingId);
		if(siding != null){
			final Vehicle vehicle = siding.findVehicleById(vehicleId);
			if(vehicle != null){
				vehicle.setIsStopRequested(true);
			}
		}
	}
}
