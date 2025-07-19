MVN = mvn
JAVA = java
VERSION = $(shell $(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout)
GIT = git

.PHONY: all clean test build release package tag test-iteration-2

all: clean build test

clean:
	$(MVN) clean
test-iteration-2:
	@echo ".libs/reference-server-0.2.3.jar">"src/main/resources/serverName"
	$(MVN) compile
	$(MVN) test -Dtest=za.co.wethinkcode.Acceptance2.Launch.LaunchRobotTests.java
	$(MVN) test -Dtest=za.co.wethinkcode.Acceptance2.Look.LookCommandTest.java
	$(MVN) test -Dtest=za/co/wethinkcode/Acceptance2/Movement/MoveForwardTest.java

build:
	$(MVN) compile
	@echo "starting reference server"
	$(JAVA) -jar .libs/reference-server-0.2.3.jar -s 2 -o 1,1 & echo $$! > serve.pid
	$(MVN) test -Dtest=za.co.wethinkcode.Acceptance2.Launch.LaunchRobotTests.java
	$(MVN) test -Dtest=za/co/wethinkcode/Acceptance2/Movement/MoveForwardTest.java
	$(MVN) test -Dtest=za/co/wethinkcode/Acceptance2/Look/LookCommandTest.java
	@echo "stopping reference server"
	@if [ -f serve.pid ]; then kill -9 cat serve.pid || true; rm -f serve.pid; fi

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