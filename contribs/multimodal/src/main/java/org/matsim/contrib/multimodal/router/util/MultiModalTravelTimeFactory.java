/* *********************************************************************** *
 * project: org.matsim.*
 * MultimodalTripRouterFactory.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.contrib.multimodal.router.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.multimodal.config.MultiModalConfigGroup;
import org.matsim.core.api.internal.MatsimFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.router.util.TravelTimeFactory;
import org.matsim.core.utils.collections.CollectionUtils;

public class MultiModalTravelTimeFactory implements MatsimFactory {
	
	protected static final Logger log = Logger.getLogger(MultiModalTravelTimeFactory.class);
	
	private final Map<String, TravelTimeFactory> factories;
	private final Map<String, TravelTimeFactory> additionalFactories;
	private final Map<Id<Link>, Double> linkSlopes;
	
	public MultiModalTravelTimeFactory(Config config) {
		this(config, null, null);
	}

	public MultiModalTravelTimeFactory(Config config, Map<Id<Link>, Double> linkSlopes) {
		this(config, linkSlopes, null);
	}
	
	public MultiModalTravelTimeFactory(Config config, Map<Id<Link>, Double> linkSlopes, Map<String, TravelTimeFactory> additionalFactories) {
		this.linkSlopes = linkSlopes;
		this.factories = new LinkedHashMap<String, TravelTimeFactory>();
		this.additionalFactories = additionalFactories;
		
		if (this.linkSlopes == null) {
			log.warn("No slope information for the links available - travel time will only take agents age and gender into account!");
		}
		
		this.initMultiModalTravelTimeFactories(config);
	}
	
	public Map<String, TravelTime> createTravelTimes() {
		Map<String, TravelTime> travelTimes = new HashMap<String, TravelTime>();
		
		for (Entry<String, TravelTimeFactory> entry : factories.entrySet()) {
			travelTimes.put(entry.getKey(), entry.getValue().createTravelTime());
		}
		
		return travelTimes;
	}
	
	private void initMultiModalTravelTimeFactories(Config config) {
		
		PlansCalcRouteConfigGroup plansCalcRouteConfigGroup = config.plansCalcRoute();
        MultiModalConfigGroup multiModalConfigGroup = (MultiModalConfigGroup) config.getModule(MultiModalConfigGroup.GROUP_NAME);
        Set<String> simulatedModes = CollectionUtils.stringToSet(multiModalConfigGroup.getSimulatedModes());
		
		for (String mode : simulatedModes) {		
			if (mode.equals(TransportMode.walk)) {
				TravelTimeFactory factory = new WalkTravelTimeFactory(plansCalcRouteConfigGroup, linkSlopes);
				this.factories.put(mode, factory);
			} else if (mode.equals(TransportMode.transit_walk)) {
				TravelTimeFactory factory = new TransitWalkTravelTimeFactory(plansCalcRouteConfigGroup, linkSlopes);
				this.factories.put(mode, factory);
			} else if (mode.equals(TransportMode.bike)) {
				TravelTimeFactory factory = new BikeTravelTimeFactory(plansCalcRouteConfigGroup, linkSlopes);
				this.factories.put(mode, factory);
			} else {
				TravelTimeFactory factory = getTravelTimeFactory(mode);
				
				if (factory == null) {
					log.warn("Mode " + mode + " is not supported! " + 
							"Use a constructor where you provide the travel time objects. " +
							"Using a UnknownTravelTime calculator based on constant speed." +
							"Agent specific attributes are not taken into account!");
					factory = new UnknownTravelTimeFactory(mode, plansCalcRouteConfigGroup);
					this.factories.put(mode, factory);
				} else {
					log.info("Found additional travel time factory from type " + factory.getClass().toString() +
							" for mode " + mode + ".");
					this.factories.put(mode, factory);
				}
			}
		}
	}
	
	private TravelTimeFactory getTravelTimeFactory(String mode) {
		if (additionalFactories != null) return this.additionalFactories.get(mode);
		else return null;
	}
}