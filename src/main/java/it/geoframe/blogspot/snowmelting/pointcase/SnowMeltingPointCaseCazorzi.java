/*
 * GNU GPL v3 License
 *
 * Copyright 2021 Niccolò Tubini, Giuseppe Formetta, Riccardo Rigon
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

package it.geoframe.blogspot.snowmelting.pointcase;

import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/*
 * Replaced with lines 50 and 51 
 * https://sourceforge.net/p/geotools/mailman/message/36652855/
 */
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.Geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import it.geoframe.blogspot.numerical.ode.NewtonRaphson;
import it.geoframe.blogspot.snowmelting.freezing.Freezing;
import it.geoframe.blogspot.snowmelting.massbalance.CheckMassBalance;
import it.geoframe.blogspot.snowmelting.meltingdischarge.MeltingDischarge;
import it.geoframe.blogspot.snowmelting.ode.ODELiquidWater;
import it.geoframe.blogspot.snowmelting.ode.ODESolidWater;
import it.geoframe.blogspot.snowmelting.snowmelt.CazorziModel;
import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Initialize;
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
@Author(name = "Marialaura Bancheri, Giuseppe Formetta, Niccolò Tubini", contact = "")
@Keywords("Hydrology, Snow Model")
@Label(HMConstants.HYDROGEOMORPHOLOGY)
@Name("Snow")
//@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

/*
 * FIXME: to complete and test
 */

public class SnowMeltingPointCaseCazorzi {

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
	
	@Description("The Hashmap with the time series of the shortwave radiation")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inShortwaveValues;
	
	@Description("The Hashmap with the time series of the EI values")
	@In
	@Unit("E / (C day)")
	public HashMap<Integer, double[]> inEIValues;


