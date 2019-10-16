package mt.powerdemo

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import java.lang.Exception

/**
 *  @author : MaoTong
 *  @date : 2019-10-15 20:00
 *  description :
 */

class WakeManager private constructor(context: Activity) {
    var wakeLock: PowerManager.WakeLock? = null
    var powerManager: PowerManager? = null
    var wakeMode: WakeMode = WakeMode.LOCK
    var handler: Handler = Handler()
    var runnable: Runnable = object : Runnable {
        override fun run() {
            Log.e(TAG, "runnable: ");
            releaseWake()
//            handler.removeCallbacks(this)
            pauseTime = 0
            startTime = 0
        }
    }

    var pauseTime: Long = 0
    var startTime: Long = 0
    var timeOut: Long = 0

    init {
        powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager?.newWakeLock(PowerManager.FULL_WAKE_LOCK, context.packageName + "wakeLock")
        wakeLock?.setReferenceCounted(false)
        timeOut = 20 * 1000 + getScreenOffTime(context)
    }

    companion object{
        private const val TAG = "WakeManager"
        @Volatile
        private var sInstance: WakeManager? = null

        fun createInstance(context: Activity): WakeManager {
            if (sInstance == null) {
                synchronized(context::class) {
                    if (sInstance == null) {
                        sInstance = WakeManager(context)
                    }
                }
            }
            return sInstance!!
        }

        @Synchronized
        fun getInstance(): WakeManager {
            if (null == sInstance) {
                Log.e(TAG, ": null");
            }
            return sInstance!!
        }
    }

    /**
     * 不息屏
     */
    fun alwaysWake() {
        acquire()
    }

    /**
     * 交由系统管理
     */
    fun releaseWake() {
        if (wakeLock != null && wakeLock?.isHeld!!) {
            wakeLock?.release()
        }
    }

    /**
     * 进入 App
     */
    fun onResume() {
        // 根据三种模式分别处理
        when(wakeMode) {
            WakeMode.LOCK -> {
                // 还剩下的时间
                acquire()
                Log.e(TAG, "onResume: " + timeOut + " " + pauseTime + " " + startTime + " " + (timeOut - (pauseTime - startTime)));
                handler.postDelayed(runnable, timeOut - (pauseTime - startTime))
            }
            WakeMode.ALWAYS -> {
                acquire()
            }
            WakeMode.SYSTEM -> {
                // do nothing
            }
        }
    }

    /**
     * 获取 wakeLock
     */
    fun acquire() {
        wakeLock?.acquire()
    }

    /**
     * 隔 timeOut 时间之后交由系统处理。
     */
    fun lock(timeOut: Long) {
        this.timeOut = timeOut
        startTime = System.currentTimeMillis()
        acquire()
        handler.postDelayed(runnable, timeOut)
    }

    fun onPause() {
        if (wakeMode == WakeMode.LOCK) {
            pauseTime = System.currentTimeMillis()
            handler.removeCallbacks(runnable)
        }
    }

    fun setMode(mode: WakeMode, timeOut: Long) {
        wakeMode = mode
        when(wakeMode) {
            WakeMode.ALWAYS -> {alwaysWake()}
            WakeMode.SYSTEM -> {releaseWake()}
            WakeMode.LOCK -> {lock(timeOut)}
        }
    }
    /**
     * 获得休眠时间 毫秒
     */
    fun getScreenOffTime(context: Activity): Long {
        var screenOffTime = 0L;
        try {
            screenOffTime = Settings.System.getLong(
                context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT
            );
        } catch (localException: Exception) {
            localException.printStackTrace()
        }
        return screenOffTime;
    }
}

enum class WakeMode {
    ALWAYS, SYSTEM, LOCK
}
