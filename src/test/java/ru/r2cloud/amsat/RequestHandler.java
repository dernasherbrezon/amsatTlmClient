package ru.r2cloud.amsat;

import java.io.InputStream;

public interface RequestHandler {

	byte[] handle(InputStream request);
	
}
