package at.punkt.lod2.local;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.changesetservice.PersistAndNotifyProvider;
import eu.lod2.rsine.dissemination.messageformatting.ToStringBindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.dissemination.notifier.logging.LoggingNotifier;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.junit.Before;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;

public abstract class LocalNotificationTest implements ApplicationContextAware  {

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private Repository changeSetRepo;

    protected ApplicationContext applicationContext;

    @Before
    public void setUp() throws IOException, RepositoryException, RDFParseException {
        changeSetRepo.getConnection().clear();
        registerUser();
    }

    private void registerUser() {
        Subscription subscription = new Subscription();
        subscription.addQuery(createQuery(), new ToStringBindingSetFormatter());
        subscription.addNotifier(getNotifier());
        registrationService.register(subscription, false);
    }

    protected INotifier getNotifier() {
        return new LoggingNotifier();
    }

    private String createQuery() {
        //preflabel changes of concepts
        return Namespaces.SKOS_PREFIX+
               Namespaces.CS_PREFIX+
               Namespaces.DCTERMS_PREFIX+
               "SELECT * " +
                    "WHERE {" +
                        "?cs a cs:ChangeSet . " +
                        "?cs cs:createdDate ?csdate . " +
                        "?cs cs:removal ?removal . " +
                        "?cs cs:addition ?addition . " +
                        "?removal rdf:subject ?concept . " +
                        "?addition rdf:subject ?concept . " +
                        "?removal rdf:predicate skos:prefLabel . " +
                        "?removal rdf:object ?oldLabel . "+
                        "?addition rdf:predicate skos:prefLabel . " +
                        "?addition rdf:object ?newLabel . "+
                        "FILTER (?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                    "}";
    }

    protected void postEditChanges() throws IOException {
        addConcept();
        setPrefLabel();
        changePrefLabel();
        addOtherConcept();
        linkConcepts();
    }

    private void addConcept() throws IOException {
        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel("http://reegle.info/glossary/1111",
                        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                        new URIImpl("http://www.w3.org/2004/02/skos/core#Concept"),
                        ChangeTripleHandler.CHANGETYPE_ADD),
                true);
    }

    private void setPrefLabel() throws IOException {
        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel("http://reegle.info/glossary/1111",
                        "http://www.w3.org/2004/02/skos/core#prefLabel",
                        new LiteralImpl("Ottakringer Helles", "en"),
                        ChangeTripleHandler.CHANGETYPE_ADD),
                true);
    }

    protected void changePrefLabel() throws IOException {
        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel("http://reegle.info/glossary/1111",
                        "http://www.w3.org/2004/02/skos/core#prefLabel",
                        new LiteralImpl("Ottakringer Helles", "en"),
                        "http://reegle.info/glossary/1111",
                        "http://www.w3.org/2004/02/skos/core#prefLabel",
                        new LiteralImpl("Schremser Edelmärzen", "en"),
                        ChangeTripleHandler.CHANGETYPE_UPDATE),
                true);
    }

    private void addOtherConcept() throws IOException {
        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel("http://reegle.info/glossary/1112",
                        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                        new URIImpl("http://www.w3.org/2004/02/skos/core#Concept"),
                        ChangeTripleHandler.CHANGETYPE_ADD),
                true);
    }

    private void linkConcepts() throws IOException {
        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel("http://reegle.info/glossary/1111",
                        "http://www.w3.org/2004/02/skos/core#related",
                        new URIImpl("http://reegle.info/glossary/1112"),
                        ChangeTripleHandler.CHANGETYPE_ADD),
                true);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
