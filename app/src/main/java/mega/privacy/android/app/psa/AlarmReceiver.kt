package mega.privacy.android.app.psa

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import mega.privacy.android.app.MegaApplication
import mega.privacy.android.app.listeners.BaseListener
import mega.privacy.android.app.utils.TimeUtils.SECOND
import nz.mega.sdk.MegaApiJava
import nz.mega.sdk.MegaError
import nz.mega.sdk.MegaRequest

class AlarmReceiver : BroadcastReceiver() {
    private val megaApi = MegaApplication.getInstance().megaApi

    override fun onReceive(context: Context, intent: Intent) {
        wakeLock.acquire(30 * SECOND)  // The wakelock will be held for at most 30 Secs

        megaApi.getPSAWithUrl(object : BaseListener(context) {

            override fun onRequestFinish(
                api: MegaApiJava,
                request: MegaRequest,
                e: MegaError
            ) {
                super.onRequestFinish(api, request, e)

                // API response may arrive after stopChecking, in this case we shouldn't
                // emit PSA anymore.
                if (e.errorCode == MegaError.API_OK) {
                    callback?.invoke(
                        Psa(
                            request.number.toInt(), request.name, request.text, request.file,
                            request.password, request.link, request.email
                        )
                    )
                }

                setAlarm(context, PsaManager.GET_PSA_INTERVAL_MS, callback)
            }
        })
    }

    companion object {
        const val CHECK_PSA_INTENT = "android.intent.action.checking.psa"
        val wakeLock: PowerManager.WakeLock =
            (MegaApplication.getInstance().getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Mega::PsaWakelockTag")}

        var callback : ((Psa) -> Unit)? = null

        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            /* cancel any pending alarm */
            alarmManager.cancel(pendingIntent)
        }

        fun setAlarm(context: Context, delayMs: Long, cb: ((Psa) -> Unit)? = null) {
            cancelAlarm(context)

            callback = cb
            var delay = delayMs
            if (delay < 0) {
                delay = 0;
            }
            val time = System.currentTimeMillis() + delay
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            /* fire the broadcast */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }
        }

        /* get the application context */
        private val pendingIntent: PendingIntent
            get() {
                val context = MegaApplication.getInstance().applicationContext
                val alarmIntent = Intent(context, AlarmReceiver::class.java)
                alarmIntent.action = CHECK_PSA_INTENT

                return PendingIntent.getBroadcast(
                    context,
                    0,
                    alarmIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            }
    }
}