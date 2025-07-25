package com.dls.pymetask.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.Roboto

@Composable
fun DashboardCardItem(card: DashboardCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = card.title,
                tint = Color(0xFF1976D2),
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFBBDEFB), shape = CircleShape)
                    .padding(4.dp)
            )

            Text(
                text = card.title,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Poppins,
                fontSize = 18.sp,
                color = Color(0xFF263238)
            )

            Text(
                text = card.subtitle,
                fontFamily = Roboto,
                fontSize = 16.sp,
                color = Color(0xFF546E7A)
            )
        }
    }
}

data class DashboardCard(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)