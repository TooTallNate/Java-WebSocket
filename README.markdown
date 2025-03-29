# Java-WebSocket Project

## Build Instructions

### Prerequisites
- **Java JDK 8 or later** (verify with `java -version`)
- **Apache Maven 3.6+** (verify with `mvn -v`)

### Building
```bash
# 1. Clone repository
git clone https://github.com/kasyAnalyst/Java-WebSocket.git
cd Java-WebSocket

# 2. Build project
mvn clean install

# 3. Run tests 
mvn test

# 4. Generate documentation (optional)
mvn javadoc:javadoc
