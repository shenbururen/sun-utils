package cn.sanenen.sunutils.utils.other;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

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
        return getBytes().length;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(getBytes());
    }

    @Override
    public void transferTo(File destination) throws IOException {
        Path parent = destination.toPath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (OutputStream outputStream = Files.newOutputStream(destination.toPath())) {
            outputStream.write(getBytes());
        }
    }

    @Override
    public byte[] getBytes() {
        return bytes == null ? Emptys.BYTES : bytes;
    }
}
