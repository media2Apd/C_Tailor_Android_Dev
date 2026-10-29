package com.cuso.tailor.view.home.pdfgenerator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.cuso.tailor.model.finance.CashDrawerShiftItemDto
import com.cuso.tailor.utils.currencySymbol
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CashDrawerPdfGenerator(private val context: Context) {

    fun printShiftReport(item: CashDrawerShiftItemDto) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val printAdapter = object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("Shift_${item.shiftNumber}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                pdfDocument = createPdf(item)
                try {
                    val out = FileOutputStream(destination?.fileDescriptor)
                    pdfDocument?.writeTo(out)
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    pdfDocument?.close()
                }
            }
        }
        printManager.print("Cash Drawer Shift Report", printAdapter, null)
    }

    private fun createPdf(item: CashDrawerShiftItemDto): PdfDocument {
        val document = PdfDocument()
        val pageWidth = 400
        val pageHeight = 650
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Palette matching POS thermal print
        val inkDark = Color.rgb(17, 24, 39)       // #111827 - primary bold black ink
        val inkRegular = Color.rgb(30, 41, 59)    // #1E293B - regular text
        val inkMuted = Color.rgb(71, 85, 105)     // #475569 - secondary descriptors
        val ruleColor = Color.rgb(30, 41, 59)     // #1E293B - dashed dividers

        val margin = 26f
        val rightEdge = pageWidth - margin
        var y = 38f

        val textPaint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.MONOSPACE
        }

        val dashedLinePaint = Paint().apply {
            color = ruleColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
            isAntiAlias = true
        }

        val solidLinePaint = Paint().apply {
            color = inkDark
            style = Paint.Style.STROKE
            strokeWidth = 1.3f
            isAntiAlias = true
        }

        fun drawDashedSeparator() {
            y += 4f
            val path = Path()
            path.moveTo(margin, y)
            path.lineTo(rightEdge, y)
            canvas.drawPath(path, dashedLinePaint)
            y += 14f
        }

        // =============================================================
        // Header: Title, Shift ID, Branch
        // =============================================================
        textPaint.color = inkDark
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textPaint.textSize = 14.5f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("CASH DRAWER SHIFT REPORT", pageWidth / 2f, y, textPaint)
        y += 20f

        // Shift #: SFT-XXXX-XXXX
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.MONOSPACE
        textPaint.color = inkMuted
        val shiftLabel = "Shift #: "
        val shiftVal = item.shiftNumber
        val shiftTotalWidth = textPaint.measureText(shiftLabel + shiftVal)
        val shiftStartX = (pageWidth - shiftTotalWidth) / 2f

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = inkMuted
        canvas.drawText(shiftLabel, shiftStartX, y, textPaint)

        textPaint.color = inkDark
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText(shiftVal, shiftStartX + textPaint.measureText(shiftLabel), y, textPaint)
        y += 16f

        // Branch Name
        textPaint.textSize = 10.5f
        textPaint.typeface = Typeface.MONOSPACE
        textPaint.color = inkMuted
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Branch: ${item.branchId?.name ?: "Nexus CC - Main Branch"}", pageWidth / 2f, y, textPaint)
        y += 12f

        drawDashedSeparator()

        // =============================================================
        // Session Meta: Ledger Account, Cashier, Dates
        // =============================================================
        fun drawRow(
            label: String,
            value: String,
            isLabelBold: Boolean = false,
            isValueBold: Boolean = false,
            labelColor: Int = inkRegular,
            valueColor: Int = inkDark,
            textSize: Float = 11f
        ) {
            textPaint.textSize = textSize

            // Draw Label (Left aligned)
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.typeface = if (isLabelBold) Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) else Typeface.MONOSPACE
            textPaint.color = labelColor
            canvas.drawText(label, margin, y, textPaint)

            // Draw Value (Right aligned)
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.typeface = if (isValueBold) Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) else Typeface.MONOSPACE
            textPaint.color = valueColor
            canvas.drawText(value, rightEdge, y, textPaint)

            y += 17f
        }

        drawRow("Ledger Account:", item.cashAccountId?.accountName ?: "Cash on Hand", isValueBold = true)
        drawRow("Cashier:", item.cashierId?.fullName ?: "Vicky Sakthi", isValueBold = true)
        drawRow("Session Start:", formatDateTime(item.openingTime))
        drawRow("Session End:", formatDateTime(item.closingTime))
        drawRow("Closed By:", item.closedBy?.fullName ?: (item.cashierId?.fullName ?: "Vicky Sakthi"))

        drawDashedSeparator()

        // =============================================================
        // Float, Collections & Withdrawals
        // =============================================================
        drawRow("(+) Opening Float:", "${currencySymbol()}${formatCurrency(item.openingBalance)}", isValueBold = true)
        drawRow("(+) Cash Collections:", "+${currencySymbol()}${formatCurrency(item.totalCashCollected)}", isValueBold = true)
        drawRow("(-) Cash Withdrawals:", "-${currencySymbol()}${formatCurrency(item.totalCashWithdrawn)}", isValueBold = true)

        drawDashedSeparator()

        // =============================================================
        // Totals & Discrepancy
        // =============================================================
        drawRow("Expected in Drawer:", "${currencySymbol()}${formatCurrency(item.expectedClosingBalance)}", isLabelBold = true, isValueBold = true)
        drawRow("Actual Physical Count:", "${currencySymbol()}${formatCurrency(item.actualClosingBalance)}", isLabelBold = false, isValueBold = true)

        val diff = item.discrepancyAmount
        val diffFormatted = when {
            diff >= 0 -> "+${currencySymbol()}${formatCurrency(diff)}"
            else -> "-${currencySymbol()}${formatCurrency(kotlin.math.abs(diff))}"
        }
        val diffType = when {
            diff > 0 -> "(Overage)"
            diff < 0 -> "(Shortage)"
            else -> "(Balanced)"
        }

        drawRow("Discrepancy /", diffFormatted, isLabelBold = true, isValueBold = true)
        drawRow("Variance:", diffType, isLabelBold = true, isValueBold = true)

        drawDashedSeparator()

        // =============================================================
        // Audit Notes Section
        // =============================================================
        textPaint.textSize = 10.5f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = inkDark
        canvas.drawText("Audit Notes:", margin, y, textPaint)
        y += 12f

        // Draw dotted/dashed rectangular box
        val boxHeight = 44f
        val boxPath = Path().apply {
            moveTo(margin, y)
            lineTo(rightEdge, y)
            lineTo(rightEdge, y + boxHeight)
            lineTo(margin, y + boxHeight)
            close()
        }
        canvas.drawPath(boxPath, dashedLinePaint)

        // Notes content inside box
        textPaint.typeface = Typeface.MONOSPACE
        textPaint.textSize = 10f
        textPaint.color = inkRegular

        var noteY = y + 14f
        val noteContent = item.notes?.ifBlank { "galla\n[Close Note: closed]" } ?: "galla\n[Close Note: closed]"
        noteContent.split("\n").take(2).forEach { line ->
            canvas.drawText(line, margin + 8f, noteY, textPaint)
            noteY += 14f
        }
        y += boxHeight + 42f

        // =============================================================
        // Signatures (Cashier & Supervisor/Manager)
        // =============================================================
        val sigLineLength = 115f
        val cashierLineEnd = margin + sigLineLength

        val supervisorLineStart = rightEdge - sigLineLength

        canvas.drawLine(margin, y, cashierLineEnd, y, solidLinePaint)
        canvas.drawLine(supervisorLineStart, y, rightEdge, y, solidLinePaint)
        y += 13f

        textPaint.textSize = 9.5f
        textPaint.color = inkRegular
        textPaint.typeface = Typeface.MONOSPACE

        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Cashier", (margin + cashierLineEnd) / 2f, y, textPaint)
        canvas.drawText("Supervisor /", (supervisorLineStart + rightEdge) / 2f, y, textPaint)
        canvas.drawText("Manager", (supervisorLineStart + rightEdge) / 2f, y + 11f, textPaint)

        // =============================================================
        // Footer: Printed Date & Time
        // =============================================================
        y += 30f
        val printDateStr = SimpleDateFormat("dd/MM/yyyy, hh:mm:ss a", Locale.US).format(Date())
        textPaint.textSize = 8.5f
        textPaint.color = inkMuted
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Printed: $printDateStr", pageWidth / 2f, y, textPaint)

        document.finishPage(page)
        return document
    }

    private fun formatCurrency(amount: Double): String =
        String.format(Locale.US, "%,.0f", amount)

    private fun formatDateTime(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val formatter = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.US)
            parser.parse(iso)?.let { formatter.format(it) } ?: iso.take(16)
        } catch (_: Exception) {
            iso.take(16)
        }
    }
}