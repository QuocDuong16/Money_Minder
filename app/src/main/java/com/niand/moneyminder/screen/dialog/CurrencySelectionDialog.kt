package com.niand.moneyminder.screen.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CurrencySelectionDialog(
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isDollarSelected by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Chọn Đơn Vị Tiền Tệ") },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    val selectedCurrency = if (isDollarSelected) "$" else "₫"
                    onCurrencySelected(selectedCurrency)
                    onDismiss() // Đóng hộp thoại sau khi xác nhận
                }) {
                    Text(text = "Chọn")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Hiển thị RadioButton để chọn đồng hoặc đô la
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isDollarSelected = !isDollarSelected
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isDollarSelected,
                        onClick = {
                            isDollarSelected = !isDollarSelected
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isDollarSelected) "Đô la (\$)" else "Đồng (₫)")
                }
            }
        }
    )
}
