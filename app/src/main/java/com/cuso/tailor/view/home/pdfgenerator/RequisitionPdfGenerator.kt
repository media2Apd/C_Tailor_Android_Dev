@file:Suppress("SpellCheckingInspection", "unused")

package com.cuso.tailor.view.home.pdfgenerator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.graphics.createBitmap
import com.cuso.tailor.model.inventory.PurchaseRequisition
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequisitionPdfExporter(private val context: Context) {

    private var activeWebView: WebView? = null

    data class RequisitionPdfItem(
        val name: String,
        val sku: String,
        val qty: String,
        val unitCost: String,
        val total: String,
        val status: String
    )

    data class RequisitionPdfData(
        val prNumber: String,
        val department: String,
        val generatedDate: String,
        val requestedBy: String,
        val priority: String,
        val requiredBy: String,
        val approvalStatus: String,
        val warehouse: String,
        val convertedToPo: String,
        val items: List<RequisitionPdfItem>,
        val estimatedSubtotal: String,
        val gstLabel: String,
        val estimatedGst: String,
        val estimatedTotal: String
    )

    data class SavedPdf(
        val uri: Uri?,
        val displayName: String,
        val file: File? = null,
        val sizeBytes: Long = 0L
    )

    private val pageWidthPt = 595
    private val pageHeightPt = 842

    private fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    private fun attachToWindow(webView: WebView) {
        val activity = context.findActivity() ?: return
        val decorView = activity.window?.decorView as? ViewGroup ?: return
        webView.visibility = View.INVISIBLE
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        try {
            decorView.addView(webView, 0)
        } catch (e: Exception) {
            Log.e("RequisitionPdfExporter", "Failed to attach WebView to window", e)
        }
    }

    private fun detachFromWindow(webView: WebView) {
        try {
            (webView.parent as? ViewGroup)?.removeView(webView)
        } catch (e: Exception) {
            Log.e("RequisitionPdfExporter", "Failed to detach WebView from window", e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun generatePdfFromHtml(
        data: RequisitionPdfData,
        fileName: String,
        saveToDownloads: Boolean = true,
        onComplete: (SavedPdf?) -> Unit
    ) {
        val density = context.resources.displayMetrics.density
        val renderWidthPx = (pageWidthPt * density).toInt().coerceAtLeast(800)

        val webView = WebView(context)
        activeWebView = webView

        webView.layoutParams = ViewGroup.LayoutParams(
            renderWidthPx,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        var finished = false
        val mainHandler = Handler(Looper.getMainLooper())

        fun finish(result: SavedPdf?) {
            if (finished) return
            finished = true
            detachFromWindow(webView)
            activeWebView = null
            onComplete(result)
        }

        mainHandler.postDelayed({
            if (!finished) {
                try {
                    val result = renderWebViewToPdf(webView, renderWidthPx, density, fileName, saveToDownloads)
                    finish(result)
                } catch (e: Exception) {
                    Log.e("RequisitionPdfExporter", "Render timeout fallback failed", e)
                    finish(null)
                }
            }
        }, 8000)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.postDelayed({
                    if (finished) return@postDelayed
                    try {
                        val result = renderWebViewToPdf(webView, renderWidthPx, density, fileName, saveToDownloads)
                        finish(result)
                    } catch (e: Exception) {
                        Log.e("RequisitionPdfExporter", "Render onPageFinished failed", e)
                        finish(null)
                    }
                }, 350)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                finish(null)
            }
        }

        attachToWindow(webView)
        webView.loadDataWithBaseURL(null, buildRequisitionHtml(data), "text/html", "UTF-8", null)
    }

    private fun renderWebViewToPdf(
        webView: WebView,
        renderWidthPx: Int,
        density: Float,
        fileName: String,
        saveToDownloads: Boolean
    ): SavedPdf? {
        webView.measure(
            View.MeasureSpec.makeMeasureSpec(renderWidthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val contentHeightPx = (webView.contentHeight * density).toInt().coerceAtLeast(webView.measuredHeight)

        if (contentHeightPx <= 0) return null

        webView.layout(0, 0, renderWidthPx, contentHeightPx)

        val fullBitmap = createBitmap(renderWidthPx, contentHeightPx)
        val canvas = Canvas(fullBitmap)
        canvas.drawColor(Color.WHITE)
        webView.draw(canvas)

        val pageHeightPx = (renderWidthPx.toFloat() * pageHeightPt / pageWidthPt).toInt()
        val pdfDocument = PdfDocument()

        var yOffset = 0
        var pageNumber = 1
        while (yOffset < contentHeightPx) {
            val sliceHeight = minOf(pageHeightPx, contentHeightPx - yOffset)
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)

            val srcRect = Rect(0, yOffset, renderWidthPx, yOffset + sliceHeight)
            val dstRect = Rect(
                0, 0, pageWidthPt,
                (pageHeightPt.toFloat() * sliceHeight / pageHeightPx).toInt()
            )
            page.canvas.drawBitmap(fullBitmap, srcRect, dstRect, null)

            pdfDocument.finishPage(page)
            yOffset += pageHeightPx
            pageNumber++
        }

        val outputStream = ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        fullBitmap.recycle()

        val bytes = outputStream.toByteArray()

        return if (saveToDownloads) {
            writeBytesToDownloads(bytes, fileName)
        } else {
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { it.write(bytes) }
            SavedPdf(uri = Uri.fromFile(file), displayName = fileName, file = file)
        }
    }

    private fun writeBytesToDownloads(bytes: ByteArray, fileName: String): SavedPdf? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return null

                resolver.openOutputStream(uri)?.use { out -> out.write(bytes) }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                SavedPdf(uri = uri, displayName = fileName, sizeBytes = bytes.size.toLong())
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { it.write(bytes) }
                SavedPdf(uri = Uri.fromFile(file), displayName = fileName, file = file)
            }
        } catch (e: Exception) {
            Log.e("RequisitionPdfExporter", "Failed to write PDF bytes to Downloads", e)
            null
        }
    }

    fun downloadRequisitionPdf(requisition: PurchaseRequisition, onComplete: ((SavedPdf?) -> Unit)? = null) {
        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())

        val requestedByName = when (val req = requisition.requestedBy) {
            is Map<*, *> -> req["name"]?.toString() ?: req["firstName"]?.toString() ?: "Staff"
            is String -> req
            else -> "Staff"
        }

        val requiredByDateStr = formatDate(requisition.requiredByDate)

        val convertedStatus = when {
            requisition.linkedPOIds.isNotEmpty() -> "Converted"
            !requisition.conversionStatus.isNullOrBlank() -> requisition.conversionStatus
            else -> "Not Yet"
        }

        val pdfItems = requisition.items.map { item ->
            RequisitionPdfItem(
                name = item.itemDisplayName,
                sku = item.itemSku.ifBlank { "N/A" },
                qty = "${item.qty.toInt()} ${item.unit ?: "Pieces (Pcs)"}",
                unitCost = "Rs. ${item.rate.toInt()}",
                total = "Rs. ${item.total.toInt()}",
                status = item.status ?: "Pending"
            )
        }

        val gstPercent = requisition.items.firstOrNull()?.taxPercent?.toInt() ?: 0

        val pdfData = RequisitionPdfData(
            prNumber = requisition.prNumber ?: "PR-00000",
            department = requisition.department ?: "Production",
            generatedDate = currentDate,
            requestedBy = requestedByName,
            priority = requisition.priority ?: "Normal",
            requiredBy = requiredByDateStr,
            approvalStatus = requisition.approvalStatus ?: "Draft",
            warehouse = requisition.warehouseDisplayName,
            convertedToPo = convertedStatus,
            items = pdfItems,
            estimatedSubtotal = "Rs. ${(requisition.estimatedSubtotal ?: 0.0).toInt()}",
            gstLabel = "GST ($gstPercent%)",
            estimatedGst = "Rs. ${(requisition.estimatedTax ?: 0.0).toInt()}",
            estimatedTotal = "Rs. ${(requisition.estimatedTotal ?: 0.0).toInt()}"
        )

        val cleanCode = (requisition.prNumber ?: "Requisition").replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "${cleanCode}_${System.currentTimeMillis()}.pdf"

        Toast.makeText(context, "Generating PDF...", Toast.LENGTH_SHORT).show()

        generatePdfFromHtml(
            data = pdfData,
            fileName = fileName,
            saveToDownloads = true,
            onComplete = { saved ->
                if (saved != null) {
                    Toast.makeText(context, "PDF saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                    onComplete?.invoke(saved)
                } else {
                    Toast.makeText(context, "Failed to download PDF", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke(null)
                }
            }
        )
    }

    private fun formatDate(rawDate: String?): String {
        if (rawDate.isNullOrBlank()) return "—"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(rawDate.take(10)) ?: return rawDate.take(10)
            SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
        } catch (_: Exception) {
            rawDate.take(10)
        }
    }

    private fun buildRequisitionHtml(data: RequisitionPdfData): String {
        val itemRows = if (data.items.isEmpty()) {
            """<tr><td colspan="5" style="text-align: center; color: #6B7280; padding: 14px;">No items requested</td></tr>"""
        } else {
            data.items.mapIndexed { idx, item ->
                val bg = if (idx % 2 == 1) "background: #F9FAFB;" else "background: #FFFFFF;"
                """
                <tr style="$bg">
                    <td>
                        <div style="font-weight: 600; color: #111827;">${item.name}</div>
                        <div style="font-size: 11px; color: #6B7280; margin-top: 3px;">SKU: ${item.sku}</div>
                    </td>
                    <td>${item.qty}</td>
                    <td>${item.unitCost}</td>
                    <td>${item.total}</td>
                    <td>${item.status}</td>
                </tr>
                """.trimIndent()
            }.joinToString("\n")
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>${data.prNumber}</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
                    background: #FFFFFF;
                    padding: 40px 48px;
                    color: #111827;
                }
                .header-container {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    margin-bottom: 28px;
                }
                .pr-title {
                    font-size: 28px;
                    font-weight: 800;
                    color: #000000;
                    margin-bottom: 4px;
                }
                .department {
                    font-size: 14px;
                    color: #6B7280;
                }
                .generated-date {
                    font-size: 14px;
                    color: #6B7280;
                    margin-top: 4px;
                }
                .section-title {
                    font-size: 18px;
                    font-weight: 700;
                    color: #000000;
                    margin-top: 26px;
                    margin-bottom: 14px;
                }
                .summary-grid {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    column-gap: 48px;
                    row-gap: 12px;
                }
                .summary-row {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    font-size: 13.5px;
                }
                .summary-label {
                    font-weight: 700;
                    color: #000000;
                }
                .summary-value {
                    color: #374151;
                    text-align: right;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 6px;
                    font-size: 13px;
                }
                th {
                    background-color: #2F2BD9;
                    color: #FFFFFF;
                    font-weight: 700;
                    text-align: left;
                    padding: 11px 14px;
                    border: none;
                }
                td {
                    padding: 12px 14px;
                    color: #1F2937;
                    border-bottom: 1px solid #E5E7EB;
                }
                .amount-container {
                    width: 320px;
                    margin-left: auto;
                    margin-top: 8px;
                }
                .amount-row {
                    display: flex;
                    justify-content: space-between;
                    padding: 6px 0;
                    font-size: 13.5px;
                }
                .amount-label {
                    font-weight: 700;
                    color: #000000;
                }
                .amount-value {
                    color: #1F2937;
                    font-weight: 500;
                }
            </style>
        </head>
        <body>
            <div class="header-container">
                <div>
                    <div class="pr-title">${data.prNumber}</div>
                    <div class="department">Department: ${data.department}</div>
                </div>
                <div class="generated-date">Generated on ${data.generatedDate}</div>
            </div>

            <div class="section-title">Request Summary</div>
            <div class="summary-grid">
                <div class="summary-row">
                    <span class="summary-label">Requested By</span>
                    <span class="summary-value">${data.requestedBy}</span>
                </div>
                <div class="summary-row">
                    <span class="summary-label">Priority</span>
                    <span class="summary-value">${data.priority}</span>
                </div>
                <div class="summary-row">
                    <span class="summary-label">Required By</span>
                    <span class="summary-value">${data.requiredBy}</span>
                </div>
                <div class="summary-row">
                    <span class="summary-label">Approval Status</span>
                    <span class="summary-value">${data.approvalStatus}</span>
                </div>
                <div class="summary-row">
                    <span class="summary-label">Warehouse</span>
                    <span class="summary-value">${data.warehouse}</span>
                </div>
                <div class="summary-row">
                    <span class="summary-label">Converted To PO</span>
                    <span class="summary-value">${data.convertedToPo}</span>
                </div>
            </div>

            <div class="section-title">Requested Items</div>
            <table>
                <thead>
                    <tr>
                        <th style="width: 42%;">Item / SKU</th>
                        <th style="width: 20%;">Qty</th>
                        <th style="width: 14%;">Unit Cost</th>
                        <th style="width: 12%;">Total</th>
                        <th style="width: 12%;">Status</th>
                    </tr>
                </thead>
                <tbody>
                    $itemRows
                </tbody>
            </table>

            <div class="section-title" style="text-align: right; width: 320px; margin-left: auto;">Estimated Amount</div>
            <div class="amount-container">
                <div class="amount-row">
                    <span class="amount-label">Estimated Subtotal</span>
                    <span class="amount-value">${data.estimatedSubtotal}</span>
                </div>
                <div class="amount-row">
                    <span class="amount-label">${data.gstLabel}</span>
                    <span class="amount-value">${data.estimatedGst}</span>
                </div>
                <div class="amount-row">
                    <span class="amount-label">Estimated Total</span>
                    <span class="amount-value">${data.estimatedTotal}</span>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}