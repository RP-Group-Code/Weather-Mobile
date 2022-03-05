package maruli.app.ruliweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button btnOneTImeTask, btnPeriodic, btnCancel;
    private Spinner spinnerCity;
    private TextView tvStatus;
    private PeriodicWorkRequest periodicWorkRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOneTImeTask = findViewById(R.id.btn_one_time_task);
        btnPeriodic = findViewById(R.id.btn_periodic_task);
        btnCancel = findViewById(R.id.btn_cancel_periodic);
        spinnerCity = findViewById(R.id.sp_city);
        tvStatus = findViewById(R.id.tv_status);

        btnOneTImeTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOneTimeTask();
            }
        });
        btnPeriodic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPeriodicTask();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPeriodic();
            }
        });

    }
    private void startOneTimeTask(){
        tvStatus.setText("Status :");
        Data data = new Data.Builder()
                .putString(WeatherCityWorkManager.EXTRA_CITY, spinnerCity.getSelectedItem().toString())
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(WeatherCityWorkManager.class)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(MainActivity.this).enqueue(oneTimeWorkRequest);

        WorkManager.getInstance(MainActivity.this)
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.getId())
                .observe(MainActivity.this, workInfo -> {
                    String status = workInfo.getState().name();
                    tvStatus.append("\n" + status);
                    btnCancel.setEnabled(false);

                    if (workInfo.getState() == WorkInfo.State.ENQUEUED){
                        btnCancel.setEnabled(true);
                    }
                });
    }
    private void cancelPeriodic(){
        WorkManager.getInstance(MainActivity.this).cancelWorkById(periodicWorkRequest.getId());
    }

    private void startPeriodicTask(){

        tvStatus.setText("status :");
        Data data = new Data.Builder()
                .putString(WeatherCityWorkManager.EXTRA_CITY, spinnerCity.getSelectedItem().toString())
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        periodicWorkRequest = new PeriodicWorkRequest.Builder(WeatherCityWorkManager.class, 15, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(MainActivity.this).enqueue(periodicWorkRequest);

        WorkManager.getInstance(MainActivity.this)
                .getWorkInfoByIdLiveData(periodicWorkRequest.getId())
                .observe(MainActivity.this, workInfo -> {
                    String status = workInfo.getState().name();
                    tvStatus.append("\n" + status);
                    btnCancel.setEnabled(false);

                    if (workInfo.getState() == WorkInfo.State.ENQUEUED){
                        btnCancel.setEnabled(true);
                    }
                });
    }
}