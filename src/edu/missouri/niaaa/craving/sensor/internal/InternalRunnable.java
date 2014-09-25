package edu.missouri.niaaa.craving.sensor.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//Ricky 2013/12/09
import android.os.AsyncTask;
import android.util.Log;
import edu.missouri.niaaa.craving.Utilities;
import edu.missouri.niaaa.craving.logger.Logger;
import edu.missouri.niaaa.craving.services.SensorLocationService;

public class InternalRunnable implements Runnable, SensorEventListener {
	private final Logger log = Logger.getLogger(InternalRunnable.class);
	private SensorManager mSensorManager;
	private static InternalRunnable _instanceInternal = null;
	int SensorType;
	int SamplingRate;
	static int Count = 0;
	static String Temp = null;
	String identifier;
	List<String> dataPoints = new ArrayList<String>();
	Calendar c = Calendar.getInstance();
	SimpleDateFormat curFormater;
	private List<Double> AccList = Collections
			.synchronizedList(new ArrayList<Double>());
	private double avgAcc = 0;

	/**
	 * Singleton Class
	 *
	 * @author Ricky
	 * @param sensorManager
	 * @param sensorType
	 * @param samplingRate
	 * @param uniqueIdentifier
	 * @return
	 */
	public static InternalRunnable getInstance(SensorManager sensorManager,
			int sensorType, int samplingRate, String uniqueIdentifier) {
		if (_instanceInternal == null) {
			_instanceInternal = new InternalRunnable(sensorManager, sensorType,
					samplingRate, uniqueIdentifier);
		}
		return _instanceInternal;
	}

	protected InternalRunnable(SensorManager sensorManager, int sensorType,
			int samplingRate, String uniqueIdentifier) {
		mSensorManager = sensorManager;
		SensorType = sensorType;
		SamplingRate = samplingRate;
		identifier = uniqueIdentifier;
	}

	@Override
	public void run() { // TODO Auto-generated method stub
		log.d("Sensor Number: " + String.valueOf(SensorType));
		setup(SensorType, SamplingRate);
	}

	public void setup(int sensorType, int samplingRate) {
		// TODO Auto-generated method stub
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(sensorType), samplingRate);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public String getDate() {
		curFormater = new SimpleDateFormat("MMMMM_dd");
		String dateObj = curFormater.format(c.getTime());
		return dateObj;
	}

	public String getTimeStamp() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("US/Central"));
		return String.valueOf(cal.getTime());
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			// log.d("Type detected: ACC Sensor");
			double ResultAcc = getOverallAcc(event.values[0], event.values[1],
					event.values[2]);
			if (compressAccelerometerData(ResultAcc)) {
				log.d("get avg Acc data");
				String Accelerometer_Values = getTimeStamp() + ","
						+ String.valueOf(avgAcc);
				String file_name = "Accelerometer." + identifier + "."
						+ getDate() + ".txt";
				File f = new File(Utilities.PHONE_BASE_PATH, file_name);
				/*
				 * dataPoints.add(Accelerometer_Values+";");
				 * if(dataPoints.size()==80) { List<String> subList =
				 * dataPoints.subList(0,41); String data=subList.toString();
				 * String formatedData=data.replaceAll("[\\[\\]]",""); //Ricky
				 * 2013/12/09
				 * //sendDatatoServer("Accelerometer."+identifier+"."+
				 * getDate(),formatedData); TransmitData transmitData=new
				 * TransmitData();
				 * transmitData.execute("Accelerometer."+identifier
				 * +"."+getDate(),formatedData); subList.clear(); }
				 */
				try {
					writeToFile(f, Accelerometer_Values);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			String LightIntensity = getTimeStamp() + "," + event.values[0];
			String file_name = "LightSensor." + identifier + "." + getDate()
					+ ".txt";
			File f = new File(Utilities.PHONE_BASE_PATH, file_name);
			/*
			 * dataPoints.add(LightIntensity+";"); if(dataPoints.size()==80) {
			 * List<String> subList = dataPoints.subList(0,41); String
			 * data=subList.toString(); String
			 * formattedData=data.replaceAll("[\\[\\]]",""); //Ricky 2013/12/09
			 * //sendDatatoServer("LightSensor."+identifier+"."+getDate(),
			 * formattedData); TransmitData transmitData=new TransmitData();
			 * transmitData
			 * .execute("LightSensor."+identifier+"."+getDate(),formattedData);
			 * subList.clear(); }
			 */
			try {
				writeToFile(f, LightIntensity);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private double getOverallAcc(float xACC, float yACC, float zACC) {
		return Math.sqrt(xACC * xACC + yACC * yACC + zACC * zACC);
	}

	protected static void writeToFile(File f, String toWrite)
			throws IOException {
		FileWriter fw = new FileWriter(f, true);
		fw.write(toWrite + '\n');
		fw.flush();
		fw.close();
	}

	protected static boolean checkDataConnectivity() {
		boolean value = SensorLocationService.checkDataConnectivity();
		return value;
	}

	public void stop() {
		mSensorManager.unregisterListener(this);
	}

	// Ricky 2013/12/09
	private class TransmitData extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... strings) {
			// TODO Auto-generated method stub
			String fileName = strings[0];
			String dataToSend = strings[1];
			if (checkDataConnectivity()) {
				HttpPost request = new HttpPost(Utilities.UPLOAD_ADDRESS);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				// file_name
				params.add(new BasicNameValuePair("file_name", fileName));
				// data
				params.add(new BasicNameValuePair("data", dataToSend));
				try {

					request.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
					HttpResponse response = new DefaultHttpClient()
							.execute(request);
					if (response.getStatusLine().getStatusCode() == 200) {
						String result = EntityUtils.toString(response
								.getEntity());
						Log.d("Sensor Data Point Info", result);
						// Log.d("Wrist Sensor Data Point Info","Data Point Successfully Uploaded!");
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

			else {
				Log.d("Sensor Data Point Info",
						"No Network Connection:Data Point was not uploaded");
				return false;
			}

		}

	}

	/**
	 * @author Ricky
	 * @param rawAccelerometerData
	 *            Resultant Value of three axis
	 * @return True/False
	 */
	private Boolean compressAccelerometerData(
			Double rawAccelerometerData) {
		synchronized (AccList) {
			if (AccList.size() <= InternalRunnableUtilities.Accelerometer.ACC_INTERVAL_MAX_POINT_VAL) {
				AccList.add(rawAccelerometerData);
				return false;
			} else {
				avgAcc = 0;
				for (int i = 0; i < AccList.size(); i++) {
					avgAcc += AccList.get(i);
				}
				avgAcc /= AccList.size();
				log.d("queue length: " + String.valueOf(AccList.size()));
				AccList.clear();
				AccList.add(rawAccelerometerData);
				return true;
			}
		}
	}

}
