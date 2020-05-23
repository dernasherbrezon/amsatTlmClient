package ru.r2cloud.amsat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AmsatTlmClient {

	private static final byte[] OK = { 0x4F, 0x4D, 0x0D, 0x0A };
	private static final byte[] FAIL = { 0x46, 0x41, 0x0D, 0x0A };

	private final List<ServerConnection> servers = new ArrayList<>();

	public AmsatTlmClient(List<InetSocketAddress> addresses, int timeout) {
		for (InetSocketAddress cur : addresses) {
			servers.add(new ServerConnection(cur, timeout));
		}
	}

	public void send(Frame frame) throws AmsatTlmException {
		AmsatTlmException lastException = null;
		for (ServerConnection cur : servers) {
			try {
				sendInternally(cur, frame);
				lastException = null;
				break;
			} catch (AmsatTlmException e) {
				lastException = e;
			}
		}
		if (lastException != null) {
			throw lastException;
		}
	}

	private static void sendInternally(ServerConnection server, Frame frame) throws AmsatTlmException {
		byte[] reply;
		try {
			reply = server.send(convert(frame));
		} catch (IOException e) {
			throw new AmsatTlmException("unable to send", e);
		}
		if (Arrays.equals(reply, OK)) {
			return;
		}
		if (Arrays.equals(reply, FAIL)) {
			throw new AmsatTlmException("response: fail");
		}
		throw new AmsatTlmException("unknown response: " + Arrays.toString(reply));
	}

	private static byte[] convert(Frame frame) {
		frame.setData(normalizeData(frame.getData()));
		StringBuilder header = new StringBuilder();
		header.append("Sequence: ").append(frame.getSequence()).append("\r\n");
		header.append("Source: ").append(getSource(frame)).append("\r\n");
		header.append("Length: ").append(frame.getData().length * 8).append("\r\n");
		header.append("Date: ").append(formatDate(frame.getTime())).append("\r\n");
		header.append("Receiver: ").append(formatCallsign(frame.getCallsign())).append("\r\n");
		header.append("Rx-Location: ").append(formatLatitude(frame.getLatitude())).append(" ").append(formatLongitude(frame.getLongitude())).append(" 0\r\n");
		header.append("Demodulator: ").append("amsatTlmClient/1.0 (dernasherbrezon)").append("\r\n");
		header.append("\r\n");

		byte[] headerBytes = header.toString().getBytes(StandardCharsets.ISO_8859_1);

		byte[] result = new byte[headerBytes.length + frame.getData().length];
		System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
		System.arraycopy(frame.getData(), 0, result, headerBytes.length, frame.getData().length);
		return result;
	}

	// append zeroed reed solomon parity bytes
	private static byte[] normalizeData(byte[] data) {
		if (data.length == 96 || data.length == 5272 || data.length == 572) {
			return data;
		}
		if (data.length == 64) {
			byte[] result = new byte[96];
			System.arraycopy(data, 0, result, 0, data.length);
			return result;
		}
		if (data.length == 476) {
			byte[] result = new byte[572];
			System.arraycopy(data, 0, result, 0, data.length);
			return result;
		}
		if (data.length == 4600) {
			byte[] result = new byte[5272];
			System.arraycopy(data, 0, result, 0, data.length);
			return result;
		}
		throw new IllegalArgumentException("unknown frame size: " + data.length);
	}

	private static String formatLongitude(double f) {
		if (f >= 0) {
			return "E " + f;
		} else {
			return "W " + Math.abs(f);
		}
	}

	private static String formatLatitude(double f) {
		if (f >= 0) {
			return "N " + f;
		} else {
			return "S " + Math.abs(f);
		}
	}

	private static String formatCallsign(String callsign) {
		if (callsign == null || callsign.trim().length() == 0) {
			return "NONE";
		}
		return callsign;
	}

	private static String formatDate(Date date) {
		DateFormat stpDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
		stpDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return stpDateFormat.format(date);
	}

	private static String getSource(Frame frame) {
		String suffix;
		int frameLength = frame.getData().length;
		if (frameLength == 96) {
			suffix = ".duv";
		} else if (frameLength == 5272) {
			suffix = ".highspeed";
		} else if (frameLength == 572) {
			suffix = ".bpsk";
		} else {
			throw new IllegalArgumentException("unknown frame length: " + frameLength);
		}
		return frame.getSatellite().getSourcePrefix() + suffix;
	}

	public void stop() {
		for (ServerConnection cur : servers) {
			cur.stop();
		}
	}

}
