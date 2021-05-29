/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.beans.factory;

/**
 * 想要在Bean工厂中知道Bean名称的Bean实现的接口。
 * 注意: 通常不推荐对象依赖其beanName，因为这表示对外部配置的潜在脆弱依赖，以及对Spring的不必要依赖。
 * Interface to be implemented by beans that want to be aware of their
 * bean name in a bean factory. Note that it is not usually recommended
 * that an object depends on its bean name, as this represents a potentially
 * brittle dependence on external configuration, as well as a possibly
 * unnecessary dependence on a Spring API.
 *
 * 有关Bean所有生命周期方法的列表，见{@link BeanFactory BeanFactory javadocs}。
 * <p>For a list of all bean lifecycle methods, see the
 * {@link BeanFactory BeanFactory javadocs}.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 01.11.2003
 * @see BeanClassLoaderAware
 * @see BeanFactoryAware
 * @see InitializingBean
 */
public interface BeanNameAware extends Aware {

	/**
	 * 在创建该bean的bean工厂中设置Bean的名称。
	 * 在填充正常bean属性后，初始化回调之前调用。
	 * 注意：该name是Bean工厂中实际的bean名称，其也许与原始的名称不同，特别是内部bean名称，
	 * 实际的bean名称可能已经通过附加"#"后缀变得唯一。
	 * 使用{@link BeanFactoryUtils#originalBeanName(String)}方法确定原始的bean名称。
	 * Set the name of the bean in the bean factory that created this bean.
	 * <p>Invoked after population of normal bean properties but before an
	 * init callback such as {@link InitializingBean#afterPropertiesSet()}
	 * or a custom init-method.
	 * @param name the name of the bean in the factory.
	 * Note that this name is the actual bean name used in the factory, which may
	 * differ from the originally specified name: in particular for inner bean
	 * names, the actual bean name might have been made unique through appending
	 * "#..." suffixes. Use the {@link BeanFactoryUtils#originalBeanName(String)}
	 * method to extract the original bean name (without suffix), if desired.
	 */
	void setBeanName(String name);

}
