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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geoframe.blogspot.snowmelting.testpointcase;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.hortonmachine.gears.io.rasterreader.OmsRasterReader;
import org.hortonmachine.gears.io.shapefile.OmsShapefileFeatureReader;
import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import it.geoframe.blogspot.snowmelting.pointcase.SnowMeltingPointCaseHock;

/**
 * Test the {@link Snow} module.
 * 
 * @author Niccolo` Tubini
 */
public class TestSnowMeltingPointCaseHock_5min {

	@Test
	public void Test() throws Exception {


		String startDate = "2013-10-31 00:00";
		String endDate = "2013-12-01 00:00";
		int timeStepMinutes = 5;
		String fId = "ID";

		String inPathToRainfall ="resources/Input/snowrainsep_precip_10_5min_1_impulso.csv";
		String inPathToSnowfall ="resources/Input/snowrainsep_snow_10_5min_1_impulso.csv";
		String inPathToAirT ="resources/Input/airT_10_5min.csv";
		String inPathToShortwave ="resources/Input/Total_10_5min.csv";



		OmsTimeSeriesIteratorReader rainfallReader = getTimeseriesReader(inPathToRainfall, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader snowfallReader = getTimeseriesReader(inPathToSnowfall, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader airTReader = getTimeseriesReader(inPathToAirT, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader swReader = getTimeseriesReader(inPathToShortwave, fId, startDate, endDate, timeStepMinutes);

		
		OmsShapefileFeatureReader stationsReader = new OmsShapefileFeatureReader();
		stationsReader.file = "resources/Input/centroide_10.shp";
		stationsReader.readFeatureCollection();
		SimpleFeatureCollection stationsFC = stationsReader.geodata;

		String pathToSWE = "resources/Output/SWEHock_1_impulso_5min_new.csv";
		String pathToMeltingDischarge = "resources/Output/MeltingHock_1_impulso_5min_new.csv";
		String pathToErrorODESolidWater = "resources/Output/errorSolidWater_5min.csv";
		String pathToErrorODELiquidWater = "resources/Output/errorLiquidWater_5min.csv";
		String pathToErrorSWE = "resources/Output/errorSWE_5min.csv";
		

		OmsTimeSeriesIteratorWriter writerSWE = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerMelting = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerErrorODESolidWater = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerErrorODELiquidWater = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerErrorSWE= new OmsTimeSeriesIteratorWriter();
		
		OmsRasterReader skyViewReader = new OmsRasterReader();
		skyViewReader.file = "resources/Input/sky_10.asc";
		skyViewReader.process();
		GridCoverage2D skyView = skyViewReader.outRaster;


		writerSWE.file = pathToSWE;
		writerSWE.tStart = startDate;
		writerSWE.tTimestep = timeStepMinutes;
		writerSWE.fileNovalue="-9999";

		writerMelting.file = pathToMeltingDischarge;
		writerMelting.tStart = startDate;
		writerMelting.tTimestep = timeStepMinutes;
		writerMelting.fileNovalue="-9999";
		
		writerErrorODESolidWater.file = pathToErrorODESolidWater;
		writerErrorODESolidWater.tStart = startDate;
		writerErrorODESolidWater.tTimestep = timeStepMinutes;
		writerErrorODESolidWater.fileNovalue="-9999";
		
		writerErrorODELiquidWater.file = pathToErrorODELiquidWater;
		writerErrorODELiquidWater.tStart = startDate;
		writerErrorODELiquidWater.tTimestep = timeStepMinutes;
		writerErrorODELiquidWater.fileNovalue="-9999";
		
		writerErrorSWE.file = pathToErrorSWE;
		writerErrorSWE.tStart = startDate;
		writerErrorSWE.tTimestep = timeStepMinutes;
		writerErrorSWE.fileNovalue="-9999";


		SnowMeltingPointCaseHock snow = new SnowMeltingPointCaseHock();

		snow.inStations = stationsFC;
		snow.fStationsid = "basinid";
		snow.inSkyview = skyView;

		
		while( airTReader.doProcess  ) { 

		
			snow.combinedMeltingFactor=0.0955102;
			snow.freezingFactor=0.0089217;
			snow.radiationFactor=0.00001;
			snow.alfa_l = 0.3504315;
			snow.meltingTemperature=0;
			snow.timeStepMinutes = 5;

			airTReader.nextRecord();	
			HashMap<Integer, double[]> id2ValueMap = airTReader.outData;
			snow.inTemperatureValues= id2ValueMap;

			
			rainfallReader.nextRecord();
			id2ValueMap = rainfallReader.outData;
			snow.inRainfallValues = id2ValueMap;
			
			snowfallReader.nextRecord();
			id2ValueMap = snowfallReader.outData;
			snow.inSnowfallValues = id2ValueMap;
			
			swReader.nextRecord();
			id2ValueMap = swReader.outData;
			snow.inShortwaveValues = id2ValueMap;

//			System.out.println(snowfallReader.tCurrent);

			snow.process();


			HashMap<Integer, double[]> outHM = snow.outSWEHM;
			HashMap<Integer, double[]> outHMQ = snow.outMeltingDischargeHM;
			HashMap<Integer, double[]> outHMErrorSolidWater = snow.outErrorODESolidWaterHM;
			HashMap<Integer, double[]> outHMErrorLiquidWater = snow.outErrorODELiquidWaterHM;
			HashMap<Integer, double[]> outHMErrorSWE = snow.outErrorSWEHM;
			

			writerSWE.inData = outHM;
			writerSWE.writeNextLine();
			
			if (pathToSWE != null) {
				writerSWE.close();
			}

			writerMelting.inData = outHMQ;
			writerMelting.writeNextLine();

			if (pathToMeltingDischarge != null) {
				writerMelting.close();
			}
			
			writerErrorODESolidWater.inData = outHMErrorSolidWater;
			writerErrorODESolidWater.writeNextLine();

			if (pathToErrorODESolidWater != null) {
				writerErrorODESolidWater.close();
			}
			
			writerErrorODELiquidWater.inData = outHMErrorLiquidWater;
			writerErrorODELiquidWater.writeNextLine();

			if (pathToErrorODELiquidWater != null) {
				writerErrorODELiquidWater.close();
			}
			
			writerErrorSWE.inData = outHMErrorSWE;
			writerErrorSWE.writeNextLine();

			if (pathToErrorSWE != null) {
				writerErrorSWE.close();
			}


		}

		airTReader.close();
		rainfallReader.close();

	}

	private OmsTimeSeriesIteratorReader getTimeseriesReader( String inPath, String id, String startDate, String endDate,
			int timeStepMinutes ) throws URISyntaxException {
		OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
		reader.file = inPath;
		reader.idfield = "ID";
		reader.tStart = startDate;
		reader.tTimestep = timeStepMinutes;
		reader.tEnd = endDate;
		reader.fileNovalue = "-9999";
		reader.initProcess();
		return reader;
	}

}
