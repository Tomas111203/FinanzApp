package com.example.finanzapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.*

data class CategoryData(
    val name: String,
    val value: Double,
    val color: Color
)

data class MonthlyData(
    val month: String,
    val income: Double,
    val expenses: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    onBackClick: () -> Unit = {}
) {
    val categoryData = remember {
        listOf(
            CategoryData("Alimentación", 1100.5, Color(0xFF10B981)),
            CategoryData("Transporte", 365.0, Color(0xFF3B82F6)),
            CategoryData("Entretenimiento", 129.0, Color(0xFF8B5CF6)),
            CategoryData("Salud", 320.5, Color(0xFFEF4444)),
            CategoryData("Servicios", 850.0, Color(0xFFF59E0B))
        )
    }

    val monthlyData = remember {
        listOf(
            MonthlyData("Ene", 18000.0, 12500.0),
            MonthlyData("Feb", 18000.0, 13200.0),
            MonthlyData("Mar", 20500.0, 15280.0)
        )
    }

    val totalExpenses = categoryData.sumOf { it.value }
    val totalIncome = 20500.0
    // FIX: tipo explícito para evitar "Cannot infer type parameter T"
    val mostSpentCategory: CategoryData = categoryData.maxByOrNull { it.value } ?: categoryData[0]
    val savings = totalIncome - totalExpenses
    val expensePercentage = (totalExpenses / totalIncome) * 100

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("es", "MX")) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // FIX: TrendingDown → KeyboardArrowDown
                    SummaryCard(
                        title = "Total Gastos",
                        amount = totalExpenses,
                        icon = Icons.Default.KeyboardArrowDown,
                        iconColor = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f),
                        currencyFormatter = currencyFormatter
                    )
                    // FIX: TrendingUp → KeyboardArrowUp
                    SummaryCard(
                        title = "Total Ingresos",
                        amount = totalIncome,
                        icon = Icons.Default.KeyboardArrowUp,
                        iconColor = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f),
                        currencyFormatter = currencyFormatter
                    )
                }
            }

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

                        AndroidView(
                            factory = { context ->
                                PieChart(context).apply {
                                    setUsePercentValues(true)
                                    description.isEnabled = false
                                    setExtraOffsets(5f, 10f, 5f, 5f)
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

                                    val entries = categoryData.map { category ->
                                        PieEntry(category.value.toFloat(), category.name)
                                    }

                                    val dataSet = PieDataSet(entries, "").apply {
                                        setDrawIcons(false)
                                        sliceSpace = 3f
                                        selectionShift = 5f
                                        colors = categoryData.map { it.color.toArgb() }
                                    }

                                    data = PieData(dataSet).apply {
                                        setValueTextSize(12f)
                                        setValueTextColor(Color.Black.toArgb())
                                        setValueFormatter(object : ValueFormatter() {
                                            override fun getFormattedValue(value: Float): String {
                                                return "${value.toInt()}%"
                                            }
                                        })
                                    }

                                    invalidate()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // FIX: Divider → HorizontalDivider
                        HorizontalDivider(color = Color(0xFFE5E7EB))

                        categoryData.forEach { category ->
                            CategoryItem(
                                category = category,
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }
                }
            }

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
                            text = "Ingresos vs Gastos (3 meses)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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
                                        // FIX: List en lugar de Array
                                        valueFormatter = IndexAxisValueFormatter(
                                            monthlyData.map { it.month }
                                        )
                                    }

                                    axisLeft.apply {
                                        setDrawGridLines(true)
                                        axisMinimum = 0f
                                    }

                                    axisRight.isEnabled = false

                                    legend.apply {
                                        verticalAlignment = Legend.LegendVerticalAlignment.TOP
                                        horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                                        orientation = Legend.LegendOrientation.HORIZONTAL
                                        setDrawInside(false)
                                    }

                                    // FIX: renombrar 'data' → 'item' para evitar conflicto con this.data
                                    val incomeEntries = monthlyData.mapIndexed { index, item ->
                                        BarEntry(index.toFloat(), item.income.toFloat())
                                    }

                                    val expenseEntries = monthlyData.mapIndexed { index, item ->
                                        BarEntry(index.toFloat(), item.expenses.toFloat())
                                    }

                                    val incomeDataSet = BarDataSet(incomeEntries, "Ingresos").apply {
                                        color = Color(0xFF10B981).toArgb()
                                        valueTextSize = 10f
                                    }

                                    val expenseDataSet = BarDataSet(expenseEntries, "Gastos").apply {
                                        color = Color(0xFFEF4444).toArgb()
                                        valueTextSize = 10f
                                    }

                                    val barData = BarData(incomeDataSet, expenseDataSet).apply {
                                        barWidth = 0.3f
                                    }

                                    data = barData

                                    groupBars(0f, 0.4f, 0.02f)

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

                        Text(
                            text = "• Tu categoría con mayor gasto es " +
                                    "${mostSpentCategory.name} con " +
                                    "$${currencyFormatter.format(mostSpentCategory.value)}",
                            fontSize = 14.sp,
                            color = Color(0xFF6B21A8),
                            lineHeight = 20.sp
                        )

                        Text(
                            text = "• Este mes has ahorrado " +
                                    "$${currencyFormatter.format(savings)}",
                            fontSize = 14.sp,
                            color = Color(0xFF6B21A8),
                            lineHeight = 20.sp
                        )

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

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                    imageVector = icon,
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
    category: CategoryData,
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
                    .background(category.color)
            )
            Text(
                text = category.name,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
        Text(
            text = "$${currencyFormatter.format(category.value)}",
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