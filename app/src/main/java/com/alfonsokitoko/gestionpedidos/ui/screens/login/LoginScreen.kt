package com.alfonsokitoko.gestionpedidos.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alfonsokitoko.gestionpedidos.ui.components.BaseLayout

@Composable
fun LoginScreen(
	viewModel: LoginViewModel,
	onNavigateToMain: () -> Unit
) {
	var passwordVisible by remember { mutableStateOf(false) }

	LaunchedEffect(viewModel.loginSuccess) {
		if (viewModel.loginSuccess) {
			onNavigateToMain()
		}
	}
	Scaffold { innerPadding ->
		BaseLayout(
			titulo = "Inicio de sesión",
			onBack = null,
			modifier = Modifier.padding(innerPadding),
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Top
			) {
				Spacer(modifier = Modifier.height(40.dp))
				Text(
					text = "Introduce tus datos",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(bottom = 32.dp),
					textAlign = TextAlign.Center
				)
				OutlinedTextField(
					value = viewModel.email,
					onValueChange = { viewModel.email = it },
					label = { Text("Email") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Email,
						imeAction = ImeAction.Next
					),
					leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
				)

				Spacer(modifier = Modifier.height(16.dp))

				OutlinedTextField(
					value = viewModel.password,
					onValueChange = { viewModel.password = it },
					label = { Text("Contraseña") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Password,
						imeAction = ImeAction.Done
					),
					leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
					trailingIcon = {
						val image = if (passwordVisible)
							Icons.Default.Visibility
						else Icons.Default.VisibilityOff
						val description = if (passwordVisible) "Ocultar contrseña" else "Mostrar contraseña"

						IconButton(onClick = { passwordVisible = !passwordVisible }) {
							Icon(imageVector = image, description)
						}
					}
				)

				if (viewModel.errorMessage != null) {
					Text(
						text = viewModel.errorMessage!!,
						color = MaterialTheme.colorScheme.error,
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.padding(top = 12.dp)
					)
				}
				Spacer(modifier = Modifier.weight(1f))

				Button(
					onClick = { viewModel.onLoginClick() },
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					enabled = !viewModel.isLoading,
					shape = MaterialTheme.shapes.large
				) {
					if (viewModel.isLoading) {
						CircularProgressIndicator(
							modifier = Modifier.size(24.dp),
							color = MaterialTheme.colorScheme.onPrimary,
							strokeWidth = 2.dp
						)
					} else {
						Text("ENTRAR", fontWeight = FontWeight.Bold)
					}
				}
				Spacer(modifier = Modifier.height(32.dp))
			}
		}
	}
}