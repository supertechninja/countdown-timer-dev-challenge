/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                CircularClock()
            }
        }
    }
}

@Composable
private fun CircularClock() {
    val scope = rememberCoroutineScope()
    val previousTimerJob by remember { mutableStateOf<Job?>(null) }

    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }

    val totalDurationSeconds = totalTimeSeconds(minutes, seconds)
    var remainingTime by remember { mutableStateOf(totalDurationSeconds) }
    var timerState by remember { mutableStateOf(TimerState.Paused) }

    var toggleCount by remember { mutableStateOf(0) }

    fun startCounter(fromLatestValue: Boolean = false) {
        val initialValue = if (fromLatestValue) remainingTime else totalDurationSeconds
        timerState = TimerState.Running
        previousTimerJob?.cancel() // Cancel previous ongoing Job when restarting - no leaking.
        scope.launch {
            val startTime = withFrameMillis { it }
            if (remainingTime == 0L) {
                remainingTime = initialValue
            }
            while (remainingTime > 0 && timerState != TimerState.Paused) {
                val elapsedTime = (withFrameMillis { it } - startTime) / 1000
                remainingTime = initialValue - elapsedTime
            }
        }
    }

    fun toggleTimerState() {
        if (timerState == TimerState.Running) {
            timerState = TimerState.Paused
        } else {
            if (toggleCount == 0) {
                startCounter(fromLatestValue = false)
                toggleCount++
            } else {
                startCounter(fromLatestValue = true)
            }
        }
    }

    fun reset() {
        remainingTime = 0
        toggleCount = 0
        minutes = 0
        seconds = 0
    }

    var printMinutes = if (remainingTime > 0) {
        (remainingTime / 60).toInt()
    } else {
        minutes
    }

    var printSeconds = if (remainingTime > 0) {
        (remainingTime - printMinutes * 60).toInt()
    } else {
        seconds
    }

    val progress: Float = (printSeconds.toFloat() / 60f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.aspectRatio(1f)) {
            var defaultPadding = 8.dp
            repeat(printMinutes) {
                CircularProgressIndicator(
                    progress = 1f, color = Color.White,
                    modifier = Modifier
                        .padding(defaultPadding)
                        .fillMaxSize()
                )
                defaultPadding += 12.dp
            }

            CircularProgressIndicator(
                progress = progress, color = MaterialTheme.colors.secondary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(defaultPadding),
                strokeWidth = 5.dp
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { minutes += 1 }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    IconButton(onClick = { seconds += 5 }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val timeText = with(AnnotatedString.Builder("$printMinutes")) {
                        pushStyle(SpanStyle(fontSize = 24.sp))
                        append("m")
                        pop()
                        append(" $printSeconds")
                        pushStyle(SpanStyle(fontSize = 24.sp))
                        append("s")
                        toAnnotatedString()
                    }

                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.h3.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { minutes -= 1 }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    IconButton(onClick = { seconds -= 5 }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.requiredHeight(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {

            val roundedCorners by animateIntAsState(targetValue = if (timerState == TimerState.Running) 50 else 10)
            FloatingActionButton(
                onClick = {
                    toggleTimerState()
                },
                backgroundColor = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(roundedCorners)
            ) {
                val padding by animateDpAsState(targetValue = if (timerState == TimerState.Running) 32.dp else 8.dp)

                Image(
                    imageVector = if (timerState == TimerState.Running) Icons.Outlined.PauseCircleOutline else Icons.Outlined.PlayCircleOutline,
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier
                        .padding(horizontal = padding)
                        .requiredSize(36.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    reset()
                },
                backgroundColor = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(roundedCorners)
            ) {
                Image(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .requiredSize(36.dp)
                )
            }
        }
    }
}

enum class TimerState {
    Running, Paused
}

private fun totalTimeSeconds(minutes: Int, seconds: Int): Long = minutes * 60L + seconds
