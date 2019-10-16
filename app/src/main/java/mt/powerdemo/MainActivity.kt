package mt.powerdemo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    var mTimer = Timer()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showScreenOffTime()

        WakeManager.createInstance(this)
    }

    private fun showScreenOffTime() {
        val screenOffTime = getScreenOffTime()
        if (screenOffTime == -1L) {
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
                    val wakeTime = ++time
                    timerTV.setText("已经亮屏：" + wakeTime + "秒")
                    Log.e("MainActivity", "已经亮屏：" + wakeTime + "秒");
                }
            }
        }
        mTimer.schedule(mTimerTask, 0, 1000)//延迟3秒执行
    }

    fun always(view: View) {
        WakeManager.getInstance().setMode(WakeMode.ALWAYS, -1)

        Toast.makeText(this, "常亮", Toast.LENGTH_LONG).show()

        showTimer()
    }

    fun release(view: View) {
        WakeManager.getInstance().setMode(WakeMode.SYSTEM, -1)

        Toast.makeText(this, "释放", Toast.LENGTH_LONG).show()

        showTimer()
    }

    @SuppressLint("InvalidWakeLockTag")
    fun lock(view: View) {

        // 前 20 * 1000 秒之内，触摸屏幕，屏幕仍会在 20 * 1000 + systemTime 熄灭；
        // 超过 20 * 1000 秒，触摸屏幕，屏幕息屏时间会从触摸屏幕开始顺延到 systemTime 时间熄灭
        WakeManager.getInstance().setMode(WakeMode.LOCK, 20 * 1000 + getScreenOffTime())

        Toast.makeText(this, "30 秒之后交由系统处理", Toast.LENGTH_LONG).show()

        showTimer()
    }

    override fun onResume() {
        super.onResume()
        WakeManager.getInstance().onResume()
        showTimer()
    }

    override fun onPause() {
        super.onPause()
        WakeManager.getInstance().onPause()
        WakeManager.getInstance().releaseWake()
        mTimer.cancel()
    }

    /**
     * 获得休眠时间 毫秒
     */
    fun getScreenOffTime(): Long {
        var screenOffTime = 0L;
        try {
            screenOffTime = Settings.System.getLong(
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
