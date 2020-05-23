package ru.r2cloud.amsat;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AmsatTlmClientTest {

	private static final byte[] FAIL = new byte[] { 0x46, 0x41, 0x0D, 0x0A };
	private static final byte[] OK = new byte[] { 0x4F, 0x4D, 0x0D, 0x0A };

	private TlmServerMock server;
	private AmsatTlmClient client;

	@Test(expected = AmsatTlmException.class)
	public void testUnknownResponse2() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xca, (byte) 0xfe }, 287);
		server.setHandler(handler);
		client.send(createValidRequest());
	}

	@Test(expected = AmsatTlmException.class)
	public void testUnknownResponse() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(new byte[] { (byte) 0xca, (byte) 0xfe }, 287);
		server.setHandler(handler);
		client.send(createValidRequest());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidFrameSize() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(OK, 287);
		server.setHandler(handler);
		Frame frame = createValidRequest();
		frame.setData(new byte[] { (byte) 0xca, (byte) 0xfe });
		client.send(frame);
	}

	@Test(expected = AmsatTlmException.class)
	public void testFailure() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(FAIL, 287);
		server.setHandler(handler);
		client.send(createValidRequest());
	}

	@Test
	public void testSuccess() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(OK, 287);
		server.setHandler(handler);

		client.send(createValidRequest());

		byte[] actual = handler.getBody();
		assertArrayEquals(new byte[] { 83, 101, 113, 117, 101, 110, 99, 101, 58, 32, 50, 13, 10, 83, 111, 117, 114, 99, 101, 58, 32, 97, 109, 115, 97, 116, 46, 102, 111, 120, 45, 49, 99, 46, 105, 104, 117, 46, 100, 117, 118, 13, 10, 76, 101, 110, 103, 116, 104, 58, 32, 55, 54, 56, 13, 10, 68, 97, 116, 101, 58, 32, 87, 101, 100, 44, 32, 50, 48, 32, 77, 97, 121, 32, 50, 48, 50, 48, 32, 48, 56, 58, 50, 53, 58, 48, 50, 13, 10, 82, 101, 99, 101, 105, 118, 101, 114, 58, 32, 77, 55, 82, 69, 68, 13, 10,
				82, 120, 45, 76, 111, 99, 97, 116, 105, 111, 110, 58, 32, 78, 32, 53, 51, 46, 55, 50, 32, 69, 32, 52, 55, 46, 53, 55, 32, 48, 13, 10, 68, 101, 109, 111, 100, 117, 108, 97, 116, 111, 114, 58, 32, 97, 109, 115, 97, 116, 84, 108, 109, 67, 108, 105, 101, 110, 116, 47, 49, 46, 48, 32, 40, 100, 101, 114, 110, 97, 115, 104, 101, 114, 98, 114, 101, 122, 111, 110, 41, 13, 10, 13, 10, -109, 3, 96, 111, -88, 16, 0, 0, 0, -61, 11, 0, 0, 0, 0, 0, 0, 0, -48, -52, -77, 117, 107, -71, 20, 108,
				-62, 101, 39, 119, 126, 87, 121, 59, 23, 95, 68, 7, 1, 0, 0, 0, -66, 85, 29, -96, -94, -123, -10, -105, -96, 95, -48, 41, 8, 1, 0, 0, 18, 56, 64, 54, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, actual);
	}

	private static Frame createValidRequest() {
		Frame frame = new Frame();
		frame.setCallsign("M7RED");
		frame.setData(new byte[] { -109, 3, 96, 111, -88, 16, 0, 0, 0, -61, 11, 0, 0, 0, 0, 0, 0, 0, -48, -52, -77, 117, 107, -71, 20, 108, -62, 101, 39, 119, 126, 87, 121, 59, 23, 95, 68, 7, 1, 0, 0, 0, -66, 85, 29, -96, -94, -123, -10, -105, -96, 95, -48, 41, 8, 1, 0, 0, 18, 56, 64, 54, 0, 32 });
		frame.setLatitude(53.72);
		frame.setLongitude(47.57);
		frame.setSatellite(Satellite.FOX1CLIFF);
		frame.setSequence(2);
		frame.setTime(new Date(1589963102295L));
		return frame;
	}

	@Before
	public void start() throws IOException {
		int port = 8000;
		server = new TlmServerMock(port);
		server.start();
		List<InetSocketAddress> address = new ArrayList<>();
		address.add(new InetSocketAddress("127.0.0.1", port));
		client = new AmsatTlmClient(address, 10000);
	}

	@After
	public void stop() {
		if (server != null) {
			server.stop();
		}
		if (client != null) {
			client.stop();
		}
	}

}
