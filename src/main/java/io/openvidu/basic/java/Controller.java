package io.openvidu.basic.java;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.openvidu.java.client.Connection;
import io.openvidu.java.client.ConnectionProperties;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.Session;
import io.openvidu.java.client.SessionProperties;

@CrossOrigin(origins = "*")
@RestController
public class Controller {

	@Value("${OPENVIDU_URL}")
	private String OPENVIDU_URL;

	@Value("${OPENVIDU_SECRET}")
	private String OPENVIDU_SECRET;

	private OpenVidu openvidu;

	@PostConstruct
	public void init() {
		this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
	}

	/**
	 * @param params The Session properties
	 * @return The Session ID
	 */
	@PostMapping("/api/sessions")
	public ResponseEntity<String> initializeSession(@RequestBody(required = false) Map<String, Object> params)
			throws OpenViduJavaClientException, OpenViduHttpException {
		SessionProperties properties = SessionProperties.fromJson(params).build();
		Session session = openvidu.createSession(properties);
		System.out.println("session = " + session);
		return new ResponseEntity<>(session.getSessionId(), HttpStatus.OK);
	}

	/**
	 * @param sessionId The Session in which to create the Connection
	 * @param params    The Connection properties
	 * @return The Token associated to the Connection
	 */
	@PostMapping("/api/sessions/{sessionId}/connections")
	public ResponseEntity<String> createConnection(@PathVariable("sessionId") String sessionId,
												   @RequestBody(required = false) Map<String, Object> params)
			throws OpenViduJavaClientException, OpenViduHttpException {

		// 세션 존재 여부 체크
		Session session = openvidu.getActiveSession(sessionId);
		if (session == null) {
			System.out.println("Session not found: " + sessionId);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		try {
			// ConnectionProperties 생성
			ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
			Connection connection = session.createConnection(properties);
			return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
		} catch (OpenViduJavaClientException e) {
			// 예외 발생 시 로깅 및 상세한 메시지 반환
			System.err.println("Error creating connection: " + e.getMessage());
			return new ResponseEntity<>("Error creating connection: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (OpenViduHttpException e) {
			// HTTP 오류 처리
			System.err.println("OpenVidu HTTP exception: " + e.getMessage());
			return new ResponseEntity<>("OpenVidu HTTP exception: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


}
