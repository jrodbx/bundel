@file:OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)

package dev.sebastiano.bundel.onboarding

import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.ButtonElevation
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DoneOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.vanpra.composematerialdialogs.datetime.timepicker.timepicker
import dev.sebastiano.bundel.BundelOnboardingTheme
import dev.sebastiano.bundel.BundelTheme
import dev.sebastiano.bundel.R
import dev.sebastiano.bundel.composables.MaterialChip
import dev.sebastiano.bundel.singlePadding
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Locale

@Preview(name = "Onboarding screen (needs permission)", showSystemUi = true)
@Composable
internal fun OnboardingScreenNeedsPermissionPreview() {
    BundelOnboardingTheme {
        OnboardingScreen(
            true,
            onSettingsIntentClick = { },
            onOnboardingDoneClicked = { },
            false,
            {}
        )
    }
}

@Preview(name = "Onboarding screen (needs permission, dark theme)", showSystemUi = true)
@Composable
internal fun OnboardingDarkScreenNeedsPermissionPreview() {
    BundelOnboardingTheme(darkModeOverride = true) {
        OnboardingScreen(
            true,
            onSettingsIntentClick = { },
            onOnboardingDoneClicked = { },
            false,
            {}
        )
    }
}

@Preview(name = "Onboarding screen (dismiss only)", showSystemUi = true)
@Composable
internal fun OnboardingScreenDismissOnlyPreview() {
    BundelOnboardingTheme {
        OnboardingScreen(
            false,
            onSettingsIntentClick = { },
            onOnboardingDoneClicked = { },
            true,
            {}
        )
    }
}

@Preview(backgroundColor = 0xFF4CE062, showBackground = true)
@Composable
private fun ScheduleDaysPagePreview() {
    BundelOnboardingTheme {
        ScheduleDaysPage()
    }
}

@Preview(backgroundColor = 0xFF4CE062, showBackground = true)
@Composable
private fun ScheduleHoursPagePreview() {
    BundelOnboardingTheme {
        ScheduleHoursPage()
    }
}

@Composable
internal fun OnboardingScreen(
    needsPermission: Boolean,
    onSettingsIntentClick: () -> Unit,
    onOnboardingDoneClicked: () -> Unit,
    crashReportingEnabled: Boolean,
    onCrashlyticsEnabledChanged: (Boolean) -> Unit
) {
    Surface {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_bundel_icon),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.width(24.dp))
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.h2)
            }

            Spacer(modifier = Modifier.height(32.dp))

            val pagerState = rememberPagerState(pageCount = 4)
            OnboardingPager(
                onSettingsIntentClick = onSettingsIntentClick,
                crashReportingEnabled = crashReportingEnabled,
                onCrashlyticsEnabledChanged = onCrashlyticsEnabledChanged,
                pagerState = pagerState,
                needsPermission = needsPermission
            )

            Spacer(modifier = Modifier.height(32.dp))

            ActionsRow(pagerState, needsPermission, onOnboardingDoneClicked)
        }
    }
}

@Composable
private fun ColumnScope.OnboardingPager(
    onSettingsIntentClick: () -> Unit,
    crashReportingEnabled: Boolean,
    onCrashlyticsEnabledChanged: (Boolean) -> Unit,
    pagerState: PagerState,
    needsPermission: Boolean
) {
    @Suppress("MagicNumber") // Yolo, page indices
    HorizontalPager(pagerState, dragEnabled = false, modifier = Modifier.weight(1F)) { pageIndex ->
        when (pageIndex) {
            0 -> IntroPage(crashReportingEnabled, onCrashlyticsEnabledChanged)
            1 -> NotificationsAccessPage(onSettingsIntentClick, needsPermission)
            2 -> ScheduleDaysPage()
            3 -> ScheduleHoursPage()
            4 -> AllSetPage()
            else -> error("Too many pages")
        }
    }
}

@Composable
fun IntroPage(
    crashReportingEnabled: Boolean,
    onCrashlyticsEnabledChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_welcome_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.onboarding_blurb),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        CrashlyticsSwitch(
            crashReportingEnabled = crashReportingEnabled,
            onCrashlyticsEnabledChanged = onCrashlyticsEnabledChanged,
            modifier = Modifier.padding(vertical = singlePadding(), horizontal = 16.dp)
        )
    }
}

