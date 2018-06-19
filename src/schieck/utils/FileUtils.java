package schieck.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

public class FileUtils {

    public static final int EQUALS_IGNORECASE = 0;
    public static final int EQUALS = 1;
    public static final int CONTAINS_IGNORECASE = 2;
    public static final int CONTAINS = 3;
    public static final int ENDSWITH_IGNORECASE = 4;
    public static final int ENDSWITH = 5;
    public static final int MATCHES = 6;//.*.exe , algumacoisa.*.exe , .*..*

    private FileUtils() {
    }

    public static List<File> find(File baseFolder, String fileName, int comparacao, boolean recursiveSearch) {
        List<File> filesFinded = new ArrayList<>();
        if (baseFolder != null && baseFolder.isDirectory()) {
            File files[] = baseFolder.listFiles();
            for (File f : files) {
                switch (comparacao) {
                    case EQUALS_IGNORECASE:
                        if (f.getName().equalsIgnoreCase(fileName)) {
                            filesFinded.add(f);
                        }
                        break;
                    case EQUALS:
                        if (f.getName().equals(fileName)) {
                            filesFinded.add(f);
                        }
                        break;
                    case CONTAINS_IGNORECASE:
                        if (f.getName().toLowerCase().contains(fileName.toLowerCase())) {
                            filesFinded.add(f);
                        }
                        break;
                    case CONTAINS:
                        if (f.getName().contains(fileName)) {
                            filesFinded.add(f);
                        }
                        break;
                    case ENDSWITH_IGNORECASE:
                        if (f.getName().toLowerCase().endsWith(fileName.toLowerCase())) {
                            filesFinded.add(f);
                        }
                        break;
                    case ENDSWITH:
                        if (f.getName().endsWith(fileName)) {
                            filesFinded.add(f);
                        }
                        break;
                    case MATCHES:
                        if (f.getName().matches(fileName)) {
                            filesFinded.add(f);
                        }
                        break;
                }

                if (f.isDirectory() && recursiveSearch) {
                    filesFinded.addAll(find(f, fileName, comparacao, recursiveSearch));
                }
            }
        }
        return filesFinded;
    }

    public static List<File> getFiles(File baseFolder, Set<String> excludes) throws IOException {
        List<File> filesFinded = new ArrayList<>();
        if (baseFolder != null && baseFolder.isDirectory()) {
            File files[] = baseFolder.listFiles();
            for (File f : files) {
                if (excludes == null || !excludes.contains(f.getName())) {
                    if (f.isDirectory()) {
                        filesFinded.addAll(getFiles(f));
                    } else {
                        filesFinded.add(f);
                    }
                }
            }
        }
        return filesFinded;
    }

    public static List<File> getFiles(File baseFolder) throws IOException {
        return getFiles(baseFolder, null);
    }

    public static List<File> getFilesOnly(File baseFolder) throws IOException {
        List<File> filesFinded = new ArrayList<>();
        if (baseFolder != null && baseFolder.isDirectory()) {
            File files[] = baseFolder.listFiles();
            for (File f : files) {
                if (!f.isDirectory()) {
                    filesFinded.add(f);
                }
            }
        }
        return filesFinded;
    }

    public static boolean deleteFiles(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isFile() && !f.delete()) {
                return false;
            }
        }
        return true;
    }

    public static boolean deleteData(File dir) {
        boolean retorno = deleteDir(dir);
        if (retorno) {
            dir.mkdir();
        }
        return retorno;
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                boolean success = deleteDir(f);
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static File copy(String orig, String dest) throws IOException {
        return copy(new File(orig), new File(dest));
    }

    public static File copy(File orig, File dest) throws IOException {
        dest.getAbsoluteFile().getParentFile().mkdirs();
        FileChannel sourceChannel = null;
        FileChannel destinationChannel = null;

        try {
            sourceChannel = new FileInputStream(orig).getChannel();
            destinationChannel = new FileOutputStream(dest).getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(),
                    destinationChannel);
        } finally {
            if (sourceChannel != null && sourceChannel.isOpen()) {
                sourceChannel.close();
            }
            if (destinationChannel != null && destinationChannel.isOpen()) {
                destinationChannel.close();
            }
        }
        return dest;
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        copyDirectory(sourceLocation, targetLocation, true);
    }

    public static void copyDirectory(File sourceLocation, File targetLocation, boolean replace) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

