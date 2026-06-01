package com.example.finanzapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    viewModel: TransactionViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val repository = remember { TransactionRepository() }
    val coroutineScope = rememberCoroutineScope()

    // Estados para datos
    var categoryStats by remember { mutableStateOf<List<CategoryStats>>(emptyList()) }
    var monthlyStats by remember { mutableStateOf<List<MonthlyStats>>(emptyList()) }
    var allTransactions by remember { mutableStateOf<List<FirestoreTransaction>>(emptyList()) }  // ← NUEVO
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        try {
            allTransactions = repository.getAllTransactions()  // ← NUEVO: cargar transacciones
            categoryStats = repository.getCategoryStats()
            monthlyStats = repository.getMonthlyStats()
        } catch (e: Exception) {
            errorMessage = "Error al cargar estadísticas: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale("es", "MX"))
    }

    // Colores para categorías
    val categoryColors = listOf(
        Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF8B5CF6),
        Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFFEC4899),
        Color(0xFF06B6D4), Color(0xFF84CC16)
    )

    // Calcular totales (usando los datos ya cargados)
    val totalExpenses = categoryStats.sumOf { it.totalAmount }
    val totalIncome = monthlyStats.sumOf { it.income }
    val savings = totalIncome - totalExpenses
    val expensePercentage = if (totalIncome > 0) (totalExpenses / totalIncome) * 100 else 0.0
    val mostSpentCategory = categoryStats.maxByOrNull { it.totalAmount }

    // Calcular gastos reales para insights (excluyendo TC y mostrando info correcta)
    val realExpenses = allTransactions
        .filter {
            it.type == "expense" &&
                    it.paymentMethod != "Tarjeta de Crédito"
        }
        .sumOf { it.amount } + allTransactions
        .filter {
            it.type == "transfer" &&
                    it.paymentMethod == "Transferencia"
        }
        .sumOf { it.amount }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = "Estadísticas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFF0FDF4)
    ) { paddingValues ->
        when {
            isLoading -> {
                // Pantalla de carga
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF16A34A)
                    )
                }
            }
            errorMessage != null -> {
                // Pantalla de error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        allTransactions = repository.getAllTransactions()
                                        categoryStats = repository.getCategoryStats()
                                        monthlyStats = repository.getMonthlyStats()
                                    } catch (e: Exception) {
                                        errorMessage = "Error al cargar estadísticas: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            categoryStats.isEmpty() && monthlyStats.isEmpty() -> {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.lucide__mail),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay datos para mostrar",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Agrega transacciones para ver estadísticas",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            else -> {
                // Contenido principal con datos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tarjetas de resumen
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryCard(
                                title = "Total Gastos",
                                amount = totalExpenses,
                                icon = painterResource(R.drawable.icon_park_outline__trending_down),
                                iconColor = Color(0xFFDC2626),
                                modifier = Modifier.weight(1f),
                                currencyFormatter = currencyFormatter
                            )
                            SummaryCard(
                                title = "Total Ingresos",
                                amount = totalIncome,
                                icon = painterResource(R.drawable.icon_park_outline__trending_up),
                                iconColor = Color(0xFF16A34A),
                                modifier = Modifier.weight(1f),
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }

                    // Gráfico de pastel por categorías
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Gastos por categoría",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (categoryStats.isNotEmpty()) {
                                    AndroidView(
                                        factory = { context ->
                                            PieChart(context).apply {
                                                setUsePercentValues(true)
                                                description.isEnabled = false
                                                setExtraOffsets(5f, 10f, 5f, 10f)
                                                dragDecelerationFrictionCoef = 0.95f
                                                isDrawHoleEnabled = true
                                                setHoleColor(Color.White.toArgb())
                                                setTransparentCircleColor(Color.White.toArgb())
                                                setTransparentCircleAlpha(110)
                                                holeRadius = 58f
                                                transparentCircleRadius = 61f
                                                setDrawCenterText(true)
                                                rotationAngle = 0f
                                                isRotationEnabled = true
                                                isHighlightPerTapEnabled = true

                                                legend.isEnabled = false

                                                setCenterTextSize(14f)
                                                setCenterText("Gastos\nTotales")
                                                setCenterTextColor(Color.Black.toArgb())

                                                val entries = ArrayList<PieEntry>()
                                                categoryStats.forEachIndexed { index, stat ->
                                                    if (stat.totalAmount > 0) {  // ← Solo mostrar categorías con gastos
                                                        entries.add(PieEntry(stat.totalAmount.toFloat(), stat.name))
                                                    }
                                                }

                                                if (entries.isNotEmpty()) {
                                                    val dataSet = PieDataSet(entries, "").apply {
                                                        setDrawIcons(false)
                                                        sliceSpace = 3f
                                                        selectionShift = 5f
                                                        colors = entries.indices.map { i ->
                                                            categoryColors[i % categoryColors.size].toArgb()
                                                        }
                                                    }

                                                    val pieData = PieData(dataSet).apply {
                                                        setValueTextSize(14f)
                                                        setValueTextColor(Color.Black.toArgb())
                                                        setValueFormatter(object : ValueFormatter() {
                                                            override fun getFormattedValue(value: Float): String {
                                                                return if (value >= 5f) "${value.toInt()}%" else ""
                                                            }
                                                        })
                                                    }

                                                    data = pieData
                                                    setEntryLabelColor(Color.Black.toArgb())
                                                    setEntryLabelTextSize(9f)
                                                    invalidate()
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    HorizontalDivider(color = Color(0xFFE5E7EB))

                                    categoryStats.forEachIndexed { index, stat ->
                                        if (stat.totalAmount > 0) {  // ← Solo mostrar categorías con gastos
                                            CategoryItem(
                                                name = stat.name,
                                                value = stat.totalAmount,
                                                color = categoryColors[index % categoryColors.size],
                                                currencyFormatter = currencyFormatter
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "No hay gastos registrados",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(vertical = 20.dp)
                                    )
                                }
                            }
                        }
                    }


                    // Gráfico de barras mensual - SOLO ÚLTIMO MES
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Ingresos vs Gastos",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Selector de mes
                                val monthsWithData = monthlyStats
                                    .filter { it.income > 0 || it.expenses > 0 }
                                    .map { "${it.month} ${it.year}" }

                                var selectedMonth by remember { mutableStateOf(monthsWithData.lastOrNull() ?: "") }
                                var expanded by remember { mutableStateOf(false) }

                                if (monthsWithData.isNotEmpty()) {
                                    // Botón selector de mes
                                    OutlinedButton(
                                        onClick = { expanded = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (selectedMonth.isNotEmpty()) "📅 $selectedMonth" else "Selecciona un mes",
                                                fontSize = 14.sp
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Seleccionar mes",
                                                tint = Color.Gray
                                            )
                                        }
                                    }

                                    // Dropdown de meses
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        monthsWithData.forEach { month ->
                                            DropdownMenuItem(
                                                text = { Text(month) },
                                                onClick = {
                                                    selectedMonth = month
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Obtener el mes seleccionado
                                    val selectedMonthData = monthlyStats.firstOrNull {
                                        "${it.month} ${it.year}" == selectedMonth
                                    }

                                    if (selectedMonthData != null) {
                                        // Mostrar leyenda
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(Color(0xFF10B981), CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Ingresos", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(Color(0xFFEF4444), CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Gastos", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // CORREGIDO: Usar key para forzar recomposición del gráfico
                                        key(selectedMonth) {
                                            AndroidView(
                                                factory = { context ->
                                                    BarChart(context).apply {
                                                        description.isEnabled = false
                                                        setDrawGridBackground(false)
                                                        setDrawBarShadow(false)
                                                        setDrawValueAboveBar(true)
                                                        setPinchZoom(false)
                                                        setScaleEnabled(false)

                                                        xAxis.apply {
                                                            position = XAxis.XAxisPosition.BOTTOM
                                                            setDrawGridLines(false)
                                                            granularity = 1f
                                                            setDrawLabels(true)
                                                            axisMinimum = -0.5f
                                                            axisMaximum = 1.5f
                                                            valueFormatter = IndexAxisValueFormatter(
                                                                listOf("Ingresos", "Gastos")
                                                            )
                                                        }

                                                        axisLeft.apply {
                                                            setDrawGridLines(true)
                                                            axisMinimum = 0f
                                                            val maxValue = maxOf(selectedMonthData.income, selectedMonthData.expenses)
                                                            axisMaximum = if (maxValue > 0) (maxValue * 1.2f).toFloat() else 100f

                                                            valueFormatter = object : ValueFormatter() {
                                                                override fun getFormattedValue(value: Float): String {
                                                                    return if (value >= 1000) "${(value / 1000).toInt()}k"
                                                                    else value.toInt().toString()
                                                                }
                                                            }
                                                        }

                                                        axisRight.isEnabled = false
                                                        legend.isEnabled = false

                                                        val entries = ArrayList<BarEntry>()
                                                        entries.add(BarEntry(0f, selectedMonthData.income.toFloat()))
                                                        entries.add(BarEntry(1f, selectedMonthData.expenses.toFloat()))

                                                        val dataSet = BarDataSet(entries, "").apply {
                                                            colors = listOf(
                                                                Color(0xFF10B981).toArgb(),
                                                                Color(0xFFEF4444).toArgb()
                                                            )
                                                            valueTextSize = 14f
                                                            valueTextColor = Color.Black.toArgb()
                                                            setDrawValues(true)
                                                        }

                                                        val barData = BarData(dataSet)
                                                        barData.barWidth = 0.5f
                                                        data = barData

                                                        animateY(1000)
                                                        invalidate()
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(280.dp),
                                                update = { chart ->
                                                    // CORREGIDO: Actualizar el gráfico cuando cambian los datos
                                                    chart.data?.clearValues()

                                                    val entries = ArrayList<BarEntry>()
                                                    entries.add(BarEntry(0f, selectedMonthData.income.toFloat()))
                                                    entries.add(BarEntry(1f, selectedMonthData.expenses.toFloat()))

                                                    val dataSet = BarDataSet(entries, "").apply {
                                                        colors = listOf(
                                                            Color(0xFF10B981).toArgb(),
                                                            Color(0xFFEF4444).toArgb()
                                                        )
                                                        valueTextSize = 14f
                                                        valueTextColor = Color.Black.toArgb()
                                                        setDrawValues(true)
                                                    }

                                                    val barData = BarData(dataSet)
                                                    barData.barWidth = 0.5f
                                                    chart.data = barData

                                                    // Actualizar el eje Y
                                                    val maxValue = maxOf(selectedMonthData.income, selectedMonthData.expenses)
                                                    chart.axisLeft.axisMaximum = if (maxValue > 0) (maxValue * 1.2f).toFloat() else 100f

                                                    chart.animateY(500)
                                                    chart.invalidate()
                                                }
                                            )
                                        }

                                        // Mostrar valores numéricos adicionales
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            // Total ingresos
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Total Ingresos", fontSize = 11.sp, color = Color.Gray)
                                                Text(
                                                    text = "$${currencyFormatter.format(selectedMonthData.income)}",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF16A34A)
                                                )
                                            }

                                            // Separador
                                            Box(
                                                modifier = Modifier
                                                    .width(1.dp)
                                                    .height(30.dp)
                                                    .background(Color.LightGray)
                                            )

                                            // Total gastos
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Total Gastos", fontSize = 11.sp, color = Color.Gray)
                                                Text(
                                                    text = "$${currencyFormatter.format(selectedMonthData.expenses)}",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFDC2626)
                                                )
                                            }

                                            // Separador
                                            Box(
                                                modifier = Modifier
                                                    .width(1.dp)
                                                    .height(30.dp)
                                                    .background(Color.LightGray)
                                            )

                                            // Balance
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Balance", fontSize = 11.sp, color = Color.Gray)
                                                val balance = selectedMonthData.income - selectedMonthData.expenses
                                                Text(
                                                    text = "$${currencyFormatter.format(balance)}",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (balance >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Selecciona un mes para ver los datos",
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                } else {
                                    // Mensaje cuando no hay datos
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                painter = painterResource(R.drawable.lucide__chart_column),
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Agrega transacciones para ver la gráfica",
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Insights (mejorado con datos reales)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3E8FF)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "💡 Insights",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B21A8)
                                )

                                mostSpentCategory?.let { category ->
                                    Text(
                                        text = "• Tu categoría con mayor gasto es " +
                                                "${category.name} con " +
                                                "$${currencyFormatter.format(category.totalAmount)}",
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B21A8),
                                        lineHeight = 20.sp
                                    )
                                }

                                Text(
                                    text = "• Has ahorrado " +
                                            "$${currencyFormatter.format(savings)}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B21A8),
                                    lineHeight = 20.sp
                                )

                                if (totalIncome > 0) {
                                    Text(
                                        text = "• Tus gastos representan el " +
                                                "${String.format("%.1f", expensePercentage)}% " +
                                                "de tus ingresos",
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B21A8),
                                        lineHeight = 20.sp
                                    )
                                }

                                // Insight adicional sobre deuda de TC
                                val totalCreditDebt = allTransactions
                                    .filter {
                                        it.type == "expense" &&
                                                it.paymentMethod == "Tarjeta de Crédito"
                                    }
                                    .sumOf { it.amount - it.creditPaidSoFar }

                                if (totalCreditDebt > 0) {
                                    Text(
                                        text = "• Tienes una deuda de TC de " +
                                                "$${currencyFormatter.format(totalCreditDebt)}",
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B21A8),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    icon: Painter,
    iconColor: Color,
    modifier: Modifier = Modifier,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$${currencyFormatter.format(amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    name: String,
    value: Double,
    color: Color,
    currencyFormatter: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = name,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
        Text(
            text = "$${currencyFormatter.format(value)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun EstadisticasScreenPreview() {
    MaterialTheme {
        EstadisticasScreen()
    }
}