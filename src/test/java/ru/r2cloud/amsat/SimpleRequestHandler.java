package ru.r2cloud.amsat;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

class SimpleRequestHandler implements RequestHandler {

	private static final Pattern NEW_LINE = Pattern.compile("\n");

	private final byte[] response;

	private byte[] body;
	private String headers;

	SimpleRequestHandler(byte[] response) {
		this.response = response;
	}

	@Override
	public byte[] handle(InputStream is) {
		headers = readHeaders(is);
		Integer length = null;
		if (headers != null) {
			length = readLength(headers);
		}
		if (length != null) {
			this.body = new byte[length / 8];
			DataInputStream dis = new DataInputStream(is);
			try {
				dis.readFully(body);
			} catch (IOException e) {
				throw new RuntimeException("unable to handle", e);
			}
		}
		return response;
	}

	private static Integer readLength(String headers) {
		String[] headersArray = NEW_LINE.split(headers);
		String prefix = "Length:";
		for (String cur : headersArray) {
			if (cur.startsWith(prefix)) {
				return Integer.valueOf(cur.substring(prefix.length()).trim());
			}
		}
		return null;
	}

	private static String readHeaders(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StringBuilder headers = new StringBuilder();
		while (!Thread.currentThread().isInterrupted()) {
			int currentByte;
			try {
				currentByte = is.read();
				if (currentByte == -1) {
					break;
				}
				if (((char) currentByte) == '\r') {
					int nextByte = is.read();
					if (((char) nextByte) == '\n') {
						String currentLine = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
						baos = new ByteArrayOutputStream();
						if (currentLine.trim().length() == 0) {
							return headers.toString().trim();
						} else {
							headers.append(currentLine).append("\n");
						}
					} else {
						baos.write(currentByte);
						baos.write(nextByte);
					}
				} else {
					baos.write(currentByte);
				}
			} catch (IOException e) {
				throw new RuntimeException("unable to handle", e);
			}
		}
		return null;
	}

	public byte[] getBody() {
		return body;
	}
	
	public String getHeaders() {
		return headers;
	}

}
