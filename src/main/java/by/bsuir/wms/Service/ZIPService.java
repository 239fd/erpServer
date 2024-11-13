package by.bsuir.wms.Service;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZIPService {

    public byte[] createZip(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(file.getKey());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(file.getValue());
                zipOutputStream.closeEntry();
            }
        }

        return byteArrayOutputStream.toByteArray();
    }
}
