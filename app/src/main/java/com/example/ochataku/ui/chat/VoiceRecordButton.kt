package com.example.ochataku.ui.chat

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ochataku.utils.PermissionUtils
import com.example.ochataku.viewmodel.ChatViewModel
import java.io.File

// ---- Voice Record Button ----
@Composable
fun VoiceRecordButton(
    context: Context,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    currentUserId: Long,
    peerId: Long,
    convId: Long,
    isGroup: Boolean
) {
    var isRecording by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        PermissionUtils.requestAudioPermission(context as Activity) {
                            isRecording = true
                            startRecording(context)
                            Toast.makeText(context, "开始录音...", Toast.LENGTH_SHORT).show()
                        }
                    }
                    ,
                    onPress = {
                        tryAwaitRelease()
                        isRecording = false
                        stopRecording { audioPath ->
                            Toast.makeText(
                                context,
                                "录音发送：$audioPath",
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.sendMessage(
                                context = context,
                                mediaUri = Uri.fromFile(File(audioPath)),
                                messageType = "voice",
                                content = "",
                                senderId = currentUserId,
                                receiverId = peerId,
                                convId = convId,
                                isGroup = isGroup
                            ) {}
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text("按住说话", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
