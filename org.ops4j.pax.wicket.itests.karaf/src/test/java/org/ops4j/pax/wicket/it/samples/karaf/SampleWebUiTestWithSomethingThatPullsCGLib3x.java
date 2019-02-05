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
 */
package org.ops4j.pax.wicket.it.samples.karaf;

import java.io.BufferedReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.inject.Inject;
import org.apache.karaf.features.BootFinished;

import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.wicket.samples.plain.simple.service.EchoService;
import org.osgi.framework.BundleContext;
import static shaded.org.apache.http.HttpHeaders.USER_AGENT;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SampleWebUiTestWithSomethingThatPullsCGLib3x {

    /**
     * WebApplicationFactory of the some of the applications we started. We
     * don't use these members, except for synchronizing the test. Injecting
     * them guarantees that the services are available before our test runs. The
     * timeouts are rather high for the benefit of our CI server.
     */
    protected static final String WEBUI_PORT = "8181";
    protected static final String LOG_LEVEL = "WARN";
    protected static final String SYMBOLIC_NAME_PAX_WICKET_SERVICE = "org.ops4j.pax.wicket.service";

    private static final int TIMEOUT = 120 * 1000;

    @Inject
    private BootFinished bootFinished;
    
    @Inject
    private BundleContext bundleContext;

    @Configuration
    public final Option[] configureAdditionalProvision() {

        MavenUrlReference wicketFeatureRepo = maven()
                .groupId("org.ops4j.pax.wicket").artifactId("paxwicket")
                .versionAsInProject().classifier("features").type("xml");

        MavenUrlReference paxwicketFeatureRepo = maven()
                .groupId("org.ops4j.pax.wicket").artifactId("features")
                .versionAsInProject().classifier("features").type("xml");
        MavenUrlReference karafSampleFeatureRepo = maven()
                .groupId("org.ops4j.pax.wicket.samples").artifactId("features")
                .versionAsInProject().classifier("features").type("xml");
        MavenUrlReference karafStandardRepo = maven()
                .groupId("org.apache.karaf.features").artifactId("standard").versionAsInProject().classifier("features").type("xml");

        MavenArtifactUrlReference karafUrl = maven()
                .groupId("org.apache.karaf").artifactId("apache-karaf")
                .version("4.2.2").type("zip");

        return new Option[]{
            KarafDistributionOption.debugConfiguration("5005", false),
            karafDistributionConfiguration()
            .frameworkUrl(karafUrl)
            .unpackDirectory(new File("target", "exam"))
            .useDeployFolder(false),
            keepRuntimeFolder(),
            /*logLevel(LogLevel.TRACE),*/
            configureConsole().ignoreLocalConsole(), logLevel(LogLevel.ERROR),
            features(karafStandardRepo, "scr"),
            features(karafStandardRepo, "webconsole"),
            provision(mavenBundle().groupId("org.slf4j").artifactId("slf4j-simple").versionAsInProject()),
            provision(mavenBundle().groupId("org.slf4j").artifactId("slf4j-api").versionAsInProject()),
            features(wicketFeatureRepo, "wicket"),
            features(paxwicketFeatureRepo, "pax-wicket"),
            features(karafSampleFeatureRepo, "wicket-samples-base"),
            features(karafSampleFeatureRepo, "wicket-samples-plain-simple"),
            //Pull in  cglib3.x
            mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib", "3.2.1_1"),};
    }

    @Test
    public void testNavigationApplicationShouldRender() throws Exception {

        String page = sendGet("http://localhost:" + WEBUI_PORT + "/navigation/");
        assertTrue(page.contains("Homepage linking all OPS4J samples"));
    }

    @Test
    public void testSamplePlainSimpleShouldRenderPage() throws Exception {
        String page = sendGet("http://localhost:" + WEBUI_PORT + "/plain/simple/");
        assertTrue(page.contains("Welcome to the most simple pax-wicket application"));
    }

    @Test
    public void checkInjectedPage() throws Exception {
        //Register a service here for later injection

        bundleContext.registerService(EchoService.class, new EchoServiceImplementation(), null);
        String page = sendGet("http://localhost:" + WEBUI_PORT + "/plain/inject/");
        assertTrue("/plain/inject/ failed to start properly", page.contains("Echo: Welcome to the most simple pax-wicket application"));

    }





    private String sendGet(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        return response.toString();

    }

    /**
     * Simple Echo Implementation for itest...
     */
    private final class EchoServiceImplementation implements EchoService {

        private static final long serialVersionUID = 6447679249771482700L;

        public String someEchoMethod(String toEcho) {
            return "Echo: " + toEcho;
        }
    }
}
