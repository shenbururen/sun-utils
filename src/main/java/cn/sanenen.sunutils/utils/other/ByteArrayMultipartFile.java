package cn.sanenen.sunutils.utils.other;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;

@Builder
@Data
public class ByteArrayMultipartFile implements MultipartFile {

    private String name;

    private String originalFilename;

    private String contentType;

    private byte[] bytes;

    @Override
    public boolean isEmpty() {
        return bytes == null ||bytes.length == 0;
    }

    @Override
    public long getSize() {
        return bytes.length;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void transferTo(File destination) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(destination.toPath())) {
            outputStream.write(bytes);
        }
    }
}