@Composable
private fun CrashlyticsSwitch(
    crashReportingEnabled: Boolean,
    onCrashlyticsEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = Modifier
            .clickable { onCrashlyticsEnabledChanged(!crashReportingEnabled) }
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = crashReportingEnabled,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = MaterialTheme.colors.secondary,
                uncheckedTrackColor = MaterialTheme.colors.onSecondary,
                checkedThumbColor = MaterialTheme.colors.secondary,
                checkedTrackColor = MaterialTheme.colors.onSecondary
            )
        )

        Spacer(modifier = Modifier.width(singlePadding()))

        Text(stringResource(R.string.onboarding_enable_crashlytics))
    }
}

@Composable
private fun NotificationsAccessPage(onSettingsIntentClick: () -> Unit, needsPermission: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(id = R.string.notifications_permission_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5
        )

        Spacer(Modifier.height(24.dp))

        if (needsPermission) {
            Text(
                text = stringResource(R.string.notifications_permission_explanation),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(onClick = onSettingsIntentClick) {
                Text(stringResource(R.string.button_notifications_access_prompt))
            }
        } else {
            Icon(
                imageVector = Icons.Rounded.DoneOutline,
                contentDescription = stringResource(R.string.notifications_permission_done_image_content_description),
                tint = LocalContentColor.current,
                modifier = Modifier
                    .size(72.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.notifications_permission_all_done),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

private enum class WeekDays(@StringRes val displayResId: Int) {
    MONDAY(R.string.day_monday),
    TUESDAY(R.string.day_tuesday),
    WEDNESDAY(R.string.day_wednesday),
    THURSDAY(R.string.day_thursday),
    FRIDAY(R.string.day_friday),
    SATURDAY(R.string.day_saturday),
    SUNDAY(R.string.day_sunday)
}

@Composable
private fun ScheduleDaysPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_schedule_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_schedule_blurb),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier.padding(horizontal = 32.dp),
            mainAxisAlignment = MainAxisAlignment.Center,
            mainAxisSpacing = singlePadding(),
            crossAxisSpacing = singlePadding()
        ) {
            val checkedDays = remember { mutableStateListOf(*WeekDays.values().indices.map { true }.toTypedArray()) }

            for ((index, weekDay) in WeekDays.values().withIndex()) {
                MaterialChip(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    checkedBackgroundColor = MaterialTheme.colors.onSurface,
                    checked = checkedDays[index],
                    onCheckedChanged = { checked -> checkedDays[index] = checked }
                ) {
                    Text(
                        text = stringResource(id = weekDay.displayResId).uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.body1.plus(TextStyle(fontWeight = FontWeight.Medium))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_schedule_blurb_2),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

private data class TimeRange(
    val from: HourOfDay,
    val to: HourOfDay
) {

    data class HourOfDay(
        @IntRange(from = 0, to = 23) val hour: Int,
        @IntRange(from = 0, to = 59) val minute: Int
    )
}

private const val MAX_TIME_RANGES = 5

@Composable
fun ScheduleHoursPage() {
    val timeRanges = remember {
        mutableStateListOf(TimeRange(from = TimeRange.HourOfDay(9, 0), to = TimeRange.HourOfDay(13, 0)))
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_schedule_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_schedule_blurb),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                for ((index, timeRange) in timeRanges.withIndex()) {
                    TimeRangeRow(
                        timeRange = timeRange,
                        onRemoved = if (index > 0) {
                            { timeRanges.remove(it) }
                        } else {
                            null
                        },
                        onTimeRangeChanged = { newTimeRange ->
                            timeRanges.remove(timeRange)
                            timeRanges.add(index, newTimeRange)
                        }
                    )

                    Spacer(modifier = Modifier.height(singlePadding()))
                }

                if (timeRanges.size < MAX_TIME_RANGES) {
                    Box(modifier = Modifier.clickable { timeRanges.add(timeRanges.last().copy()) }) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                            TimeRangeRow(timeRange = null, enabled = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeRangeRow(
    timeRange: TimeRange? = null,
    pillButtonColors: ButtonColors = buttonColors(
        backgroundColor = MaterialTheme.colors.onSurface
    ),
    pillButtonElevation: ButtonElevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
    enabled: Boolean = true,
    onRemoved: ((TimeRange) -> Unit)? = null,
    onTimeRangeChanged: (TimeRange) -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onRemoved != null && timeRange != null) {
            IconButton(onClick = { onRemoved(timeRange) }) {
                Icon(Icons.Rounded.Clear, contentDescription = "Remove")
            }

            Spacer(modifier = Modifier.width(singlePadding()))
        } else {
            Box(Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(singlePadding()))
        }

        Text(text = "From")

        Spacer(modifier = Modifier.width(singlePadding()))

        // TODO use proper date/time formatting
        fun TimeRange.HourOfDay?.asDisplayString() = if (this != null) {
            "$hour:${minute.toString().padStart(2, '0')}"
        } else {
            ""
        }

        TimePillButton(
            text = timeRange?.from.asDisplayString(),
            pillButtonColors = pillButtonColors,
            pillButtonElevation = pillButtonElevation,
            enabled = enabled
        ) { onTimeRangeChanged(checkNotNull(timeRange) { "Null timeRange" }.copy(from = TimeRange.HourOfDay(it.hour, it.minute))) }

        Spacer(modifier = Modifier.width(singlePadding()))

        Text(text = "to")

        Spacer(modifier = Modifier.width(singlePadding()))

        TimePillButton(
            text = timeRange?.to.asDisplayString(),
            pillButtonColors = pillButtonColors,
            pillButtonElevation = pillButtonElevation,
            enabled = enabled
        ) { onTimeRangeChanged(checkNotNull(timeRange) { "Null timeRange" }.copy(to = TimeRange.HourOfDay(it.hour, it.minute))) }
    }
}

@Composable
private fun TimePillButton(
    text: String,
    pillButtonColors: ButtonColors,
    pillButtonElevation: ButtonElevation,
    enabled: Boolean,
    onValueChanged: (LocalTime) -> Unit
) {
    val timePickerDialog = remember { MaterialDialog() }
    BundelTheme {
        timePickerDialog.build(shape = RoundedCornerShape(16.dp)) {
            timepicker(is24HourClock = true) { time ->
                onValueChanged(time)
            }
            buttons {
                positiveButton("Ok")
                negativeButton("Cancel")
            }
        }
    }

    Button(
        shape = CircleShape,
        colors = pillButtonColors,
        elevation = pillButtonElevation,
        enabled = enabled,
        onClick = { timePickerDialog.show() }
    ) {
        Text(text = text)
    }
}

@Composable
fun AllSetPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_all_set),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.onboarding_all_set_blurb),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionsRow(
    pagerState: PagerState,
    needsPermission: Boolean,
    onOnboardingDoneClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        val scope = rememberCoroutineScope()
        val buttonColors = buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = MaterialTheme.colors.onSurface
        )
        val buttonElevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)

        AnimatedVisibility(
            visible = pagerState.currentPage > 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                modifier = Modifier.align(Alignment.CenterStart),
                colors = buttonColors,
                elevation = buttonElevation
            ) {
                Text(text = stringResource(id = R.string.back).uppercase(Locale.getDefault()))
            }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.align(Alignment.Center)
        )

        when {
            pagerState.currentPage < pagerState.pageCount - 1 -> {
                Button(
                    enabled = if (needsPermission) pagerState.currentPage != 1 else true,
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    modifier = Modifier.align(Alignment.CenterEnd),
                    colors = buttonColors,
                    elevation = buttonElevation
                ) {
                    Text(text = stringResource(id = R.string.next).uppercase(Locale.getDefault()))
                }
            }
            else -> {
                Button(
                    onClick = { onOnboardingDoneClicked() },
                    modifier = Modifier.align(Alignment.CenterEnd),
                    colors = buttonColors,
                    elevation = buttonElevation
                ) {
                    Text(text = stringResource(id = R.string.done).uppercase(Locale.getDefault()))
                }
            }
        }
    }
}
