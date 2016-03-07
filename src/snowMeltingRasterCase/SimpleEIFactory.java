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
	
	private static double hoursToJan = 720;
	private static double hoursToFeb = 1416;
	private static double hoursToMar = 2160;
	private static double hoursToApr = 2880;
	private static double hoursToMay = 3631;
	private static double hoursToJune = 4351;

	public static EnergyIndex createModel(String type, DateTime date, double latitude, int i, int j, WritableRaster energyIJanuary,
			WritableRaster energyIFebruary, WritableRaster energyIMarch, WritableRaster energyIApril,
			WritableRaster energyIMay, WritableRaster energyIJune){

		EnergyIndex model=null;
		
		if(type.equals("Daily")){
			hoursToJan = 720/24;
			hoursToFeb = 1416/24;
			hoursToMar = 2160/24;
			hoursToApr = 2880/24;
			hoursToMay = 3631/24;
			hoursToJune = 4351/24;
		}

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

		if ((hour>= (sunrise) && hour <= (sunset))|type.equals("Daily")) EIvalue="daily";
		else EIvalue="minimum";
		



		if (EIvalue.equals("daily") &month==1){
			model=new EIdaily(energyIJanuary, i,j,hoursToJan);

		}else if (EIvalue.equals("daily") &month==2){
			model=new EIdaily(energyIFebruary, i,j,hoursToFeb);

		}else if (EIvalue.equals("daily") &month==3){
			model=new EIdaily(energyIMarch, i,j,hoursToMar);

		}else if (EIvalue.equals("daily") &month==4){
			model=new EIdaily(energyIApril, i,j,hoursToApr);

		}else if (EIvalue.equals("daily") &month==5){
			model=new EIdaily(energyIMay, i,j,hoursToMay);

		}else if (EIvalue.equals("daily") &month==6){
			model=new EIdaily(energyIJune, i,j,hoursToJune);

		}else if (EIvalue.equals("daily") &month==7){
			model=new EIdaily(energyIJune, i,j,hoursToJune);

		}else if (EIvalue.equals("daily") &month==8){
			model=new EIdaily(energyIJune, i,j,hoursToJune);

		}else if (EIvalue.equals("daily") &month==9){

			model=new EIdaily(energyIJune, i,j,hoursToJune);

		}else if (EIvalue.equals("daily") &month==10){
			model=new EIdaily(energyIFebruary, i,j,hoursToFeb);

		}else if (EIvalue.equals("daily") &month==11){
			model=new EIdaily(energyIFebruary, i,j,hoursToFeb);

		}else if (EIvalue.equals("daily") &month==12){
			model=new EIdaily(energyIFebruary, i,j,hoursToFeb);
			
			


		}else	if (EIvalue.equals("mimimum") &month==1){
			model=new EIminimum(energyIJanuary,hoursToJan);

		}else if (EIvalue.equals("minimum") &month==2){
			model=new EIminimum(energyIFebruary,hoursToFeb);

		}else if (EIvalue.equals("minimum") &month==3){
			model=new EIminimum(energyIMarch,hoursToMar);

		}else if (EIvalue.equals("minimum") &month==4){
			model=new EIminimum(energyIApril,hoursToApr);

		}else if (EIvalue.equals("minimum") &month==5){
			model=new EIminimum(energyIMay,hoursToMay);

		}else if (EIvalue.equals("minimum") &month==6){
			model=new EIminimum(energyIJune,hoursToJune);

		}else if (EIvalue.equals("minimum") &month==7){
			model=new EIminimum(energyIJune,hoursToJune);

		}else if (EIvalue.equals("minimum") &month==8){
			model=new EIminimum( energyIJune,hoursToJune);

		}else if (EIvalue.equals("minimum") &month==9){

			model=new EIminimum(energyIJune,hoursToJune);

		}else if (EIvalue.equals("minimum") &month==10){
			model=new EIminimum(energyIFebruary,hoursToFeb);

		}else if (EIvalue.equals("minimum") &month==11){
			model=new EIminimum(energyIFebruary,hoursToFeb);

		}else if (EIvalue.equals("minimum") &month==12){
			model=new EIminimum(energyIFebruary,hoursToFeb);}

		return model;

	}

}
