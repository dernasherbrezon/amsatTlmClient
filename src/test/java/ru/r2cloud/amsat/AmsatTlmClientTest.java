package ru.r2cloud.amsat;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmsatTlmClientTest {

	private static final Logger LOG = LoggerFactory.getLogger(AmsatTlmClientTest.class);

	private static final byte[] FAIL = new byte[] { 0x46, 0x41, 0x0D, 0x0A };
	private static final byte[] OK = new byte[] { 0x4F, 0x4D, 0x0D, 0x0A };

	private TlmServerMock server;
	private AmsatTlmClient client;

	@Test(expected = AmsatTlmException.class)
	public void testUnknownResponse2() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xca, (byte) 0xfe });
		server.setHandler(handler);
		client.send(createValidRequest());
	}

	@Test(expected = AmsatTlmException.class)
	public void testUnknownResponse() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(new byte[] { (byte) 0xca, (byte) 0xfe });
		server.setHandler(handler);
		client.send(createValidRequest());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidFrameSize() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(OK);
		server.setHandler(handler);
		Frame frame = createValidRequest();
		frame.setData(new byte[] { (byte) 0xca, (byte) 0xfe });
		client.send(frame);
	}

	@Test(expected = AmsatTlmException.class)
	public void testFailure() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(FAIL);
		server.setHandler(handler);
		client.send(createValidRequest());
	}

	@Test
	public void testHighspeedFrame() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(OK);
		server.setHandler(handler);

		Frame request = createValidRequest();
		request.setData(new byte[4600]);
		client.send(request);

		assertArrayEquals(new byte[5272], handler.getBody());
		String expectedHeaders = "Sequence: 2\n" + "Source: amsat.fox-1c.ihu.highspeed\n" + "Length: 42176\n" + "Date: Wed, 20 May 2020 08:25:02\n" + "Receiver: M7RED\n" + "Rx-Location: N 53.72 E 47.57 0\n" + "Demodulator: amsatTlmClient/test-1.0 (dernasherbrezon)";
		assertEquals(expectedHeaders, handler.getHeaders());
	}

	@Test
	public void testPskFrameAndEmptyCallsign() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(OK);
		server.setHandler(handler);

		Frame request = createValidRequest();
		request.setCallsign("");
		request.setData(new byte[476]);
		client.send(request);

		assertArrayEquals(new byte[572], handler.getBody());
		String expectedHeaders = "Sequence: 2\n" + "Source: amsat.fox-1c.ihu.bpsk\n" + "Length: 4576\n" + "Date: Wed, 20 May 2020 08:25:02\n" + "Receiver: NONE\n" + "Rx-Location: N 53.72 E 47.57 0\n" + "Demodulator: amsatTlmClient/test-1.0 (dernasherbrezon)";
		assertEquals(expectedHeaders, handler.getHeaders());
	}

	@Test
	public void testSuccess() throws Exception {
		SimpleRequestHandler handler = new SimpleRequestHandler(OK);
		server.setHandler(handler);

		client.send(createValidRequest());

		assertArrayEquals(new byte[96], handler.getBody());
		String expectedHeaders = "Sequence: 2\n" + "Source: amsat.fox-1c.ihu.duv\n" + "Length: 768\n" + "Date: Wed, 20 May 2020 08:25:02\n" + "Receiver: M7RED\n" + "Rx-Location: N 53.72 E 47.57 0\n" + "Demodulator: amsatTlmClient/test-1.0 (dernasherbrezon)";
		assertEquals(expectedHeaders, handler.getHeaders());
	}

	private static Frame createValidRequest() {
		Frame frame = new Frame();
		frame.setCallsign("M7RED");
		frame.setData(new byte[64]);
		frame.setLatitude(53.72);
		frame.setLongitude(47.57);
		frame.setSatellite(Satellite.FOX1CLIFF);
		frame.setSequence(2);
		frame.setTime(new Date(1589963102295L));
		return frame;
	}

	@Before
	public void start() throws IOException {
		List<InetSocketAddress> address = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			int port = 8000 + i;
			server = new TlmServerMock(port);
			try {
				server.start();
			} catch (BindException e) {
				LOG.info("port: {} taken. trying new", port);
				continue;
			}
			address.add(new InetSocketAddress("127.0.0.1", port));
			break;
		}
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
