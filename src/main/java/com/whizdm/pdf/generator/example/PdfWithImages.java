package com.whizdm.pdf.generator.example;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.whizdm.pdf.generator.PdfGenerator;
import com.whizdm.pdf.generator.PdfPage;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author satyampriyam
 * @since 18-October-2024
 **/
public class PdfWithImages {
        public static void main(String[] args) throws DocumentException, IOException {
                // Define the output file path
                String outputFileName = "./output/StudentReportCard.pdf";
                String inputFileName = "./input/BlankDocument.pdf";

                // Create a new PdfGenerator instance
                PdfGenerator pdfGenerator = new PdfGenerator(inputFileName, outputFileName);
                PdfContentByte over = pdfGenerator.getStamper().getOverContent(1);
                PdfPage pdfPage = new PdfPage(pdfGenerator, 0, 0);
                pdfPage.over = over;

                // Get page dimensions
                float pageWidth = pdfGenerator.getReader().getPageSize(1).getWidth();
                float pageHeight = pdfGenerator.getReader().getPageSize(1).getHeight();

                // Coordinates for the text
                float margin = 100;
                float y = pageHeight - margin;

                // Write the header
                pdfPage.writeString(margin, y, 10, "Report Card");
                y -= 20;

                // Write the student details
                pdfPage.writeString(margin, y, 10, "Name: XYZ");
                y -= 15;
                pdfPage.writeString(margin, y, 10, "ID: 123");
                y -= 15;
                pdfPage.writeString(margin, y, 10, "Class: 4");
                y -= 20;

                // Write the subjects header
                pdfPage.writeString(margin, y, 10, "Subjects");
                y -= 15;

                // Write the subjects and scores
                pdfPage.writeString(margin, y, 10, "Maths: 100");
                y -= 15;
                pdfPage.writeString(margin, y, 10, "Science: 90");

                // Add space for student image at the top right corner
                float imageX = pageWidth - margin - 100; // Adjust the width as needed
                float imageY = pageHeight - margin - 100; // Adjust the height as needed
                pdfPage.writeImage(imageX, imageY, 100, 100, Paths.get("./images/sample.png").toUri().toURL().getPath()); // Placeholder for the image

                // Write the signature at the bottom right
                float signatureX = pageWidth - margin - 150; // Adjust the width as needed
                float signatureY = margin+400;
                pdfPage.writeString(signatureX, signatureY, 10, "Verified By: ABC, Bangalore");

                // Close the stamper and reader
                pdfGenerator.getStamper().close();
                pdfGenerator.getReader().close();

                System.out.println("PDF created successfully!");
        }
}
