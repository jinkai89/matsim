package opdytsintegration.networkmodes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.VehicleAbortsEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleAbortsEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

import opdytsintegration.MATSimCountingStateAnalyzer;
import opdytsintegration.utils.TimeDiscretization;

/**
 * 
 * @author Gunnar Flötteröd
 *
 */
public class DifferentiatedLinkOccupancyAnalyzer implements LinkLeaveEventHandler, LinkEnterEventHandler,
		VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler, VehicleAbortsEventHandler {

	// -------------------- MEMBERS --------------------

	private final Set<Id<Link>> relevantLinks;

	private final Map<String, MATSimCountingStateAnalyzer<Link>> mode2stateAnalyzer;

	private Map<Id<Vehicle>, MATSimCountingStateAnalyzer<Link>> vehicleId2stateAnalyzer = null;

	// -------------------- CONSTRUCTION --------------------

	public DifferentiatedLinkOccupancyAnalyzer(final int startTime_s, final int binSize_s, final int binCnt,
			final Set<String> relevantModes, final Set<Id<Link>> relevantLinks) {
		this.mode2stateAnalyzer = new LinkedHashMap<>();
		for (String mode : relevantModes) {
			this.mode2stateAnalyzer.put(mode, new MATSimCountingStateAnalyzer<Link>(startTime_s, binSize_s, binCnt));
		}
		this.relevantLinks = relevantLinks;
	}

	public DifferentiatedLinkOccupancyAnalyzer(final TimeDiscretization timeDiscretization,
			final Set<String> relevantModes, final Set<Id<Link>> relevantLinks) {
		this(timeDiscretization.getStartTime_s(), timeDiscretization.getBinSize_s(), timeDiscretization.getBinCnt(),
				relevantModes, relevantLinks);
	}

	// -------------------- INTERNALS --------------------

	private boolean relevantLink(final Id<Link> link) {
		return ((this.relevantLinks == null) || this.relevantLinks.contains(link));
	}

	// ---------- IMPLEMENTATION OF *EventHandler INTERFACES ----------

	@Override
	public void reset(final int iteration) {
		for (MATSimCountingStateAnalyzer<?> stateAnalyzer : this.mode2stateAnalyzer.values()) {
			stateAnalyzer.reset(iteration);
		}
		this.vehicleId2stateAnalyzer = new LinkedHashMap<>();
	}

	@Override
	public void handleEvent(final VehicleEntersTrafficEvent event) {
		final MATSimCountingStateAnalyzer<Link> stateAnalyzer = this.mode2stateAnalyzer.get(event.getNetworkMode());
		if (stateAnalyzer != null) { // relevantMode
			this.vehicleId2stateAnalyzer.put(event.getVehicleId(), stateAnalyzer);
			if (this.relevantLink(event.getLinkId())) {
				stateAnalyzer.registerIncrease(event.getLinkId(), (int) event.getTime());
			}
		}
	}

	@Override
	public void handleEvent(final VehicleLeavesTrafficEvent event) {
		final MATSimCountingStateAnalyzer<Link> stateAnalyzer = this.vehicleId2stateAnalyzer.get(event.getVehicleId());
		if (stateAnalyzer != null) { // relevant mode
			if (this.relevantLink(event.getLinkId())) {
				stateAnalyzer.registerDecrease(event.getLinkId(), (int) event.getTime());
			}
			this.vehicleId2stateAnalyzer.remove(event.getVehicleId());
		}
	}

	@Override
	public void handleEvent(final LinkEnterEvent event) {
		final MATSimCountingStateAnalyzer<Link> stateAnalyzer = this.vehicleId2stateAnalyzer.get(event.getVehicleId());
		if (stateAnalyzer != null) { // relevant mode
			if (this.relevantLink(event.getLinkId())) {
				stateAnalyzer.registerIncrease(event.getLinkId(), (int) event.getTime());
			}
		}
	}

	@Override
	public void handleEvent(final LinkLeaveEvent event) {
		final MATSimCountingStateAnalyzer<Link> stateAnalyzer = this.vehicleId2stateAnalyzer.get(event.getVehicleId());
		if (stateAnalyzer != null) { // relevant mode
			if (this.relevantLink(event.getLinkId())) {
				stateAnalyzer.registerDecrease(event.getLinkId(), (int) event.getTime());
			}
		}
	}

	@Override
	public void handleEvent(final VehicleAbortsEvent event) {
		final MATSimCountingStateAnalyzer<Link> stateAnalyzer = this.vehicleId2stateAnalyzer.get(event.getVehicleId());
		if (stateAnalyzer != null) { // relevant mode
			if (this.relevantLink(event.getLinkId())) {
				stateAnalyzer.registerDecrease(event.getLinkId(), (int) event.getTime());
			}
			// TODO What does an abort imply? Should the vehicle be taken out?
		}
	}
}
