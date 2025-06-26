# Network Packet Protocol Example

## How to test
1. Run Server
```bash
mvn exec:java -Dexec.mainClass="org.konceptosociala.netpkt.warehouse.Server"
```

2. Run Tests
```bash
mvn test -Dtest=HttpServerTest
```