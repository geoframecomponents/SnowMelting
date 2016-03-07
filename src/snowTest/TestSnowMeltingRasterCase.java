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


import org.geotools.coverage.grid.GridCoverage2D;

import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.rasterwriter.OmsRasterWriter;

import org.jgrasstools.hortonmachine.utils.HMTestCase;

import snowMeltingRasterCase.SnowMeltingRasterCase;

/**
 * Test the Snow module.
 * 
 * @author Marialaura Bancheri
 */
public class TestSnowMeltingRasterCase extends HMTestCase {

	GridCoverage2D outSweDataGrid = null;
	GridCoverage2D outMeltingDataGrid = null;

	public TestSnowMeltingRasterCase() throws Exception {


		String startDate = "2007-10-17 00:00" ;

		OmsRasterReader demReader = new OmsRasterReader();
		demReader.file = "/Users/marialaura/Desktop/dottorato/CSU/NeveGiuseppe/data/Maps/dem.asc";
		demReader.fileNovalue = -9999.0;
		demReader.geodataNovalue = Double.NaN;
		demReader.process();
		GridCoverage2D dem = demReader.outRaster;
		
		OmsRasterReader skyViewReader = new OmsRasterReader();
		skyViewReader.file = "/Users/marialaura/Desktop/dottorato/CSU/NeveGiuseppe/data/Maps/sky.asc";
		skyViewReader.fileNovalue = -9999.0;
		skyViewReader.geodataNovalue = Double.NaN;
		skyViewReader.process();
		GridCoverage2D skyView = skyViewReader.outRaster;

		OmsRasterReader EIJanReader = new OmsRasterReader();
		EIJanReader.file = "/Users/marialaura/Desktop/dottorato/CSU/NeveGiuseppe/data/Maps/FEBBRAIO.asc";
		EIJanReader.fileNovalue = -9999.0;
		EIJanReader.geodataNovalue = Double.NaN;
		EIJanReader.process();
		GridCoverage2D gennaio = EIJanReader.outRaster;

		OmsRasterReader EIFebReader = new OmsRasterReader();
		EIFebReader.file = "/Users/marialaura/Desktop/dottorato/CSU/NeveGiuseppe/data/Maps/FEBBRAIO.asc";
		EIFebReader.fileNovalue = -9999.0;
		EIFebReader.geodataNovalue = Double.NaN;
		EIFebReader.process();
		GridCoverage2D febbraio = EIFebReader.outRaster;

		OmsRasterReader EIMarReader = new OmsRasterReader();
		EIMarReader.file = "/Users/marialaura/Desktop/dottorato/CSU/NeveGiuseppe/data/Maps/MARZO.asc";
		EIMarReader.fileNovalue = -9999.0;
		EIMarReader.geodataNovalue = Double.NaN;
		EIMarReader.process();
		GridCoverage2D marzo = EIMarReader.outRaster;

		OmsRasterReader EIAprReader = new OmsRasterReader();
		EIAprReader.file = "/Users/marialaura/Desktop/dottorato/CSU/NeveGiuseppe/data/Maps/APRILE.asc";
		EIAprReader.fileNovalue = -9999.0;
		EIAprReader.geodataNovalue = Double.NaN;
		EIAprReader.process();
		GridCoverage2D aprile = EIAprReader.outRaster;

		OmsRasterReader EIMayReader = new OmsRasterReader();
		EIMayReader.file = "/Users/marialaura/Desktop/dottorato/CSU/NeveGiuseppe/data/Maps/MAGGIO.asc";
		EIMayReader.fileNovalue = -9999.0;
		EIMayReader.geodataNovalue = Double.NaN;
		EIMayReader.process();
		GridCoverage2D maggio = EIMayReader.outRaster;

		OmsRasterReader EIJuneReader = new OmsRasterReader();
		EIJuneReader.file = "/Users/marialaura/Desktop/dottorato/CSU/NeveGiuseppe/data/Maps/GIUGNO.asc";
		EIJuneReader.fileNovalue = -9999.0;
		EIJuneReader.geodataNovalue = Double.NaN;
		EIJuneReader.process();
		GridCoverage2D giugno = EIJuneReader.outRaster;



		SnowMeltingRasterCase snow = new SnowMeltingRasterCase();
		snow.inRainGrid=dem;
		snow.inSnowfallGrid=dem;
		snow.inSolarGrid=dem;
		snow.inTempGrid=dem;	
		snow.inSkyview = skyView;
		snow.inInsJan=gennaio;
		snow.inInsFeb=febbraio;
		snow.inInsMar=marzo;
		snow.inInsApr=aprile;
		snow.inInsMay=maggio;
		snow.inInsJun=giugno;
		snow.inDem = dem;


		snow.timeStep="Hourly";
		snow.model="Cazorzi";
		snow.tStartDate=startDate;
		snow.combinedMeltingFactor=0.1813454712889037;
		snow.freezingFactor=0.006000776959626719;
		snow.radiationFactor=0.006000776959626719;
		snow.alfa_r=1.12963980507173877;
		snow.alfa_s= 1.07229882570334652;
		snow.alfa_l = 0.553150174200997571;
		snow.meltingTemperature=-0.64798915634369553;

		snow.pm = pm;

		snow.process();



		outMeltingDataGrid = snow.outMeltingGrid;
		outSweDataGrid = snow.outSWEGrid;

		OmsRasterWriter writerSWEraster = new OmsRasterWriter();
		writerSWEraster.inRaster = outSweDataGrid;
		writerSWEraster.file = "/Users/marialaura/Desktop/dottorato/CSU/NeveGiuseppe/data/JoeWrigth/mapSWE.asc";
		writerSWEraster.process();

		OmsRasterWriter writerMeltingraster = new OmsRasterWriter();
		writerMeltingraster.inRaster = outMeltingDataGrid;
		writerMeltingraster.file = "/Users/marialaura/Desktop/dottorato/CSU/NeveGiuseppe/data/JoeWrigth/mapMelting.asc";
		writerMeltingraster.process();

	}


}
