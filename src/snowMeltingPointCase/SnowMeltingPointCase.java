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


import org.jgrasstools.gears.libs.modules.JGTConstants;

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
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.utils.CrsUtilities;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.jgrasstools.gears.utils.geometry.GeometryUtilities;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


@Description("The component computes the snow water equivalent and the melting discharge with"
		+ "punctual data. The inputs of the components are the rainfall,"
		+ "the shortwave, the temperature values, the skyview, the energy index maps and the DEM maps")
@Author(name = "Marialaura Bancheri & Giuseppe Formetta", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Snow Model")
@Label(JGTConstants.HYDROGEOMORPHOLOGY)
@Name("Snow")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class SnowMeltingPointCase extends JGTModel {


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

	@Description("The Hashmap with the time series of the temperature values")
	@In
	public HashMap<Integer, double[]> inTemperatureValues;

	@Description("The double value of the  temperature, once read from the HashMap")
	double temperature;

	@Description("The timeStep allows to chose between the hourly time step"
			+ " or the daily time step. It could be: "
			+ " Hourly or Daily")
	@In
	public String timeStep;
	
	@Description("It is needed to iterate on the date")
	int step;

	@Description("The map of the skyview factor.")
	@In
	public GridCoverage2D inSkyview;
	WritableRaster skyview;

	@Description("the skyview factor value, read from the map")
	double skyviewValue;

	@Description("The digital elevation model.")
	@In
	public GridCoverage2D inDem;

	@Description("The map of the energy index for the month of January")
	@In
	public GridCoverage2D inInsJan;
	WritableRaster energyIJanuary;

	@Description("The map of the energy index for the month of February")
	@In
	public GridCoverage2D inInsFeb;
	WritableRaster energyIFebruary;

	@Description("The map of the energy index for the month of Marh")
	@In
	public GridCoverage2D inInsMar;
	WritableRaster energyIMarch;

	@Description("The map of the energy index for the month of April")
	@In
	public GridCoverage2D inInsApr;
	WritableRaster energyIApril;

	@Description("The map of the energy index for the month of May")
	@In
	public GridCoverage2D inInsMay;
	WritableRaster energyIMay;

	@Description("The map of the energy index for the month of June")
	@In
	public GridCoverage2D inInsJun;
	WritableRaster energyIJune;

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
			+ " Classical; Cazorzi; Hoock")
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

	@Description("The energy index value")
	double EIvalue;
	EnergyIndex EImode;

	SnowModel snowModel;

	@Description("solid water value obtained from the soultion of the budget")
	double solidWater;

	@Description("liquid water value obtained from the soultion of the budget")
	double liquidWater;

	@Description("The maximum value of the liquid water")
	double maxLiquidWater;

	@Description("Integration interval")
	double dt=1;

	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(DateTimeZone.UTC);

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

	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception { 

		// This 2 operations allow to define if we are working with daily or hourly time step
		// if we are working with Daily time step, every time it adds to the start date a day
		// otherwise it adds an hour, "step increments at the end of the process
		// the actual date is needed to compute the actual energy index	
		DateTime startDateTime = formatter.parseDateTime(tStartDate);
		DateTime date=(timeStep.equals("Daily"))?startDateTime.plusDays(step):startDateTime.plusHours(step);

		// computing the reference system of the input DEM
		CoordinateReferenceSystem sourceCRS = inDem.getCoordinateReferenceSystem2D();

		//  from pixel coordinates (in coverage image) to geographic coordinates (in coverage CRS)
		MathTransform transf = inDem.getGridGeometry().getCRSToGrid2D();

		// transform the GrifCoverage2D maps into writable rasters
		if(step==0){
			skyview=mapsTransform(inSkyview);
			energyIJanuary=mapsTransform (inInsJan);
			energyIFebruary=mapsTransform (inInsFeb);
			energyIMarch=mapsTransform (inInsMar);
			energyIApril=mapsTransform (inInsApr);
			energyIMay=mapsTransform (inInsMay);
			energyIJune=mapsTransform (inInsJun);
		}



		// starting from the shp file containing the stations, get the coordinate
		//of each station
		stationCoordinates = getCoordinate(inStations, fStationsid);

		//create the set of the coordinate of the station, so we can 
		//iterate over the set
		Set<Integer> stationCoordinatesIdSet = stationCoordinates.keySet();
		Iterator<Integer> idIterator = stationCoordinatesIdSet.iterator();

		// trasform the list of idStation into an array
		idStations= stationCoordinatesIdSet.toArray();

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

			//reproject the map in WGS84 and compute the latitude
			Point [] idPoint=getPoint(coordinate,sourceCRS, targetCRS);
			latitudeStation.add(Math.toRadians(idPoint[0].getY()));


			// read the input data for the given station
			temperature=inTemperatureValues.get(idStations[i])[0];
			rainfall=inRainfallValues.get(idStations[i])[0];
			snowfall=inSnowfallValues.get(idStations[i])[0];
			shortwaveRadiation=inShortwaveRadiationValues.get(idStations[i])[0];

			//read the input skyview for the given station position
			skyviewValue=skyview.getSampleDouble(columnStation.get(i), rowStation.get(i), 0);

			// compute the energy index, considering the two cases, daily and hourly:
			// if it is daily is the value in the map in the station position 
			// if it is hourly, we have to distinguish between night and day. During night 
			//the value is the minimum of the map, during the day is the value at the 
			//given station position
			EImode=SimpleEIFactory.createModel(timeStep, date, latitudeStation.get(i), 
					columnStation.get(i), rowStation.get(i), energyIJanuary,
					energyIFebruary, energyIMarch,  energyIApril,energyIMay, energyIJune);

			EIvalue=EImode.eiValues();

			// compute the melting and the discharge and stores the results into Hashmap
			storeResult_series((Integer)idStations[i],computeMeltingDischarge(), computeSWE());

		}

		// upgrade the step for the date
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
	 * Gets the point coordinates (row and column) of the station, after its reprojection in WGS84.
	 *
	 * @param coordinate is the coordinate of the point in the original reference system
	 * @param sourceCRS the original reference system
	 * @param targetCRS is the WGS84 system
	 * @return the point vector with the x and y values of its position
	 * @throws Exception 
	 */
	private Point[] getPoint(Coordinate coordinate, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS)
			throws Exception{
		Point[] point = new Point[] { GeometryUtilities.gf().createPoint(coordinate) };
		CrsUtilities.reproject(sourceCRS, targetCRS, point);
		return point;
	}

	/**
	 * Compute the melting discharge according to the model chosen.
	 *
	 * @return the double value of the melting discharge
	 */
	private double computeMeltingDischarge(){
		
		// compute the snowmelt 
		double melting=(temperature>meltingTemperature)?computeMelting(model,combinedMeltingFactor, temperature, 
				meltingTemperature, EIvalue,skyviewValue,radiationFactor,shortwaveRadiation):0;

		melting = Math.min(melting, SWE);

		// compute the freezing
		double freezing=(temperature<meltingTemperature)?computeFreezing(freezingFactor, temperature,meltingTemperature):0;


		// solve the differential equation for the solid water
		solidWater=solidWater+ dt * (snowfall + freezing - melting);  
		if (solidWater<0){ 
			solidWater=0; 
			melting=0;
		}


		// solve the differential equation for the liquid water
		liquidWater=liquidWater+ dt * (rainfall - freezing + melting); 
		if (liquidWater<0) liquidWater=0;

		// compute the maximum value of the liquid water
		maxLiquidWater = alfa_l * solidWater;

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
	private double computeSWE(){

		SWE=solidWater+liquidWater;
		return SWE;

	}





	/**
	 * Compute the melting according to the model used.
	 *
	 * @param model is the string containing the name of the model chosen 
	 * @param combinedMeltingFactor is the combined melting factor
	 * @param temperature is the input temperature
	 * @param meltingTemperature is the melting temperature
	 * @param EIvalue is the energy index value
	 * @param skyviewValue is the the skyview factor value
	 * @param radiationFactor is the radiation factor
	 * @param shortwaveRadiation is the shortwave radiation
	 * @return the double value of the snowmelt
	 */
	private double computeMelting(String model,double combinedMeltingFactor,double temperature,double meltingTemperature,double EIvalue,
			double skyviewValue,double radiationFactor,double shortwaveRadiation) {

		snowModel=SimpleModelFactory.createModel(model, combinedMeltingFactor, temperature, meltingTemperature, 
				EIvalue, skyviewValue, radiationFactor, shortwaveRadiation);

		return snowModel.snowValues();
	}



	/**
	 * Compute the freezing.
	 *
	 * @param freezingFactor the freezing factor
	 * @param temperature the temperature
	 * @param meltingTemperature the melting temperature
	 * @return the double
	 */
	private double computeFreezing(double freezingFactor, double temperature,
			double meltingTemperature) {

		return freezingFactor*(meltingTemperature-temperature);
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