package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.messageformatting.FormatterFactory;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.dissemination.notifier.NotifierDescriptor;
import eu.lod2.rsine.dissemination.notifier.NotifierParameters;
import eu.lod2.util.ItemNotFoundException;
import eu.lod2.util.Namespaces;
import org.openrdf.model.*;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;

import java.util.*;

public class SubscriptionParser {

    private ValueFactory valueFactory = new ValueFactoryImpl();
    private Model rdfSubscription;

    public SubscriptionParser(Model rdfSubscription) {
        this.rdfSubscription = rdfSubscription;
    }
    
    public Subscription createSubscription() {
        Subscription subscription = new Subscription();

        setId(subscription);
        setDescription(subscription);

        for (INotifier notifier : createNotifiers()) {
            subscription.addNotifier(notifier);
        }

        for (NotificationQuery notificationQuery : createNotificationQueries(subscription)) {
            subscription.addQuery(notificationQuery);
        }

        return subscription;
    }

    private void setId(Subscription subscription) {
        Set<Resource> subscriptionResources = rdfSubscription.filter(
            null,
            RDF.TYPE,
            valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "Subscription")).subjects();

        for (Resource subscriptionResource : subscriptionResources) {
            subscription.setId(subscriptionResource);
        }
    }

    private void setDescription(Subscription subscription) {
        Set<Value> descriptions = rdfSubscription.filter(null, DCTERMS.DESCRIPTION, null).objects();
        if (!descriptions.isEmpty()) {
            subscription.setDescription(descriptions.iterator().next().stringValue());
        }
    }

    private Collection<INotifier> createNotifiers() {
        Collection<INotifier> notifiers = new ArrayList<INotifier>();

        Set<Value> allNotifiers = rdfSubscription.filter(
            null,
            valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "notifier"),
            null).objects();

        for (Value notifier : allNotifiers) {
            URI type = rdfSubscription.filter((Resource) notifier, RDF.TYPE, null).objectURI();

            NotifierDescriptor notifierDescriptor = getDescriptorByType(type);
            NotifierParameters notifierParameters = setParameterValues((Resource) notifier, notifierDescriptor.getParameters());
            notifiers.add(notifierDescriptor.create(notifierParameters));
        }

        return notifiers;
    }

    private NotifierDescriptor getDescriptorByType(URI type) {
        ServiceLoader<NotifierDescriptor> loader = ServiceLoader.load(NotifierDescriptor.class);
        Iterator<NotifierDescriptor> it = loader.iterator();
        while (it.hasNext()) {
            NotifierDescriptor notifierDescriptor = it.next();
            if (notifierDescriptor.getType().equals(type)) return notifierDescriptor;
        }

        throw  new ItemNotFoundException("No notifier descriptor covering type '" +type+ "' registered");
    }

    private NotifierParameters setParameterValues(Resource notifier, NotifierParameters notifierParameters) {
        Iterator<NotifierParameters.NotifierParameter> parameterIterator = notifierParameters.getParameterIterator();

        while (parameterIterator.hasNext()) {
            NotifierParameters.NotifierParameter notifierParameter = parameterIterator.next();
            Value value = getValueOfPredicate(notifierParameter.getPredicate(), notifier);
            notifierParameter.setValue(value);
        }

        return notifierParameters;
    }

    private Value getValueOfPredicate(URI predicate, Resource notifier) {
        return rdfSubscription.filter(notifier, predicate, null).objectValue();
    }

    private Collection<NotificationQuery> createNotificationQueries(Subscription subscription) {
        Collection<NotificationQuery> notificationQueries = new ArrayList<NotificationQuery>();

        Set<Value> allQueries = rdfSubscription.filter(
            null,
            valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "query"),
            null).objects();

        for (Value query : allQueries) {
            String sparql = rdfSubscription.filter(
                (Resource) query,
                valueFactory.createURI(Namespaces.SPIN.getName(), "text"),
                null).objectString();
            NotificationQuery notificationQuery = new NotificationQuery(sparql,
                    getFormatter((Resource) query),
                    subscription);
            notificationQuery.setConditions(getConditions((Resource) query));
            notificationQuery.addAuxiliaryQueries(getAuxiliaryQueries((Resource) query));
            notificationQueries.add(notificationQuery);

        }

        return notificationQueries;
    }

    private BindingSetFormatter getFormatter(Resource query) {
        Resource formatter = rdfSubscription.filter(
            query,
            valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "formatter"),
            null).objectResource();

        if (formatter == null) {
            formatter = new URIImpl("http://unknown");
        }

        return new FormatterFactory().createFormatter(rdfSubscription, formatter);
    }

    private Collection<Condition> getConditions(Resource query) {
        Collection<Condition> conditions = new ArrayList<Condition>();

        Set<Value> allConditions = rdfSubscription.filter(
            query,
            valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "condition"),
            null).objects();

        for (Value condition : allConditions) {
            String askQuery = rdfSubscription.filter(
                (Resource) condition,
                valueFactory.createURI(Namespaces.SPIN.getName(), "text"),
                null).objectString();
            boolean expect = rdfSubscription.filter(
                (Resource) condition,
                valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "expect"),
                null).objectLiteral().booleanValue();
            conditions.add(new Condition(askQuery, expect));
        }

        return conditions;
    }

    private Collection<String> getAuxiliaryQueries(Resource query) {
        Collection<String> auxQueries = new ArrayList<String>();

        Set<Value> allAuxiliaries = rdfSubscription.filter(
                query,
                valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "auxiliary"),
                null).objects();

        for (Value condition : allAuxiliaries) {
            Set<Value> allAuxQueries = rdfSubscription.filter(
                (Resource) condition,
                valueFactory.createURI(Namespaces.SPIN.getName(), "text"),
                null).objects();
            for (Value auxQuery : allAuxQueries) {
                auxQueries.add(auxQuery.stringValue());
            }
        }

        return auxQueries;
    }

}
