package mt.powerdemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.os.PowerManager
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    var wakeLock: PowerManager.WakeLock? = null
    var mTimer = Timer()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showScreenOffTime()

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, packageName + "wakeLock")
    }

    private fun showScreenOffTime() {
        val screenOffTime = getScreenOffTime()
        if (screenOffTime == -1) {
            sysScreenOffTime.setText("当前系统息屏时长：" + "永不休眠")
        }
        val screenTime = (getScreenOffTime().div(1000))
        sysScreenOffTime.setText("当前系统息屏时长：" + screenTime + "秒")
    }

    private fun showTimer() {
        var time = 0
        mTimer.cancel()
        mTimer = Timer()

        val mTimerTask = object : TimerTask() {
            //创建一个线程来执行run方法中的代码
            override fun run() {
                //要执行的代码
                runOnUiThread {
                    timerTV.setText("已经亮屏：" + (++time) + "秒")
                }
            }
        }
        mTimer.schedule(mTimerTask, 0, 1000)//延迟3秒执行
    }

    fun always(view: View) {
        wakeLock?.acquire()

        Toast.makeText(this, "常亮", Toast.LENGTH_LONG).show()

        showTimer()
    }

    fun release(view: View) {
        wakeLock?.release()

        Toast.makeText(this, "释放", Toast.LENGTH_LONG).show()

        showTimer()
    }

    @SuppressLint("InvalidWakeLockTag")
    fun lock(view: View) {

        // 20 秒内没有触摸，则 20 秒后熄灭；若有触摸，从触摸时开始，按系统时间熄灭
        wakeLock?.acquire(20 * 1000)

        Toast.makeText(this, "20 秒息屏", Toast.LENGTH_LONG).show()

        showTimer()
    }

    override fun onPause() {
        super.onPause()
        if (wakeLock != null && wakeLock?.isHeld!!) {
            wakeLock?.release()
        }
    }

    /**
     * 获得休眠时间 毫秒
     */
    fun getScreenOffTime(): Int {
        var screenOffTime = 0;
        try {
            screenOffTime = Settings.System.getInt(
                getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT
            );
        } catch (localException: Exception) {
            localException.printStackTrace()
        }
        return screenOffTime;
    }

    /**
     * 设置休眠时间 毫秒
     */
    fun setScreenOffTime(paramInt: Int) {
        try {
            Settings.System.putInt(
                getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                paramInt
            );
        } catch (localException: Exception) {
            localException.printStackTrace();
        }
    }

}
