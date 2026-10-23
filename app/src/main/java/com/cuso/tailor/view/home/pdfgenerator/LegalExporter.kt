package com.cuso.tailor.view.home.pdfgenerator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.cuso.tailor.model.legal.LegalDashboardData
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LegalDashboardExporter {

    private val dateStamp: String
        get() = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())

    // =========================================================================
    // 1. EXCEL EXPORT (.CSV - Opens natively in MS Excel with exact columns)
    // =========================================================================
    fun exportToExcel(context: Context, data: LegalDashboardData) {
        try {
            val file = File(context.cacheDir, "legal-dashboard-$dateStamp.csv")
            val writer = FileWriter(file)

            fun escape(text: String?): String {
                if (text == null) return ""
                val clean = text.replace("\"", "\"\"")
                return if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
                    "\"$clean\""
                } else clean
            }

            // Title & Timestamp
            writer.append("Legal Executive Dashboard Summary,Generated: ${data.generatedAt ?: SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")

            // Section 1: KPIs
            writer.append("KPI,Value,Change vs last month\n")
            val kpis = data.kpis
            writer.append("Total Legal Records,${kpis?.totalRecords?.value?.toInt() ?: 0},${kpis?.totalRecords?.change ?: 0}%\n")
            writer.append("Critical Risks,${kpis?.criticalRisks?.value?.toInt() ?: 0},${kpis?.criticalRisks?.change ?: 0}%\n")
            writer.append("Expiring Contracts,${kpis?.expiringContracts?.value?.toInt() ?: 0},${kpis?.expiringContracts?.change ?: 0}%\n")
            writer.append("Overdue Actions,${kpis?.overdueActions?.value?.toInt() ?: 0},${kpis?.overdueActions?.change ?: 0}%\n")
            writer.append("Pending Approvals,${kpis?.pendingApprovals?.value?.toInt() ?: 0},${kpis?.pendingApprovals?.change ?: 0}%\n")
            writer.append("Compliance Score,${kpis?.complianceScore?.value?.toInt() ?: 0}%,${kpis?.complianceScore?.change ?: 0}%\n")
            writer.append("Newly Filed Cases,${kpis?.newCases?.value?.toInt() ?: 0},${kpis?.newCases?.change ?: 0}%\n\n")

            // Section 2: Legal Health
            writer.append("Legal Health,Percent,Status\n")
            data.health.orEmpty().forEach { h ->
                writer.append("${escape(h.label)},${h.percent ?: 0}%,${escape(h.status)}\n")
            }
            writer.append("\n")

            // Section 3: Risk Distribution
            writer.append("Risk Distribution,Records,Percent\n")
            data.riskDistribution?.items.orEmpty().forEach { r ->
                writer.append("${escape(r.label)},${r.count ?: 0},${r.percent ?: 0.0}%\n")
            }
            writer.append("\n")

            // Section 4: Legal Categories
            writer.append("Legal Categories,Records,Open,High Risk\n")
            data.categories.orEmpty().forEach { c ->
                writer.append("${escape(c.label)},${c.records ?: 0},${c.open ?: 0},${c.highRisk ?: 0}\n")
            }
            writer.append("\n")

            // Section 5: Upcoming / Critical Legal Items
            writer.append("Upcoming Item,Module,Owner,Due Date,Priority,Status\n")
            data.upcoming.orEmpty().forEach { u ->
                val dateStr = u.date?.take(10) ?: "—"
                writer.append("${escape(u.title ?: u.number)},${escape(u.moduleLabel)},${escape(u.owner ?: "—")},${escape(dateStr)},${escape(u.riskLevel)},${escape(u.eventType ?: u.status ?: "—")}\n")
            }
            writer.append("\n")

            // Section 6: Approval Queue
            writer.append("Approval Request,Source Module,Requested By,Due Date,Status\n")
            data.approvals.orEmpty().forEach { a ->
                val dateStr = a.dueDate?.take(10) ?: "—"
                writer.append("${escape(a.title ?: a.requestNumber)},${escape(a.moduleLabel)},${escape(a.requestedBy)},${escape(dateStr)},${escape(a.state ?: a.stage ?: "Pending")}\n")
            }

            writer.flush()
            writer.close()

            openExportedFile(context, file, "text/csv")
        } catch (e: Exception) {
            Toast.makeText(context, "Export to Excel failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================
    // 2. TEXT / NOTEPAD EXPORT (.TXT)
    // =========================================================================
    fun exportToText(context: Context, data: LegalDashboardData) {
        try {
            val file = File(context.cacheDir, "legal-dashboard-$dateStamp.txt")
            val writer = FileWriter(file)

            writer.append("================================================================================\n")
            writer.append("                     LEGAL EXECUTIVE DASHBOARD SUMMARY                          \n")
            writer.append("================================================================================\n")
            writer.append("Generated Date: ${data.generatedAt ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
            writer.append("Dashboard Year: ${data.year ?: "Current"}\n\n")

            // 1. KPIs
            writer.append("--------------------------------------------------------------------------------\n")
            writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", "KPI", "VALUE", "CHANGE (vs mo)"))
            writer.append("--------------------------------------------------------------------------------\n")
            val kpis = data.kpis
            writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", "Total Legal Records", kpis?.totalRecords?.value?.toInt() ?: 0, "${kpis?.totalRecords?.change ?: 0}%"))
            writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", "Critical Risks", kpis?.criticalRisks?.value?.toInt() ?: 0, "${kpis?.criticalRisks?.change ?: 0}%"))
            writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", "Expiring Contracts", kpis?.expiringContracts?.value?.toInt() ?: 0, "${kpis?.expiringContracts?.change ?: 0}%"))
            writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", "Overdue Actions", kpis?.overdueActions?.value?.toInt() ?: 0, "${kpis?.overdueActions?.change ?: 0}%"))
            writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", "Pending Approvals", kpis?.pendingApprovals?.value?.toInt() ?: 0, "${kpis?.pendingApprovals?.change ?: 0}%"))
            writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", "Compliance Score", "${kpis?.complianceScore?.value?.toInt() ?: 0}%", "${kpis?.complianceScore?.change ?: 0}%"))
            writer.append("\n")

            // 2. Legal Health
            writer.append("--------------------------------------------------------------------------------\n")
            writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", "LEGAL HEALTH", "PERCENT", "STATUS"))
            writer.append("--------------------------------------------------------------------------------\n")
            data.health.orEmpty().forEach { h ->
                writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", h.label.orEmpty(), "${h.percent ?: 0}%", h.status.orEmpty()))
            }
            writer.append("\n")

            // 3. Risk Distribution
            writer.append("--------------------------------------------------------------------------------\n")
            writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", "RISK DISTRIBUTION", "RECORDS", "PERCENT"))
            writer.append("--------------------------------------------------------------------------------\n")
            data.riskDistribution?.items.orEmpty().forEach { r ->
                writer.append(String.format(Locale.getDefault(), "%-30s %-15s %-15s\n", r.label.orEmpty(), r.count ?: 0, "${r.percent ?: 0.0}%"))
            }
            writer.append("\n")

            // 4. Critical Legal Items
            writer.append("--------------------------------------------------------------------------------\n")
            writer.append(String.format(Locale.getDefault(), "%-32s %-16s %-12s %-10s\n", "UPCOMING ITEM", "OWNER", "DUE DATE", "PRIORITY"))
            writer.append("--------------------------------------------------------------------------------\n")
            data.upcoming.orEmpty().forEach { u ->
                val title = (u.title ?: u.number.orEmpty()).take(30)
                val owner = (u.owner ?: "—").take(15)
                val date = (u.date?.take(10) ?: "—").take(12)
                val priority = (u.riskLevel ?: "—").take(10)
                writer.append(String.format(Locale.getDefault(), "%-32s %-16s %-12s %-10s\n", title, owner, date, priority))
            }
            writer.append("\n")

            // 5. Approval Queue
            writer.append("--------------------------------------------------------------------------------\n")
            writer.append(String.format(Locale.getDefault(), "%-32s %-20s %-12s %-10s\n", "APPROVAL REQUEST", "REQUESTED BY", "DUE DATE", "STATUS"))
            writer.append("--------------------------------------------------------------------------------\n")
            data.approvals.orEmpty().forEach { a ->
                val title = (a.title ?: a.requestNumber.orEmpty()).take(30)
                val reqBy = (a.requestedBy ?: "—").take(18)
                val date = (a.dueDate?.take(10) ?: "—").take(12)
                val status = (a.state ?: a.stage ?: "Pending").take(10)
                writer.append(String.format(Locale.getDefault(), "%-32s %-20s %-12s %-10s\n", title, reqBy, date, status))
            }

            writer.flush()
            writer.close()

            openExportedFile(context, file, "text/plain")
        } catch (e: Exception) {
            Toast.makeText(context, "Export to Text failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================
    // 3. PDF EXPORT (.PDF - Native Android PdfDocument)
    // =========================================================================
    @SuppressLint("UseKtx")
    fun exportToPdf(context: Context, data: LegalDashboardData) {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (Points)
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = AndroidColor.parseColor("#111827")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val headerPaint = Paint().apply {
            color = AndroidColor.parseColor("#4B5563")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textPaint = Paint().apply {
            color = AndroidColor.parseColor("#1F2937")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val linePaint = Paint().apply {
            color = AndroidColor.parseColor("#E5E7EB")
            strokeWidth = 1f
        }

        var y = 40f

        // Title Header
        canvas.drawText("Legal Executive Dashboard Summary", 40f, y, titlePaint)
        y += 16f
        canvas.drawText("Generated: ${data.generatedAt ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}", 40f, y, headerPaint)
        y += 24f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        // KPI Summary Block
        canvas.drawText("KEY PERFORMANCE METRICS", 40f, y, headerPaint)
        y += 14f
        val kpis = data.kpis
        canvas.drawText("Total Records: ${kpis?.totalRecords?.value?.toInt() ?: 0} (${kpis?.totalRecords?.change ?: 0}%)", 40f, y, textPaint)
        canvas.drawText("Critical Risks: ${kpis?.criticalRisks?.value?.toInt() ?: 0} (${kpis?.criticalRisks?.change ?: 0}%)", 200f, y, textPaint)
        canvas.drawText("Expiring Contracts: ${kpis?.expiringContracts?.value?.toInt() ?: 0}", 380f, y, textPaint)
        y += 14f
        canvas.drawText("Overdue Actions: ${kpis?.overdueActions?.value?.toInt() ?: 0}", 40f, y, textPaint)
        canvas.drawText("Compliance Score: ${kpis?.complianceScore?.value?.toInt() ?: 0}%", 200f, y, textPaint)
        canvas.drawText("Pending Approvals: ${kpis?.pendingApprovals?.value?.toInt() ?: 0}", 380f, y, textPaint)
        y += 22f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        // Critical Items Section
        canvas.drawText("CRITICAL LEGAL ITEMS", 40f, y, headerPaint)
        y += 14f
        canvas.drawText("RECORD / ITEM", 40f, y, headerPaint)
        canvas.drawText("OWNER", 230f, y, headerPaint)
        canvas.drawText("DUE DATE", 350f, y, headerPaint)
        canvas.drawText("PRIORITY", 460f, y, headerPaint)
        y += 6f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 12f

        data.upcoming.orEmpty().take(8).forEach { u ->
            val title = (u.title ?: u.number.orEmpty()).take(28)
            val owner = (u.owner ?: "—").take(18)
            val date = u.date?.take(10) ?: "—"
            val priority = u.riskLevel ?: "Medium"

            canvas.drawText(title, 40f, y, textPaint)
            canvas.drawText(owner, 230f, y, textPaint)
            canvas.drawText(date, 350f, y, textPaint)
            canvas.drawText(priority, 460f, y, textPaint)
            y += 14f
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        // Approval Queue Section
        canvas.drawText("APPROVAL QUEUE", 40f, y, headerPaint)
        y += 14f
        canvas.drawText("REQUEST / ITEM", 40f, y, headerPaint)
        canvas.drawText("REQUESTED BY", 230f, y, headerPaint)
        canvas.drawText("DUE DATE", 350f, y, headerPaint)
        canvas.drawText("STATUS", 460f, y, headerPaint)
        y += 6f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 12f

        data.approvals.orEmpty().take(6).forEach { a ->
            val title = (a.title ?: a.requestNumber.orEmpty()).take(28)
            val reqBy = (a.requestedBy ?: "—").take(18)
            val date = a.dueDate?.take(10) ?: "—"
            val status = a.state ?: a.stage ?: "Pending"

            canvas.drawText(title, 40f, y, textPaint)
            canvas.drawText(reqBy, 230f, y, textPaint)
            canvas.drawText(date, 350f, y, textPaint)
            canvas.drawText(status, 460f, y, textPaint)
            y += 14f
        }

        pdfDoc.finishPage(page)

        try {
            val file = File(context.cacheDir, "legal-dashboard-$dateStamp.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            pdfDoc.close()
            outputStream.close()

            openExportedFile(context, file, "application/pdf")
        } catch (e: Exception) {
            pdfDoc.close()
            Toast.makeText(context, "Export to PDF failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================
    // Universal Open / Share Intent Trigger
    // =========================================================================
    private fun openExportedFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Legal Dashboard Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Open / Share Exported File"))
    }
}