package maruli.app.ruliweather;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class WeatherCityWorkManager extends Worker {

    private static final String TAG = WeatherCityWorkManager.class.getSimpleName();
    private static final String CHANEL_ID = "Work_Manager_Chanel01";
    private static final CharSequence CHANEL_NAME = "WorkManagerChanel";
    public static final String EXTRA_CITY = "city";
    private Result resultStatus;

    public WeatherCityWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String city = getInputData().getString("city");
        return getCurrentWeather(city);
    }

    private Result getCurrentWeather (String city){
        Log.d(TAG, "getCurrentWeather : Startef...");
        Looper.prepare();
        SyncHttpClient client = new SyncHttpClient();
        String uri = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + BuildConfig.ApiKey;
        client.get(uri, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String result = new String(responseBody );
                        Log.d(TAG, result);
                        try {
                            JSONObject reponseObject = new JSONObject(result);
                            String currentWeather = reponseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                            String description = reponseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                            double tempInKelvin = reponseObject.getJSONObject("main").getDouble("temp");
                            double tempInCelcius = tempInKelvin - 273;
                            String temprature = new DecimalFormat("##.##").format(tempInCelcius);

                            String title = "Cuaca dari kota " + city;
                            String message = currentWeather+ ", " + description + " with " + temprature + " celcius ";
                            int notifID = 201;
                            showNotofication(getApplicationContext(), title, message, notifID);

                            Log.d(TAG, "onSucces : finished");
                            resultStatus = Result.success();

                        }catch (JSONException e){
                            e.printStackTrace();
                            showNotofication(getApplicationContext(), "Get Current Weather Not Succes", e.getMessage(), 201);
                            Log.d(TAG, "onFailure : failed");
                            resultStatus = Result.failure();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        showNotofication(getApplicationContext(), "Get Current Weather Not Succes", error.getMessage(), 201);
                        Log.d(TAG, "onFailure : failed");
                        resultStatus = Result.failure();
                    }
                });

        return resultStatus;
    }

    private void showNotofication(Context context, String title, String message, int notifId){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alaramSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mbuilder = new NotificationCompat.Builder(context, CHANEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_cloud_sun_solid)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setVibrate(new long[]{1000, 1000, 1000, 1000})
                .setSound(alaramSound)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{1000,1000,1000,1000});
            mbuilder.setChannelId(CHANEL_ID);
            if (notificationManager != null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        Notification notification = mbuilder.build();
         if (notificationManager != null){
             notificationManager.notify(notifId, notification);
         }
    }
}
