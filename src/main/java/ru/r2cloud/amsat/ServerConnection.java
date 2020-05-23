package ru.r2cloud.amsat;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServerConnection {

	private static final Logger LOG = LoggerFactory.getLogger(ServerConnection.class);

	private final int timeout;
	private final InetSocketAddress address;
	private Socket socket;

	ServerConnection(InetSocketAddress address, int timeout) {
		this.address = address;
		this.timeout = timeout;
	}

	byte[] send(byte[] data) throws IOException {
		// initialize connection lazily
		if (socket == null) {
			socket = new Socket();
			socket.connect(address, timeout);
			socket.setSoTimeout(timeout);
		}
		socket.getOutputStream().write(data);
		socket.getOutputStream().flush();
		byte[] b = new byte[4];
		try {
			readFully(b);
		} finally {
			// amsat supports only auto close connections
			stop();
		}
		return b;
	}

	private void readFully(byte[] b) throws IOException {
		int len = b.length;
		int n = 0;
		while (n < len) {
			int count = socket.getInputStream().read(b, n, len - n);
			if (count < 0) {
				throw new EOFException();
			}
			n += count;
		}
	}

	void stop() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				LOG.info("unable to close connection: {}", address, e);
			}
			socket = null;
		}
	}

}
