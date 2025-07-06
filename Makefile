MVN = mvn
JAVA = java
.PHONY: build

build:
	@echo "starting ref server"
	$(JAVA) -jar .libs/reference-server-0.1.0.jar & echo $$! > serve.pid
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.launch.LaunchRobotTests
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.Look.LookCommandTest
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.state.StateRobotTests
	@echo "stopping ref server"
	kill -9 `cat serve.pid`
	@echo "################################# starting local server"
	$(MVN) exec:java -Dexec.mainClass="za.co.wethinkcode.server.RobotWorldServer" & echo $$! > server.pid
	$(MVN) test -Dtest=za.co.wethinkcode.acceptance.state.StateRobotTests
	@if [ -f server.pid ]; then kill -9 cat server.pid || true; rm -f server.pid; fi
	$(MVN) verify
	$(MVN) compile
	$(MVN) package

