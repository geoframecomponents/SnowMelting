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

package it.geoframe.blogspot.snowmelting.massbalance;

/**
 * @author Niccolò Tubini, Giuseppe Formetta
 * 
 */
public class CheckMassBalance {
	
	
	
	public double errorODESolidWater(double initialConditionSolidWater, double solidWater, double snowfall, double freezing, double melting) {
		
		return solidWater - initialConditionSolidWater - snowfall - freezing + melting;
		
	}
	
	
	public double errorODELiquidWater(double initialConditionLiquidWater, double liquidWater, double rainfall, double freezing, double melting, double meltingDischarge) {
		
		return liquidWater - initialConditionLiquidWater - rainfall + freezing - melting + meltingDischarge;
		
	}
	
	
    public double errorSWE(double swe, double initialConditionLiquidWater, double initialConditionSolidWater, double rainfall, double snowfall, double meltingDischarge) {
		
		return rainfall + snowfall - (swe - (initialConditionLiquidWater+initialConditionSolidWater)) - meltingDischarge;
		
	}

}
