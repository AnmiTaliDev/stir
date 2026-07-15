package dev.anmitali.stir.ui.alarmedit

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.text.format.DateFormat
import kotlin.math.abs

private val ITEM_HEIGHT = 44.dp
private const val VISIBLE_ITEMS = 5
private const val CENTER_SLOT = VISIBLE_ITEMS / 2

@Composable
fun StirTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            SelectionBand()

            if (is24Hour) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Wheel(
                        itemCount = 24,
                        selected = hour,
                        label = { it.toString().padStart(2, '0') },
                        onSelectedChange = { onTimeChange(it, minute) },
                    )
                    Separator()
                    Wheel(
                        itemCount = 60,
                        selected = minute,
                        label = { it.toString().padStart(2, '0') },
                        onSelectedChange = { onTimeChange(hour, it) },
                    )
                }
            } else {
                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                val amPmIndex = if (hour < 12) 0 else 1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Wheel(
                        itemCount = 12,
                        selected = displayHour - 1,
                        label = { (it + 1).toString() },
                        onSelectedChange = { newDisplayHour ->
                            onTimeChange(to24Hour(newDisplayHour + 1, amPmIndex), minute)
                        },
                    )
                    Separator()
                    Wheel(
                        itemCount = 60,
                        selected = minute,
                        label = { it.toString().padStart(2, '0') },
                        onSelectedChange = { onTimeChange(hour, it) },
                    )
                    Wheel(
                        itemCount = 2,
                        selected = amPmIndex,
                        label = { if (it == 0) "AM" else "PM" },
                        onSelectedChange = { newAmPm ->
                            onTimeChange(to24Hour(displayHour, newAmPm), minute)
                        },
                        widthDp = 56,
                    )
                }
            }
        }
    }
}

private fun to24Hour(displayHour: Int, amPmIndex: Int): Int {
    val base = displayHour % 12
    return if (amPmIndex == 0) base else base + 12
}

@Composable
private fun Separator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun SelectionBand() {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.86f)
            .height(ITEM_HEIGHT)
            .background(
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f),
                RoundedCornerShape(14.dp),
            ),
    )
}

@Composable
private fun Wheel(
    itemCount: Int,
    selected: Int,
    label: (Int) -> String,
    onSelectedChange: (Int) -> Unit,
    widthDp: Int = 64,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val snapLayoutInfoProvider = remember(listState) { SnapLayoutInfoProvider(listState) }
    val flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider)

    val centeredIndex by remember { derivedStateOf { centeredItemIndex(listState) } }

    LaunchedEffect(centeredIndex) {
        if (centeredIndex in 0 until itemCount && centeredIndex != selected) {
            onSelectedChange(centeredIndex)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(vertical = ITEM_HEIGHT * CENTER_SLOT),
        modifier = Modifier
            .width(widthDp.dp)
            .height(ITEM_HEIGHT * VISIBLE_ITEMS),
    ) {
        items(itemCount) { index ->
            val isSelected = index == centeredIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ITEM_HEIGHT),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(index),
                    fontSize = if (isSelected) 26.sp else 20.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
                    },
                )
            }
        }
    }
}

private fun centeredItemIndex(listState: LazyListState): Int {
    val layoutInfo = listState.layoutInfo
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    return layoutInfo.visibleItemsInfo
        .minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }
        ?.index
        ?: listState.firstVisibleItemIndex
}