	/*
	 * Hock model parameters
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

	@Description("The map of the skyview factor.")
	@In
	public GridCoverage2D inSkyview;

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




	private double rainfall;
	private double snowfall;
	private double temperature;
	private double shortwave;
	private double energyIndex;

	private double melting;
	private double freezing;
	private double meltingDischarge;
	private double snowPorosity;
	
	private double liquidWater;
	private double solidWater;
	private double swe;
	
	private double errorODESolidWater;
	private double errorODELiquidWater;
	private double errorSWE;

	@Description(" The vetor containing the id of the station")
	private Object []idStations;

	@Description("the linked HashMap with the coordinate of the stations")
	private LinkedHashMap<Integer, Coordinate> stationCoordinates;

	private Set<Integer> stationCoordinatesIdSet;

	private HashMap<Integer, double[]>initialConditionSolidWater= new HashMap<Integer, double[]>();
	private HashMap<Integer, double[]> initialConditionLiquidWater= new HashMap<Integer, double[]>();


	private Iterator<Integer> idIterator;
	
	private WritableRaster skyview;
	private double skyviewValue;
	private CoordinateReferenceSystem sourceCRS;
	private MathTransform transf;
	private Coordinate coordinate;
	private DirectPosition point;
	private DirectPosition gridPoint;
	
	@Description("List of the indeces of the columns of the station in the map")
	private ArrayList <Integer> columnStation= new ArrayList <Integer>();
	
	@Description("List of the indeces of the rows of the station in the map")
	private ArrayList <Integer> rowStation= new ArrayList <Integer>();
	
//	@Description("List of the latitudes of the station ")
//	private ArrayList <Double> latitudeStation= new ArrayList <Double>();
//	
	
	private CazorziModel cazorziModel;
	private Freezing computeFreezing;
	private MeltingDischarge computeMeltingDischarge;
	private NewtonRaphson newton;
	private ODESolidWater odeSolidWater;
	private ODELiquidWater odeLiquidWater;
	private CheckMassBalance checkMassBalance;

//	private int step;
	
	

	@Initialize
	public void setup() throws Exception {
		
		sourceCRS = inSkyview.getCoordinateReferenceSystem2D();
		transf = inSkyview.getGridGeometry().getCRSToGrid2D();
		skyview = mapsTransform(inSkyview);
		
		stationCoordinates = getCoordinate(inStations, fStationsid);


		stationCoordinatesIdSet = stationCoordinates.keySet();
//		idIterator = stationCoordinatesIdSet.iterator();

		idStations= stationCoordinatesIdSet.toArray();


		for (int i=0;i<idStations.length;i++){

			initialConditionSolidWater.put(i,new double[]{0.0});
			initialConditionLiquidWater.put(i,new double[]{0.0});

		}

		odeSolidWater = new ODESolidWater();
		odeLiquidWater = new ODELiquidWater();
		newton = new NewtonRaphson();

		computeFreezing = new Freezing();
		cazorziModel = new CazorziModel();
		computeMeltingDischarge = new MeltingDischarge();
		checkMassBalance = new CheckMassBalance();
		
		
	}
	
	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception { 

//
//		if (step==0){
//
//
//			stationCoordinates = getCoordinate(inStations, fStationsid);
//
//
//			stationCoordinatesIdSet = stationCoordinates.keySet();
//			idIterator = stationCoordinatesIdSet.iterator();
//
//			idStations= stationCoordinatesIdSet.toArray();
//
//
//			for (int i=0;i<idStations.length;i++){
//
//				initialConditionSolidWater.put(i,new double[]{0.0});
//				initialConditionLiquidWater.put(i,new double[]{0.0});
//
//			}
//
//			odeSolidWater = new ODESolidWater();
//			odeLiquidWater = new ODELiquidWater();
//			newton = new NewtonRaphson();
//
//			computeFreezing = new Freezing();
//			degreeDayMelting = new DegreeDay();
//			computeMeltingDischarge = new MeltingDischarge();
//			checkMassBalance = new CheckMassBalance();
//
//		}//close step==0

		
		freezingFactor = freezingFactor/1440*timeStepMinutes;
		combinedMeltingFactor = combinedMeltingFactor/1440*timeStepMinutes;

		
		// iterate over the list of the stations
		for (int i=0;i<idStations.length;i++){

			idIterator = stationCoordinatesIdSet.iterator();

			coordinate = (Coordinate) stationCoordinates.get(idIterator.next());

			// define the position, according to the CRS, of the station in the map
			point = new DirectPosition2D(sourceCRS, coordinate.x, coordinate.y);

			// trasform the position in two the indices of row and column 
			gridPoint = transf.transform(point, null);

			// add the indices to a list
			columnStation.add((int) gridPoint.getCoordinate()[0]);
			rowStation.add((int) gridPoint.getCoordinate()[1]);

			skyviewValue = skyview.getSampleDouble(columnStation.get(i), rowStation.get(i), 0);


			// read the input data for the given station
			temperature = inTemperatureValues.get(idStations[i])[0];

			rainfall = inRainfallValues.get(idStations[i])[0];
			if(isNovalue(rainfall)|rainfall<0)rainfall=0;


			snowfall = inSnowfallValues.get(idStations[i])[0];
			if(isNovalue(snowfall)|snowfall<0)snowfall=0;
			
			shortwave = inShortwaveValues.get(idStations[i])[0];
			if(isNovalue(shortwave)|shortwave<0)shortwave=0;
			
			energyIndex = inEIValues.get(idStations[i])[0];
			if(isNovalue(energyIndex)|energyIndex<0)energyIndex=0;


			freezing = computeFreezing.compute(temperature, meltingTemperature, freezingFactor);
//			freezing = Math.min(freezing, liquidWater+rainfall);
			freezing = computeFreezing.checkFreezing(initialConditionLiquidWater.get(i)[0], rainfall, freezing);

			melting = cazorziModel.computeMelting(combinedMeltingFactor, temperature, meltingTemperature, energyIndex, skyviewValue);
//			melting = Math.min(melting, solidWater+snowfall);
			melting = cazorziModel.checkMelting(initialConditionSolidWater.get(i)[0], snowfall, melting);
			
			odeSolidWater.set(initialConditionSolidWater.get(i)[0], snowfall, freezing, melting);
			solidWater = newton.solve(initialConditionSolidWater.get(i)[0], odeSolidWater);


			snowPorosity = solidWater*alfa_l;

//			if(freezing >(initialConditionLiquidWater.get(i)[0] + rainfall + melting)) {
//				freezing = initialConditionLiquidWater.get(i)[0] + rainfall + melting;
//			}
			odeLiquidWater.set(initialConditionLiquidWater.get(i)[0], rainfall, freezing, melting, snowPorosity);
			liquidWater = newton.solve(initialConditionLiquidWater.get(i)[0], odeLiquidWater);

//			if(liquidWater<0) {
//
//				liquidWater = 0;
//
//			}

			meltingDischarge = computeMeltingDischarge.compute(snowPorosity, liquidWater, solidWater)[0];
			liquidWater = computeMeltingDischarge.compute(snowPorosity, liquidWater, solidWater)[1];

			swe = solidWater + liquidWater;


			errorODESolidWater = checkMassBalance.errorODESolidWater(initialConditionSolidWater.get(i)[0], solidWater, snowfall, freezing, melting);
			errorODELiquidWater = checkMassBalance.errorODELiquidWater(initialConditionLiquidWater.get(i)[0], liquidWater, rainfall, freezing, melting, meltingDischarge);
			errorSWE = checkMassBalance.errorSWE(swe, initialConditionLiquidWater.get(i)[0], initialConditionSolidWater.get(i)[0],
																rainfall, snowfall, meltingDischarge);

//			System.out.println("error ODE solid water " + errorODESolidWater);
//			System.out.println("error ODE liquid water " + errorODELiquidWater);
//			System.out.println("error swe" + errorSWE);
		

//						System.out.println(snowfall +"\t"+ rainfall +"\t"+ solidWater +"\t"+ liquidWater +"\t"+ swe +"\t"+ freezing +"\t"+ melting +"\t"+ meltingDischarge);

			
			initialConditionSolidWater.put(i,new double[]{solidWater});
			initialConditionLiquidWater.put(i,new double[]{liquidWater});
			
			outSWEHM.put((Integer)idStations[i], new double[]{swe});

			outMeltingDischargeHM.put((Integer)idStations[i], new double[]{meltingDischarge});
			
			outErrorODESolidWaterHM.put((Integer)idStations[i], new double[]{errorODESolidWater});
			
			outErrorODELiquidWaterHM.put((Integer)idStations[i], new double[]{errorODELiquidWater});
			
			outErrorSWEHM.put((Integer)idStations[i], new double[]{errorSWE});

		}

//		step++;

	}
	
	
	private WritableRaster mapsTransform ( GridCoverage2D inValues){	
		RenderedImage inValuesRenderedImage = inValues.getRenderedImage();
		WritableRaster inValuesWR = CoverageUtilities.replaceNovalue(inValuesRenderedImage, -9999.0);
		inValuesRenderedImage = null;
		return inValuesWR;
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


}
