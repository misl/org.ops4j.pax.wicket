
/**
 * Copyright OPS4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author nmw
 * @version $Id: $Id
 */
package org.ops4j.pax.wicket.internal.injection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebSession;
import org.ops4j.pax.wicket.api.PaxWicketInjector;
public abstract class AbstractPaxWicketInjector implements PaxWicketInjector {

    /**
     * <p>getSingleLevelOfFields.</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @return a {@link java.util.List} object.
     */
    protected List<Field> getSingleLevelOfFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            fields.add(field);
        }
        return fields;
    }

    /**
     * <p>getFields.</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @return a {@link java.util.List} object.
     */
    protected List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();

        while (clazz != null && !isBoundaryClass(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Inject.class)) {
                    continue;
                }
                fields.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * <p>isBoundaryClass.</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @return a boolean.
     */
    protected boolean isBoundaryClass(Class<?> clazz) {
        if (clazz.equals(WebPage.class) || clazz.equals(Page.class) || clazz.equals(Panel.class)
                || clazz.equals(MarkupContainer.class) || clazz.equals(Component.class)
                || clazz.equals(AuthenticatedWebSession.class) || clazz.equals(WebSession.class)
                || clazz.equals(Session.class) || clazz.equals(Object.class)) {
            return true;
        }
        return false;
    }

    /**
     * <p>setField.</p>
     *
     * @param component a {@link java.lang.Object} object.
     * @param field a {@link java.lang.reflect.Field} object.
     * @param proxy a {@link java.lang.Object} object.
     */
    protected void setField(Object component, Field field, Object proxy) {
        try {
            checkAccessabilityOfField(field);
            field.set(component, proxy);
        } catch (Exception e) {
            throw new RuntimeException("Bumm", e);
        }
    }

    private void checkAccessabilityOfField(Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * <p>getBeanType.</p>
     *
     * @param field a {@link java.lang.reflect.Field} object.
     * @return a {@link java.lang.Class} object.
     */
    protected Class<?> getBeanType(Field field) {
        Class<?> beanType = field.getType();
        return beanType;
    }

    /**
     * <p>countComponentContainPaxWicketBeanAnnotatedFieldsHierachical.</p>
     *
     * @param component a {@link java.lang.Class} object.
     * @return a int.
     */
    protected int countComponentContainPaxWicketBeanAnnotatedFieldsHierachical(Class<?> component) {
        Class<?> clazz = component;
        int numberOfInjectionFields = 0;
        while (clazz != null && !isBoundaryClass(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    numberOfInjectionFields++;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return numberOfInjectionFields;
    }

    /**
     * <p>countComponentContainPaxWicketBeanAnnotatedOneLevel.</p>
     *
     * @param component a {@link java.lang.Class} object.
     * @return a int.
     */
    protected int countComponentContainPaxWicketBeanAnnotatedOneLevel(Class<?> component) {
        Class<?> clazz = component;
        int numberOfInjectionFields = 0;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                numberOfInjectionFields++;
            }
        }
        return numberOfInjectionFields;
    }
}
