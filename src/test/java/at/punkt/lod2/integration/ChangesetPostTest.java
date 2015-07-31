package at.punkt.lod2.integration;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.service.ChangeTripleService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

public class ChangesetPostTest {

    private final int PORT = 2221;
    private Server server;

    @Before
    public void setUp() throws Exception {
        server = Rsine.initAndStart(PORT, "test", null, null);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void postTripleChange() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_ADDEDTRIPLES, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        Assert.assertEquals(200, postChangeset(props));
    }    

    @Test
    public void postEmptyTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_ADDEDTRIPLES, "");

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postIllegalTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_ADDEDTRIPLES, "http://www.example.org/someconcept a skos:Concept .");

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postInvalidEofTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_ADDEDTRIPLES, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en");

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postEmptyContent() throws IOException {
        Assert.assertEquals(400, postChangeset(new Properties()));
    }

    @Test
    public void postUpdate() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_REMOVEDTRIPLES, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");
        props.setProperty(ChangeTripleService.POST_BODY_ADDEDTRIPLES, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"updatedlabel\"@en .");

        Assert.assertEquals(200, postChangeset(props));
    }

    @Test
    public void tripleChangeToRepo() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_ADDEDTRIPLES, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        Assert.assertEquals(200, postChangeset(props));
    }

    private int postChangeset(Properties properties) throws IOException {
        HttpPost httpPost = new HttpPost("http://localhost:" +PORT);
        StringWriter sw = new StringWriter();
        properties.store(sw, null);
        httpPost.setEntity(new StringEntity(sw.toString()));
        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        return response.getStatusLine().getStatusCode();
    }

}
