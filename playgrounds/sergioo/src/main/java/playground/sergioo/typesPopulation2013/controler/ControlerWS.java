/* *********************************************************************** *
 * project: org.matsim.*
 * ControlerWW.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,  *
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

package playground.sergioo.typesPopulation2013.controler;

//import java.util.HashSet;

//import org.matsim.api.core.v01.Id;

import com.google.inject.Provider;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.replanning.StrategyManager;
import playground.sergioo.ptsim2013.qnetsimengine.PTQSimFactory;
import playground.sergioo.singapore2012.transitRouterVariable.TransitRouterWSImplFactory;
import playground.sergioo.singapore2012.transitRouterVariable.stopStopTimes.StopStopTimeCalculator;
import playground.sergioo.singapore2012.transitRouterVariable.waitTimes.WaitTimeStuckCalculator;
import playground.sergioo.typesPopulation2013.config.groups.StrategyPopsConfigGroup;
import playground.sergioo.typesPopulation2013.controler.corelisteners.LegHistogramListener;
import playground.sergioo.typesPopulation2013.replanning.StrategyManagerPops;
import playground.sergioo.typesPopulation2013.replanning.StrategyManagerPopsConfigLoader;
import playground.sergioo.typesPopulation2013.scenario.ScenarioUtils;

//import playground.artemc.calibration.CalibrationStatsListener;


/**
 * A run Controler for a transit router that depends on the travel times and wait times
 * 
 * @author sergioo
 */

public class ControlerWS extends Controler {

	public ControlerWS(Config config) {
		super(config);
	}
	public ControlerWS(Scenario scenario) {
		super(scenario);
	}
	public static void main(String[] args) {
		Config config = ConfigUtils.createConfig();
		config.removeModule(StrategyConfigGroup.GROUP_NAME);
		config.addModule(new StrategyPopsConfigGroup());
		ConfigUtils.loadConfig(config, args[0]);
		final ControlerWS controler = new ControlerWS(ScenarioUtils.loadScenario(config));
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				bindMobsim().toProvider(new Provider<Mobsim>() {
					@Override
					public Mobsim get() {
						return new PTQSimFactory().createMobsim(controler.getScenario(), controler.getEvents());
					}
				});
			}
		});
		controler.setOverwriteFiles(true);
        controler.addCoreControlerListener(new LegHistogramListener(controler.getEvents(), true, controler.getScenario().getPopulation()));
		//controler.addControlerListener(new CalibrationStatsListener(controler.getEvents(), new String[]{args[1], args[2]}, 1, "Travel Survey (Benchmark)", "Red_Scheme", new HashSet<Id<Person>>()));
        WaitTimeStuckCalculator waitTimeCalculator = new WaitTimeStuckCalculator(controler.getScenario().getPopulation(), controler.getScenario().getTransitSchedule(), controler.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int) (controler.getConfig().qsim().getEndTime()-controler.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(waitTimeCalculator);
		StopStopTimeCalculator stopStopTimeCalculator = new StopStopTimeCalculator(controler.getScenario().getTransitSchedule(), controler.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int) (controler.getConfig().qsim().getEndTime()-controler.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(stopStopTimeCalculator);
		TransitRouterWSImplFactory factory = new TransitRouterWSImplFactory(controler.getScenario(), waitTimeCalculator.getWaitTimes(), stopStopTimeCalculator.getStopStopTimes());
		controler.setTransitRouterFactory(factory);
        AbstractModule myStrategyManagerModule = new AbstractModule() {

            @Override
            public void install() {
				bind(StrategyManager.class).toInstance(controler.myLoadStrategyManager());
			}
        };
        controler.addOverridingModule(myStrategyManagerModule);
        controler.run();
	}

	/**
	 * @return A fully initialized StrategyManager for the plans replanning.
	 */
	private StrategyManager myLoadStrategyManager() {
		StrategyManagerPops manager = new StrategyManagerPops();
		StrategyManagerPopsConfigLoader.load(this, manager);
		return manager;
	}
	
}
