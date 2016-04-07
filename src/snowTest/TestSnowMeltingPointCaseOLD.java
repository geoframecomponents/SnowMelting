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




import snowMeltingPointCaseOLD.SnowMeltingPointCase;

/**
 * Test the {@link Snow} module.
 * 
 * @author Marialaura BAncheri
 */
public class TestSnowMeltingPointCaseOLD extends HMTestCase {


	public TestSnowMeltingPointCaseOLD() throws Exception {


		String startDate = "2007-10-17 00:00" ;
		String endDate = "2007-10-18 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToRainfall ="resources/Input/rainfall.csv";
		String inPathToSnowfall ="resources/Input/snowfall.csv";
		String inPathToAirT ="resources/Input/Temperature.csv";
		String inPathToSWRB ="resources/Input/DIRETTA.csv";


		OmsTimeSeriesIteratorReader rainfallReader = getTimeseriesReader(inPathToRainfall, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader snowfallReader = getTimeseriesReader(inPathToSnowfall, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader airTReader = getTimeseriesReader(inPathToAirT, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader SWRBReader = getTimeseriesReader(inPathToSWRB, fId, startDate, endDate, timeStepMinutes);



		OmsRasterReader demReader = new OmsRasterReader();
		demReader.file = "resources/Input/dem.asc";
		demReader.fileNovalue = -9999.0;
		demReader.geodataNovalue = Double.NaN;
		demReader.process();
		GridCoverage2D dem = demReader.outRaster;

		OmsRasterReader skyViewReader = new OmsRasterReader();
		skyViewReader.file = "resources/Input/sky.asc";
		skyViewReader.fileNovalue = -9999.0;
		skyViewReader.geodataNovalue = Double.NaN;
		skyViewReader.process();
		GridCoverage2D skyView = skyViewReader.outRaster;

		/*
		OmsRasterReader EIJanReader = new OmsRasterReader();
		EIJanReader.file = "resources/Input/GENNAIO.asc";
		EIJanReader.fileNovalue = -9999.0;
		EIJanReader.geodataNovalue = Double.NaN;
		EIJanReader.process();
		GridCoverage2D gennaio = EIJanReader.outRaster;*/

		OmsRasterReader EIFebReader = new OmsRasterReader();
		EIFebReader.file = "resources/Input/FEBBRAIO.asc";
		EIFebReader.fileNovalue = -9999.0;
		EIFebReader.geodataNovalue = Double.NaN;
		EIFebReader.process();
		GridCoverage2D febbraio = EIFebReader.outRaster;

		/*
		OmsRasterReader EIMarReader = new OmsRasterReader();
		EIMarReader.file = "resources/Input/MARZO.asc";
		EIMarReader.fileNovalue = -9999.0;
		EIMarReader.geodataNovalue = Double.NaN;
		EIMarReader.process();
		GridCoverage2D marzo = EIMarReader.outRaster;

		OmsRasterReader EIAprReader = new OmsRasterReader();
		EIAprReader.file = "resources/Input/APRILE.asc";
		EIAprReader.fileNovalue = -9999.0;
		EIAprReader.geodataNovalue = Double.NaN;
		EIAprReader.process();
		GridCoverage2D aprile = EIAprReader.outRaster;

		OmsRasterReader EIMayReader = new OmsRasterReader();
		EIMayReader.file = "resources/Input/MAGGIO.asc";
		EIMayReader.fileNovalue = -9999.0;
		EIMayReader.geodataNovalue = Double.NaN;
		EIMayReader.process();
		GridCoverage2D maggio = EIMayReader.outRaster;

		OmsRasterReader EIJuneReader = new OmsRasterReader();
		EIJuneReader.file = "resources/Input/GIUGNO.asc";
		EIJuneReader.fileNovalue = -9999.0;
		EIJuneReader.geodataNovalue = Double.NaN;
		EIJuneReader.process();
		GridCoverage2D giugno = EIJuneReader.outRaster; */


		OmsShapefileFeatureReader stationsReader = new OmsShapefileFeatureReader();
		stationsReader.file = "resources/Input/stations.shp";
		stationsReader.readFeatureCollection();
		SimpleFeatureCollection stationsFC = stationsReader.geodata;

		String pathToSWE= "resources/Output/SWE_hourly.csv";
		String pathToMeltingDischarge= "resources/Output/Melting_Hourly.csv";

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
		snow.inInsJan=febbraio;
		snow.inInsFeb=febbraio;
		snow.inInsMar=febbraio;
		snow.inInsApr=febbraio;
		snow.inInsMay=febbraio;
		snow.inInsJun=febbraio;
		snow.inDem = dem;
		snow.inStations = stationsFC;
		snow.fStationsid = "Field2";

		while( airTReader.doProcess  ) { 

			snow.doHourly= true;
			snow.model="Cazorzi";
			snow.tStartDate=startDate;
			snow.fStationsid="Field2";
			snow.combinedMeltingFactor=0.1813454712889037;
			snow.freezingFactor=0.006000776959626719;
			snow.radiationFactor=0.006000776959626719;
			snow.alfa_l = 0.553150174200997571;
			snow.meltingTemperature=-0.64798915634369553;


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
