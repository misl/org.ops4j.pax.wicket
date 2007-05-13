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
package org.ops4j.pax.wicket.samples.departmentstore.view.floor.internal;

import java.util.ArrayList;
import java.util.List;
import org.ops4j.pax.wicket.api.ContentAggregator;
import org.ops4j.pax.wicket.samples.departmentstore.model.DepartmentStore;
import org.ops4j.pax.wicket.samples.departmentstore.model.Floor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class Activator
    implements BundleActivator
{

    private final List<ServiceRegistration> m_registrations;
    private final List<ContentAggregator> m_floors;

    public Activator()
    {
        m_registrations = new ArrayList<ServiceRegistration>();
        m_floors = new ArrayList<ContentAggregator>();
    }

    public void start( BundleContext bundleContext )
        throws Exception
    {
        String depStoreServiceName = DepartmentStore.class.getName();
        ServiceReference depStoreServiceReference = bundleContext.getServiceReference( depStoreServiceName );
        DepartmentStore departmentStore = (DepartmentStore) bundleContext.getService( depStoreServiceReference );

        List<Floor> floors = departmentStore.getFloors();
        String destinationId = "swp.floor";
        for( Floor floor : floors )
        {
            String floorName = floor.getName();
            FloorAggregatedSource aggregatedSource = new FloorAggregatedSource(
                floor, floorName, destinationId, bundleContext, "departmentstore"
            );
            aggregatedSource.setDestination( destinationId );
            aggregatedSource.setAggregationPointName( floor.getName() );
            ServiceRegistration registration = aggregatedSource.register();

            m_registrations.add( registration );
            m_floors.add( aggregatedSource );
        }
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        for( ServiceRegistration registration : m_registrations )
        {
            registration.unregister();
        }

        m_registrations.clear();

        for( ContentAggregator floor : m_floors )
        {
            floor.dispose();
        }
        m_floors.clear();
    }
}
