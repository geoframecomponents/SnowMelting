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

import java.awt.image.WritableRaster;


// TODO: Auto-generated Javadoc
/**
 * The Class EIminimum.
 */
public class EIminimum implements EnergyIndex{

	/** The energy index map. */
	WritableRaster energyImap;
	
	/** The hours to the considered month from the 21st December. */
	double hoursToMonth;
	
	/** The height of the input map. */
	int height;
	
	/** The width of the input map. */
	int width;



	/**
	 * Instantiates a new energy index minimum model.
	 *
	 * @param energyImap the energy index map
	 * @param hoursToMonth the hours to the considered month
	 */
	public EIminimum(WritableRaster energyImap, double hoursToMonth){

		this.energyImap=energyImap;
		this.hoursToMonth=hoursToMonth;
		height=energyImap.getHeight();
		width=energyImap.getWidth();		
	}

	/* (non-Javadoc)
	 * @see snowMeltingPointCase.EnergyIndex#eiValues()
	 */
	public double eiValues() {

		double minimo = 10000000;
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				if (energyImap.getSample(i, j, 0) != -9999) {
					if (energyImap.getSample(i, j, 0) / hoursToMonth > 0.0
							&& energyImap.getSample(i, j, 0) / hoursToMonth < minimo) {
						minimo = energyImap.getSample(i, j, 0) / hoursToMonth;

					}
				}
			}
		}

		return minimo;

	}





}
