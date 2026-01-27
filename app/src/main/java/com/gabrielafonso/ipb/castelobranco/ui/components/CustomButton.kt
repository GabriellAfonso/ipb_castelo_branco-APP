package com.gabrielafonso.ipb.castelobranco.ui.components

import com.gabrielafonso.ipb.castelobranco.R
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

@Composable
fun CustomButton(
    onClick: () -> Unit,
    image: Painter = painterResource(id = R.drawable.sarca_ipb),
    text: String = "",
    backgroundColor: Color = Color.Blue,
    size: Dp = 100.dp
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(8.dp), // padding interno
        modifier = Modifier.size(size)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = image,
                contentDescription = text,
                modifier = Modifier.size(size * 0.6f) // ocupa 60% do botão
            )
            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
