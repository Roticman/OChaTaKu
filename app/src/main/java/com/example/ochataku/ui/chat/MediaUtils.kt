package com.example.ochataku.ui.chat


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Environment
import com.example.ochataku.service.MessageDisplay
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date

// 全局音频路径与 MediaRecorder 引用
var mediaRecorder: MediaRecorder? = null
var audioFilePath: String? = null

fun getDurationFromUrl(url: String): Int {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(url, HashMap())
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        val durationMs = durationStr?.toLongOrNull() ?: 0L
        (durationMs / 1000).toInt()
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

fun getFileSizeFromUrl(fileUrl: String): Long {
    return try {
        val url = URL(fileUrl)
        val connection = url.openConnection()
        connection.connect()
        connection.contentLengthLong
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

fun getVideoThumbnailBitmap(context: Context, videoUrl: String): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoUrl, HashMap())
        val bitmap = retriever.getFrameAtTime(1_000_000) // 获取 1 秒处的帧
        retriever.release()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun parseMessage(json: JSONObject): MessageDisplay {
    val rowGroup = json.getBoolean("is_group")
    return MessageDisplay(
        conv_id = json.getLong("conv_id"),
        sender_id = json.getLong("sender_id"),
        sender_name = json.getString("sender_name"),
        sender_avatar = json.getString("sender_avatar"),
        content = json.getString("content"),
        timestamp = json.getLong("timestamp"),
        is_group = if (rowGroup) 1 else 0,
        message_type = json.getString("message_type"),
        media_url = if (json.has("media_url") && !json.isNull("media_url")) json.getString("media_url") else null,
    )
}

@SuppressLint("SimpleDateFormat")
fun startRecording(context: Context) {
    val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
    val fileName = "REC_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.mp3"
    val outputFile = File(outputDir, fileName)
    audioFilePath = outputFile.absolutePath

    mediaRecorder = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setOutputFile(audioFilePath)
        prepare()
        start()
    }
}

fun stopRecording(onComplete: (String) -> Unit) {
    mediaRecorder?.apply {
        stop()
        release()
    }
    mediaRecorder = null
    audioFilePath?.let { onComplete(it) }
}