//            String[] children = sourceLocation.list();
//            for (int i = 0; i < children.length; i++) {
            for (File f : sourceLocation.listFiles()) {
                copyDirectory(new File(sourceLocation, f.getName()),
                        new File(targetLocation, f.getName()), replace);
            }
        } else if (!targetLocation.exists() || replace) {
            OutputStream out;
            try (InputStream in = new FileInputStream(sourceLocation)) {
                out = new FileOutputStream(targetLocation);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            out.close();
        }
    }

    public static String getHumanReadableFormat(File f) {
        return getHumanReadableFormat(f.length());
    }

    public static String getHumanReadableFormat(long size) {
        double tam = size;
        DecimalFormat df = new DecimalFormat("###,##0.00");
        String s = "b";

        if (tam / 1024 > 1) {
            tam = tam / 1024;
            s = "kb";
        }
        if (tam / 1024 > 1) {
            tam = tam / 1024;
            s = "Mb";
        }
        if (tam / 1024 > 1) {
            tam = tam / 1024;
            s = "Gb";
        }

        return df.format(tam) + " " + s;
    }

    public static String getHumanReadableFormat(long size, String mask) {
        double tam = size;
        String s = "b";

        if (tam / 1024 > 1) {
            tam = tam / 1024;
            s = "kb";
        }
        if (tam / 1024 > 1) {
            tam = tam / 1024;
            s = "Mb";
        }
        if (tam / 1024 > 1) {
            tam = tam / 1024;
            s = "Gb";
        }

        return new DecimalFormat(mask + s).format(tam);
    }

    public static String getHumanReadableFormat(long size, int casas) {
        BigDecimal tam = new BigDecimal(size);
        String s = "b";
        BigDecimal ref = new BigDecimal(1024);

        if (tam.divide(ref).compareTo(BigDecimal.ONE) > 0) {
            tam = tam.divide(ref, casas, RoundingMode.HALF_UP);
            s = "kb";
        }
        if (tam.divide(ref).compareTo(BigDecimal.ONE) > 0) {
            tam = tam.divide(ref, casas, RoundingMode.HALF_UP);
            s = "Mb";
        }
        if (tam.divide(ref).compareTo(BigDecimal.ONE) > 0) {
            tam = tam.divide(ref, casas, RoundingMode.HALF_UP);
            s = "Gb";
        }

        return tam.toString() + " " + s;
    }

    public static byte[] toByteArray(File inFile) throws IOException {
        InputStream is = null;
        byte[] buffer = null;
        is = new FileInputStream(inFile);
        buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        return buffer;
    }

    public static String geraMD5(File arquivo) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest;
        StringBuilder md5 = new StringBuilder();
        digest = MessageDigest.getInstance("MD5");
        try (InputStream is = new FileInputStream(arquivo)) {
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            for (int i = 0; i < md5sum.length; i++) {
                int parteAlta = ((md5sum[i] >> 4) & 0xf) << 4;
                int parteBaixa = md5sum[i] & 0xf;
                if (parteAlta == 0) {
                    md5.append('0');
                }
                md5.append(Integer.toHexString(parteAlta | parteBaixa));
            }
            return md5.toString();
        }
    }

    public static File relativeToAbsolutePath(File baseFolder, String relativePath) {
        if (relativePath.contains("..") || !new File(relativePath).exists()) {
            relativePath = relativePath.replace("\\", "/");
            File f = baseFolder.getAbsoluteFile();
            for (String a : relativePath.split("/")) {
                if (a.equals("..")) {
                    f = f.getParentFile();
                } else {
                    break;
                }
            }

            return new File(f, relativePath.replace("../", ""));
        } else {
            return new File(relativePath);
        }
    }

    public static String relativeToAbsolutePath(String baseFolder, String relativePath) {
        return relativeToAbsolutePath(new File(baseFolder), relativePath).getAbsolutePath();
    }

    public static File rename(File f, String to) {
        File dest = new File(f.getAbsoluteFile().getParentFile(), to);
        if (!f.renameTo(dest)) {
            return null;
        }
        return dest;
    }

    public static File rename(String f, String to) {
        return rename(new File(f), to);
    }

    public static void move(File f, File to) {
        if (!f.renameTo(to)) {
            throw new RuntimeException("Não foi possível mover " + f);
        }
    }

    public static void move(String f, String to) {
        move(new File(f), new File(to));
    }

    public static String normalizar(String nome) {
        return nome.replace(" ", "_").toUpperCase();
    }

    public static void byteArrayToFile(byte[] b, File f) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(b);
        }
    }

    public static void base64ToFile(String b64, File f) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(b64);
        byteArrayToFile(bytes, f);
    }

    public static String toBase64(File f) throws IOException {
        return Base64.getEncoder().encodeToString(toByteArray(f));
    }
    
    public static String toBase64(byte[] b) throws IOException {
        return Base64.getEncoder().encodeToString(b);
    }

    public static void binaryToFileSave(byte[] bin, String name, String dirDestino) throws IOException {
        File f = new File(dirDestino, name);
        byteArrayToFile(bin, f);
    }

    public static String toText(File f) throws IOException {
        return toText(f, null);
    }

    public static String toText(File f, String charset) throws IOException {
        BufferedReader reader = null;
        FileInputStream fis = new FileInputStream(f);
        reader = new BufferedReader(new InputStreamReader(fis,
                charset != null ? Charset.forName(charset) : Charset.forName("UTF-8")));

        StringBuilder sb = new StringBuilder();
        String str = "";
        while ((str = reader.readLine()) != null) {
            sb.append(str + "\n");
        }
        str = null;
        fis.close();
        reader.close();
        fis = null;
        reader = null;
        return sb.toString();
    }

    public static void saveToFile(String text, File file) throws IOException {
        saveToFile(text, file, null);
    }

    public static void saveToFile(String text, File file, String charset) throws IOException {
        try (Writer writer = charset != null ? new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), charset))
                : new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file)))) {
            writer.write(text);
            writer.flush();
        }
    }

    public static boolean dif(File f1, File f2) throws IOException {
        BufferedReader reader = null;
        FileInputStream fis = null;
        BufferedReader reader2 = null;
        FileInputStream fis2 = null;

        fis = new FileInputStream(f1);
        reader = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
        fis2 = new FileInputStream(f2);
        reader2 = new BufferedReader(new InputStreamReader(fis2, Charset.forName("UTF-8")));

        String l = "";
        String l2 = "";
        try {
            while (true) {
                l = reader.readLine();
                l2 = reader2.readLine();

                if (l == null && l2 == null) {
                    return false;
                } else if (l == null && l2 != null) {
                    return true;
                } else if (l != null && l2 == null) {
                    return true;
                } else if (!l.equals(l2)) {
                    return true;
                }
            }
        } finally {
            try {
                fis.close();
                reader.close();
                fis2.close();
                reader2.close();
            } catch (Exception e) {

            }
        }
    }

}
