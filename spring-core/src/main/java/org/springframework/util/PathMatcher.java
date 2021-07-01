/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.util;

import java.util.Comparator;
import java.util.Map;

/**
 * 基于{@code String}的路径匹配的策略接口。默认实现类是{@link AntPathMatcher}，支持Ant风格的模式语法。
 * Strategy interface for {@code String}-based path matching.
 *
 * <p>Used by {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver},
 * {@link org.springframework.web.servlet.handler.AbstractUrlHandlerMapping},
 * and {@link org.springframework.web.servlet.mvc.WebContentInterceptor}.
 *
 * <p>The default implementation is {@link AntPathMatcher}, supporting the
 * Ant-style pattern syntax.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see AntPathMatcher
 */
public interface PathMatcher {

	/**
	 * 给定的{@code path}是否表示可以与此接口的实现匹配的模式？若返回值为{@code false}，
	 * 则不必使用{@link #match}，因为静态路径字符串上直接进行相等比较可以得到相同的结果。
	 * Does the given {@code path} represent a pattern that can be matched
	 * by an implementation of this interface?
	 * <p>If the return value is {@code false}, then the {@link #match}
	 * method does not have to be used because direct equality comparisons
	 * on the static path Strings will lead to the same result.
	 * @param path the path to check
	 * @return {@code true} if the given {@code path} represents a pattern
	 */
	boolean isPattern(String path);

	/**
	 * 根据PathMatcher的匹配策略，讲给定的{@code path}路径与给定的{@code pattern}匹配。
	 * Match the given {@code path} against the given {@code pattern},
	 * according to this PathMatcher's matching strategy.
	 * @param pattern the pattern to match against
	 * @param path the path to test
	 * @return {@code true} if the supplied {@code path} matched,
	 * {@code false} if it didn't
	 */
	boolean match(String pattern, String path);

	/**
	 * 根据PathMatcher的匹配策略，将给定的{@code path}与给定的{@code pattern}的相应部分进行匹配。
	 * 确定pattern是否至少匹配给定的基本路径，假设完整路径也可能匹配。
	 * Match the given {@code path} against the corresponding part of the given
	 * {@code pattern}, according to this PathMatcher's matching strategy.
	 * <p>Determines whether the pattern at least matches as far as the given base
	 * path goes, assuming that a full path may then match as well.
	 * @param pattern the pattern to match against
	 * @param path the path to test
	 * @return {@code true} if the supplied {@code path} matched,
	 * {@code false} if it didn't
	 */
	boolean matchStart(String pattern, String path);

	/**
	 * 给定一个pattern和一个全路径，确定模式映射部分。
	 * 该方法应该通过实际的pattern找出path的哪一部分是动态匹配的，也就是说，其从给定的
	 * 完整路径中剥离静态定义的前导路径，仅返回路径中实际模式匹配的部分。
	 * 例如：对于"myroot/*.html"作为pattern和"myroot/myfile.html"作为完整路径，该方法应该返回myfile.html。
	 * 该PathMatcher的匹配策略指定了详细的判断规则。
	 * 一个简单的实现可以在实际模式的情况下按原样返回给定的完整路径，
	 * 在模式不包含任何动态部分的情况下返回空字符串(即{@code pattern})参数是一个静态路径。
	 * 复杂的实现将会区分给定路径模式的静态和动态部分。
	 * Given a pattern and a full path, determine the pattern-mapped part.
	 * <p>This method is supposed to find out which part of the path is matched
	 * dynamically through an actual pattern, that is, it strips off a statically
	 * defined leading path from the given full path, returning only the actually
	 * pattern-matched part of the path.
	 * <p>For example: For "myroot/*.html" as pattern and "myroot/myfile.html"
	 * as full path, this method should return "myfile.html". The detailed
	 * determination rules are specified to this PathMatcher's matching strategy.
	 * <p>A simple implementation may return the given full path as-is in case
	 * of an actual pattern, and the empty String in case of the pattern not
	 * containing any dynamic parts (i.e. the {@code pattern} parameter being
	 * a static path that wouldn't qualify as an actual {@link #isPattern pattern}).
	 * A sophisticated implementation will differentiate between the static parts
	 * and the dynamic parts of the given path pattern.
	 * @param pattern the path pattern
	 * @param path the full path to introspect
	 * @return the pattern-mapped part of the given {@code path}
	 * (never {@code null})
	 */
	String extractPathWithinPattern(String pattern, String path);

	/**
	 * @see org.springframework.web.bind.annotation.PathVariable。
	 * 给定一个pattern和一个全路径，提取URL模板变量。URL模板变量用"{"和"}"表示
	 * Given a pattern and a full path, extract the URI template variables. URI template
	 * variables are expressed through curly brackets ('{' and '}').
	 * <p>For example: For pattern "/hotels/{hotel}" and path "/hotels/1", this method will
	 * return a map containing "hotel"->"1".
	 * @param pattern the path pattern, possibly containing URI templates
	 * @param path the full path to extract template variables from
	 * @return a map, containing variable names as keys; variables values as values
	 */
	Map<String, String> extractUriTemplateVariables(String pattern, String path);

	/**
	 * 给定一个full path，返回一个{@link Comparator}适合按照该路径的显式顺序对pattern对象排序。
	 * 默认式{@link AntPathMatcher}。
	 * Given a full path, returns a {@link Comparator} suitable for sorting patterns
	 * in order of explicitness for that path.
	 * <p>The full algorithm used depends on the underlying implementation,
	 * but generally, the returned {@code Comparator} will
	 * {@linkplain java.util.List#sort(java.util.Comparator) sort}
	 * a list so that more specific patterns come before generic patterns.
	 * @param path the full path to use for comparison
	 * @return a comparator capable of sorting patterns in order of explicitness
	 */
	Comparator<String> getPatternComparator(String path);

	/**
	 * 将两个pattern组合为一个新的pattern。
	 * 用于组合这两种pattern的完整算法取决于底层实现。
	 * Combines two patterns into a new pattern that is returned.
	 * <p>The full algorithm used for combining the two pattern depends on the underlying implementation.
	 * @param pattern1 the first pattern
	 * @param pattern2 the second pattern
	 * @return the combination of the two patterns
	 * @throws IllegalArgumentException when the two patterns cannot be combined
	 */
	String combine(String pattern1, String pattern2);

}
