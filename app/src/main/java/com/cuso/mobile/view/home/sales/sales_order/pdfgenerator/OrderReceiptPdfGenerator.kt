package com.cuso.mobile.view.home.sales.sales_order.pdfgenerator

import android.content.Context
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.util.*
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine

class OrderReceiptPdfGenerator(private val context: Context) {

    data class OrderReceiptData(
        val orderNumber: String,
        val customerName: String,
        val items: List<OrderItem>,
        val otherCharges: Double,
        val totalAmount: Double,
        val paidAmount: Double,
        val balanceAmount: Double,
        val deliveryDate: String?,
        val thankYouMessage: String = "Thank you for choosing us!",
        val poweredBy: String = "Powered by Cuso Tailor"
    )

    data class OrderItem(
        val quantity: Int,
        val name: String,
        val price: Double,
        val additionalCharge: Double = 0.0
    )

    fun generateReceiptPdf(data: OrderReceiptData, fileName: String = "order_receipt_${data.orderNumber}.pdf"): File? {
        return try {
            val file = File(context.getExternalFilesDir(null), fileName)
            val fileOutputStream = FileOutputStream(file)

            val writer = PdfWriter(fileOutputStream)
            val pdf = PdfDocument(writer)
            val document = Document(pdf, PageSize.A4)
            document.setMargins(50f, 50f, 50f, 50f)

            // Title
            document.add(Paragraph("ORDER SUMMARY")
                .setFontSize(24f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLACK))

            document.add(Paragraph(" ").setFontSize(6f))

            // Separator
            document.add(
                LineSeparator(SolidLine())
            )
            // Order Details
            document.add(Paragraph("Order: ${data.orderNumber}")
                .setFontSize(14f)
                .setFontColor(ColorConstants.BLACK))
            document.add(Paragraph("Cust: ${data.customerName}")
                .setFontSize(14f)
                .setFontColor(ColorConstants.BLACK))

            document.add(Paragraph(" ").setFontSize(6f))

            // Items Table
            val itemTable = Table(UnitValue.createPercentArray(floatArrayOf(15f, 40f, 20f, 25f)))
            itemTable.setWidth(UnitValue.createPercentValue(100f))

            // Table Headers
            val headerCell = Cell().add(Paragraph("Qty").setBold()).setTextAlignment(TextAlignment.CENTER)
            itemTable.addCell(headerCell)

            val headerCell2 = Cell().add(Paragraph("Item").setBold()).setTextAlignment(TextAlignment.LEFT)
            itemTable.addCell(headerCell2)

            val headerCell3 = Cell().add(Paragraph("Price").setBold()).setTextAlignment(TextAlignment.RIGHT)
            itemTable.addCell(headerCell3)

            val headerCell4 = Cell().add(Paragraph("Total").setBold()).setTextAlignment(TextAlignment.RIGHT)
            itemTable.addCell(headerCell4)

            // Table Rows
            data.items.forEach { item ->
                val itemTotal = (item.quantity * item.price) + item.additionalCharge

                val qtyCell = Cell().add(Paragraph(item.quantity.toString())).setTextAlignment(TextAlignment.CENTER)
                itemTable.addCell(qtyCell)

                val nameCell = Cell().add(Paragraph(item.name)).setTextAlignment(TextAlignment.LEFT)
                itemTable.addCell(nameCell)

                val priceCell = Cell().add(Paragraph(String.format(Locale.US, "₹%.2f", item.price)))
                    .setTextAlignment(TextAlignment.RIGHT)
                itemTable.addCell(priceCell)

                val totalCell = Cell().add(Paragraph(String.format(Locale.US, "₹%.2f", itemTotal)))
                    .setTextAlignment(TextAlignment.RIGHT)
                itemTable.addCell(totalCell)

                // Add additional charge as sub-item if exists
                if (item.additionalCharge > 0) {
                    val emptyCell = Cell().add(Paragraph("")).setTextAlignment(TextAlignment.CENTER)
                    itemTable.addCell(emptyCell)

                    val addLabel = Cell().add(Paragraph("  (Add1: )").setFontSize(10f))
                        .setTextAlignment(TextAlignment.LEFT)
                    itemTable.addCell(addLabel)

                    val addPrice = Cell().add(Paragraph(String.format(Locale.US, "₹%.2f", item.additionalCharge))
                        .setFontSize(10f))
                        .setTextAlignment(TextAlignment.RIGHT)
                    itemTable.addCell(addPrice)

                    val addTotal = Cell().add(Paragraph("")).setTextAlignment(TextAlignment.RIGHT)
                    itemTable.addCell(addTotal)
                }
            }

            document.add(itemTable)

            document.add(Paragraph(" ").setFontSize(6f))

            // Separator
            document.add(
                LineSeparator(SolidLine())
            )
            // Charges Summary
            if (data.otherCharges > 0) {
                document.add(Paragraph(String.format(Locale.US, "Other Charges: ₹%.2f", data.otherCharges))
                    .setFontSize(14f)
                    .setFontColor(ColorConstants.BLACK))
            }

            document.add(Paragraph(String.format(Locale.US, "Total: ₹%.2f", data.totalAmount))
                .setFontSize(16f)
                .setBold()
                .setFontColor(ColorConstants.BLACK))

            document.add(Paragraph(String.format(Locale.US, "Paid: ₹%.2f", data.paidAmount))
                .setFontSize(14f)
                .setFontColor(ColorConstants.BLACK))

            document.add(Paragraph(" ").setFontSize(6f))

            // Separator
            document.add(
                LineSeparator(SolidLine())
            )
            // Balance
            document.add(Paragraph(String.format(Locale.US, "BALANCE: ₹%.2f", data.balanceAmount))
                .setFontSize(18f)
                .setBold()
                .setFontColor(ColorConstants.RED))

            document.add(Paragraph(" ").setFontSize(6f))

            // Footer
            document.add(Paragraph(data.thankYouMessage)
                .setFontSize(12f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLACK))

            data.deliveryDate?.let {
                document.add(Paragraph("Delivery: $it")
                    .setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLACK))
            }

            document.add(Paragraph(data.poweredBy)
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY))

