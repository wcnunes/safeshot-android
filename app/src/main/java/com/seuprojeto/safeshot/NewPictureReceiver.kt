package com.seuprojeto.safeshot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.exifinterface.media.ExifInterface

class NewPictureReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Verifica se a função está ativada nas preferências do usuário
        val prefs = context.getSharedPreferences("safeshot_prefs", Context.MODE_PRIVATE)
        val autoCleanEnabled = prefs.getBoolean("auto_clean_camera", false)
        if (!autoCleanEnabled) return

        val uri = intent.data
        uri?.let {
            val path = getRealPathFromURI(context, it)
            path?.let { processImage(context, it) }
        }
    }

    private fun processImage(context: Context, imagePath: String) {
        val exif = ExifInterface(imagePath)
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
        exif.saveAttributes()
        showNotification(context, imagePath)
        logCleanAction(context, imagePath)
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, proj, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val path = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return path
    }

    private fun showNotification(context: Context, imagePath: String) {
        val channelId = "safeshot_channel"
        val notificationId = 1

        // Cria o canal de notificação (necessário para Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SafeShot"
            val descriptionText = "Notificações do SafeShot"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir o app ao clicar na notificação
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("SafeShot")
            .setContentText("Metadados removidos de uma nova foto!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    private fun logCleanAction(context: Context, filePath: String?) {
        filePath ?: return
        val prefs = context.getSharedPreferences("safeshot_log", Context.MODE_PRIVATE)
        val logs = prefs.getStringSet("log_entries", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val entry = "[${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(java.util.Date())}] Limpeza (Broadcast): ${filePath.substringAfterLast('/')}"
        logs.add(entry)
        prefs.edit().putStringSet("log_entries", logs).apply()
    }
} 