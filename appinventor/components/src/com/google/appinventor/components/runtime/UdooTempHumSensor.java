// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import org.json.JSONObject;


/**
 * A component that interfaces with sensors connected to UDOO boards.
 *
 * @author francesco.monte@gmail.com
 */
@DesignerComponent(version = YaVersion.UDOO_TEMPERATURE_HUMIDITY_SENSOR_COMPONENT_VERSION,
    description = "A component that interfaces with temperature/humidity sensors connected to UDOO boards.",
    category = ComponentCategory.UDOO,
    nonVisible = true,
    iconName = "images/udoo.png")
@SimpleObject
public class UdooTempHumSensor extends AndroidNonvisibleComponent
implements OnResumeListener, OnDestroyListener, UdooConnectedInterface
{
  private String TAG = "UdooTemperatureHumiditySensor";
  private UdooConnectionInterface connection = null;
  private final String SENSOR_TYPE_DHT11 = "dht11";

  private String transport = "local";

  /**
   * Sets the transport property
   *
   * @param transport
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_UDOO_TRANSPORTS,
      defaultValue = "local")
  @SimpleProperty(
      description = "Connect to a local (via ADK) or remote (via TCP) board.",
      userVisible = false)
  public void Transport(String transport) {
    this.transport = transport;
  }
  
  public boolean isLocal() {
    return this.transport.equals("local");
  }
  
  private String remoteAddress;
  
  /**
   * Sets the remote IP address
   *
   * @param remoteAddress
   */
  @DesignerProperty()
  @SimpleProperty(
      description = "If transport=remote, the IP address of the remote board.",
      userVisible = false)
  public void RemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }
 
  public String getRemoteAddress() {
    return this.remoteAddress;
  }

  private String remotePort;
  
  /**
   * Sets the remote TCP port number
   *
   * @param remotePort
   */
  @DesignerProperty()
  @SimpleProperty(
      description = "If transport=remote, the TCP port of the remote board.",
      userVisible = false)
  public void RemotePort(String remotePort) {
    this.remotePort = remotePort;
  }
  
  public String getRemotePort() {
    return this.remotePort;
  }

  private String remoteSecret;
  
  /**
   * Sets the remote secret string for authentication
   *
   * @param remoteSecret
   */
  @DesignerProperty()
  @SimpleProperty(
      description = "If transport=remote, the secret key to connect to the remote board.",
      userVisible = false)
  public void RemoteSecret(String remoteSecret) {
    this.remoteSecret = remoteSecret;
  }

  public String getRemoteSecret() {
    return this.remoteSecret;
  }
  
  private String sensor = SENSOR_TYPE_DHT11;

  /**
   * Sets the transport property
   *
   * @param transport
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_UDOO_TEMP_HUM_SENSORS,
      defaultValue = SENSOR_TYPE_DHT11)
  @SimpleProperty(
      description = "Select the temperature/humidity sensor connected to your board.",
      userVisible = false)
  public void Sensor(String sensor) {
    this.sensor = sensor;
  }
  
  public synchronized boolean isConnected()
  {
    boolean isc = getTransport().isConnected();
    if (!isc) {
      Log.d(TAG, "isConnected called, but disconnected!");
      getTransport().disconnect();
      getTransport().connect();
    }
    return isc;
  }

  public UdooTempHumSensor(Form form)
  {
    super(form);
    
    Log.d("UDOOLIFECYCLE", "UdooArduino");
    
    form.registerForOnResume(this);
    form.registerForOnDestroy(this);
    
    getTransport().onCreate(form);
  }
  
  @Override
  public void onResume()
  {
    Log.d("UDOOLIFECYCLE", "onResume");
    
    this.isConnected(); //connects, if disconnected
  }

  @Override
  public void onDestroy()
  {
    Log.d("UDOOLIFECYCLE", "onDestroy");
    
    getTransport().disconnect();
    getTransport().onDestroy();
  }

  @SimpleFunction
  public void ReadSensor(String pin)
  {
    if (this.isConnected()) {
      try {
        JSONObject response = getTransport().arduino().sensor(pin, this.sensor);
        this.DataReady(response.getInt("temperature"), response.getInt("humidity"));
      } catch (Exception ex) {
        Log.d(TAG, "Invalid JSON");
      }
    }
  }
  
  @SimpleEvent(description = "Fires when the Arduino is (re)connected.")
  public void Connected()
  {
    Log.d(TAG, "Connected EVENT");
    EventDispatcher.dispatchEvent(this, "Connected");
  }
  
  @SimpleEvent(description = "Fires when the Arduino returns the temperature and humidity.")
  public void DataReady(int temperature, int humidity)
  {
    Log.d(TAG, "Data ready");
    
    EventDispatcher.dispatchEvent(this, "DataReady", temperature, humidity);
  }
  
  private UdooConnectionInterface getTransport()
  {
    if (this.connection == null) {
      this.connection = UdooConnectionFactory.getConnection(this, form);
      this.connection.registerComponent(this, form);
    }
    return this.connection;
  }
}