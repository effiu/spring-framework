/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;

/**
 * 在异步请求处理后，使用回调方法扩展{@code HandlerInterceptor}.
 * Extends {@code HandlerInterceptor} with a callback method invoked after the
 * start of asynchronous request handling.
 *
 * 当处理程序开始异步请求时，{@link DispatcherServlet}会直接退出，不会调用{@code postHandle}和
 * {@code afterCompletion}，因为请求处理结果(例如: ModelAndView)可能还未完成，将改为调用{@link #afterConcurrentHandlingStarted}，
 * 以允许线程释放到Servlet容器之前执行清理线程绑定属性之类的任务.
 * <p>When a handler starts an asynchronous request, the {@link DispatcherServlet}
 * exits without invoking {@code postHandle} and {@code afterCompletion} as it
 * normally does for a synchronous request, since the result of request handling
 * (e.g. ModelAndView) is likely not yet ready and will be produced concurrently
 * from another thread. In such scenarios, {@link #afterConcurrentHandlingStarted}
 * is invoked instead, allowing implementations to perform tasks such as cleaning
 * up thread-bound attributes before releasing the thread to the Servlet container.
 *
 * 当异步处理完成后，请求被分派到容器去进行下一步的处理。在该阶段，{@code DispatcherServlet}调用
 * {@code preHandle}、{@code postHandle}和{@code afterCompletion}。为了在异步处理完成后区分
 * 初始请求和后续调度，拦截器可以检查{@link javax.servlet.ServletRequest}的{@code javax.servlet.DispatcherType}
 * 是{@code "REQUEST"}还是{@code "ASYNC"}.
 * <p>When asynchronous handling completes, the request is dispatched to the
 * container for further processing. At this stage the {@code DispatcherServlet}
 * invokes {@code preHandle}, {@code postHandle}, and {@code afterCompletion}.
 * To distinguish between the initial request and the subsequent dispatch
 * after asynchronous handling completes, interceptors can check whether the
 * {@code javax.servlet.DispatcherType} of {@link javax.servlet.ServletRequest}
 * is {@code "REQUEST"} or {@code "ASYNC"}.
 *
 * 注意当一个异步请求超时或者出现一个网络错误时，{@code HandlerInterceptor}实现类可能需要工作。
 * 这种情况下Servlet容器不在分发，因此{@code postHandle}和{@code afterCompletion}方法不再被调用.
 * 相反，拦截器可以通过{@link org.springframework.web.context.request.async.WebAsyncManager WebAsyncManager}
 * 上的{@code registerCallbackInterceptor}和{@code registerDeferredResultInterceptor}方法进行注册，以跟踪
 * 异步请求。无论是否开始异步请求处理，都可以对{@code preHandle}发出的每个请求进行主动处理.
 * <p>Note that {@code HandlerInterceptor} implementations may need to do work
 * when an async request times out or completes with a network error. For such
 * cases the Servlet container does not dispatch and therefore the
 * {@code postHandle} and {@code afterCompletion} methods will not be invoked.
 * Instead, interceptors can register to track an asynchronous request through
 * the {@code registerCallbackInterceptor} and {@code registerDeferredResultInterceptor}
 * methods on {@link org.springframework.web.context.request.async.WebAsyncManager
 * WebAsyncManager}. This can be done proactively on every request from
 * {@code preHandle} regardless of whether async request processing will start.
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 * @see org.springframework.web.context.request.async.WebAsyncManager
 * @see org.springframework.web.context.request.async.CallableProcessingInterceptor
 * @see org.springframework.web.context.request.async.DeferredResultProcessingInterceptor
 */
public interface AsyncHandlerInterceptor extends HandlerInterceptor {

	/**
	 * 当并发执行处理程序时，代替{@code postHandle}和{@code afterCompletion}方法.
	 * Called instead of {@code postHandle} and {@code afterCompletion}
	 * when the handler is being executed concurrently.
	 * 实现该方法可以用于提供的请求和响应，但是应该避免与处理程序的并发执行冲突的方式修改。
	 * 该方法的典型用法是清楚线程局部变量.
	 * <p>Implementations may use the provided request and response but should
	 * avoid modifying them in ways that would conflict with the concurrent
	 * execution of the handler. A typical use of this method would be to
	 * clean up thread-local variables.
	 * @param request the current request
	 * @param response the current response
	 * @param handler the handler (or {@link HandlerMethod}) that started async
	 * execution, for type and/or instance examination
	 * @throws Exception in case of errors
	 */
	default void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
	}

}
