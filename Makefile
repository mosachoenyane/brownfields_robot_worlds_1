MVN = mvn
JAVA = java
VERSION = $(shell $(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout)
GIT = git

.PHONY: all clean test build release package tag

all: clean build test

clean:
	$(MVN) clean

build:
	$(MVN) compile
	@echo "starting ref server"
	$(JAVA) -jar .libs/reference-server-0.2.3.jar -s 2 -o 1,1 & echo $$! > serve.pid
	$(MVN) test -Dtest=za/co/wethinkcode/Acceptance2/Launch/LaunchRobotTests2.java
	$(MVN) test -Dtest=za/co/wethinkcode/Acceptance2/Movement/MoveForwardTest.java
	@echo "stoping reference server"

	@if [ -f serve.pid ]; then kill -9 cat serve.pid || true; rm -f serve.pid; fi

	@echo "starting ref server against look"
	$(JAVA) -jar .libs/reference-server-0.2.3.jar -s 2 -o 0,1 & echo $$! > serve.pid
	$(MVN) test -Dtest=za/co/wethinkcode/Acceptance2/Look/LookCommandTest.java
	@echo "stoping ref server"

	@if [ -f serve.pid ]; then kill -9 cat serve.pid || true; rm -f serve.pid; fi

	@echo "Stopping local server..."
	@if [ -f server.pid ]; then kill -9 cat server.pid || true; rm -f server.pid; fi

package:
	$(MVN) verify -DskipTests
	$(MVN) package -DskipTests

release:
	@echo "Current version: $(VERSION)"
	@if echo "$(VERSION)" | grep -q "SNAPSHOT"; then \
	echo "Error: Cannot release SNAPSHOT version"; \
	exit 1; \
	fi
	$(MAKE) clean
	$(MAKE) test
	$(MAKE) package
	$(MAKE) tag

tag:
	$(GIT) tag -a release-$(VERSION) -m "Release version $(VERSION)"
	$(GIT) push origin release-$(VERSION)

dev-build:
	$(MVN) versions:set -DnewVersion=$(VERSION)-SNAPSHOT
	$(MAKE) clean build test package