            document.close()
            file

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ── HTML Receipt for WebView Printing ──
    fun buildReceiptHtml(data: OrderReceiptData): String {
        val itemsHtml = data.items.joinToString("") { item ->
            val total = (item.quantity * item.price) + item.additionalCharge
            """
            <tr>
                <td style="text-align: center;">${item.quantity}</td>
                <td>${item.name}</td>
                <td style="text-align: right;">₹${String.format(Locale.US, "%.2f", item.price)}</td>
                <td style="text-align: right;">₹${String.format(Locale.US, "%.2f", total)}</td>
            </tr>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: 'Courier New', monospace; padding: 20px; }
                .header { text-align: center; font-size: 24px; font-weight: bold; }
                .divider { border-top: 1px solid #000; margin: 10px 0; }
                .order-details { font-size: 14px; }
                .order-details div { margin: 4px 0; }
                table { width: 100%; border-collapse: collapse; margin: 10px 0; }
                th { text-align: left; border-bottom: 1px solid #000; padding: 8px; }
                td { padding: 8px; }
                .total { font-size: 16px; font-weight: bold; }
                .balance { font-size: 18px; font-weight: bold; color: #EF4444; }
                .footer { text-align: center; font-size: 12px; margin-top: 20px; }
                .powered { text-align: center; font-size: 10px; color: #9CA3AF; }
                @media print {
                    body { padding: 10px; }
                }
            </style>
        </head>
        <body>
            <div class="header">ORDER SUMMARY</div>
            <div class="divider"></div>
            
            <div class="order-details">
                <div>Order: ${data.orderNumber}</div>
                <div>Cust: ${data.customerName}</div>
            </div>
            
            <div class="divider"></div>
            
            <table>
                <thead>
                    <tr>
                        <th style="text-align: center;">Qty</th>
                        <th>Item</th>
                        <th style="text-align: right;">Price</th>
                        <th style="text-align: right;">Total</th>
                    </tr>
                </thead>
                <tbody>
                    $itemsHtml
                </tbody>
            </table>
            
            <div class="divider"></div>
            
            ${if (data.otherCharges > 0) """
                <div>Other Charges: ₹${String.format(Locale.US, "%.2f", data.otherCharges)}</div>
            """ else ""}
            
            <div class="total">Total: ₹${String.format(Locale.US, "%.2f", data.totalAmount)}</div>
            <div>Paid: ₹${String.format(Locale.US, "%.2f", data.paidAmount)}</div>
            
            <div class="divider"></div>
            
            <div class="balance">BALANCE: ₹${String.format(Locale.US, "%.2f", data.balanceAmount)}</div>
            
            <div class="footer">${data.thankYouMessage}</div>
            ${data.deliveryDate?.let { "<div class='footer'>Delivery: $it</div>" } ?: ""}
            <div class="powered">${data.poweredBy}</div>
        </body>
        </html>
        """.trimIndent()
    }

    // ── Print via WebView (Recommended - No PDF library needed) ──
    fun printReceiptViaWebView(data: OrderReceiptData) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("OrderReceipt_${data.orderNumber}")
                printManager.print("Order Receipt", printAdapter, null)
            }
        }
        webView.loadDataWithBaseURL(
            null,
            buildReceiptHtml(data),
            "text/html",
            "UTF-8",
            null
        )
    }
}