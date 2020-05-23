package ru.r2cloud.amsat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class SimpleRequestHandler implements RequestHandler {

	private final byte[] response;
	private final int bodyLength;

	private byte[] body;

	SimpleRequestHandler(byte[] response, int bodyLength) {
		this.response = response;
		this.bodyLength = bodyLength;
	}

	@Override
	public byte[] handle(InputStream is) {
		this.body = new byte[bodyLength];
		DataInputStream dis = new DataInputStream(is);
		try {
			dis.readFully(body);
		} catch (IOException e) {
			throw new RuntimeException("unable to handle", e);
		}
		return response;
	}

	public byte[] getBody() {
		return body;
	}

}
