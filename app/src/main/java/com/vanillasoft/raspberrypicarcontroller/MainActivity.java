package com.vanillasoft.raspberrypicarcontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.vanillasoft.raspberrypicarcontroller.inputmanagercompat.InputManagerCompat.InputDeviceListener;
import com.vanillasoft.raspberrypicarcontroller.inputmanagercompat.InputManagerV16;

public class MainActivity extends AppCompatActivity implements InputDeviceListener {
    private TextView dpadTextView;
    private TextView leftStickTextView;
    private TextView rightStickTextView;
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        webview = (WebView) findViewById(R.id.webview);
        final Activity activity = this;
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress * 1000);
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedHttpError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getApplicationContext(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });
        webview.loadUrl("http://10.0.1.12:8080/?action=stream");
        leftStickTextView = (TextView) findViewById(R.id.left_stick);
        rightStickTextView = (TextView) findViewById(R.id.right_stick);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webview.reload();
        //webview.zoomBy(60.0f);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onInputDeviceAdded(int deviceId) {
        Toast.makeText(this, "Deviced Added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {
        Toast.makeText(this, "Deviced changed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Control Desconectado");
        alert.setMessage("Se ha desconectado el control");
        alert.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Toast.makeText(this, "TEST " + String.valueOf(keyCode), Toast.LENGTH_SHORT).show();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                //Toast.makeText(this, "Left Pressed", Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                //Toast.makeText(this, "Right Pressed", Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                //Toast.makeText(this, "Up Pressed", Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                //Toast.makeText(this, "Down Pressed", Toast.LENGTH_SHORT).show();
                break;
            default:

                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        int eventSource = event.getSource();
        if ((((eventSource & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
                ((eventSource & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK))
                && event.getAction() == MotionEvent.ACTION_MOVE) {
            int id = event.getDeviceId();
            if (-1 != id) {
                    this.processJoystickInput(event, -1);
            }
        }
        return super.onGenericMotionEvent(event);
    }

    private void processJoystickInput(MotionEvent event,
                                      int historyPos) {

        InputDevice mInputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_X, historyPos);
        if (x == 0) {
            x = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_HAT_X, historyPos);
        }
        float cameraX = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Z, historyPos);

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        float y = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Y, historyPos);
        if (y == 0) {
            y = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_HAT_Y, historyPos);
        }
        float cameraY = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_RZ, historyPos);

        // Update the robot movement based on x and y values
        leftStickTextView.setText("X: " + x + ", Y: " + y);
        rightStickTextView.setText("X: " + cameraX + ", Y: " + cameraY);
    }

    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value > 1 ? 1 : value;
            }
        }
        return 0;
    }
}
