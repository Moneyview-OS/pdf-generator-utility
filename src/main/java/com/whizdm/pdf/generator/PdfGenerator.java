package com.whizdm.pdf.generator;

import com.google.common.base.Throwables;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.whizdm.pdf.generator.util.FileUtil;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author satyampriyam
 * @since 17-October-2024
 **/
@Getter
public class PdfGenerator {
    protected PdfReader reader;
    protected PdfStamper stamper;
    protected BaseFont font;

    public PdfGenerator(String inputFileName, String outputFileName) throws IOException, DocumentException {
        reader = new PdfReader(inputFileName);
        stamper = new PdfStamper(reader, FileUtil.getOutputStream(outputFileName));
        font = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }

    protected static final Log log = LogFactory.getLog(PdfGenerator.class);

    public static void rotatePdf(String destinationPath, String fileIdentifier, int pageNumber) throws Exception {
        //get absolute path of destination path
        File file = new File(destinationPath);
        String absolutePath = file.getAbsolutePath();
        String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));


        PdfReader inputReader = null;
        PdfReader finalReader = null;
        PdfStamper inputStamper = null;
        PdfStamper finalStamper = null;

        String temporaryFilePath = filePath + "/" + fileIdentifier + "_dummyFile.pdf";
        try {
            //rotate the desired page and write to a temp file
            inputReader = new PdfReader(destinationPath);
            inputReader.getPageN(pageNumber).put(PdfName.ROTATE, new PdfNumber(0)); // Rotating to 0 degrees
            inputStamper = new PdfStamper(inputReader, FileUtil.getOutputStream(temporaryFilePath));
            inputStamper.setFullCompression();
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new Exception(e);
        } finally {
            if (inputReader != null) {
                inputReader.close();
            }
            if (inputStamper != null) {
                inputStamper.close();
            }
        }

        try {
            //after rotating again store in destination path itself
            finalReader = new PdfReader(temporaryFilePath);
            finalStamper = new PdfStamper(finalReader, FileUtil.getOutputStream(destinationPath));
            finalStamper.setFullCompression();
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new Exception(e);
        } finally {
            if (finalReader != null) {
                finalReader.close();
            }
            if (finalStamper != null) {
                finalStamper.close();
            }
        }


        //delete dummy file
        FileUtil.deleteFile(temporaryFilePath);
    }

    public static void splitPdf(File pdfFile, File pageFile, int fromPage, int toPage)
            throws IOException, DocumentException {
        InputStream inputStream;
        OutputStream outputStream = null;
        Document document = new Document();
        try {
            inputStream = Files.newInputStream(pdfFile.toPath());
            outputStream = Files.newOutputStream(pageFile.toPath());

            PdfReader inputPDF = new PdfReader(inputStream);
            int totalPages = inputPDF.getNumberOfPages();

            // Make fromPage equals to toPage if it is greater
            if (fromPage > toPage) {
                fromPage = toPage;
            }
            if (toPage > totalPages) {
                toPage = totalPages;
            }

            // Create a writer for the output stream
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            // Holds the PDF data
            PdfContentByte cb = writer.getDirectContent();
            PdfImportedPage page;

            while (fromPage <= toPage) {
                document.newPage();
                page = writer.getImportedPage(inputPDF, fromPage);
                cb.addTemplate(page, 0, 0);
                fromPage++;
            }
            outputStream.flush();

        } catch (Exception e) {
            log.error(e);
            throw e;
        } finally {
            try {
                if (document.isOpen()) {
                    document.close();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        }
    }

    public static void mergePdfFiles(List<InputStream> inputPdfList, OutputStream outputStream) throws Exception {

        //Create document and pdfReader objects.
        Document document = new Document();

        List<PdfReader> readers = new ArrayList<>();
        int totalPages = 0;

        // Create reader list for the input pdf files.
        for (InputStream pdf : inputPdfList) {
            PdfReader pdfReader = new PdfReader(pdf);
            readers.add(pdfReader);
            totalPages = totalPages + pdfReader.getNumberOfPages();
            pdf.close();
        }

        // Create writer for the outputStream
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);

        //Open document.
        document.open();

        //Contain the pdf data.
        PdfContentByte pageContentByte = writer.getDirectContent();

        PdfImportedPage pdfImportedPage;
        int currentPdfReaderPage = 1;

        // Iterate and process the reader list.
        for (PdfReader pdfReader : readers) {
            //Create page and add content.
            while (currentPdfReaderPage <= pdfReader.getNumberOfPages()) {
                document.newPage();
                pdfImportedPage = writer.getImportedPage(
                        pdfReader, currentPdfReaderPage);
                pageContentByte.addTemplate(pdfImportedPage, 0, 0);
                currentPdfReaderPage++;
            }
            currentPdfReaderPage = 1;
        }

        //Close document and outputStream.
        outputStream.flush();
        document.close();
        log.debug("Pdf files merged successfully.");
    }

    public static void removePdfPages(File pdfFile, int fromPage, int toPage) throws IOException, DocumentException {
        InputStream inputStream;
        OutputStream outputStream = null;
        File tempFile = File.createTempFile("temp" + UUID.randomUUID(), ".pdf");
        try {
            inputStream = Files.newInputStream(pdfFile.toPath());
            outputStream = Files.newOutputStream(tempFile.toPath());
            PdfReader inputPdf = getPdfReader(fromPage, toPage, inputStream);
            PdfStamper stamper = new PdfStamper(inputPdf, outputStream);
            outputStream.flush();
            inputStream.close();
            stamper.close();

            String absPath = pdfFile.getAbsolutePath();
            FileUtil.deleteFile(absPath);

            pdfFile = new File(absPath);
            FileUtils.copyFile(tempFile, pdfFile);
        } catch (Exception e) {
            log.error(e);
            throw e;
        } finally {
            try {
                FileUtil.deleteFile(tempFile);
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        }
    }

    private static PdfReader getPdfReader(int fromPage, int toPage, InputStream inputStream)
            throws IOException, DocumentException {
        PdfReader inputPDF = new PdfReader(inputStream);
        int totalPages = inputPDF.getNumberOfPages();
        if (fromPage > totalPages || toPage > totalPages) {
            throw new DocumentException("From and To Values supplied to remove pages exceeds the number of pages.");
        }
        List<Integer> pageList = new ArrayList<>();
        for (int i = 0; i <= totalPages; i++) {
            if (i < fromPage || i > toPage) {
                pageList.add(i);
            }
        }
        inputPDF.selectPages(pageList);
        return inputPDF;
    }

    public static void insertPdfPages(File srcFile, File insertionFile, int fromPage) throws Exception {
        File tempPrefixFile = File.createTempFile("temp" + UUID.randomUUID(), ".pdf");
        File tempSuffixFile = File.createTempFile("temp" + UUID.randomUUID(), "suffix.pdf");
        File destFileTemp = File.createTempFile("temp" + UUID.randomUUID(), "dest.pdf");
        try (InputStream inputStream = Files.newInputStream(srcFile.toPath());
             OutputStream outputStream = Files.newOutputStream(destFileTemp.toPath())) {
            PdfReader inputPDF = new PdfReader(inputStream);
            int totalPages = inputPDF.getNumberOfPages();
            splitPdf(srcFile, tempPrefixFile, 1, fromPage);
            if (fromPage < totalPages) {
                splitPdf(srcFile, tempSuffixFile, fromPage + 1, totalPages);
            }
            List<InputStream> streamList = new ArrayList<>();
            streamList.add(Files.newInputStream(tempPrefixFile.toPath()));
            if (insertionFile.exists()) {
                streamList.add(Files.newInputStream(insertionFile.toPath()));
            }
            if (fromPage < totalPages) {
                streamList.add(Files.newInputStream(tempSuffixFile.toPath()));
            }
            mergePdfFiles(streamList, outputStream);
            FileUtils.copyFile(destFileTemp, srcFile);
            Thread.sleep(500);
        } catch (Exception e) {
            log.error(e);
            throw e;
        } finally {
            FileUtil.deleteFile(tempPrefixFile);
            FileUtil.deleteFile(tempSuffixFile);
            FileUtil.deleteFile(destFileTemp);
        }
    }

    public static BufferedImage getBufferedImage(URL url) throws Exception {

        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); InputStream inputStream = url.openStream()) {
            int n;
            byte[] buffer = new byte[1024];
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            byte[] bytes = output.toByteArray();
            return ImageIO.read(new ByteArrayInputStream(bytes));
        }

    }
}