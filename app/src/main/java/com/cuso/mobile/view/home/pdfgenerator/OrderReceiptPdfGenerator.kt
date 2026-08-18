package com.cuso.mobile.view.home.pdfgenerator

import android.content.Context
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.layout.borders.Border
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

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
        val thankYouMessage: String = "Thank you!",
        val poweredBy: String = "Powered by Cuso Tailor"
    )

    data class OrderItem(
        val quantity: Int,
        val name: String,
        val price: Double,
        val additionalCharge: Double = 0.0
    )

    /**
     * Generates a PDF receipt with a monospaced "Thermal Receipt" look
     */
    fun generateReceiptPdf(data: OrderReceiptData, fileName: String = "order_receipt_${data.orderNumber}.pdf"): File? {
        return try {
            val file = File(context.getExternalFilesDir(null), fileName)
            val writer = PdfWriter(FileOutputStream(file))
            val pdf = PdfDocument(writer)

            // Set Page Size to a smaller width to simulate a thermal receipt
            val document = Document(pdf, PageSize.A6)
            document.setMargins(20f, 20f, 20f, 20f)

            // Load Courier (Monospaced font)
            val font: PdfFont = PdfFontFactory.createFont(StandardFonts.COURIER)
            val fontBold: PdfFont = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD)

            document.setFont(font)

            // Header Section
            document.add(Paragraph("Cuso Tailor")
                .setFont(fontBold).setFontSize(18f).setTextAlignment(TextAlignment.CENTER))
            document.add(Paragraph("Payment Receipt")
                .setFont(fontBold).setFontSize(12f).setTextAlignment(TextAlignment.CENTER))

            document.add(Paragraph("-----------------------------").setTextAlignment(TextAlignment.CENTER))

            // Date and Order Info Table
            val infoTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f))).useAllAvailableWidth()
            infoTable.setBorder(Border.NO_BORDER)

            val currentDate = SimpleDateFormat("dd/M/yyyy", Locale.getDefault()).format(Date())

            addReceiptRow(infoTable, "Date:", currentDate, font)
            addReceiptRow(infoTable, "Order:", data.orderNumber, fontBold)
            addReceiptRow(infoTable, "Cust:", data.customerName, font)
            document.add(infoTable)

            document.add(Paragraph("-----------------------------").setTextAlignment(TextAlignment.CENTER))

            // Items Section
            document.add(Paragraph("ITEMS:").setFont(fontBold))

            val itemTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f))).useAllAvailableWidth()
            itemTable.setBorder(Border.NO_BORDER)

            data.items.forEach { item ->
                // Main Item Row: Qty x Name
                addReceiptRow(itemTable, "${item.quantity} x ${item.name}", "₹${item.price.toInt()}", font)

                // Additional Charges Sub-Row
                if (item.additionalCharge > 0) {
                    addReceiptRow(itemTable, "  + Addl Charges", "₹${item.additionalCharge.toInt()}", font, 10f, ColorConstants.GRAY)
                }
            }
            document.add(itemTable)

            document.add(Paragraph("-----------------------------").setTextAlignment(TextAlignment.CENTER))

            // Charges and Payments
            val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f))).useAllAvailableWidth()
            addReceiptRow(summaryTable, "Extra Charges:", "₹${data.otherCharges.toInt()}", font)
            addReceiptRow(summaryTable, "Discount:", "- ₹${0}", font) // Placeholder for discount if needed
            addReceiptRow(summaryTable, "PAID NOW:", "₹${data.paidAmount.toInt()}", fontBold)
            document.add(summaryTable)

            document.add(Paragraph("-----------------------------").setTextAlignment(TextAlignment.CENTER))

            // Final Bill and Balance
            val finalTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f))).useAllAvailableWidth()
            addReceiptRow(finalTable, "Total Bill:", "₹${data.totalAmount.toInt()}", font)
            addReceiptRow(finalTable, "Balance:", "₹${data.balanceAmount.toInt()}", font)
            document.add(finalTable)

            document.add(Paragraph("-----------------------------").setTextAlignment(TextAlignment.CENTER))

            // Footer
            document.add(Paragraph(data.thankYouMessage)
                .setFont(fontBold).setTextAlignment(TextAlignment.CENTER).setMarginTop(10f))
            document.add(Paragraph(data.poweredBy)
                .setFontSize(8f).setTextAlignment(TextAlignment.CENTER))

            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addReceiptRow(table: Table, label: String, value: String, font: PdfFont, size: Float = 11f, color: com.itextpdf.kernel.colors.Color = ColorConstants.BLACK) {
        table.addCell(Cell().add(Paragraph(label).setFont(font).setFontSize(size).setFontColor(color)).setBorder(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph(value).setFont(font).setFontSize(size).setFontColor(color)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER))
    }

    /**
     * Builds HTML that matches the "Thermal Printer" visual style shown in the image
     */
    fun buildReceiptHtml(data: OrderReceiptData): String {
        val currentDate = SimpleDateFormat("dd/M/yyyy", Locale.getDefault()).format(Date())

        val itemsHtml = data.items.joinToString("") { item ->
            """
            <div class="row">
                <span>${item.quantity} x ${item.name}</span>
                <span>₹${item.price.toInt()}</span>
            </div>
            ${if (item.additionalCharge > 0) """
            <div class="row sub-item">
                <span>&nbsp;&nbsp;+ Addl Charges</span>
                <span>₹${item.additionalCharge.toInt()}</span>
            </div>
            """ else ""}
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { 
                    font-family: 'Courier New', Courier, monospace; 
                    width: 300px; padding: 10px; color: #000;
                }
                .center { text-align: center; }
                .bold { font-weight: bold; }
                .larger { font-size: 20px; }
                .divider { border-top: 1px dashed #000; margin: 8px 0; }
                .row { display: flex; justify-content: space-between; margin: 4px 0; font-size: 14px; }
                .sub-item { font-size: 12px; color: #666; }
                .footer-space { margin-top: 20px; }
                @media print {
                    body { width: 100%; padding: 0; }
                }
            </style>
        </head>
        <body>
            <div class="center bold larger">Cuso Tailor</div>
            <div class="center bold">Payment Receipt</div>
            
            <div class="divider"></div>
            
            <div class="row"><span>Date:</span><span>$currentDate</span></div>
            <div class="row bold"><span>Order:</span><span>${data.orderNumber}</span></div>
            <div class="row"><span>Cust:</span><span>${data.customerName}</span></div>
            
            <div class="divider"></div>
            
            <div class="bold" style="margin-bottom: 5px;">ITEMS:</div>
            $itemsHtml
            
            <div class="divider"></div>
            
            <div class="row"><span>Extra Charges:</span><span>₹${data.otherCharges.toInt()}</span></div>
            <div class="row"><span>Discount:</span><span>- ₹0</span></div>
            <div class="row bold"><span>PAID NOW:</span><span>₹${data.paidAmount.toInt()}</span></div>
            
            <div class="divider"></div>
            
            <div class="row"><span>Total Bill:</span><span>₹${data.totalAmount.toInt()}</span></div>
            <div class="row"><span>Balance:</span><span>₹${data.balanceAmount.toInt()}</span></div>
            
            <div class="divider"></div>
            
            <div class="center bold footer-space" style="font-size: 16px;">${data.thankYouMessage}</div>
            <div class="center" style="font-size: 10px;">${data.poweredBy}</div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Executes the Print command using WebView
     */
    fun printReceiptViaWebView(data: OrderReceiptData) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Receipt_${data.orderNumber}")
                printManager.print("Receipt", printAdapter, null)
            }
        }
        webView.loadDataWithBaseURL(null, buildReceiptHtml(data), "text/html", "UTF-8", null)
    }
}