package com.example.kotlin_lab5

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.pow

@Composable
fun ReliabilityCalculator() {
    var connection by remember { mutableStateOf("6") }
    var accidentPrice by remember { mutableStateOf("23.6") }
    var plannedPrice by remember { mutableStateOf("17.6") }
    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputField(
            value = connection,
            label = "Підключення",
            keyboardType = KeyboardType.Number
        ) { connection = it }

        Spacer(modifier = Modifier.height(8.dp))

        InputField(
            value = accidentPrice,
            label = "Ціна аварії",
            keyboardType = KeyboardType.Decimal
        ) { accidentPrice = it }

        Spacer(modifier = Modifier.height(8.dp))

        InputField(
            value = plannedPrice,
            label = "Планова ціна",
            keyboardType = KeyboardType.Decimal
        ) { plannedPrice = it }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                calculationResult = calculateReliability(
                    connection.toFloatOrNull() ?: 6f,
                    accidentPrice.toFloatOrNull() ?: 23.6f,
                    plannedPrice.toFloatOrNull() ?: 17.6f
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Розрахувати")
        }

        Spacer(modifier = Modifier.height(16.dp))

        calculationResult?.let { result ->
            ResultCard(result)
        }
    }
}

@Composable
fun InputField(value: String, label: String, keyboardType: KeyboardType, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ResultCard(result: CalculationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ResultRow("Частота відмов (W_oc)", result.wOc)
            ResultRow("Середній час відновлення (t_v_oc)", result.tvOc, "рік^-1")
            ResultRow("Коефіцієнт аварійного простою (k_a_oc)", result.kaOc, "год")
            ResultRow("Коефіцієнт планового простою (k_p_oc)", result.kpOc)
            ResultRow("Частота відмов (W_dk)", result.wDk, "рік^-1")
            ResultRow("Частота відмов з урахуванням вимикача (W_dc)", result.wDc, "рік^-1")
            Text("Математичні сподівання:")
            ResultRow("аварійних поломок (math_W_ned_a)", result.mathWNedA, "кВт*год")
            ResultRow("планових поломок (math_W_ned_p)", result.mathWNedP, "кВт*год")
            ResultRow("збитків (math_loses)", result.mathLoses, "грн")
        }
    }
}

@Composable
fun ResultRow(label: String, value: Float, unit: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            softWrap = true,
            maxLines = Int.MAX_VALUE
        )
        Text(
            text = "${String.format("%.4f", value)} $unit",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

data class CalculationResult(
    val wOc: Float,
    val tvOc: Float,
    val kaOc: Float,
    val kpOc: Float,
    val wDk: Float,
    val wDc: Float,
    val mathWNedA: Float,
    val mathWNedP: Float,
    val mathLoses: Float
)

fun calculateReliability(n: Float, accidentPrice: Float, plannedPrice: Float): CalculationResult {
    val wOc = 0.01f + 0.07f + 0.015f + 0.02f + 0.03f * n
    val tvOc = (0.01f * 30 + 0.07f * 10 + 0.015f * 100 + 0.02f * 15 + (0.03f * n) * 2) / wOc
    val kaOc = (wOc * tvOc) / 8760
    val kpOc = 1.2f * (43f / 8760f)
    val wDk = 2 * wOc * (kaOc + kpOc)
    val wDc = wDk + 0.02f

    val mathWNedA = 0.01f * 45f * 10f.pow(-3) * 5.12f * 10f.pow(3) * 6451f
    val mathWNedP = 4f * 10f.pow(3) * 5.12f * 10f.pow(3) * 6451f
    val mathLoses = accidentPrice * mathWNedA + plannedPrice * mathWNedP

    return CalculationResult(
        wOc = wOc,
        tvOc = tvOc,
        kaOc = kaOc,
        kpOc = kpOc,
        wDk = wDk,
        wDc = wDc,
        mathWNedA = mathWNedA,
        mathWNedP = mathWNedP,
        mathLoses = mathLoses
    )
}
