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
import org.jgrasstools.hortonmachine.utils.HMTestCase;


import snowMeltingPointCaseSWRB.SnowMeltingPointCaseSWRB;

/**
 * Test the {@link Snow} module.
 * 
 * @author Marialaura BAncheri
 */
public class TestSnowMeltingPointCaseSWRB extends HMTestCase {


	public TestSnowMeltingPointCaseSWRB() throws Exception {


		String startDate = "1994-01-01 00:00" ;
		String endDate = "1994-03-01 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToRainfall ="/Users/marialaura/Desktop/output/Posina/rain.csv";
		String inPathToSnowfall ="/Users/marialaura/Desktop/output/Posina/snow.csv";
		String inPathToAirT ="/Users/marialaura/Desktop/data/Posina/Subbasin_kriging_temp.csv";
		String inPathToSWRB ="/Users/marialaura/Desktop/output/Posina/DIRETTA.csv";


		OmsTimeSeriesIteratorReader rainfallReader = getTimeseriesReader(inPathToRainfall, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader snowfallReader = getTimeseriesReader(inPathToSnowfall, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader airTReader = getTimeseriesReader(inPathToAirT, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader SWRBReader = getTimeseriesReader(inPathToSWRB, fId, startDate, endDate, timeStepMinutes);



		OmsRasterReader demReader = new OmsRasterReader();
		demReader.file = "/Users/marialaura/Desktop/data/Posina/dem_stazioni.asc";
		demReader.fileNovalue = -9999.0;
		demReader.geodataNovalue = Double.NaN;
		demReader.process();
		GridCoverage2D dem = demReader.outRaster;

		OmsRasterReader skyViewReader = new OmsRasterReader();
		skyViewReader.file = "/Users/marialaura/Desktop/data/Posina/skystaz.asc";
		skyViewReader.fileNovalue = -9999.0;
		skyViewReader.geodataNovalue = Double.NaN;
		skyViewReader.process();
		GridCoverage2D skyView = skyViewReader.outRaster;


		
		OmsShapefileFeatureReader stationsReader = new OmsShapefileFeatureReader();
		stationsReader.file = "/Users/marialaura/Desktop/Posina2marialura/prova/centroid_netnum__8.shp";
		stationsReader.readFeatureCollection();
		SimpleFeatureCollection stationsFC = stationsReader.geodata;

		String pathToSWE= "/Users/marialaura/Desktop/SWE_hourly_SWRB.csv";
		String pathToMeltingDischarge= "/Users/marialaura/Desktop/Melting_Hourly_SWRB.csv";

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


		SnowMeltingPointCaseSWRB snow = new SnowMeltingPointCaseSWRB();
		snow.inSkyview = skyView;
		snow.inDem = dem;
		snow.inStations = stationsFC;
		snow.fStationsid = "netnum";

		while( airTReader.doProcess  ) { 

			snow.model="Classical";
			snow.tStartDate=startDate;
			snow.combinedMeltingFactor=0.00856;
			snow.freezingFactor=0.0367;
			snow.radiationFactor=0.006000776959626719;
			snow.alfa_l = 0.949;
			snow.meltingTemperature=1.94;


			airTReader.nextRecord();	
			HashMap<Integer, double[]> id2ValueMap = airTReader.outData;
			snow.inTemperatureValues= id2ValueMap;

			rainfallReader.nextRecord();
			id2ValueMap = rainfallReader.outData;
			snow.inRainfallValues = id2ValueMap;

			snowfallReader.nextRecord();
			id2ValueMap = snowfallReader.outData;
			snow.inSnowfallValues = id2ValueMap;

			SWRBReader.nextRecord();
			id2ValueMap = SWRBReader.outData;
			snow.inShortwaveRadiationValues = id2ValueMap;

			snow.pm = pm;

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
		SWRBReader.close();

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
