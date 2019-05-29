package com.example.gpsitp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*




@RequiresApi(Build.VERSION_CODES.CUPCAKE)
class MainActivity : AppCompatActivity(), SensorEventListener {
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onSensorChanged(event: SensorEvent?) {
        val ambient_temperature = event?.values!![0]
        textViewTemperature.setText("Ambient Temperature:\n " + ambient_temperature.toString() + "C")

    }

    private val CHANNEL_ID: String = "1"
    var state = true;

    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    fun turnOnLed(){
        if(this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            val cam = Camera.open()
            val p = cam.getParameters()
            p.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            cam.parameters = p
            cam.startPreview()
        } else {
            Log.d("tag", "FEATURE_CAMERA_FLASH not available");
        }
    }

    fun turnOffLed(){
        if(this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            val cam = Camera.open()
            cam.stopPreview();
            cam.release();
        } else {
            Log.d("tag", "FEATURE_CAMERA_FLASH not available");
        }
    }

    //NOTIFICATION
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel_name"
            val descriptionText = "channel_description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    var builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle("GPS ITP")
        .setContentText("Działa w tle")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    override fun onPause() {
        super.onPause()
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(2, builder.build())
        }
    }



    private val LED_NOTIFICATION_ID = 0 //arbitrary constant

    private fun RedFlashLight() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = Notification()
        //notif.color = RED
        notif.flags = Notification.FLAG_SHOW_LIGHTS
        notif.ledOnMS = 100
        notif.ledOffMS = 100
        //Błąd - nie ma ustwionego small icon
        //nm.notify(LED_NOTIFICATION_ID, notif)
    }


    fun showTemp(){
        val ambient_temperature = this.mTemperature
        textViewTemperature.setText("Ambient Temperature:\n " + ambient_temperature.toString() + "C")
    }

    private var locationManager : LocationManager? = null
    private var mSensorManager: SensorManager? = null
    private var mTemperature: Sensor? = null


    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    override fun onCreate(savedInstanceState: Bundle?) {

        createNotificationChannel()

        if(this.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE)){
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                mTemperature= mSensorManager!!.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE); // requires API level 14.
            }
        } else {
            Log.d("tag", "FEATURE_SENSOR_AMBIENT_TEMPERATURE not available");
        }

        // Create persistent LocationManager reference
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?;

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        buttonRefresh.setOnClickListener { view ->
            try {
                // Request location updates
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener);
            } catch(ex: SecurityException) {
                Log.d("tag", "Security Exception, no location available");
            }
        }

        buttonLed.setOnClickListener{
            showTemp()
            RedFlashLight()



            if(state){
                state = false
                turnOnLed()

            } else {
                state = true
                turnOffLed()
            }
        }
    }


    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            textViewGPS.setText("" + location.longitude + ":" + location.latitude);
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
}
