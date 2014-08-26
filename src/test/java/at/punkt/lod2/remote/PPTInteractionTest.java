package at.punkt.lod2.remote;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.ExpectedCountReached;
import at.punkt.lod2.util.Helper;
import com.jayway.awaitility.Awaitility;
import eu.lod2.rsine.dissemination.messageformatting.ToStringBindingSetFormatter;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.service.RsineController;
import eu.lod2.util.Namespaces;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.rio.RDFHandlerException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This test needs a running PPT instance hosting a project named "thesaurus" on localhost with an rsine
 * service configured to be running also on localhost. Make also sure to have the PoolPartyTuckeyForwarder installed
 * in a version that supports rsine configuration properties
 */
public class PPTInteractionTest {

    private RsineController remotePptRsineInstance, localRsineInstance;
    private AbstractApplicationContext remoteContext;
    private Helper localHelper;
    private CountingNotifier countingNotifier;

    @Before
    public void setUp() throws IOException {
        remoteContext = new ClassPathXmlApplicationContext("/at/punkt/lod2/remote/PPTInteractionTest-PPTcontext.xml");

        AbstractApplicationContext localContext = new ClassPathXmlApplicationContext("/at/punkt/lod2/remote/PPTInteractionTest-localContext.xml");
        localRsineInstance = localContext.getBean(RsineController.class);

        subscribeForRemoteReferencesAtRemoteRsine();
    }

    private void subscribeForRemoteReferencesAtRemoteRsine() {
        Subscription subscription = new Subscription();
        subscription.addQuery(new NotificationQuery(createMappingQuery(), new ToStringBindingSetFormatter(), subscription));
        subscription.addNotifier(countingNotifier = new CountingNotifier());

        RegistrationService remoteRegistrationService = remoteContext.getBean(
                "remoteRegistrationService",
                RegistrationService.class);
        remoteRegistrationService.register(subscription, false);
    }

    private String createMappingQuery() {
        return Namespaces.SKOS_PREFIX+
                Namespaces.CS_PREFIX+
                Namespaces.DCTERMS_PREFIX+
                "SELECT * " +
                "WHERE {" +
                    "?cs a cs:ChangeSet . " +
                    "?cs cs:createdDate ?csdate . " +
                    "?cs cs:addition ?addition . " +

                    "?addition rdf:subject ?concept . " +
                    "?addition rdf:predicate skos:exactMatch . " +
                    "?addition rdf:object ?mappedConcept . "+

                    "FILTER (?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                "}";
    }

    @Ignore
    @Test
    public void notifyRemotePPT() throws IOException, RDFHandlerException {
        referenceRemoteConcept();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).until(new ExpectedCountReached(countingNotifier, 1));
    }

    private void referenceRemoteConcept() throws IOException, RDFHandlerException {
        /*
        localHelper.postStatementAdded(new StatementImpl(
            new URIImpl("http://localhost/myThesaurus/myLocalConcept"),
            new URIImpl(Namespaces.SKOS_NAMESPACE.getName() + "exactMatch"),
            new URIImpl("http://localhost/thesaurus/3")
        ));
        */
    }

}
