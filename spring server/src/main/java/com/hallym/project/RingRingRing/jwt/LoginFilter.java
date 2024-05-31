package com.hallym.project.RingRingRing.jwt;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hallym.project.RingRingRing.joinmember.DTO.UserDTO;
import com.hallym.project.RingRingRing.joinmember.entity.UserEntity;
import com.hallym.project.RingRingRing.message.Message;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * UsernamePasswordAuthenticationFilter 상속하여 오버라이드하고 시큐리티 필터에 addFilterAt하는 클래스
 */
@Slf4j
@Deprecated
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;

	private final JWTUtil jwtUtil;

	public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {

		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	/**
	 * EndPoint: /login 로그인 요청이 들어 오면 처리하는 부분 form-data형식의 테이터를 UserEntity에 매핑
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			log.info("[login attempt]:  {}", request.getRemoteAddr());
			if (!request.getMethod().equals("POST")) {
				throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
			}
			if (!request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
				throw new AuthenticationServiceException("Content type not supported: " + request.getContentType());
			}

			ObjectMapper objectMapper = new ObjectMapper();

			UserEntity credentials = objectMapper.readValue(request.getInputStream(), UserEntity.class);

			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
					credentials.getEmail(), credentials.getPwd());
//			int i = 1;
//			if(i==1) {
//				throw new AuthenticationServiceException("오류");
//			}

			return authenticationManager.authenticate(authToken);
		} catch (AuthenticationServiceException e) {
			request.setAttribute("exception", e);
			throw new AuthenticationServiceException("Failed to Authentication", e);
		} catch (IOException e) {
			throw new AuthenticationServiceException("Failed to read JSON data from request body", e);
		}

	}

	/**
	 * 로그인 성공시 실행하는 메소드 코드: 200 Headers: Authorization에 jwt 토큰 "Bearer "jwt토큰 Body:
	 * {"id": "","name": "사용자이름","email": "이메일","pwd": null}
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authentication) {

//		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
//
//		String username = customUserDetails.getUsername();
//
//		String strRoles = populateAuthorities(authentication.getAuthorities());
//
//		// 토큰 생성 만료시간 30000000
//		String token = jwtUtil.createJwt(customUserDetails.getId(),username, strRoles.toString(), 30000000L);
//
//		response.setHeader("Authorization", "Bearer " + token);
//		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//		response.setCharacterEncoding("UTF-8");
//		response.setStatus(HttpStatus.OK.value());
//
//		UserDTO user = new UserDTO(customUserDetails.getUserEntity().getId(),
//				customUserDetails.getUserEntity().getName(), customUserDetails.getUsername(), null);
//
//		ObjectMapper objectMapper = new ObjectMapper();
//		try {
//			
//			int i = 1;
//			if(i==1) {
//				throw new IOException();
//			}
//			
//			objectMapper.writeValue(response.getWriter(), user);
//			log.info("[login Successful]:  {} {}", request.getRemoteAddr(),username);
//		} catch (IOException e) {
//			response.setHeader("Authorization", "");
//			response.setStatus(HttpStatus.BAD_REQUEST.value());
//			logger.error("Failed to write JSON response: " + e.getMessage());
//		}
	}

	/**
	 * 로그인 실패시 실행하는 메소드 코드: 404 Body: {"date": null,"massage": "이메일 또는 비번이 없거나
	 * 클렸습니다."}
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		if(failed.getMessage().equals("Failed to Authentication")){
			request.setAttribute("exception", failed);
			throw failed;
		}
//		log.warn("Authentication failed: " + failed.getMessage());
		
		Message errorResponse = new Message(null, "이메일 또는 비번이 없거나 클렸습니다.");

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpStatus.NOT_FOUND.value());

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(response.getWriter(), errorResponse);

	}

	/**
	 * role을 문자열로 변경 권한 뒤, 추가형태로
	 * 
	 * @param collection Set의 role권한
	 * @return 권한1,권한2,...
	 */
	private String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
		Set<String> authoritiesSet = new HashSet<>();
		for (GrantedAuthority authority : collection) {
			authoritiesSet.add(authority.getAuthority());
		}
		return String.join(",", authoritiesSet);
	}
}
