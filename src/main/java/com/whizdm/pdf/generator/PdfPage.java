package com.whizdm.pdf.generator;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * @author satyampriyam
 * @since 17-October-2024
 **/
public class PdfPage {

    protected PdfGenerator pdfGenerator;
    protected PdfContentByte over;
    protected int fontSize = 14;
    protected int fontSizeForDetailsPage = 9;

    protected int x = 0;
    protected int y = 0;
    protected DecimalFormat nf = new DecimalFormat("#.00");

    public PdfReader getReader() {
        return pdfGenerator.getReader();
    }

    public PdfStamper getStamper() {
        return pdfGenerator.getStamper();
    }

    public BaseFont getFont() {
        return pdfGenerator.getFont();
    }

    public PdfPage(PdfGenerator pdfGenerator, int x, int y) {
        this.pdfGenerator = pdfGenerator;
        this.x = x;
        this.y = y;
    }

    public void write(int i) throws IOException, DocumentException, Exception {
    }

//    public void write(int i, int pageNo, int fileType) throws IOException, DocumentException {
//    }


    /**
     * write a string with given font, size and position
     *
     * @param x
     * @param y
     * @param fontSize
     * @param value
     */
    public void writeString(float x, float y, int fontSize, String value) {
        writeString(x, y, fontSize, value, false, false, false);
    }

    public void writeString(float x, float y, int fontSize, BaseColor fontColor, String value) {
        writeString(x, y, fontSize, fontColor, value, false, false, false);
    }

    public void writeString(float x, float y, int fontSize, BaseFont font, String value) {
        writeString(x, y, fontSize, font, value, false, false, false, false, 0);
    }

    public void writeString(float x, float y, int fontSize, String value, boolean multiline, boolean first, boolean last) {
        writeString(x, y, fontSize, value, multiline, first, last, false, 0);
    }

    private void writeString(float x, float y, int fontSize, BaseColor fontColor, String value, boolean multiline,
                             boolean first, boolean last) {
        writeString(x, y, fontSize, fontColor, value, multiline, first, last, false, 0);
    }

    public void writeString(float x, float y, int fontSize, String value, boolean multiline, boolean first,
                            boolean last, boolean dynamicFontChange, int minLengthForFontChange) {
        writeString(x, y, fontSize, getFont(), value, multiline, first, last, dynamicFontChange,
                minLengthForFontChange);
    }

    private void writeString(float x, float y, int fontSize, BaseColor fontColor, String value, boolean multiline,
                             boolean first, boolean last, boolean dynamicFontChange, int minLengthForFontChange) {
        writeString(x, y, fontSize, getFont(), fontColor, value, multiline, first, last, dynamicFontChange,
                minLengthForFontChange);
    }

    private void writeString(float x, float y, int fontSize, BaseFont font, String value, boolean multiline,
                             boolean first, boolean last, boolean dynamicFontChange, int minLengthForFontChange) {
        writeString(x, y, fontSize, font, null, value, multiline, first, last, dynamicFontChange,
                minLengthForFontChange);
    }

    private void writeString(float x, float y, int fontSize, BaseFont font, BaseColor fontColor, String value, boolean multiline,
                             boolean first, boolean last, boolean dynamicFontChange, int minLengthForFontChange) {
        if (value == null) {
            value = "";
        }

        if (dynamicFontChange) {
            int size = value.length();
            while (size > minLengthForFontChange) {
                if (fontSize > 5) {
                    fontSize--;
                } else {
                    break;
                }
                size -= 3;
            }
        }

        if (!multiline || first) {
            over.beginText();
        }
        over.setFontAndSize(font, fontSize);
        over.setTextMatrix(x, y);
        if (fontColor != null) {
            over.setColorFill(fontColor);
        }
        over.showText(value);
        if (!multiline || last) {
            over.endText();
        }
    }

    public void writeString(float lx, float ly, float ux, float uy, String value, int fontSize, PdfContentByte contentByte) throws Exception {
        ColumnText ct = new ColumnText(contentByte);
        ct.setSimpleColumn(lx, ly, ux, uy);
        com.itextpdf.text.Font font = FontFactory.getFont(FontFactory.HELVETICA, fontSize, Font.BOLD, new CMYKColor(255, 255, 255, 255));
        Paragraph p = new Paragraph(value, font);
        ct.addElement(p);
        ct.go();
    }

    public void writeImage(float x, float y, String url) throws IOException, DocumentException {
        int scaleX = 80;
        int scaleY = 80;
        writeImage(x, y, url, scaleX, scaleY);
    }

    public void writeImage(float x, float y, String url, int scaleX, int scaleY) throws IOException, DocumentException {
        Image image4;
        if (url != null) {
            image4 = url.startsWith("/") ? Image.getInstance(url) : Image.getInstance(new URL(url));
            image4.setAbsolutePosition(x, y);
            image4.scaleToFit(scaleX, scaleY);
            over.addImage(image4);
        }
    }

    public void writeImage(float x, float y, int height, int width, String url) throws IOException, DocumentException {
        Image image4;
        if (url != null) {
            image4 = url.startsWith("/") ? Image.getInstance(url) : Image.getInstance(new URL(url));
            image4.setAbsolutePosition(x, y);
            image4.scaleAbsolute(width, height);
            over.addImage(image4);
        }
    }

    public boolean printAlignedImages(BufferedImage img, String url1, String url2) throws Exception {
        int imgHeight = img.getHeight();
        int imgWidth = img.getWidth();
        if (imgHeight > imgWidth) {
            writeImage(50, 340, url1, 300, 300);
            //image3 in second page
            writeImage(330, 340, url2, 300, 300);
        } else {
            writeImage(50, 100, url2, 300, 300);
            //image3 in second page
            writeImage(50, 500, url1, 300, 300);
        }
        return true;
    }
}