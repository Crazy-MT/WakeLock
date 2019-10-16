package mt.powerdemo

import android.app.Activity
import android.content.Context
import android.os.PowerManager
import android.util.Log

/**
 * @author : MaoTong
 * @date : 2019-10-16 10:40
 * description :
 */
class WakeManager1 private constructor(context: Activity) {
    private val wakeLock: PowerManager.WakeLock

    init {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, context.packageName + "wakeLock")
    }

    companion object {
        private const val TAG = "WakeManager"
        private var instance: WakeManager1? = null

        fun createInstance(context: Activity): WakeManager1 {
            if (instance == null) {
                instance = WakeManager1(context)
            }
            return instance as WakeManager1
        }

        fun getInstance(): WakeManager1 {
            if(null == instance) {
                Log.e(TAG, ": ");
            }
            return instance!!
        }
    }
}
