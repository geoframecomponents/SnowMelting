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
package snowMeltingRasterCase;

import java.awt.image.WritableRaster;


public class EIminimum implements EnergyIndex{

	WritableRaster energyImap;
	double hoursToMonth;
	int height;
	int width;



	public EIminimum(WritableRaster energyImap, double hoursToMonth){

		this.energyImap=energyImap;
		this.hoursToMonth=hoursToMonth;
		height=energyImap.getHeight();
		width=energyImap.getWidth();		
	}

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
