package com.example.bankomat.security;

import com.example.bankomat.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.NestedServletException;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.AccessDeniedException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    final JwtProvider jwtProvider;
    final AuthService authService;
    String[] urls=new String[]{
            "/api/auth/**",
            "/api/operation/input/**",
            "/api/operation/output/**",
            "/api/operation/check"
    };

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) {
        try {
            for (String url : urls) {
                int index = url.indexOf("*");
                if(index==-1){
                    if(req.getRequestURI().equals(url)){
                        doFilter(req,res,filterChain);
                        return;
                    }
                }else {
                    if(req.getRequestURI().startsWith(url.substring(0, index))){
                        doFilter(req,res,filterChain);
                        return;
                    }
                }

            }

            String authorization = req.getHeader("Authorization");
            if(authorization==null || authorization.length()<=7){
                throw new AccessDeniedException("");
            }
            String token = authorization.split(" ")[1];
            if (jwtProvider.validateToken(token)) {
                UserDetails userDetails = authService.loadUserByUsername(jwtProvider.getUsernameFromToken(token));
                if(userDetails==null){
                    throw new AccessDeniedException("");
                }
                UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(
                        userDetails, userDetails.getPassword(), userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }else {
                res.setStatus(403);
                return;
            }

            doFilter(req,res,filterChain);
        }
        catch (AccessDeniedException | NestedServletException e){
            res.setStatus(403);
        }
        catch (Exception e){
            e.printStackTrace();
            res.setStatus(500);
        }

    }

}
