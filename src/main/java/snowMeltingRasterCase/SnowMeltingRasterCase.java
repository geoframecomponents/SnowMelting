/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package snowMeltingRasterCase;


import org.hortonmachine.gears.libs.modules.HMConstants;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.LinkedHashMap;



import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;



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
import snowMeltingPointCase.SimpleModelFactory;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.hortonmachine.gears.libs.modules.HMModel;
import org.hortonmachine.gears.utils.CrsUtilities;
import org.hortonmachine.gears.utils.RegionMap;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/*
 * Replaced with lines 40 and 41 
 * https://sourceforge.net/p/geotools/mailman/message/36652855/
 */
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.Point;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

@Description("The component computes the snow water equivalent and the melting discharge both with"
		+ "raster data. The inputs of the components are the precipitation,"
		+ "the shortwave, the temperature values, the skyview and the energy index maps and the DEM")
@Author(name = "Marialaura Bancheri & Giuseppe Formetta", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Snow Model")
@Label(HMConstants.HYDROGEOMORPHOLOGY)
@Name("Snow")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class SnowMeltingRasterCase extends HMModel {

	@Description("The map of the interpolated temperature.")
	@In
	public GridCoverage2D inTempGrid;

	@Description("The double value of the  temperature")
	double temperature;

	@Description("The map of the the interpolated precipitation.")
	@In
	public GridCoverage2D inRainGrid;

	@Description("The double value of the rainfall")
	double rainfall;

	@Description("The map of the interpolated solar radiation.")
	@In
	public GridCoverage2D inSnowfallGrid;

	@Description("The double value of the snowfall")
	double snowfall;

	@Description("The map of the interpolated solar radiation.")
	@In
	public GridCoverage2D inSolarGrid;

	@Description("The double value of the  shortwave radiation")
	double shortwaveRadiation;


	@Description("doHourly allows to chose between the hourly time step"
			+ " or the daily time step. It could be: "
			+ " Hourly--> true or Daily-->false")
	@In
    public boolean doHourly;

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

	@Description("Alfa_r is the adjustment parameter for the precipitation measurements errors")
	@In
	public double alfa_r;

	@Description("Alfa_s is the adjustment parameter for the snow measurements errors")
	@In
	public double alfa_s;

	@Description("Alfa_l is the coefficient for the computation of the maximum liquid water")
	@In
	public double alfa_l;

	@Description("m1 is the smoothing parameter, for the detecting ot the rainfall in "
			+ "the total precipitation")
	@In
	public double m1 = 1.0;

	@Description("List of the latitudes of the station ")
	ArrayList <Double> latitudeStation= new ArrayList <Double>();

	@Description("It is needed to iterate on the date")
	int step;
	
	SnowModelRaster snowModelRaster;

	@Description("The energy index value")
	double EIvalue;
	EnergyIndex EImode;


	@Description("solid water value obtained from the soultion of the budget")
	double solidWater;

	@Description("liquid water value obtained from the soultion of the budget")
	double liquidWater;

	@Description("The maximum value of the liquid water")
	double maxLiquidWater;
	
	double melting;

	@Description("Integration interval")
	double dt=1;

	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(DateTimeZone.UTC);

	@Description("Final target CRS")
	CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;

	@Description("The output melting dicharge map")
	@Out
	public GridCoverage2D outMeltingGrid;

	@Description("The output melting dicharge map")
	@Out
	public GridCoverage2D outSWEGrid;


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
		DateTime date=(doHourly==false)?startDateTime.plusDays(step):startDateTime.plusHours(step).plusMinutes(30);

		// computing the reference system of the input DEM
		CoordinateReferenceSystem sourceCRS = inDem.getCoordinateReferenceSystem2D();


		// transform the GrifCoverage2D maps into writable rasters
		if(step==0){
			skyview=mapsReader(inSkyview);
			energyIJanuary=mapsReader(inInsJan);
			energyIFebruary=mapsReader(inInsFeb);
			energyIMarch=mapsReader(inInsMar);
			energyIApril=mapsReader(inInsApr);
			energyIMay=mapsReader(inInsMay);
			energyIJune=mapsReader(inInsJun);
			
			solidWater=0;
			liquidWater=0;
		}



		// transform the GrifCoverage2D maps into writable rasters
		WritableRaster temperatureMap=mapsReader(inTempGrid);
		WritableRaster rainfallMap=mapsReader(inRainGrid);
		WritableRaster snowfallMap=mapsReader(inSnowfallGrid);
		WritableRaster radiationMap=mapsReader(inSolarGrid);



		// get the dimension of the maps
		RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inDem);
		int cols = regionMap.getCols();
		int rows = regionMap.getRows();

		WritableRaster outMeltingWritableRaster = CoverageUtilities.createWritableRaster(cols, rows, null, null, null);
		WritableRaster outSWEWritableRaster =CoverageUtilities.createWritableRaster(cols, rows, null, null, null); 

		WritableRandomIter MeltingIter = RandomIterFactory.createWritable(outMeltingWritableRaster, null);       
		WritableRandomIter SWEIter = RandomIterFactory.createWritable(outSWEWritableRaster, null);

		// get the geometry of the maps and the coordinates of the stations
		GridGeometry2D inDemGridGeo = inDem.getGridGeometry();
		stationCoordinates = getCoordinate(inDemGridGeo);

		// iterate over the entire domain and compute for each pixel the SWE
		for( int r = 1; r < rows - 1; r++ ) {
			for( int c = 1; c < cols - 1; c++ ) {
				int k=0;

				// get the exact value of the variable in the pixel i, j 
				temperature=temperatureMap.getSampleDouble(c, r, 0);
				rainfall=rainfallMap.getSampleDouble(c, r, 0);
				snowfall=snowfallMap.getSampleDouble(c, r, 0);
				shortwaveRadiation=radiationMap.getSampleDouble(c, r, 0);

				// get the coordinate of the given pixel
				Coordinate coordinate = (Coordinate) stationCoordinates.get(k);

				//compute the latitude, after a reprojection in WGS 84
				Point [] idPoint=getPoint(coordinate,sourceCRS, targetCRS);
				latitudeStation.add(Math.toRadians(idPoint[0].getY()));

				// compute the energy index as in the previous case
				EImode=SimpleEIFactory.createModel(doHourly, date, latitudeStation.get(k), c, r, 
						energyIJanuary,energyIFebruary, energyIMarch,  energyIApril,energyIMay, energyIJune);

				EIvalue=EImode.eiValues();
				
				
				double freezing=(temperature<meltingTemperature)?computeFreezing():0;
				
				 melting=(temperature>meltingTemperature)?computeMelting():0;
				 
				solidWater=computeSolidWater(solidWater,freezing);
				computeLiquidWater(liquidWater, freezing, melting);



				MeltingIter.setSample(c, r, 0, computeMeltingDischarge(solidWater));
				SWEIter.setSample(c, r, 0, computeSWE(solidWater));

				// the index k is for the loop over the list
				k++;
				
				

			}
		}

		CoverageUtilities.setNovalueBorder(outMeltingWritableRaster);
		CoverageUtilities.setNovalueBorder(outSWEWritableRaster);

		outMeltingGrid = CoverageUtilities.buildCoverage("Melting", outMeltingWritableRaster, 
				regionMap, inDem.getCoordinateReferenceSystem());
		outSWEGrid= CoverageUtilities.buildCoverage("SWE", outSWEWritableRaster, 
				regionMap, inDem.getCoordinateReferenceSystem());

		// upgrade the step for the new date
		step++;	

	}

	/**
	 * Maps reader transform the GrifCoverage2D in to the writable raster and
	 * replace the -9999.0 value with no value.
	 *
	 * @param inValues: the input map values
	 * @return the writable raster of the given map
	 */
	private WritableRaster mapsReader ( GridCoverage2D inValues){	
		RenderedImage inValuesRenderedImage = inValues.getRenderedImage();
		WritableRaster inValuesWR = CoverageUtilities.replaceNovalue(inValuesRenderedImage, -9999.0);
		inValuesRenderedImage = null;
		return inValuesWR;
	}



	/**
	 * Gets the coordinate of each pixel of the given map.
	 *
	 * @param GridGeometry2D grid is the map 
	 * @return the coordinate of each point
	 */
	private LinkedHashMap<Integer, Coordinate> getCoordinate(GridGeometry2D grid) {
		LinkedHashMap<Integer, Coordinate> out = new LinkedHashMap<Integer, Coordinate>();
		int count = 0;
		RegionMap regionMap = CoverageUtilities.gridGeometry2RegionParamsMap(grid);
		double cols = regionMap.getCols();
		double rows = regionMap.getRows();
		double south = regionMap.getSouth();
		double west = regionMap.getWest();
		double xres = regionMap.getXres();
		double yres = regionMap.getYres();
		double northing = south;
		double easting = west;
		for (int i = 0; i < cols; i++) {
			easting = easting + xres;
			for (int j = 0; j < rows; j++) {
				northing = northing + yres;
				Coordinate coordinate = new Coordinate();
				coordinate.x = west + i * xres;
				coordinate.y = south + j * yres;
				out.put(count, coordinate);
				count++;
			}
		}

		return out;
	}


	/**
	 * Gets the point.
	 *
	 * @param coordinate the coordinate
	 * @param sourceCRS is the source crs
	 * @param targetCRS the target crs
	 * @return the point
	 * @throws Exception the exception
	 */
	private Point[] getPoint(Coordinate coordinate, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) 
			throws Exception{
		Point[] point = new Point[] { GeometryUtilities.gf().createPoint(coordinate) };
		CrsUtilities.reproject(sourceCRS, targetCRS, point);
		return point;
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
		snowModelRaster=SimpleModelFactoryRaster.createModelRaster(model, combinedMeltingFactor, temperature, meltingTemperature, skyviewValue, radiationFactor, shortwaveRadiation, EIvalue);
		return snowModelRaster.snowValues();
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



}