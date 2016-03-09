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
package snowMeltingPointCaseEI;
import java.awt.image.WritableRaster;

// TODO: Auto-generated Javadoc
/**
 * The Class EIdaily.
 */
public class EIdaily implements EnergyIndex{


	/** The energy index map. */
	WritableRaster energyImap;
	
	/** The i index of the column of the pixel in the DEM. */
	int i;
	
	/** The j index of the row of the pixel in the DEM. */
	int j;
	
	/** The hours to the considered month from the 21st of December. */
	double hoursToMonth;
	

	/**
	 * Instantiates a new energy index model daily hour.
	 *
	 * @param energyImap the energy index map
	 * @param i the i index
	 * @param j the j index 
	 * @param hoursToMonth the hours to month considered month from the 21st of December
	 */
	public EIdaily(WritableRaster energyImap, int i, int j, double hoursToMonth){
		
	this.energyImap=energyImap;
	this.i=i;
	this.j=j;
	this.hoursToMonth=hoursToMonth;

	}

	/* (non-Javadoc)
	 * @see snowMeltingPointCase.EnergyIndex#eiValues()
	 */
	public double eiValues() {
		// TODO Auto-generated method stub
		return energyImap.getSampleDouble(i, j, 0) / (hoursToMonth );
	}



}
