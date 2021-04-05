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

package org.springframework.transaction.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * 启用Spring注解式事务管理能力，类似于Spring的XML命名空间中的支持。其被使用在
 * {@link org.springframework.context.annotation.Configuration @Configuration}类中。
 * Enables Spring's annotation-driven transaction management capability, similar to
 * the support found in Spring's {@code <tx:*>} XML namespace. To be used on
 * {@link org.springframework.context.annotation.Configuration @Configuration}
 * classes as follows:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableTransactionManagement
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public FooRepository fooRepository() {
 *         // configure and return a class having &#064;Transactional methods
 *         return new JdbcFooRepository(dataSource());
 *     }
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // configure and return the necessary JDBC DataSource
 *     }
 *
 *     &#064;Bean
 *     public PlatformTransactionManager txManager() {
 *         return new DataSourceTransactionManager(dataSource());
 *     }
 * }</pre>
 *
 * <p>For reference, the example above can be compared to the following Spring XML
 * configuration:
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;tx:annotation-driven/&gt;
 *
 *     &lt;bean id="fooRepository" class="com.foo.JdbcFooRepository"&gt;
 *         &lt;constructor-arg ref="dataSource"/&gt;
 *     &lt;/bean&gt;
 *
 *     &lt;bean id="dataSource" class="com.vendor.VendorDataSource"/&gt;
 *
 *     &lt;bean id="transactionManager" class="org.sfwk...DataSourceTransactionManager"&gt;
 *         &lt;constructor-arg ref="dataSource"/&gt;
 *     &lt;/bean&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * 在以上两种情况中，{@code @EnableTransactionManagement}和{@code <tx:annotation-driven/>}
 * 负责注册需要的Spring支持事务管理驱动的组件，例如TransactionInterceptor和基于代理或者AspectJ的advice，
 * 这些组件在调用{@code @Transactional}时，会将拦截器编织到调用栈中。
 * In both of the scenarios above, {@code @EnableTransactionManagement} and {@code
 * <tx:annotation-driven/>} are responsible for registering the necessary Spring
 * components that power annotation-driven transaction management, such as the
 * TransactionInterceptor and the proxy- or AspectJ-based advice that weave the
 * interceptor into the call stack when {@code JdbcFooRepository}'s {@code @Transactional}
 * methods are invoked.
 *
 * 两个例子的一个微小的区别是{@code PlatformTransactionManager}bean的命名：在{@code @Bean}中，
 * bean的名字是txManager(根据方法的名称)；在XML中，命名是transactionManager。
 * {@code <tx:annotation-driven/>}硬连接，默认情况下查找名为"transactionManager"的bean，
 * 然而{@code @EnableTransactionManagement}是更加灵活的，其将在容器中查找任何
 * {@code PlatformTransactionManager}类型的bean。
 * <p>A minor difference between the two examples lies in the naming of the {@code
 * PlatformTransactionManager} bean: In the {@code @Bean} case, the name is
 * <em>"txManager"</em> (per the name of the method); in the XML case, the name is
 * <em>"transactionManager"</em>. The {@code <tx:annotation-driven/>} is hard-wired to
 * look for a bean named "transactionManager" by default, however
 * {@code @EnableTransactionManagement} is more flexible; it will fall back to a by-type
 * lookup for any {@code PlatformTransactionManager} bean in the container. Thus the name
 * can be "txManager", "transactionManager", or "tm": it simply does not matter.
 *
 * 对于那些希望在{@code @EnableTransactionManagement}与要使用的确切事务管理器之间建立更加直接的联系的人，可以实现
 * {@link TransactionManagementConfigurer}回调接口，注意{@code implements}子句和{@code @Override}下方的注释方法：
 * <p>For those that wish to establish a more direct relationship between
 * {@code @EnableTransactionManagement} and the exact transaction manager bean to be used,
 * the {@link TransactionManagementConfigurer} callback interface may be implemented -
 * notice the {@code implements} clause and the {@code @Override}-annotated method below:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableTransactionManagement
 * public class AppConfig implements TransactionManagementConfigurer {
 *
 *     &#064;Bean
 *     public FooRepository fooRepository() {
 *         // configure and return a class having &#064;Transactional methods
 *         return new JdbcFooRepository(dataSource());
 *     }
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // configure and return the necessary JDBC DataSource
 *     }
 *
 *     &#064;Bean
 *     public PlatformTransactionManager txManager() {
 *         return new DataSourceTransactionManager(dataSource());
 *     }
 *
 *     &#064;Override
 *     public PlatformTransactionManager annotationDrivenTransactionManager() {
 *         return txManager();
 *     }
 * }</pre>
 *
 * 这个方法更明确，所以可能需要使用该方法，也可能需要使用该方法区分存在于同一容器中的两个
 * {@code PlatformTransactionManager}bean。顾名思义，{@code annotationDrivenTransactionManager()}
 * 是用于处理{@code @Transactional}的方法。
 * This approach may be desirable simply because it is more explicit, or it may be
 * necessary in order to distinguish between two {@code PlatformTransactionManager} beans
 * present in the same container.  As the name suggests, the
 * {@code annotationDrivenTransactionManager()} will be the one used for processing
 * {@code @Transactional} methods. See {@link TransactionManagementConfigurer} Javadoc
 * for further details.
 *
 * {@link #mode}属性控制advice如何应用：若mode是{@link AdviceMode#PROXY}(默认)，那么
 * 其他属性控制代理的行为。请注意，代理模式仅允许通过代理拦截器呼叫，同一类中的本地调用无法被拦截。
 * <p>The {@link #mode} attribute controls how advice is applied: If the mode is
 * {@link AdviceMode#PROXY} (the default), then the other attributes control the behavior
 * of the proxying. Please note that proxy mode allows for interception of calls through
 * the proxy only; local calls within the same class cannot get intercepted that way.
 *
 * 注意，若{@linkplain #mode}设置为{@link AdviceMode#ASPECTJ}，那么{@link #proxyTargetClass}属性
 * 值将会被忽略。也要注意，在这种情况下必需在类路径上使用{@code spring-aspects}模块JAR，并使用编译时
 * 织入或者加载时织入将切面应用到受影响的类。
 * <p>Note that if the {@linkplain #mode} is set to {@link AdviceMode#ASPECTJ}, then the
 * value of the {@link #proxyTargetClass} attribute will be ignored. Note also that in
 * this case the {@code spring-aspects} module JAR must be present on the classpath, with
 * compile-time weaving or load-time weaving applying the aspect to the affected classes.
 * There is no proxy involved in such a scenario; local calls will be intercepted as well.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see TransactionManagementConfigurer
 * @see TransactionManagementConfigurationSelector
 * @see ProxyTransactionManagementConfiguration
 * @see org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TransactionManagementConfigurationSelector.class)
public @interface EnableTransactionManagement {

	/**
	 * 表明是否创建基于类的代理(CGLIB)({@code true})，而不是标准的基于Java接口的代理({@code false})。
	 * 仅仅在{@link #mode()}是{@link AdviceMode#PROXY}应用。
	 * Indicate whether subclass-based (CGLIB) proxies are to be created ({@code true}) as
	 * opposed to standard Java interface-based proxies ({@code false}). The default is
	 * {@code false}. <strong>Applicable only if {@link #mode()} is set to
	 * {@link AdviceMode#PROXY}</strong>.
	 * 注意，设置该属性为{@code true}将会影响所有Spring管理的需要代理的bean，不仅仅是{@code @Transactional}
	 * 标记的bean。例如，其他使用Spring的{@code @Async}注解标记的bean将使用CGLIB代理。
	 * <p>Note that setting this attribute to {@code true} will affect <em>all</em>
	 * Spring-managed beans requiring proxying, not just those marked with
	 * {@code @Transactional}. For example, other beans marked with Spring's
	 * {@code @Async} annotation will be upgraded to subclass proxying at the same
	 * time. This approach has no negative impact in practice unless one is explicitly
	 * expecting one type of proxy vs another, e.g. in tests.
	 */
	boolean proxyTargetClass() default false;

	/**
	 * 表明事物advice如何应用。默认是{@link AdviceMode#PROXY}。
	 * Indicate how transactional advice should be applied.
	 * <p><b>The default is {@link AdviceMode#PROXY}.</b>
	 * 请注意，代理模式仅允许通过拦截器调用，在同一类中的本地调用无法被拦截。
	 * 在本地调用方法上的{@link Transactional}注解将会被忽略，因为在这种场景下，Spring的拦截器不会启动。
	 * Please note that proxy mode allows for interception of calls through the proxy
	 * only. Local calls within the same class cannot get intercepted that way; an
	 * {@link Transactional} annotation on such a method within a local call will be
	 * ignored since Spring's interceptor does not even kick in for such a runtime
	 * scenario. For a more advanced mode of interception, consider switching this to
	 * {@link AdviceMode#ASPECTJ}.
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * 表明当在特定的连接点上应用多个advice时，事物Advice执行的顺序
	 * Indicate the ordering of the execution of the transaction advisor
	 * when multiple advices are applied at a specific joinpoint.
	 * <p>The default is {@link Ordered#LOWEST_PRECEDENCE}.
	 */
	int order() default Ordered.LOWEST_PRECEDENCE;

}
