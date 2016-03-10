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

import org.joda.time.DateTime;

// TODO: Auto-generated Javadoc
/**
 * A simple design factory for creating Model objects.
 */
public class SimpleEIFactory {
	
	private static double daysToJan = 30;
	private static double daysToFeb = 59;
	private static double daysToMar = 90;
	private static double daysToApr = 120;
	private static double daysToMay = 151;
	private static double daysToJune = 181;

	public static EnergyIndex createModel(boolean doHourly, DateTime date, double latitude, int i, int j, WritableRaster energyIJanuary,
			WritableRaster energyIFebruary, WritableRaster energyIMarch, WritableRaster energyIApril,
			WritableRaster energyIMay, WritableRaster energyIJune){

		EnergyIndex model=null;
		


		int month=date.getMonthOfYear();
		int hour=date.getHourOfDay();
		int day = date.getDayOfYear();

		double dayangb = Math.toRadians((360 / 365.25) * (day - 79.436));

		// Evaluate the declination of the sun.
		double delta = .3723 + 23.2567 * Math.sin(dayangb) - .758
				* Math.cos(dayangb) + .1149 * Math.sin(2 * dayangb) + .3656
				* Math.cos(2 * dayangb) - .1712 * Math.sin(3 * dayangb) + .0201
				* Math.cos(3 * dayangb);
		// Evaluate the radiation in this day.
		double ss = Math.acos(-Math.tan(Math.toRadians(delta)) * Math.tan(latitude));

		double sunrise = 12 * (1.0 - ss / Math.PI);
		double sunset = 12 * (1.0 + ss / Math.PI);

		String EIvalue;

		if ((hour>= (sunrise) && hour <= (sunset))|doHourly==false) EIvalue="daily";
		else EIvalue="minimum";
		



		if (EIvalue.equals("daily") &month==1){
			model=new EIdaily(energyIJanuary, i,j,daysToJan);

		}else if (EIvalue.equals("daily") &month==2){
			model=new EIdaily(energyIFebruary, i,j,daysToFeb);

		}else if (EIvalue.equals("daily") &month==3){
			model=new EIdaily(energyIMarch, i,j,daysToMar);

		}else if (EIvalue.equals("daily") &month==4){
			model=new EIdaily(energyIApril, i,j,daysToApr);

		}else if (EIvalue.equals("daily") &month==5){
			model=new EIdaily(energyIMay, i,j,daysToMay);

		}else if (EIvalue.equals("daily") &month==6){
			model=new EIdaily(energyIJune, i,j,daysToJune);

		}else if (EIvalue.equals("daily") &month==7){
			model=new EIdaily(energyIJune, i,j,daysToJune);

		}else if (EIvalue.equals("daily") &month==8){
			model=new EIdaily(energyIJune, i,j,daysToJune);

		}else if (EIvalue.equals("daily") &month==9){

			model=new EIdaily(energyIJune, i,j,daysToJune);

		}else if (EIvalue.equals("daily") &month==10){
			model=new EIdaily(energyIFebruary, i,j,daysToFeb);

		}else if (EIvalue.equals("daily") &month==11){
			model=new EIdaily(energyIFebruary, i,j,daysToFeb);

		}else if (EIvalue.equals("daily") &month==12){
			model=new EIdaily(energyIFebruary, i,j,daysToFeb);
			
			


		}else	if (EIvalue.equals("mimimum") &month==1){
			model=new EIminimum(energyIJanuary,daysToJan);

		}else if (EIvalue.equals("minimum") &month==2){
			model=new EIminimum(energyIFebruary,daysToFeb);

		}else if (EIvalue.equals("minimum") &month==3){
			model=new EIminimum(energyIMarch,daysToMar);

		}else if (EIvalue.equals("minimum") &month==4){
			model=new EIminimum(energyIApril,daysToApr);

		}else if (EIvalue.equals("minimum") &month==5){
			model=new EIminimum(energyIMay,daysToMay);

		}else if (EIvalue.equals("minimum") &month==6){
			model=new EIminimum(energyIJune,daysToJune);

		}else if (EIvalue.equals("minimum") &month==7){
			model=new EIminimum(energyIJune,daysToJune);

		}else if (EIvalue.equals("minimum") &month==8){
			model=new EIminimum( energyIJune,daysToJune);

		}else if (EIvalue.equals("minimum") &month==9){

			model=new EIminimum(energyIJune,daysToJune);

		}else if (EIvalue.equals("minimum") &month==10){
			model=new EIminimum(energyIFebruary,daysToFeb);

		}else if (EIvalue.equals("minimum") &month==11){
			model=new EIminimum(energyIFebruary,daysToFeb);

		}else if (EIvalue.equals("minimum") &month==12){
			model=new EIminimum(energyIFebruary,daysToFeb);}

		return model;

	}

}
