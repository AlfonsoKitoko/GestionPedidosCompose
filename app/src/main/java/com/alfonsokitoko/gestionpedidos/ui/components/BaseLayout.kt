package com.alfonsokitoko.gestionpedidos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfonsokitoko.gestionpedidos.R

@Composable
fun BaseLayout(
	titulo: String,
	onBack: (() -> Unit)? = null,
	modifier: Modifier,
	actions: @Composable (RowScope.() -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.then(modifier)
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Image(
			painter = painterResource(id = R.drawable.logo_generico),
			contentDescription = "Logo Genérico",
			modifier = Modifier
				.fillMaxWidth()
				.height(60.dp),
			contentScale = ContentScale.Fit
		)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			if (onBack != null) {
				IconButton(onClick = onBack) {
					Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Volver")
				}
			}
			Text(
				text = titulo,
				fontSize = 22.sp,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.weight(1f)
			)
			actions?.invoke(this)
		}
		content(this)
	}
}