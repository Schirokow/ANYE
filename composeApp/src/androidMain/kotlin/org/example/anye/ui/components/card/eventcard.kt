package org.example.anye.ui.components.card


import androidx.compose.ui.Alignment
import org.example.anye.data.Event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.example.anye.R


@Preview //(showBackground = true)
@Composable
fun PreviewEventCard() {
    val event = Event(
        userId = "1",
        imageUrl = "",
        title = "Smooth Sound",
        description = "Free Entry - All Welcome",
        startData = "05.05.2025",
        city = "Stuttgart",
        location = null

    )
    EventCard(
        event = event,
        onClick = {},
        isLarge = false
    )
}

@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isLarge: Boolean = false,
    textIsLarge: Boolean = false
) {

    val cardSize = if (isLarge) 200.dp else 100.dp

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        modifier = modifier
            .size(cardSize)
            .clickable(onClick = onClick)
    ) {
        Box {

            AsyncImage(
                model = event.imageUrl ?: R.drawable.img,
                contentDescription = event.title,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .align(Alignment.BottomStart)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = event.title ?: "No Title",
                        color = Color.White,
                        fontSize = if (textIsLarge) 34.sp else 10.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = event.startData ?: "No Date",
                        color = Color.White,
                        fontSize = if (textIsLarge) 22.sp else 8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))


                }

            }
        }
    }
}