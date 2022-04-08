/*
 * GNU GPL v3 License
 *
 * Copyright 2021 Niccolò Tubini, Giuseppe Formetta, Riccardo Rigon
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

package it.geoframe.blogspot.snowmelting.freezing;

/**
 * @author Niccolò Tubini, Giuseppe Formetta
 * 
 */
public class Freezing {



	public double compute(double temperature, double meltingTemperature, double freezingFactor) {

		if(temperature<meltingTemperature) {

			return freezingFactor*(meltingTemperature-temperature);	

		} else {

			return 0.0;

		}

	}
	
	
	
	public double checkFreezing(double initialConditionLiquidWater, double rainfall, double freezing) {
		
		return Math.min(initialConditionLiquidWater+rainfall, freezing);
		
	}
	
	
	
}
