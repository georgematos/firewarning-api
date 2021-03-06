package com.fortalezasec.firewarning.resources.Interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fortalezasec.firewarning.customexceptions.UriMalFormadaException;

import org.springframework.web.servlet.HandlerInterceptor;

public class EmpresaControllerInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    
    // Valida o formato da URI de consulta de Incidentes
    if (request.getRequestURI().toString().equals("/empresas/incidentes") && !request.getParameterMap().isEmpty())
      if (request.getParameter("tipo") == null || request.getParameter("valor") == null)
        throw new UriMalFormadaException("Um parametro obrigatório não foi informado");
    return true;

  }

}
