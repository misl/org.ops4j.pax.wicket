/*
 * Copyright 2006 Niclas Hedhman.
 * Copyright 2006 Edward F. Yakop
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ops4j.pax.wicket.samples.departmentstore.view.internal;

import org.ops4j.pax.wicket.samples.departmentstore.view.OverviewPage;
import org.ops4j.pax.wicket.service.ContentContainer;
import org.ops4j.pax.wicket.service.DefaultPageContainer;
import org.ops4j.pax.wicket.service.PaxWicketApplicationFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import wicket.IPageFactory;

/**
 * @since 1.0.0
 */
public class Activator
    implements BundleActivator
{

    private ContentContainer m_store;
    private ServiceRegistration m_serviceRegistration;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        m_store = new DefaultPageContainer( bundleContext, "swp", "departmentstore" );
        IPageFactory factory = new OverviewPageFactory( bundleContext, m_store );
        String mountPoint = "swp";
        PaxWicketApplicationFactory applicationFactory =
            new PaxWicketApplicationFactory( bundleContext, factory, OverviewPage.class, mountPoint );
        m_serviceRegistration = applicationFactory.register();
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        m_serviceRegistration.unregister();
        m_store.dispose();
    }

}