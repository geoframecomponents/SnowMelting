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
package snowTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.shapefile.OmsShapefileFeatureReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import snowMeltingPointCase.SnowMeltingPointCase;

/**
 * Test the {@link Snow} module.
 * 
 * @author Marialaura BAncheri
 */
public class TestSnowMeltingPointCaseClassical_1h {

	@Test
	public void Test() throws Exception {


		String startDate = "2013-11-01 00:00";
		String endDate = "2013-11-02 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToRainfall ="resources/myinput/snowrainsep_precip_10_1_impulso.csv";
		String inPathToSnowfall ="resources/myinput/snowrainsep_snow_10_1_impulso.csv";
		String inPathToAirT ="resources/myinput/airT_10.csv";



		OmsTimeSeriesIteratorReader rainfallReader = getTimeseriesReader(inPathToRainfall, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader snowfallReader = getTimeseriesReader(inPathToSnowfall, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader airTReader = getTimeseriesReader(inPathToAirT, fId, startDate, endDate, timeStepMinutes);



		OmsRasterReader demReader = new OmsRasterReader();
		demReader.file = "resources/myinput/dtm_10.asc";
		demReader.fileNovalue = -9999.0;
		demReader.geodataNovalue = Double.NaN;
		demReader.process();
		GridCoverage2D dem = demReader.outRaster;

		OmsRasterReader skyViewReader = new OmsRasterReader();
		skyViewReader.file = "resources/myinput/sky_10.asc";
		skyViewReader.fileNovalue = -9999.0;
		skyViewReader.geodataNovalue = Double.NaN;
		skyViewReader.process();
		GridCoverage2D skyView = skyViewReader.outRaster;


		
		OmsShapefileFeatureReader stationsReader = new OmsShapefileFeatureReader();
		stationsReader.file = "resources/myinput/centroide_10.shp";
		stationsReader.readFeatureCollection();
		SimpleFeatureCollection stationsFC = stationsReader.geodata;

		String pathToSWE= "resources/myinput/SWEClassical_1_impulso_hourly.csv";
		String pathToMeltingDischarge= "resources/myinput/MeltingClassical_1_impulso_hourly.csv";

		OmsTimeSeriesIteratorWriter writerSWE = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerMelting = new OmsTimeSeriesIteratorWriter();


		writerSWE.file = pathToSWE;
		writerSWE.tStart = startDate;
		writerSWE.tTimestep = timeStepMinutes;
		writerSWE.fileNovalue="-9999";

		writerMelting.file = pathToMeltingDischarge;
		writerMelting.tStart = startDate;
		writerMelting.tTimestep = timeStepMinutes;
		writerMelting.fileNovalue="-9999";


		SnowMeltingPointCase snow = new SnowMeltingPointCase();
		snow.inSkyview = skyView;
		snow.inDem = dem;
		snow.inStations = stationsFC;
		snow.fStationsid = "basinid";

		while( airTReader.doProcess  ) { 

			snow.model="Classical";
			snow.tStartDate=startDate;
			snow.combinedMeltingFactor=0.0955102*24;
			snow.freezingFactor=0.0089217*24;
			snow.radiationFactor=0.000061;
			snow.alfa_l = 0.3504315;
			snow.meltingTemperature=0;
			snow.timeStepMinutes = 60;


			airTReader.nextRecord();	
			HashMap<Integer, double[]> id2ValueMap = airTReader.outData;
			snow.inTemperatureValues= id2ValueMap;

			String date = airTReader.tCurrent;

			
			rainfallReader.nextRecord();
			id2ValueMap = rainfallReader.outData;
			snow.inRainfallValues = id2ValueMap;
			
			snowfallReader.nextRecord();
			id2ValueMap = snowfallReader.outData;
			snow.inSnowfallValues = id2ValueMap;


			snow.process();


			HashMap<Integer, double[]> outHM = snow.outSWEHM;
			HashMap<Integer, double[]> outHMQ = snow.outMeltingDischargeHM;

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
