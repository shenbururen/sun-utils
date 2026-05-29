package cn.sanenen.sunutils.utils.other;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class ByteArrayMultipartFileTest {

	@Test
	public void nullBytesBehaveAsEmptyFile() throws Exception {
		ByteArrayMultipartFile file = ByteArrayMultipartFile.builder().name("file").build();

		assertTrue(file.isEmpty());
		assertEquals(0, file.getSize());
		assertEquals(-1, file.getInputStream().read());
	}

	@Test
	public void transferToCreatesParentDirectory() throws Exception {
		ByteArrayMultipartFile file = ByteArrayMultipartFile.builder()
				.name("file")
				.bytes("hello".getBytes(StandardCharsets.UTF_8))
				.build();
		File destination = new File(System.getProperty("java.io.tmpdir"),
				"sun-utils-multipart-" + System.nanoTime() + "/nested/file.txt");

		file.transferTo(destination);

		assertEquals("hello", new String(Files.readAllBytes(destination.toPath()), StandardCharsets.UTF_8));
	}
}
