package playground.wrashid.PHEV.Triangle;

import java.util.ArrayList;
import java.util.Iterator;

import org.matsim.basic.v01.IdImpl;
import org.matsim.facilities.MatsimFacilitiesReader;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.basic.v01.population.BasicLeg;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.ActivityOption;
import org.matsim.interfaces.core.v01.Facilities;
import org.matsim.interfaces.core.v01.Facility;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.population.Knowledge;
import org.matsim.population.PersonImpl;
import org.matsim.population.PopulationImpl;
import org.matsim.population.PopulationWriter;
import org.matsim.world.World;

public class CreatePlans {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO: am schluss alle meiste pfade in config.xml reintun...
		Population plans = new PopulationImpl(false);
		Gbl.reset();
		args=new String[1];
		args[0]="C:/data/SandboxCVS/ivt/studies/wrashid/Energy and Transport/triangle/config.xml";
		Gbl.createConfig(args);
		Gbl.getConfig().plans().setOutputFile("C:/data/SandboxCVS/ivt/studies/wrashid/Energy and Transport/triangle/5000plan/plans.xml");
		final World world = Gbl.getWorld();

		// read facilities
		Facilities facilities = (Facilities)world.createLayer(Facilities.LAYER_TYPE,null);
		new MatsimFacilitiesReader(facilities).readFile("C:/data/SandboxCVS/ivt/studies/wrashid/Energy and Transport/triangle/facilities/facilities.xml");


		// get home and work activity
		ActivityOption home=null;
		ActivityOption work=null;
		for (Facility f : facilities.getFacilities().values()) {
			Iterator<ActivityOption> a_it = f.getActivityOptions().values().iterator();
			while (a_it.hasNext()) {
				ActivityOption a = a_it.next();
				//System.out.println(a.getType());
				if (a.getType().equals("home")) {
					home=a;
				} else if (a.getType().equals("work")){
					work=a;
				}
			}
		}






		// create 100 persons
		for (int i=0;i<5000;i++){
			Person person = new PersonImpl(new IdImpl(i));
			plans.addPerson(person);


			Knowledge k = person.createKnowledge("");
			k.addActivity(home, false);
			k.addActivity(work, false);

			Plan plan = person.createPlan(true);
			Facility home_facility = person.getKnowledge().getActivities("home").get(0).getFacility();
			Facility work_facility = person.getKnowledge().getActivities("work").get(0).getFacility();
			ArrayList<ActivityOption> acts = person.getKnowledge().getActivities();

			double depTime=3600*8;
			double duration=3600*8;

			Activity a = plan.createAct("home",home_facility.getCoord());
			a.setLink(home_facility.getLink());
			a.setEndTime(depTime);
			Leg l = plan.createLeg(BasicLeg.Mode.car);
			l.setArrivalTime(depTime);
			l.setTravelTime(0.0);
			l.setDepartureTime(depTime);
			a = plan.createAct("work",work_facility.getCoord());
			a.setLink(work_facility.getLink());
			a.setStartTime(depTime);
			a.setEndTime(depTime+duration);
			a.setDuration(duration);
			l = plan.createLeg(BasicLeg.Mode.car);
			l.setArrivalTime(depTime+duration);
			l.setTravelTime(0.0);
			l.setDepartureTime(depTime+duration);
			a = plan.createAct("home",home_facility.getCoord());
			a.setLink(home_facility.getLink());
			// assign home-work-home activities to each person


//			Leg l=null;

		}



		new PopulationWriter(plans).write();
	}

}
