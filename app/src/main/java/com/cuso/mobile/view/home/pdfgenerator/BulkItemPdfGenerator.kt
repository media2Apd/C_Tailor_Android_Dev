package com.cuso.mobile.view.home.pdfgenerator

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
import com.cuso.mobile.model.inventory.AssociatedItemDto
import com.cuso.mobile.model.inventory.BulkItemDoc
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BulkItemPdfGenerator(private val context: Context) {

    private var activeWebView: WebView? = null

    data class BulkItemPdfData(
        val name: String,
        val sku: String,
        val generatedDate: String,
        val assemblyType: String,
        val category: String,
        val unit: String,
        val status: String,
        val taxCategory: String,
        val taxPercent: String,
        val createdOn: String,
        val costPrice: String,
        val sellingPrice: String,
        val associatedItems: List<AssociatedItemDto>,
        val openingStock: String,
        val reorderPoint: String,
        val stockOnHand: String,
        val committedStock: String,
        val availableForSale: String,
        val actualPhysicalStock: String
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
            Log.e("BulkItemPdfGenerator", "attachToWindow failed", e)
        }
    }

    private fun detachFromWindow(webView: WebView) {
        try {
            (webView.parent as? ViewGroup)?.removeView(webView)
        } catch (e: Exception) {
            Log.e("BulkItemPdfGenerator", "detachFromWindow failed", e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun generatePdfFromHtml(
        data: BulkItemPdfData,
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
                    Log.e("BulkItemPdfGenerator", "Render timeout fallback failed", e)
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
                        Log.e("BulkItemPdfGenerator", "Render onPageFinished path failed", e)
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
        webView.loadDataWithBaseURL(null, buildBulkItemHtml(data), "text/html", "UTF-8", null)
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
            Log.e("BulkItemPdfGenerator", "writeBytesToDownloads failed for '$fileName'", e)
            null
        }
    }

    fun downloadBulkItemPdf(item: BulkItemDoc, onComplete: ((SavedPdf?) -> Unit)? = null) {
        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val snap = item.inventorySnapshot

        val pdfData = BulkItemPdfData(
            name = if (item.name.isBlank()) "Bulk Item" else item.name,
            sku = if (item.sku.isBlank()) "-" else item.sku,
            generatedDate = currentDate,
            assemblyType = if (item.assemblyType.isBlank()) "Pre-assembled" else item.assemblyType,
            category = item.brand ?: "-",
            unit = if (item.unit.isBlank()) "-" else item.unit,
            status = if (item.status.isBlank()) "active" else item.status,
            taxCategory = item.taxCategory ?: "-",
            taxPercent = "${item.taxPercent.toInt()}%",
            createdOn = currentDate,
            costPrice = "Rs. ${item.costPrice.toInt()}",
            sellingPrice = "Rs. ${item.sellingPrice.toInt()}",
            associatedItems = item.associatedItems,
            openingStock = (snap?.openingStock ?: item.openingStock).toInt().toString(),
            reorderPoint = (snap?.reorderPoint ?: item.reorderPoint).toInt().toString(),
            stockOnHand = (snap?.stockOnHand ?: item.stockOnHand).toInt().toString(),
            committedStock = (snap?.committedStock ?: 0.0).toInt().toString(),
            availableForSale = (snap?.availableForSale ?: item.stockOnHand).toInt().toString(),
            actualPhysicalStock = (snap?.actualPhysicalStock ?: item.stockOnHand).toInt().toString()
        )

        val cleanName = item.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "${cleanName}_Details_${System.currentTimeMillis()}.pdf"

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

    private fun buildBulkItemHtml(data: BulkItemPdfData): String {
        val associatedRows = if (data.associatedItems.isEmpty()) {
            "<tr><td colspan=\"5\" style=\"text-align: center; color: #6b7280; padding: 12px;\">No Associated Items</td></tr>"
        } else {
            data.associatedItems.mapIndexed { idx, item ->
                val bg = if (idx % 2 == 1) "background: #F9FAFB;" else "background: #FFFFFF;"
                """
                <tr style="$bg">
                    <td>${item.name}</td>
                    <td>${item.sku}</td>
                    <td>${item.accountingStock.toInt()}</td>
                    <td>${item.qtyRequired}</td>
                    <td>Rs. ${item.totalValue}</td>
                </tr>
                """.trimIndent()
            }.joinToString("\n")
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>${data.name}</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
                    background: #ffffff;
                    padding: 36px 44px;
                    color: #111827;
                }
                .header-row {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    margin-bottom: 24px;
                }
                .title {
                    font-size: 26px;
                    font-weight: 800;
                    color: #000000;
                    margin-bottom: 6px;
                }
                .sku {
                    font-size: 14px;
                    color: #6B7280;
                }
                .generated-date {
                    font-size: 14px;
                    color: #6B7280;
                    margin-top: 24px;
                }
                .section-title {
                    font-size: 18px;
                    font-weight: 700;
                    color: #000000;
                    margin-top: 28px;
                    margin-bottom: 14px;
                }
                .grid-container {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    column-gap: 40px;
                    row-gap: 12px;
                }
                .grid-row {
                    display: flex;
                    justify-content: space-between;
                    font-size: 14px;
                }
                .grid-label {
                    font-weight: 700;
                    color: #000000;
                }
                .grid-value {
                    color: #374151;
                    min-width: 140px;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 10px;
                    font-size: 13.5px;
                }
                th {
                    background-color: #2F2BD9;
                    color: #FFFFFF;
                    font-weight: 700;
                    text-align: left;
                    padding: 10px 14px;
                }
                td {
                    padding: 10px 14px;
                    color: #1F2937;
                }
                .border-table td, .border-table th {
                    border: 1px solid #E5E7EB;
                }
                .border-table th {
                    border: none;
                }
            </style>
        </head>
        <body>
            <div class="header-row">
                <div>
                    <div class="title">${data.name}</div>
                    <div class="sku">SKU: ${data.sku}</div>
                </div>
                <div class="generated-date">Generated on ${data.generatedDate}</div>
            </div>

            <div class="section-title">Item Details</div>
            <div class="grid-container">
                <div class="grid-row">
                    <span class="grid-label">Assembly Type</span>
                    <span class="grid-value">${data.assemblyType}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Category</span>
                    <span class="grid-value">${data.category}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Unit</span>
                    <span class="grid-value">${data.unit}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Status</span>
                    <span class="grid-value">${data.status}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Tax Category</span>
                    <span class="grid-value">${data.taxCategory}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Tax %</span>
                    <span class="grid-value">${data.taxPercent}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Created On</span>
                    <span class="grid-value">${data.createdOn}</span>
                </div>
            </div>

            <div class="section-title">Purchase & Sales Info</div>
            <table class="border-table">
                <thead>
                    <tr>
                        <th style="width: 33%;">Metric</th>
                        <th style="width: 33%;">Purchase</th>
                        <th style="width: 34%;">Sales</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Price</td>
                        <td>${data.costPrice}</td>
                        <td>${data.sellingPrice}</td>
                    </tr>
                </tbody>
            </table>

            <div class="section-title">Associated Items</div>
            <table class="border-table">
                <thead>
                    <tr>
                        <th style="width: 32%;">Item</th>
                        <th style="width: 18%;">SKU</th>
                        <th style="width: 20%;">Accounting Stock</th>
                        <th style="width: 15%;">Qty Required</th>
                        <th style="width: 15%;">Total Value</th>
                    </tr>
                </thead>
                <tbody>
                    $associatedRows
                </tbody>
            </table>

            <div class="section-title">Inventory Snapshot</div>
            <div class="grid-container">
                <div class="grid-row">
                    <span class="grid-label">Opening Stock</span>
                    <span class="grid-value">${data.openingStock}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Reorder Point</span>
                    <span class="grid-value">${data.reorderPoint}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Stock on Hand</span>
                    <span class="grid-value">${data.stockOnHand}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Committed Stock</span>
                    <span class="grid-value">${data.committedStock}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Available for Sale</span>
                    <span class="grid-value">${data.availableForSale}</span>
                </div>
                <div class="grid-row">
                    <span class="grid-label">Actual Physical Stock</span>
                    <span class="grid-value">${data.actualPhysicalStock}</span>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}