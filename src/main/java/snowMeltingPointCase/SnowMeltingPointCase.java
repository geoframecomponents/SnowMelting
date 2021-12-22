/*
 * GNU GPL v3 License
 *
 * Copyright 2015 Marialaura Bancheri
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
package snowMeltingPointCase;


import org.hortonmachine.gears.libs.modules.HMConstants;
import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;


import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.hortonmachine.gears.libs.modules.HMModel;
import org.hortonmachine.gears.utils.CrsUtilities;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


@Description("The component computes the snow water equivalent and the melting discharge with"
		+ "punctual data. The inputs of the components are the rainfall,"
		+ "the shortwave, the temperature values, the skyview, the energy index and the DEM maps")
@Author(name = "Marialaura Bancheri & Giuseppe Formetta", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Snow Model")
@Label(HMConstants.HYDROGEOMORPHOLOGY)
@Name("Snow")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class SnowMeltingPointCase extends HMModel {

	@Description("The Hashmap with the time series of the rainfall values")
	@In
	public HashMap<Integer, double[]> inRainfallValues;

	@Description("The double value of the , once read from the HashMap")
	double rainfall;

	@Description("The Hashmap with the time series of the snowfall values")
	@In
	public HashMap<Integer, double[]> inSnowfallValues;

	@Description("The double value of the snowfall, once read from the HashMap")
	double snowfall;

	@Description("The Hashmap with the time series of the SWRB values")
	@In
	public HashMap<Integer, double[]> inShortwaveRadiationValues;

	@Description("The double value of the  shortwave radiation, once read from the HashMap")
	double shortwaveRadiation;

	@Description("The Hashmap with the time series of the EI values")
	@In
	public HashMap<Integer, double[]> inEIValues;

	@Description("The double value of the  EI, once read from the HashMap")
	double EI;


	@Description("The Hashmap with the time series of the temperature values")
	@In
	public HashMap<Integer, double[]> inTemperatureValues;

	@Description("The double value of the  temperature, once read from the HashMap")
	double temperature;


	@Description("The map of the skyview factor.")
	@In
	public GridCoverage2D inSkyview;
	WritableRaster skyview;

	@Description("the skyview factor value, read from the map")
	double skyviewValue;

	@Description("The digital elevation model.")
	@In
	public GridCoverage2D inDem;

	@Description("The shape file with the station measuremnts")
	@In
	public SimpleFeatureCollection inStations;

	@Description("The name of the field containing the ID of the station in the shape file")
	@In
	public String fStationsid;

	@Description(" The vetor containing the id of the station")
	Object []idStations;

	@Description("the linked HashMap with the coordinate of the stations")
	LinkedHashMap<Integer, Coordinate> stationCoordinates;

	@Description("The first day of the simulation.")
	@In
	public String tStartDate;

	@Description("It is possibile to chose between 3 different models to compute the melting: "
			+ " Classical; Cazorzi; Hock")
	@In
	public String model;

	@Description("The melting temperature")
	@In
	@Unit("C")
	public double meltingTemperature;

	@Description("Combined melting factor")
	@In
	public double combinedMeltingFactor;

	@Description("Radiation factor")
	@In
	public double radiationFactor;

	@Description("Freezing factor")
	@In
	public double freezingFactor;

	@Description("Alfa_l is the coefficient for the computation of the maximum liquid water")
	@In
	public double alfa_l;

	@Description("List of the indeces of the columns of the station in the map")
	ArrayList <Integer> columnStation= new ArrayList <Integer>();

	@Description("List of the indeces of the rows of the station in the map")
	ArrayList <Integer> rowStation= new ArrayList <Integer>();

	@Description("List of the latitudes of the station ")
	ArrayList <Double> latitudeStation= new ArrayList <Double>();


	SnowModel snowModel;


	@Description("liquid water value obtained from the soultion of the budget")
	double liquidWater;
	
	double melting;

	@Description("Integration interval")
	double dt=1;


	@Description("Final target CRS")
	CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;

	@Description(" The output melting discharge HashMap")
	@Out
	public HashMap<Integer, double[]> outMeltingDischargeHM= new HashMap<Integer, double[]>();;

	@Description(" The output SWE HashMap")
	@Out
	public HashMap<Integer, double[]> outSWEHM= new HashMap<Integer, double[]>();;

	@Description(" The output SWE value")
	double SWE;

	int step;

	HashMap<Integer, double[]>initialConditionSolidWater= new HashMap<Integer, double[]>();
	HashMap<Integer, double[]> initialConditionLiquidWater= new HashMap<Integer, double[]>();

	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception { 

		// computing the reference system of the input DEM
		CoordinateReferenceSystem sourceCRS = inDem.getCoordinateReferenceSystem2D();


		//  from pixel coordinates (in coverage image) to geographic coordinates (in coverage CRS)
		MathTransform transf = inDem.getGridGeometry().getCRSToGrid2D();

		if (step==0){
			skyview=mapsTransform(inSkyview);

			// starting from the shp file containing the stations, get the coordinate
			//of each station
			stationCoordinates = getCoordinate(inStations, fStationsid);}

		//create the set of the coordinate of the station, so we can 
		//iterate over the set
		Set<Integer> stationCoordinatesIdSet = stationCoordinates.keySet();
		Iterator<Integer> idIterator = stationCoordinatesIdSet.iterator();
		
		

		// trasform the list of idStation into an array
		idStations= stationCoordinatesIdSet.toArray();

		if(step==0){
			for (int i=0;i<idStations.length;i++){
				initialConditionSolidWater.put(i,new double[]{0.0});
				initialConditionLiquidWater.put(i,new double[]{0.0});
			}

		}

		// iterate over the list of the stations to detect their position in the
		// map and their latitude
		// iterate over the list of the stations
		for (int i=0;i<idStations.length;i++){

			// compute the coordinate of the station from the linked hashMap
			Coordinate coordinate = (Coordinate) stationCoordinates.get(idIterator.next());

			// define the position, according to the CRS, of the station in the map
			DirectPosition point = new DirectPosition2D(sourceCRS, coordinate.x, coordinate.y);

			// trasform the position in two the indices of row and column 
			DirectPosition gridPoint = transf.transform(point, null);

			// add the indices to a list
			columnStation.add((int) gridPoint.getCoordinate()[0]);
			rowStation.add((int) gridPoint.getCoordinate()[1]);


			// read the input data for the given station
			temperature=inTemperatureValues.get(idStations[i])[0];
			
			rainfall=inRainfallValues.get(idStations[i])[0];
			if(isNovalue(rainfall)|rainfall<0)rainfall=0;
			
			//System.out.println(rainfall+"snow");
			
			snowfall=inSnowfallValues.get(idStations[i])[0];
			if(isNovalue(snowfall)|snowfall<0)snowfall=0;
			
			shortwaveRadiation=inShortwaveRadiationValues.get(idStations[i])[0];
			
			
			EI= (model=="Cazorzi")?inEIValues.get(idStations[i])[0]:0;

			//read the input skyview for the given station position
			skyviewValue=skyview.getSampleDouble(columnStation.get(i), rowStation.get(i), 0);

			double freezing=(temperature<meltingTemperature)?computeFreezing():0;
			
			 melting=(temperature>meltingTemperature)?computeMelting():0;
			 
			double solidWater=computeSolidWater(initialConditionSolidWater.get(i)[0],freezing);
			computeLiquidWater(initialConditionLiquidWater.get(i)[0], freezing, melting);

			// compute the melting and the discharge and stores the results into Hashmap
			storeResult_series((Integer)idStations[i],computeMeltingDischarge(solidWater), computeSWE(solidWater));



			initialConditionSolidWater.put(i,new double[]{solidWater});
			initialConditionLiquidWater.put(i,new double[]{liquidWater});

		}

		step++;
	}

	/**
	 * Maps reader transform the GrifCoverage2D in to the writable raster,
	 * replace the -9999.0 value with no value.
	 *
	 * @param inValues: the input map values
	 * @return the writable raster of the given map
	 */
	private WritableRaster mapsTransform ( GridCoverage2D inValues){	
		RenderedImage inValuesRenderedImage = inValues.getRenderedImage();
		WritableRaster inValuesWR = CoverageUtilities.replaceNovalue(inValuesRenderedImage, -9999.0);
		inValuesRenderedImage = null;
		return inValuesWR;
	}


	/**
	 * Gets the coordinate given the shp file and the field name in the shape with the coordinate of the station.
	 *
	 * @param collection is the shp file with the stations
	 * @param idField is the name of the field with the id of the stations 
	 * @return the coordinate of each station
	 * @throws Exception the exception in a linked hash map
	 */
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


	/**
	 * Compute the freezing.
	 *
	 * @param freezingFactor the freezing factor
	 * @param temperature the temperature
	 * @param meltingTemperature the melting temperature
	 * @return the double
	 */
	private double computeFreezing(){
		// compute the freezing
		return freezingFactor*(meltingTemperature-temperature);		
	}


	/**
	 * Compute the melting according to the model used.
	 *
	 * @param model is the string containing the name of the model chosen 
	 * @param combinedMeltingFactor is the combined melting factor
	 * @param temperature is the input temperature
	 * @param meltingTemperature is the melting temperature
	 * @param skyviewValue is the the skyview factor value
	 * @param radiationFactor is the radiation factor
	 * @param shortwaveRadiation is the shortwave radiation
	 * @return the double value of the snowmelt
	 */

	private double computeMelting(){
		// compute the snowmelt 

		snowModel=SimpleModelFactory.createModel(model, combinedMeltingFactor, temperature, meltingTemperature, 
				skyviewValue, radiationFactor, shortwaveRadiation,EI);
		
		return snowModel.snowValues();

		//return Math.min(snowModel.snowValues(), SWE);				
	}

	private double computeSolidWater(double initialConditionSolidWater, double freezing){
		// solve the differential equation for the solid water
		double solidWater=initialConditionSolidWater+ dt * (snowfall + freezing - melting);  
		if (solidWater<0){ 
			solidWater=0; 
			melting=0;
		}	
		return solidWater;	
	}


	void computeLiquidWater(double initialConditionLiquidWater, double freezing, double melting){
		// solve the differential equation for the liquid water
		liquidWater=initialConditionLiquidWater+ dt * (rainfall - freezing + melting); 
		if (liquidWater<0) liquidWater=0;	

	}

	/**
	 * Compute the melting discharge according to the model chosen.
	 *
	 * @return the double value of the melting discharge
	 */
	private double computeMeltingDischarge(double solidWater){
		// compute the maximum value of the liquid water
		double maxLiquidWater = alfa_l * solidWater;


		// compute the melting discharge
		double melting_discharge=0;
		if (liquidWater > maxLiquidWater) {
			melting_discharge = liquidWater - maxLiquidWater;
			liquidWater = maxLiquidWater;		
		}

		return melting_discharge;
	}


	/**
	 * Compute the snow water equivalent.
	 *
	 * @return the double value of the snow water equivalent
	 */
	private double computeSWE(double solidWater){

		SWE=solidWater+liquidWater;
		return SWE;

	}



	/**
	 * Store result_series stores the results in the hashMaps .
	 *
	 * @param ID is the id of the station 
	 * @param meltingDischarge is the melting discharge
	 * @param SWE is the snow water equivalent
	 * @throws SchemaException 
	 */
	private void storeResult_series(Integer ID,double meltingDischarge , double SWE) throws SchemaException {


		outSWEHM.put(ID, new double[]{SWE});

		outMeltingDischargeHM.put(ID, new double[]{meltingDischarge});


	}


}