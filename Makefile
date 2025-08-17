MVN = mvn
JAVA = java
VERSION = $(shell $(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout)
GIT = git

.PHONY: all clean test build release package tag test-iteration-2 test-iteration-1 test-our-server

all: clean build test
clean:
	$(MVN) clean

test-iteration-1:
	@echo ".libs/reference-server-0.1.0.jar">"src/main/resources/serverName"
	$(MVN) compile
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.launch.LaunchRobotTests.java
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.Look.LookCommandTest.java
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.state.StateRobotTests.java
test-iteration-2:
	@echo ".libs/reference-server-0.2.3.jar">"src/main/resources/serverName"

	$(MVN) compile
	$(MVN) test -Dtest=za.co.wethinkcode.Acceptance2.Launch.LaunchRobotTests.java
	$(MVN) test -Dtest=za.co.wethinkcode.Acceptance2.Look.LookCommandTest.java
	$(MVN) test -Dtest=za.co.wethinkcode.Acceptance2.Movement.MoveForwardTest.java
	$(MVN) test -Dtest=za/co/wethinkcode.Acceptance2.state.StateRobotTest.java
test-our-server:
	@echo "target/robot-world-0.0.2.jar">"src/main/resources/OurserverName"
	$(MVN) compile
	$(MVN) test -Dtest=za.co.wethinkcode.Acceptance2.Launch.LaunchRobotTests.java
	$(MVN) test -Dtest=za.co.wethinkcode.Acceptance2.Look.LookCommandTest.java
	$(MVN) test -Dtest=za.co.wethinkcode.Acceptance2.Movement.MoveForwardTest.java
	$(MVN) test -Dtest=za.co.wethinkcode.Acceptance2.state.StateRobotTest.java

test-iteration-3:
	$(MVN)	test -Dtest=za.co.wethinkcode.Acceptance3.Save.SaveExistingWorldTests.java
	$(MVN)	test -Dtest=za.co.wethinkcode.Acceptance3.Save.SaveWorldTests.java
	$(MVN)	test -Dtest=za.co.wethinkcode.Acceptance3.Save.SaveObstaclesTests.java
	$(MVN)	test -Dtest=za.co.wethinkcode.Acceptance3.Restore.RestoreCommandTest.java

test-iteration-4:
	$(MVN) test -Dtest=apiTests.GetAllWorldsTests,apiTests.GetCurrentWorldTests,apiTests.GetSpecifiedWorldTests,apiTests.WebApiSmokeTest

#test-iteration-4:
#	$(MVN)	test -Dtest=apiTests.GetAllWorldsTests
#	$(MVN)	test -Dtest=apiTests.GetCurrentWorldTests
#	$(MVN)	test -Dtest=apiTests.GetSpecifiedWorldTests
#	$(MVN)	test -Dtest=apiTests.WebApiSmokeTest
all-test: test-iteration-2 test-our-server test-iteration-3


build:
	$(MVN) compile
	$(MVN) verify -DskipTests
	$(MVN) package -DskipTests
	$(MAKE) test

release:
	@echo "Current version: $(VERSION)"
	@if echo "$(VERSION)" | grep -q "SNAPSHOT"; then \
	echo "Error: Cannot release SNAPSHOT version"; \
	exit 1; \
	fi
	$(MAKE) clean
	$(MAKE) package
	$(MAKE) tag

tag:
	$(GIT) tag -a release-$(VERSION) -m "Release version $(VERSION)"
	$(GIT) push origin release-$(VERSION)

dev-build:
	$(MVN) versions:set -DnewVersion=$(VERSION)-SNAPSHOT
	$(MAKE) clean build test package