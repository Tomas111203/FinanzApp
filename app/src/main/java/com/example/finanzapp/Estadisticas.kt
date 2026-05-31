package com.example.finanzapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        try {
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

    // Calcular totales
    val totalExpenses = categoryStats.sumOf { it.totalAmount }
    val totalIncome = monthlyStats.sumOf { it.income }
    val savings = totalIncome - totalExpenses
    val expensePercentage = if (totalIncome > 0) (totalExpenses / totalIncome) * 100 else 0.0
    val mostSpentCategory = categoryStats.maxByOrNull { it.totalAmount }

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
                                                    entries.add(PieEntry(stat.totalAmount.toFloat(), stat.name))
                                                }

                                                val dataSet = PieDataSet(entries, "").apply {
                                                    setDrawIcons(false)
                                                    sliceSpace = 3f
                                                    selectionShift = 5f
                                                    colors = categoryStats.indices.map { i ->
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
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    HorizontalDivider(color = Color(0xFFE5E7EB))

                                    categoryStats.forEachIndexed { index, stat ->
                                        CategoryItem(
                                            name = stat.name,
                                            value = stat.totalAmount,
                                            color = categoryColors[index % categoryColors.size],
                                            currencyFormatter = currencyFormatter
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Gráfico de barras mensual
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
                                    text = "Ingresos vs Gastos (últimos meses)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (monthlyStats.isNotEmpty()) {
                                    AndroidView(
                                        factory = { context ->
                                            BarChart(context).apply {
                                                description.isEnabled = false
                                                setDrawGridBackground(false)
                                                setDrawBarShadow(false)
                                                setDrawValueAboveBar(true)
                                                setPinchZoom(false)
                                                setScaleEnabled(false)
                                                setMaxVisibleValueCount(60)

                                                xAxis.apply {
                                                    position = XAxis.XAxisPosition.BOTTOM
                                                    setDrawGridLines(false)
                                                    granularity = 1f
                                                    valueFormatter = IndexAxisValueFormatter(
                                                        monthlyStats.map { it.month }
                                                    )
                                                    setCenterAxisLabels(true)
                                                }

                                                axisLeft.apply {
                                                    setDrawGridLines(true)
                                                    axisMinimum = 0f
                                                    valueFormatter = object : ValueFormatter() {
                                                        override fun getFormattedValue(value: Float): String {
                                                            return if (value >= 1000) "${(value / 1000).toInt()}k"
                                                            else value.toInt().toString()
                                                        }
                                                    }
                                                }

                                                axisRight.isEnabled = false

                                                legend.apply {
                                                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                                                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
                                                    orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                                                    setDrawInside(false)
                                                }

                                                val incomeEntries = ArrayList<BarEntry>()
                                                val expenseEntries = ArrayList<BarEntry>()

                                                monthlyStats.forEachIndexed { index, item ->
                                                    incomeEntries.add(BarEntry(index.toFloat(), item.income.toFloat()))
                                                    expenseEntries.add(BarEntry(index.toFloat(), item.expenses.toFloat()))
                                                }

                                                val incomeDataSet = BarDataSet(incomeEntries, "Ingresos").apply {
                                                    color = Color(0xFF10B981).toArgb()
                                                    valueTextSize = 10f
                                                    valueTextColor = Color.Black.toArgb()
                                                }

                                                val expenseDataSet = BarDataSet(expenseEntries, "Gastos").apply {
                                                    color = Color(0xFFEF4444).toArgb()
                                                    valueTextSize = 10f
                                                    valueTextColor = Color.Black.toArgb()
                                                }

                                                val barData = BarData(incomeDataSet, expenseDataSet)
                                                barData.barWidth = 0.3f
                                                data = barData
                                                groupBars(0f, 0.4f, 0.05f)
                                                xAxis.axisMinimum = -0.5f
                                                xAxis.axisMaximum = monthlyStats.size.toFloat() - 0.5f
                                                invalidate()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Insights
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
                                    text = "• Este mes has ahorrado " +
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