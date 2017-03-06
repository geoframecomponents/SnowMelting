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


// TODO: Auto-generated Javadoc
/**
 * A simple design factory for creating Model objects.
 */
public class SimpleModelFactoryRaster {
	

	public static SnowModelRaster createModelRaster(String snowModel,double combinedMeltingFactor,double temperature,
			double meltingTemperature,double skyviewValue,double radiationFactor,double shortwaveRadiation, double EI ){

		SnowModelRaster model=null;

		
		if (snowModel.equals("Classical")){
			model=new ClassicalModel(combinedMeltingFactor, temperature,  meltingTemperature);

		}else if (snowModel.equals("Cazorzi")){
			model=new CazorziModel(combinedMeltingFactor, temperature,  meltingTemperature,EI,skyviewValue);

		}else if (snowModel.equals("Hock")){
			model=new HockModel(combinedMeltingFactor, radiationFactor, temperature, meltingTemperature,
					shortwaveRadiation, skyviewValue);

		}
		return model;

	}

}
