package ru.r2cloud.amsat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TlmServerMock {

	private static final Logger LOG = LoggerFactory.getLogger(TlmServerMock.class);

	private final int port;

	private ServerSocket socket;
	private Thread thread;
	private RequestHandler handler;

	TlmServerMock(int port) {
		this.port = port;
	}

	public void setHandler(RequestHandler handler) {
		this.handler = handler;
	}

	void start() throws IOException {
		socket = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"));
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						Socket client = socket.accept();
						if (handler != null) {
							byte[] response = handler.handle(client.getInputStream());
							client.getOutputStream().write(response);
							client.getOutputStream().flush();
						}
						client.close();
					} catch (IOException e) {
						return;
					}
				}
				LOG.info("shutting down");
			}
		}, "client-handler");
		thread.start();
	}

	void stop() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				LOG.error("unable to close socket", e);
			}
		}
		if (thread != null) {
			thread.interrupt();
		}
	}

}
