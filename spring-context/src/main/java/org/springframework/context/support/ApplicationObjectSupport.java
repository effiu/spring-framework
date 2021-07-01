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

package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 想要了解应用程序上下文的应用程序对象的方便超类，例如，用于协作bean的自定义查找
 * 或者特定于上下文的资源访问。它保存应用程序上下文的引用，且提供一个初始化回调方法。
 * 此外，还提供了许多方便的消息查找方法。
 * Convenient superclass for application objects that want to be aware of
 * the application context, e.g. for custom lookup of collaborating beans
 * or for context-specific resource access. It saves the application
 * context reference and provides an initialization callback method.
 * Furthermore, it offers numerous convenience methods for message lookup.
 *
 * 不需要对此类进行子类化：若想要访问上下文，其会变得容易一些。例如访问文件资源或者消息源。
 * 注意，许多应用程序对象不需要知道应用程序上下文，因为它们可以通过bean引用接受协作bean。
 * <p>There is no requirement to subclass this class: It just makes things
 * a little easier if you need access to the context, e.g. for access to
 * file resources or to the message source. Note that many application
 * objects do not need to be aware of the application context at all,
 * as they can receive collaborating beans via bean references.
 * 许多框架类都是从这个类派生的，特别是web中。
 * <p>Many framework classes are derived from this class, particularly
 * within the web support.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.context.support.WebApplicationObjectSupport
 */
public abstract class ApplicationObjectSupport implements ApplicationContextAware {

	/** Logger that is available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	/** ApplicationContext this object runs in. */
	@Nullable
	private ApplicationContext applicationContext;

	/** MessageSourceAccessor for easy message access. */
	@Nullable
	private MessageSourceAccessor messageSourceAccessor;


	@Override
	public final void setApplicationContext(@Nullable ApplicationContext context) throws BeansException {
		if (context == null && !isContextRequired()) {
			// Reset internal context state.
			this.applicationContext = null;
			this.messageSourceAccessor = null;
		}
		else if (this.applicationContext == null) {
			// Initialize with passed-in context.
			if (!requiredContextClass().isInstance(context)) {
				throw new ApplicationContextException(
						"Invalid application context: needs to be of type [" + requiredContextClass().getName() + "]");
			}
			this.applicationContext = context;
			this.messageSourceAccessor = new MessageSourceAccessor(context);
			initApplicationContext(context);
		}
		else {
			// Ignore reinitialization if same context passed in.
			if (this.applicationContext != context) {
				throw new ApplicationContextException(
						"Cannot reinitialize with different application context: current one is [" +
						this.applicationContext + "], passed-in one is [" + context + "]");
			}
		}
	}

	/**
	 * 判断是否应用对象需要在应用程序上下文运行。默认为false，可以重载强制要求在ApplicationContext中运行。
	 * Determine whether this application object needs to run in an ApplicationContext.
	 * <p>Default is "false". Can be overridden to enforce running in a context
	 * (i.e. to throw IllegalStateException on accessors if outside a context).
	 * @see #getApplicationContext
	 * @see #getMessageSourceAccessor
	 */
	protected boolean isContextRequired() {
		return false;
	}

	/**
	 * 确定传递给{@code setApplicationContext}的任何上下文必须是其实例的上下文类。可以在子类中覆盖重写。
	 * Determine the context class that any context passed to
	 * {@code setApplicationContext} must be an instance of.
	 * Can be overridden in subclasses.
	 * @see #setApplicationContext
	 */
	protected Class<?> requiredContextClass() {
		return ApplicationContext.class;
	}

	/**
	 * 子类可以重写该方法用于自定义初始化行为。
	 * 注意：不会在上下文重写初始化时被调用，而是在此对象的上下文引用的第一次初始化时被调用，默认调用重载的方法。
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called by {@code setApplicationContext} after setting the context instance.
	 * <p>Note: Does <i>not</i> get called on re-initialization of the context
	 * but rather just on first initialization of this object's context reference.
	 * <p>The default implementation calls the overloaded {@link #initApplicationContext()}
	 * method without ApplicationContext reference.
	 * @param context the containing ApplicationContext
	 * @throws ApplicationContextException in case of initialization errors
	 * @throws BeansException if thrown by ApplicationContext methods
	 * @see #setApplicationContext
	 */
	protected void initApplicationContext(ApplicationContext context) throws BeansException {
		initApplicationContext();
	}

	/**
	 * 子类重载该方法用于初始化。
	 * Subclasses can override this for custom initialization behavior.
	 * <p>The default implementation is empty. Called by
	 * {@link #initApplicationContext(org.springframework.context.ApplicationContext)}.
	 * @throws ApplicationContextException in case of initialization errors
	 * @throws BeansException if thrown by ApplicationContext methods
	 * @see #setApplicationContext
	 */
	protected void initApplicationContext() throws BeansException {
	}


	/**
	 * Return the ApplicationContext that this object is associated with.
	 * @throws IllegalStateException if not running in an ApplicationContext
	 */
	@Nullable
	public final ApplicationContext getApplicationContext() throws IllegalStateException {
		if (this.applicationContext == null && isContextRequired()) {
			throw new IllegalStateException(
					"ApplicationObjectSupport instance [" + this + "] does not run in an ApplicationContext");
		}
		return this.applicationContext;
	}

	/**
	 * Obtain the ApplicationContext for actual use.
	 * @return the ApplicationContext (never {@code null})
	 * @throws IllegalStateException in case of no ApplicationContext set
	 * @since 5.0
	 */
	protected final ApplicationContext obtainApplicationContext() {
		ApplicationContext applicationContext = getApplicationContext();
		Assert.state(applicationContext != null, "No ApplicationContext");
		return applicationContext;
	}

	/**
	 * Return a MessageSourceAccessor for the application context
	 * used by this object, for easy message access.
	 * @throws IllegalStateException if not running in an ApplicationContext
	 */
	@Nullable
	protected final MessageSourceAccessor getMessageSourceAccessor() throws IllegalStateException {
		if (this.messageSourceAccessor == null && isContextRequired()) {
			throw new IllegalStateException(
					"ApplicationObjectSupport instance [" + this + "] does not run in an ApplicationContext");
		}
		return this.messageSourceAccessor;
	}

}
