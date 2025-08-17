package za.co.wethinkcode.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * A JSON-based implementation of {@link RobotWorldClient}.
 * This client connects to a Robot Worlds server using TCP sockets
 * and communicates by sending and receiving JSON messages.
 */
public class RobotWorldJsonClient implements RobotWorldClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Socket socket;
    private PrintStream out;
    private BufferedReader in;

    /**
     * Connects to the Robot Worlds server at the specified IP address and port.
     * Initializes the socket and input/output streams for communication.
     *
     * @param ipAddress the server IP address (e.g., {@code "localhost"} or remote IP)
     * @param port      the port number the server is listening on
     * @throws RuntimeException if a connection cannot be established
     */
    @Override
    public void connect(String ipAddress, int port) {
        try {
            socket = new Socket(ipAddress, port);
            out = new PrintStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
        } catch (IOException e) {
            //error connecting should just throw Runtime error and fail test
            throw new RuntimeException("Error connecting to Robot Worlds server.", e);
        }
    }

    /**
     * Checks whether the client is connected to the server.
     * @return {@code true} if the socket is connected, {@code false} otherwise
     */
    @Override
    public boolean isConnected() {
        return socket.isConnected();
    }

    /**
     * Disconnects from the Robot Worlds server by closing the input/output streams
     * and the underlying socket.
     *
     * @throws RuntimeException if an error occurs while disconnecting
     */
    @Override
    public void disconnect() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            //error connecting should just throw Runtime error and fail test
            throw new RuntimeException("Error disconnecting from Robot Worlds server.", e);
        }
    }

    /**
     * Sends a JSON request string to the server and parses the response
     * into a {@link JsonNode}.
     *
     * @param requestJsonString a JSON-formatted request string
     * @return the server's response as a {@link JsonNode}
     * @throws RuntimeException if the server response cannot be read or parsed
     */
    @Override
    public JsonNode sendRequest(String requestJsonString) {
        try {
            out.println(requestJsonString);
            out.flush();
            return OBJECT_MAPPER.readTree(in.readLine());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing server response as JSON.", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading server response.", e);
        }
    }

    /**
     * Sends a JSON request string to the server and parses the response
     * into a {@link JsonNode}.
     *
     * @param requestJsonString a JSON-formatted request string
     * @return the server's response as a {@link JsonNode}
     * @throws RuntimeException if the server response cannot be read or parsed
     */
    @Override
    public String sendRequestAsString(String requestString) {
        try {
            out.println(requestString);
            out.flush();
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Error reading server response.", e);
        }
    }
}
