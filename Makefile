MVN = mvn
JAVA = java
VERSION = $(shell $(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout)
GIT = git

.PHONY: all clean test build run-ref-tests run-local-tests release package tag

all: clean build test

clean:
	$(MVN) clean

build:
	$(MVN) compile

test: run-ref-tests run-local-tests

run-ref-tests:
	@echo "Running tests against reference server..."
	@echo "Starting reference server..."
	$(JAVA) -jar .libs/reference-server-0.1.0.jar & echo $$! > serve.pid
	@sleep 2
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.launch.LaunchRobotTests
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.Look.LookCommandTest
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.state.StateRobotTests
	@echo "Stopping reference server..."
	@if [ -f serve.pid ]; then kill -9 cat serve.pid || true; rm -f serve.pid; fi

run-local-tests:
	@echo "Running tests against local server..."
	@echo "Starting local server..."
	$(MVN) exec:java -Dexec.mainClass="za.co.wethinkcode.server.RobotWorldServer" & echo $$! > server.pid
	@sleep 2
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.launch.LaunchRobotTests
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.Look.LookCommandTest
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.state.StateRobotTests
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