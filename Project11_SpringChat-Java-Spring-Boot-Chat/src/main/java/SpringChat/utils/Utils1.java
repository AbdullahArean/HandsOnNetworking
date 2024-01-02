package SpringChat.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import SpringChat.SpringChat;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class Utils1 {

    @Autowired
    private ServletContext servletContext;

    private HashMap<String, Long> lastModified = new HashMap<>();
    private HashMap<String, byte[]> cachedBytes = new HashMap<>();

    /**
     * 
     * This method retrieves the media type of a file based on its file name
     * 
     * @param fileName the name of the file
     * @return the media type of the file, or the default "application/octet-stream"
     *         if the media type cannot be determined
     */
    public MediaType getMediaType(String fileName) {
        try {
            String mimeType = servletContext.getMimeType(fileName);
            MediaType mediaType = MediaType.parseMediaType(mimeType);
            return mediaType;
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * 
     * This method generates a SHA-256 hash of a given string
     * 
     * @param s the input string to hash
     * @return the SHA-256 hash of the input string, represented as a hexadecimal
     *         string
     */
    public String hash(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] b = digest.digest(s.getBytes("UTF-8"));
            return toHex(b, "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
    public String toHex(byte b[], String delimeter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String h = String.format("%h", b[i] & 0xff);
            if (h.length() == 1) {
                h = "0" + h;
            }
            sb.append((i == 0) ? h : (delimeter + h));
        }
        return sb.toString();
    }

    /**
     * 
     * This method reads the contents of a file into a string
     * 
     * @param file the file to read
     * @return the contents of the file as a string, encoded in UTF-8
     * @throws IOException if there is an error reading from the file
     */
    public String readString(File file) throws IOException {
        return new String(readBytes(file, false), "UTF-8");
    }

    /**
     * 
     * This method reads the contents of a file into a byte array
     * 
     * @param file        the file to read
     * @param skipCaching whether or not to skip caching the contents of the file in
     *                    memory
     * @return the contents of the file as a byte array
     * @throws IOException if there is an error reading from the file
     */
    public synchronized byte[] readBytes(File file, boolean skipCaching) throws IOException {
        String path = file.getAbsolutePath();
        long lm = file.lastModified();
        if (cachedBytes.containsKey(path)) {
            if (lm == lastModified.get(path)) {
                return cachedBytes.get(path);
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(new FileInputStream(file), os, true, true);
        byte b[] = os.toByteArray();
        if (!skipCaching) {
            lastModified.put(path, lm);
            cachedBytes.put(path, b);
        }
        return b;
    }

    /**
     * 
     * This method copies the contents of an input stream to an output stream
     * 
     * @param is          the input stream to read from
     * @param os          the output stream to write to
     * @param closeInput  whether or not to close the input stream after copying is
     *                    complete
     * @param closeOutput whether or not to close the output stream after copying is
     *                    complete
     * @throws IOException if there is an error reading from or writing to the
     *                     streams
     */
    public void copy(InputStream is, OutputStream os, boolean closeInput, boolean closeOutput) throws IOException {
        byte b[] = new byte[10000];
        while (true) {
            int r = is.read(b);
            if (r < 0) {
                break;
            }
            os.write(b, 0, r);
        }
        if (closeInput) {
            is.close();
        }
        if (closeOutput) {
            os.flush();
            os.close();
        }
    }

    /**
     * 
     * This method serializes an exception to a string
     * 
     * @param ex the exception to serialize
     * @return a string representation of the exception stack trace, encoded in
     *         UTF-8
     */
    public String serializeException(Exception ex) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintWriter writer = null;
            writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            ex.printStackTrace(writer);
            writer.close();
            return new String(os.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "Unable to serialize";
        }
    }

    /**
     * 
     * Reads and processes a page from a file, replacing placeholders with values
     * from a map of parameters.
     * 
     * @param fileName the name of the file to read the page from
     * @param params   a map of parameter names and values to replace placeholders
     *                 in the page with
     * @return a string containing the processed page with placeholders replaced
     * @throws IOException if an error occurs while reading or processing the page
     */
    public String readPage(String fileName, Map<String, String> params) throws IOException {
        // Read the contents of the file into a string
        String page = readString(new File(SpringChat.PAGE_RESOURCE_PATH + fileName));
        int si = 0, ei = -2;
        StringBuilder sb = new StringBuilder();
        // Iterate over the placeholders in the page and replace them with their
        // corresponding values
        while (true) {
            int oei = ei;
            si = page.indexOf("<%", ei + 2);
            if (si < 0) {
                break;
            }
            ei = page.indexOf("%>", si);
            String cmd = page.substring(si + 2, ei).trim();
            String rep;
            switch (cmd.charAt(0)) {
                // If the placeholder is a parameter, replace it with the corresponding value
                // from the map
                case '$':
                    rep = params.get(cmd.substring(1));
                    break;
                // If the placeholder is a file reference, recursively process the referenced
                // file and replace it with the result
                case '#':
                    rep = readPage("/" + cmd.substring(1), params);
                    break;
                // If the placeholder is invalid, throw an exception
                default:
                    throw new IOException("Invalid identifier");
            }
            // Append the text before the placeholder and the replacement value to the
            // output string
            sb.append(page, oei + 2, si);
            sb.append(rep);
        }
        // Append the text after the last placeholder to the output string
        sb.append(page, ei + 2, page.length());
        return sb.toString();
    }

    /**
     * 
     * Formats a file size in bytes to a human-readable string.
     * 
     * @param len the length of the file in bytes
     * @return a string containing the file size in human-readable format
     */

    public String humanReadableSize(long len) {
        double size = 0;
        String fix = null;
        if (len < 1024) {
            size = len;
            fix = "B";
        } else if (len < 1024L * 1024L) {
            size = len / 1024.0;
            fix = "KiB";
        } else if (len < 1024L * 1024L * 1024L) {
            size = len / 1024.0 / 1024.0;
            fix = "MiB";
        } else if (len < 1024L * 1024L * 1024L * 1024L) {
            size = len / 1024.0 / 1024.0 / 1024.0;
            fix = "GiB";
        } else if (len < 1024L * 1024L * 1024L * 1024L * 1024L) {
            size = len / 1024.0 / 1024.0 / 1024.0 / 1024.0;
            fix = "TiB";
        } else if (len < 1024L * 1024L * 1024L * 1024L * 1024L * 1024L) {
            size = len / 1024.0 / 1024.0 / 1024.0 / 1024.0 / 1024.0;
            fix = "PiB";
        }
        if (fix == null) {
            return "Too Big";
        } else {
            String sizeStr = String.format("%.3f", size);
            while (sizeStr.endsWith("0")) {
                sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
            }
            if (sizeStr.endsWith(".")) {
                sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
            }
            return sizeStr + " " + fix;
        }
    }

    public String formatTime(long t) {
        Date d = new Date(t);
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        return formatter.format(d);
    }

    /**
     * 
     * This method generates a thumbnail image from the input image stream and
     * writes it to the output stream
     * 
     * @param format            the format of the thumbnail image (e.g. "jpeg",
     *                          "png")
     * @param width             the maximum width of the thumbnail image
     * @param input             the input stream of the original image
     * @param output            the output stream to write the thumbnail image to
     * @param closeInputStream  whether or not to close the input stream after the
     *                          thumbnail is generated
     * @param closeOutputStream whether or not to close the output stream after the
     *                          thumbnail is written
     * 
     * @throws IOException if there is an error reading from or writing to the
     *                     streams
     */
    public void makeThumbnailImage(String format, int width, InputStream input, OutputStream output,
            boolean closeInputStream, boolean closeOutputStream) throws IOException {
        // Read the original image from the input stream
        BufferedImage orgImg = ImageIO.read(input);

        // If the image cannot be read, return without generating a thumbnail
        if (orgImg == null) {
            return;
        }

        // Calculate the height of the thumbnail based on the aspect ratio of the
        // original image
        int w = Math.min(width, orgImg.getWidth());
        int h = (int) ((double) orgImg.getHeight() / orgImg.getWidth() * w);

        // Scale the original image to create the thumbnail image
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.createGraphics().drawImage(orgImg.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);

        // Write the thumbnail image to the output stream in the specified format
        ImageIO.write(img, format, output);

        // If specified, close the output stream after writing the thumbnail image
        if (closeOutputStream) {
            output.flush();
            output.close();
        }
    }
}
