/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.support;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.lang.Nullable;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 工具类，便于访问当前请求在{@code DispatcherServlet}中指定的状态。支持
 * 查找当前的ApplicationContext、localeResolver、Local、ThemeResolver、Theme、MultipartResolver。
 * Utility class for easy access to request-specific state which has been
 * set by the {@link org.springframework.web.servlet.DispatcherServlet}.
 *
 * <p>Supports lookup of current WebApplicationContext, LocaleResolver,
 * Locale, ThemeResolver, Theme, and MultipartResolver.
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 03.03.2003
 * @see RequestContext
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public abstract class RequestContextUtils {

	/**
	 * 用于在{@link RequestDataValueProcessor}的实现中查找bean的名称。
	 * The name of the bean to use to look up in an implementation of
	 * {@link RequestDataValueProcessor} has been configured.
	 * @since 4.2.1
	 */
	public static final String REQUEST_DATA_VALUE_PROCESSOR_BEAN_NAME = "requestDataValueProcessor";


	/**
	 * 查找与初始化请求处理的DispatcherServlet关联的WebApplicationContext，如果没有找到，则查找全局上下文。
	 * 通过ServletContext或者ContextLoader查找全局上下文。
	 * Look for the WebApplicationContext associated with the DispatcherServlet
	 * that has initiated request processing, and for the global context if none
	 * was found associated with the current request. The global context will
	 * be found via the ServletContext or via ContextLoader's current context.
	 * <p>NOTE: This variant remains compatible with Servlet 2.5, explicitly
	 * checking a given ServletContext instead of deriving it from the request.
	 * @param request current HTTP request
	 * @param servletContext current servlet context
	 * @return the request-specific WebApplicationContext, or the global one
	 * if no request-specific context has been found, or {@code null} if none
	 * @since 4.2.1
	 * @see DispatcherServlet#WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 * @see WebApplicationContextUtils#getWebApplicationContext(ServletContext)
	 * @see ContextLoader#getCurrentWebApplicationContext()
	 */
	@Nullable
	public static WebApplicationContext findWebApplicationContext(
			HttpServletRequest request, @Nullable ServletContext servletContext) {

		WebApplicationContext webApplicationContext = (WebApplicationContext) request.getAttribute(
				DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (webApplicationContext == null) {
			if (servletContext != null) {
				webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			}
			if (webApplicationContext == null) {
				webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
			}
		}
		return webApplicationContext;
	}

	/**
	 * Look for the WebApplicationContext associated with the DispatcherServlet
	 * that has initiated request processing, and for the global context if none
	 * was found associated with the current request. The global context will
	 * be found via the ServletContext or via ContextLoader's current context.
	 * <p>NOTE: This variant requires Servlet 3.0+ and is generally recommended
	 * for forward-looking custom user code.
	 * @param request current HTTP request
	 * @return the request-specific WebApplicationContext, or the global one
	 * if no request-specific context has been found, or {@code null} if none
	 * @since 4.2.1
	 * @see #findWebApplicationContext(HttpServletRequest, ServletContext)
	 * @see ServletRequest#getServletContext()
	 * @see ContextLoader#getCurrentWebApplicationContext()
	 */
	@Nullable
	public static WebApplicationContext findWebApplicationContext(HttpServletRequest request) {
		return findWebApplicationContext(request, request.getServletContext());
	}

	/**
	 * 返回已经被DispatcherServlet绑定到request中的LocaleResolver
	 * Return the LocaleResolver that has been bound to the request by the
	 * DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current LocaleResolver, or {@code null} if not found
	 */
	@Nullable
	public static LocaleResolver getLocaleResolver(HttpServletRequest request) {
		return (LocaleResolver) request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
	}

	/**
	 * 检索给定请求的当前语言环境，使用DispatcherServlet(若可用)绑定到request的LocaleResolver，
	 * 回退到请求的accept-header语言环境。该方法是标准Servlet{@link javax.servlet.http.HttpServletRequest#getLocale()}的
	 * 替代方法，若找不到更具体的语言环境，则使用后者。考虑使用{@code LocaleContextHolder#getLocale()}，其会填充相同的区域设置。
	 * Retrieve the current locale from the given request, using the
	 * LocaleResolver bound to the request by the DispatcherServlet
	 * (if available), falling back to the request's accept-header Locale.
	 * <p>This method serves as a straightforward alternative to the standard
	 * Servlet {@link javax.servlet.http.HttpServletRequest#getLocale()} method,
	 * falling back to the latter if no more specific locale has been found.
	 * <p>Consider using {@link org.springframework.context.i18n.LocaleContextHolder#getLocale()}
	 * which will normally be populated with the same Locale.
	 * @param request current HTTP request
	 * @return the current locale for the given request, either from the
	 * LocaleResolver or from the plain request itself
	 * @see #getLocaleResolver
	 * @see org.springframework.context.i18n.LocaleContextHolder#getLocale()
	 */
	public static Locale getLocale(HttpServletRequest request) {
		LocaleResolver localeResolver = getLocaleResolver(request);
		return (localeResolver != null ? localeResolver.resolveLocale(request) : request.getLocale());
	}

	/**
	 * 从当前request中检索当前时区，使用DispatcherServlet绑定到request中的
	 * {@code TimeZoneAwareLocaleResolver}(若可用)，回退到系统的默认时区。
	 * 注意: 若request未指定时区，则该方法返回{@code null}。这与{@link #getLocale}
	 * 形成对比，后者总是有请求的接受头语言环境要回退。
	 * 考虑使用{@link org.springframework.context.i18n.LocaleContextHolder#getTimeZone()},
	 * 其通常会填充相同的时区。若未提供{@code LocaleResolver}则该方法仅在回退到系统时区方面有所不同。
	 * Retrieve the current time zone from the given request, using the
	 * TimeZoneAwareLocaleResolver bound to the request by the DispatcherServlet
	 * (if available), falling back to the system's default time zone.
	 * <p>Note: This method returns {@code null} if no specific time zone can be
	 * resolved for the given request. This is in contrast to {@link #getLocale}
	 * where there is always the request's accept-header locale to fall back to.
	 * <p>Consider using {@link org.springframework.context.i18n.LocaleContextHolder#getTimeZone()}
	 * which will normally be populated with the same TimeZone: That method only
	 * differs in terms of its fallback to the system time zone if the LocaleResolver
	 * hasn't provided a specific time zone (instead of this method's {@code null}).
	 * @param request current HTTP request
	 * @return the current time zone for the given request, either from the
	 * TimeZoneAwareLocaleResolver or {@code null} if none associated
	 * @see #getLocaleResolver
	 * @see org.springframework.context.i18n.LocaleContextHolder#getTimeZone()
	 */
	@Nullable
	public static TimeZone getTimeZone(HttpServletRequest request) {
		LocaleResolver localeResolver = getLocaleResolver(request);
		if (localeResolver instanceof LocaleContextResolver) {
			LocaleContext localeContext = ((LocaleContextResolver) localeResolver).resolveLocaleContext(request);
			if (localeContext instanceof TimeZoneAwareLocaleContext) {
				return ((TimeZoneAwareLocaleContext) localeContext).getTimeZone();
			}
		}
		return null;
	}

	/**
	 * Return the ThemeResolver that has been bound to the request by the
	 * DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current ThemeResolver, or {@code null} if not found
	 */
	@Nullable
	public static ThemeResolver getThemeResolver(HttpServletRequest request) {
		return (ThemeResolver) request.getAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE);
	}

	/**
	 * Return the ThemeSource that has been bound to the request by the
	 * DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current ThemeSource
	 */
	@Nullable
	public static ThemeSource getThemeSource(HttpServletRequest request) {
		return (ThemeSource) request.getAttribute(DispatcherServlet.THEME_SOURCE_ATTRIBUTE);
	}

	/**
	 * Retrieves the current theme from the given request, using the ThemeResolver
	 * and ThemeSource bound to the request by the DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current theme, or {@code null} if not found
	 * @see #getThemeResolver
	 */
	@Nullable
	public static Theme getTheme(HttpServletRequest request) {
		ThemeResolver themeResolver = getThemeResolver(request);
		ThemeSource themeSource = getThemeSource(request);
		if (themeResolver != null && themeSource != null) {
			String themeName = themeResolver.resolveThemeName(request);
			return themeSource.getTheme(themeName);
		}
		else {
			return null;
		}
	}

	/**
	 * 在重定向之前从请求中返回只读的input flash属性。
	 * Return read-only "input" flash attributes from request before redirect.
	 * @param request current request
	 * @return a read-only Map, or {@code null} if not found
	 * @see FlashMap
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static Map<String, ?> getInputFlashMap(HttpServletRequest request) {
		return (Map<String, ?>) request.getAttribute(DispatcherServlet.INPUT_FLASH_MAP_ATTRIBUTE);
	}

	/**
	 * Return "output" FlashMap to save attributes for request after redirect.
	 * @param request current request
	 * @return a {@link FlashMap} instance, never {@code null} within a
	 * {@code DispatcherServlet}-handled request
	 */
	public static FlashMap getOutputFlashMap(HttpServletRequest request) {
		return (FlashMap) request.getAttribute(DispatcherServlet.OUTPUT_FLASH_MAP_ATTRIBUTE);
	}

	/**
	 * Return the {@code FlashMapManager} instance to save flash attributes.
	 * <p>As of 5.0 the convenience method {@link #saveOutputFlashMap} may be
	 * used to save the "output" FlashMap.
	 * @param request the current request
	 * @return a {@link FlashMapManager} instance, never {@code null} within a
	 * {@code DispatcherServlet}-handled request
	 */
	@Nullable
	public static FlashMapManager getFlashMapManager(HttpServletRequest request) {
		return (FlashMapManager) request.getAttribute(DispatcherServlet.FLASH_MAP_MANAGER_ATTRIBUTE);
	}

	/**
	 * Convenience method that retrieves the {@link #getOutputFlashMap "output"
	 * FlashMap}, updates it with the path and query params of the target URL,
	 * and then saves it using the {@link #getFlashMapManager FlashMapManager}.
	 * @param location the target URL for the redirect
	 * @param request the current request
	 * @param response the current response
	 * @since 5.0
	 */
	public static void saveOutputFlashMap(String location, HttpServletRequest request, HttpServletResponse response) {
		FlashMap flashMap = getOutputFlashMap(request);
		if (CollectionUtils.isEmpty(flashMap)) {
			return;
		}

		UriComponents uriComponents = UriComponentsBuilder.fromUriString(location).build();
		flashMap.setTargetRequestPath(uriComponents.getPath());
		flashMap.addTargetRequestParams(uriComponents.getQueryParams());

		FlashMapManager manager = getFlashMapManager(request);
		Assert.state(manager != null, "No FlashMapManager. Is this a DispatcherServlet handled request?");
		manager.saveOutputFlashMap(flashMap, request, response);
	}

}
