package com.hallym.project.RingRingRing.jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hallym.project.RingRingRing.joinmember.entity.AuthorityEntity;
import com.hallym.project.RingRingRing.joinmember.entity.UserEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
/**
 *  JWT토큰 유효성 검증 필터
 */
@Slf4j
public class JWTFilter extends OncePerRequestFilter {


	/**
	 * 검증 메소드
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			log.info("[인중 가 진행] ensd point: {}", request.getRequestURL());
			String jwt = request.getHeader("Authorization");
			if (jwt == null || jwt.equals("")) {
				throw new NullPointerException("토큰이 없습니다.");
			}
			// 키 가져오기
			SecretKey key = new SecretKeySpec(JWTConstants.JWT_KEY.getBytes(StandardCharsets.UTF_8),
					Jwts.SIG.HS256.key().build().getAlgorithm());
			
			// 토큰 유효성 검증
			Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt).getPayload();
			
			Long id = ((Integer) claims.get("id")).longValue();
			String username = String.valueOf(claims.get("username"));
			String roles = (String) claims.get("roles");

			String[] authoritiesArray = roles.split(",");
			
			Set<AuthorityEntity> authoritiesSet = new HashSet<>();

			for (String authority : authoritiesArray) {
				AuthorityEntity auth = AuthorityEntity.builder().role(authority).build();
				authoritiesSet.add(auth);
			}
			
			UserEntity userEntity = UserEntity.builder()
					.id(id)
					.email(username)
					.pwd("temppassword")
					.authorities(authoritiesSet)
					.build();

			CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

			Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null,
					customUserDetails.getAuthorities());
			
			
			SecurityContextHolder.getContext().setAuthentication(authToken);
			logger.info("[인중 가 통과]: "+username+" endpoint: "+request.getRequestURI());

		}catch(NullPointerException e){
			request.setAttribute("exception", e);
			log.warn("Authorization is not found ");
		}
		catch (MalformedJwtException e) {
			request.setAttribute("exception", e);
			log.warn("JWT validation failed: " + e.getMessage());
			return;
		} catch (SignatureException e) {
			request.setAttribute("exception", e);
			log.warn("JWT validation failed: " + e.getMessage());
			return;
		}catch (ExpiredJwtException e) {
			request.setAttribute("exception", e);
			log.warn("JWT validation failed: " + e.getMessage());
			return;
		}
		finally {
			
			filterChain.doFilter(request, response);
		}

	}
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
    	if( request.getServletPath().equals("/signup")) {
    		return true;
    	}
    	else if( request.getServletPath().equals("/codecheck")) {
    		return true;
    	}    	
    	else if( request.getServletPath().equals("/login")) {
    		return true;
    	}
    	else if(request.getRequestURI().startsWith("/emailcheck/")) {
    		return true;
    	}
    	else if(request.getRequestURI().startsWith("/mailsender/")) {
    		return true;
    	}
    	else if(request.getRequestURI().startsWith("/swagger-ui/")) {
    		return true;
    	}
    	else if(request.getRequestURI().startsWith("/v3/")) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

}
