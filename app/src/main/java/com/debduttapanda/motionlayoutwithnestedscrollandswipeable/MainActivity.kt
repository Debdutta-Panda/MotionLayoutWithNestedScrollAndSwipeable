package com.debduttapanda.motionlayoutwithnestedscrollandswipeable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.*
import com.debduttapanda.motionlayoutwithnestedscrollandswipeable.ui.theme.MotionLayoutWithNestedScrollAndSwipeableTheme
import java.util.*

enum class SwipingStates {
    EXPANDED,
    COLLAPSED
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class, ExperimentalMotionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotionLayoutWithNestedScrollAndSwipeableTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val swipingState = rememberSwipeableState(initialValue = SwipingStates.EXPANDED)
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val heightInPx = with(LocalDensity.current) { maxHeight.toPx() }
                        val connection = remember {
                            object : NestedScrollConnection {
                                override fun onPreScroll(
                                    available: Offset,
                                    source: NestedScrollSource
                                ): Offset {
                                    val delta = available.y
                                    return if (delta < 0) {
                                        swipingState.performDrag(delta).toOffset()
                                    } else {
                                        Offset.Zero
                                    }
                                }

                                override fun onPostScroll(
                                    consumed: Offset,
                                    available: Offset,
                                    source: NestedScrollSource
                                ): Offset {
                                    val delta = available.y
                                    return swipingState.performDrag(delta).toOffset()
                                }

                                override suspend fun onPostFling(
                                    consumed: Velocity,
                                    available: Velocity
                                ): Velocity {
                                    swipingState.performFling(velocity = available.y)
                                    return super.onPostFling(consumed, available)
                                }

                                private fun Float.toOffset() = Offset(0f, this)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .swipeable(
                                    state = swipingState,
                                    thresholds = { _, _ -> FractionalThreshold(0.05f) },
                                    orientation = Orientation.Vertical,
                                    anchors = mapOf(
                                        0f to SwipingStates.COLLAPSED,
                                        heightInPx to SwipingStates.EXPANDED,
                                    )
                                )
                                .nestedScroll(connection)
                        ) {
                            val computedProgress by remember {
                                derivedStateOf {
                                    if (swipingState.progress.to == SwipingStates.COLLAPSED)
                                        swipingState.progress.fraction
                                    else
                                        1f - swipingState.progress.fraction
                                }
                            }
                            val startHeightNum = 300
                            MotionLayout(
                                debug = EnumSet.of(MotionLayoutDebugFlags.SHOW_ALL),
                                modifier = Modifier.fillMaxSize(),
                                start = ConstraintSet {
                                    val header = createRefFor("header")
                                    val body = createRefFor("body")
                                    val content1 = createRefFor("content1")
                                    val content2 = createRefFor("content2")
                                    constrain(header){
                                        this.width = Dimension.matchParent
                                        this.height = Dimension.value(300.dp)
                                    }
                                    constrain(body){
                                        this.width = Dimension.matchParent
                                        this.height = Dimension.fillToConstraints
                                        this.top.linkTo(header.bottom,0.dp)
                                        this.bottom.linkTo(parent.bottom,0.dp)
                                    }
                                    constrain(content1){
                                        this.start.linkTo(header.start)
                                        this.end.linkTo(header.end)
                                        this.top.linkTo(header.top,24.dp)
                                        this.bottom.linkTo(content2.top,12.dp)
                                        this.height = Dimension.fillToConstraints
                                    }
                                    constrain(content2){
                                        this.start.linkTo(header.start)
                                        this.end.linkTo(header.end)
                                        this.bottom.linkTo(header.bottom,24.dp)
                                    }
                                },
                                end = ConstraintSet {
                                    val header = createRefFor("header")
                                    val body = createRefFor("body")
                                    val content1 = createRefFor("content1")
                                    val content2 = createRefFor("content2")
                                    constrain(header){
                                        this.height = Dimension.value(60.dp)
                                    }
                                    constrain(body){
                                        this.width = Dimension.matchParent
                                        this.height = Dimension.fillToConstraints
                                        this.top.linkTo(header.bottom,0.dp)
                                        this.bottom.linkTo(parent.bottom,0.dp)
                                    }
                                    constrain(content1){
                                        this.start.linkTo(header.start,24.dp)
                                        this.top.linkTo(header.top,8.dp)
                                        this.bottom.linkTo(header.bottom,8.dp)
                                        this.height = Dimension.fillToConstraints
                                    }
                                    constrain(content2){
                                        this.start.linkTo(content1.end,12.dp)
                                        this.bottom.linkTo(header.bottom)
                                        this.top.linkTo(header.top)
                                    }
                                },
                                progress = computedProgress,
                            ) {

                                Box(
                                    modifier = Modifier
                                        .layoutId("body")
                                        .fillMaxWidth()
                                        .background(Color.White)
                                ){
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize()
                                    ){
                                        items(200, key = {it}){
                                            Box(
                                                modifier = Modifier
                                                    .padding(24.dp)
                                                    .fillMaxWidth()
                                                    .background(
                                                        Color.White,
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .border(
                                                        BorderStroke(
                                                            2.dp,
                                                            Color.Gray
                                                        ),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(50.dp),
                                                contentAlignment = Alignment.Center
                                            ){
                                                Text(it.toString())
                                            }
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .layoutId("header")
                                        .fillMaxWidth()
                                        .height(startHeightNum.dp)
                                        .background(Color.Gray)
                                ){

                                }

                                Box(
                                    modifier = Modifier
                                        .layoutId("content1")
                                        .aspectRatio(1f)
                                        .border(
                                            BorderStroke(
                                                4.dp,
                                                Color.Red
                                            ),
                                            CircleShape
                                        )
                                        .padding(8.dp)
                                        .background(Color.Blue, CircleShape)
                                ){

                                }

                                Text(
                                    "Numbers",
                                    color = Color.White,
                                    modifier = Modifier
                                        .layoutId("content2")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}