# amsatTlmClient

Java client for sending telemetry data to AMSAT.

# Usage

1. Add maven dependency:

```xml
<dependency>
  <groupId>ru.r2cloud</groupId>
  <artifactId>amsatTlmClient</artifactId>
  <version>1.0</version>
</dependency>
```

2. Setup client:

```java
List<InetSocketAddress> servers = new ArrayList<>();
servers.add(new InetSocketAddress("tlm.amsat.org", 41042));
servers.add(new InetSocketAddress("tlm.amsat.us", 41042));
AmsatTlmClient client = new AmsatTlmClient(servers, 10000);
```

3. Prepare and send request:

```java
Frame frame = new Frame();
frame.setCallsign("callsign");
frame.setFrame(new byte[]{ ... });
frame.setLatitude(0.0);
frame.setLongitude(0.0);
frame.setSatellite(Satellite.FOX1A);
frame.setSequence(127631L);
frame.setTime(new Date());
client.send(frame);
```