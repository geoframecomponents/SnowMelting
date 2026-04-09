/*
 * GNU GPL v3 License
 *
 * Copyright 2021 Niccolo Tubini, Giuseppe Formetta, Riccardo Rigon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geoframe.snowmelting.pointcase;

import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.hortonmachine.gears.libs.modules.HMConstants;

/*
 * Replaced with lines 40 and 41 
 * https://sourceforge.net/p/geotools/mailman/message/36652855/
 */
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.Geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import org.geoframe.numerical.ode.NewtonRaphson;
import it.geoframe.blogspot.snowmelting.freezing.Freezing;
import it.geoframe.blogspot.snowmelting.massbalance.CheckMassBalance;
import it.geoframe.blogspot.snowmelting.meltingdischarge.MeltingDischarge;
import it.geoframe.blogspot.snowmelting.ode.ODELiquidWater;
import it.geoframe.blogspot.snowmelting.ode.ODESolidWater;
import it.geoframe.blogspot.snowmelting.snowmelt.DegreeDayModel;
import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;

@Description("The component computes the snow water equivalent and the melting discharge with"
		+ "punctual data. The snow melting is computed by using a degree day model. "
		+ "The inputs of the components are the rainfall, the snowfall"
		+ "the temperature values")
@Author(name = "Marialaura Bancheri, Giuseppe Formetta, Niccolo Tubini", contact = "")
@Keywords("Hydrology, Snow Model")
@Label(HMConstants.HYDROGEOMORPHOLOGY)
@Name("Snow")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class SnowMeltingPointCaseDegreeDay {

	/*
	 * Meteo data
	 */
	@Description("The Hashmap with the time series of the rainfall values")
	@In
	@Unit("mm")
	public HashMap<Integer, double[]> inRainfallValues;

	@Description("The Hashmap with the time series of the snowfall values")
	@In
	@Unit("mm")
	public HashMap<Integer, double[]> inSnowfallValues;

	@Description("The Hashmap with the time series of the temperature values")
	@In
	@Unit("C")
	public HashMap<Integer, double[]> inTemperatureValues;

	/*
	 * Degree day parameters
	 */
	@Description("The melting temperature")
	@In
	@Unit("C")
	public double meltingTemperature;

	@Description("Combined melting factor")
	@In
	@Unit("mm / (C day)")
	public double combinedMeltingFactor;

	@Description("Freezing factor")
	@In
	@Unit("mm / (C day)")
	public double freezingFactor;

	@Description("Alfa_l is the coefficient for the computation of the maximum liquid water")
	@In
	@Unit("-")
	public double alfa_l;


	@Description("The time step in minutes")
	@In
	public double timeStepMinutes;	

	/*
	 * Geographic data
	 */
	@Description("The shape file with the station measuremnts")
	@In
	public SimpleFeatureCollection inStations;

	@Description("The name of the field containing the ID of the station in the shape file")
	@In
	public String fStationsid;


	/*
	 * Component output
	 */
	@Description(" The output melting discharge HashMap")
	@Out
	public HashMap<Integer, double[]> outMeltingDischargeHM= new HashMap<Integer, double[]>();

	@Description(" The output SWE HashMap")
	@Out
	public HashMap<Integer, double[]> outSWEHM= new HashMap<Integer, double[]>();
	
	@Description(" The error for the solid water ode HashMap")
	@Out
	public HashMap<Integer, double[]> outErrorODESolidWaterHM= new HashMap<Integer, double[]>();
	
	@Description(" The error for the liquid water ode HashMap")
	@Out
	public HashMap<Integer, double[]> outErrorODELiquidWaterHM= new HashMap<Integer, double[]>();
	
	@Description(" The error for the swe HashMap")
	@Out
	public HashMap<Integer, double[]> outErrorSWEHM= new HashMap<Integer, double[]>();
	
	@Description(" The freezing flux HashMap")
	@Out
	public HashMap<Integer, double[]> outFreezing= new HashMap<Integer, double[]>();
	
	@Description(" The melting flux HashMap")
	@Out
	public HashMap<Integer, double[]> outMelting= new HashMap<Integer, double[]>();



	@Description(" The vetor containing the id of the station")
	private Object []idStations;


	private Set<Integer> stationCoordinatesIdSet;

	private HashMap<Integer, double[]>initialConditionSolidWater= new HashMap<Integer, double[]>();
	private HashMap<Integer, double[]> initialConditionLiquidWater= new HashMap<Integer, double[]>();


	private Iterator<Integer> idIterator;

	private int step;
	
	

	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception { 


		if (step==0){


			LinkedHashMap<Integer, Coordinate> stationCoordinates = getCoordinate(inStations, fStationsid);


			stationCoordinatesIdSet = stationCoordinates.keySet();
			idIterator = stationCoordinatesIdSet.iterator();

			idStations= stationCoordinatesIdSet.toArray();


			for (int i=0;i<idStations.length;i++){

				initialConditionSolidWater.put(i,new double[]{0.0});
				initialConditionLiquidWater.put(i,new double[]{0.0});

			}


		}//close step==0

		
		freezingFactor = freezingFactor/1440*timeStepMinutes;
		combinedMeltingFactor = combinedMeltingFactor/1440*timeStepMinutes;

		
		// iterate over the list of the stations
		for (int i=0;i<idStations.length;i++){


			// read the input data for the given station
			double temperature = inTemperatureValues.get(idStations[i])[0];

			double rainfall = inRainfallValues.get(idStations[i])[0];
			if(isNovalue(rainfall) || rainfall<0)rainfall=0;

			double snowfall = inSnowfallValues.get(idStations[i])[0];
			if(isNovalue(snowfall) || snowfall<0)snowfall=0;

			double initialSolidWater = initialConditionSolidWater.get(i)[0];
			double initialLiquidWater = initialConditionLiquidWater.get(i)[0];

			SnowStepResult stRes = computeSnowStep(//
					temperature, //
					rainfall, //
					snowfall, //
					meltingTemperature, //
					combinedMeltingFactor, // 
					freezingFactor, //
					alfa_l, //
					initialSolidWater, //
					initialLiquidWater //
					);
			
			initialConditionSolidWater.put(i,new double[]{stRes.solidWater});
			initialConditionLiquidWater.put(i,new double[]{stRes.liquidWater});
			outSWEHM.put((Integer)idStations[i], new double[]{stRes.swe});
			outMeltingDischargeHM.put((Integer)idStations[i], new double[]{stRes.meltingDischarge});
			outErrorODESolidWaterHM.put((Integer)idStations[i], new double[]{stRes.errorODESolidWater});
			outErrorODELiquidWaterHM.put((Integer)idStations[i], new double[]{stRes.errorODELiquidWater});
			outErrorSWEHM.put((Integer)idStations[i], new double[]{stRes.errorSWE});
			outFreezing.put((Integer)idStations[i], new double[]{stRes.freezing});
			outMelting.put((Integer)idStations[i], new double[]{stRes.melting});
		}

		step++;

	}
	
	public static SnowStepResult computeSnowStep(//
	        double temperature,//
	        double rainfall,//
	        double snowfall,//
	        double meltingTemperature,//
	        double combinedMeltingFactor, // already scaled to timestep
	        double freezingFactor,        // already scaled to timestep
	        double alfa_l,//
	        double initialSolidWater,//
	        double initialLiquidWater//
	        ) {

		// basic cleaning, same logic as in process()
		if (isNovalue(rainfall) || rainfall < 0) {
			rainfall = 0.0;
		}
		if (isNovalue(snowfall) || snowfall < 0) {
			snowfall = 0.0;
		}

		// freezing
		double freezing = Freezing.compute(temperature, meltingTemperature, freezingFactor);
		freezing = Freezing.checkFreezing(initialLiquidWater, rainfall, freezing);

		// melting
		double melting = DegreeDayModel.computeMelting(combinedMeltingFactor, temperature, meltingTemperature);
		melting = DegreeDayModel.checkMelting(initialSolidWater, snowfall, melting);

		// solid water ODE
		ODESolidWater odeSolidWater = new ODESolidWater();
		ODELiquidWater odeLiquidWater = new ODELiquidWater();
		NewtonRaphson newton = new NewtonRaphson();
		odeSolidWater.set(initialSolidWater, snowfall, freezing, melting);
		double solidWater = newton.solve(initialSolidWater, odeSolidWater);

		// porosity
		double snowPorosity = solidWater * alfa_l;

		// liquid water ODE
		odeLiquidWater.set(initialLiquidWater, rainfall, freezing, melting, snowPorosity);
		double liquidWater = newton.solve(initialLiquidWater, odeLiquidWater);

		// melting discharge + updated liquid water
		double[] meltRes = MeltingDischarge.compute(snowPorosity, liquidWater, solidWater);
		double meltingDischarge = meltRes[0];
		liquidWater = meltRes[1];

		// SWE
		double swe = solidWater + liquidWater;

		// errors
		double errorODESolidWater = CheckMassBalance.errorODESolidWater(initialSolidWater, solidWater, snowfall,
				freezing, melting);

		double errorODELiquidWater = CheckMassBalance.errorODELiquidWater(initialLiquidWater, liquidWater, rainfall,
				freezing, melting, meltingDischarge);

		double errorSWE = CheckMassBalance.errorSWE(swe, initialLiquidWater, initialSolidWater, rainfall, snowfall,
				meltingDischarge);

		return new SnowStepResult(//
				solidWater, //
				liquidWater, //
				swe, //
				freezing, //
				melting, //
				meltingDischarge, //
				errorODESolidWater, //
				errorODELiquidWater, //
				errorSWE//
		);
	}


	private LinkedHashMap<Integer, Coordinate> getCoordinate(SimpleFeatureCollection collection, String idField)
			throws Exception {
		LinkedHashMap<Integer, Coordinate> id2CoordinatesMap = new LinkedHashMap<Integer, Coordinate>();
		FeatureIterator<SimpleFeature> iterator = collection.features();
		Coordinate coordinate = null;
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				int stationNumber = ((Number) feature.getAttribute(idField)).intValue();
				coordinate = ((Geometry) feature.getDefaultGeometry()).getCentroid().getCoordinate();
				id2CoordinatesMap.put(stationNumber, coordinate);
			}
		} finally {
			iterator.close();
		}

		return id2CoordinatesMap;

	}

	public record SnowStepResult(//
			double solidWater, //
			double liquidWater, //
			double swe, //
			double freezing, //
			double melting, //
			double meltingDischarge, //
			double errorODESolidWater, //
			double errorODELiquidWater, //
			double errorSWE //
	) {
	}

}
