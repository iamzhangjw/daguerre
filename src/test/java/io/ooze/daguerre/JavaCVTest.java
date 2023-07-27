package io.ooze.daguerre;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.Objects;


/**
 * JavaCVTest
 *
 * @author zhangjw
 * @date 2022/11/08 0008 17:20
 */
public class JavaCVTest {

    public static void testLargeFile() throws IOException {
        byte[] buffer = new byte[5_242_880];
        java.io.File tmpFile = Files.createTempFile("dag-", ".tmp").toFile();
        try (InputStream is = new FileInputStream("E:\\downloads\\570786578-1-80.flv");
             OutputStream os = new FileOutputStream(tmpFile)) {
            int read = is.read(buffer);
            os.write(buffer, 0, read);
        }
        testSmallFile(tmpFile.getAbsolutePath(), "flv", "E:\\downloads\\002.png");
        tmpFile.delete();
    }

    public static void testSmallFile(String sourceFile, String format, String coverFile) throws IOException {
        FrameGrabber grabber = new FFmpegFrameGrabber(sourceFile);
        grabber.setFormat(format);
        grabber.start();
        Frame frame = grabber.grab();
        int i = 0;
        while (Objects.isNull(frame) || Objects.isNull(frame.image)) {
            frame = grabber.grabFrame();
            System.out.println(i++);
        }
        BufferedImage image = new Java2DFrameConverter().convert(frame);
        ImageIO.write(image, "png", new File(coverFile));
        System.out.println("duration:" + grabber.getLengthInTime());
        grabber.stop();
    }

    public static void main(String[] args) throws IOException {
        //testSmallFile("E:\\downloads\\001.mp4", "mp4", "E:\\downloads\\001.png");
        testLargeFile();
    }
}
