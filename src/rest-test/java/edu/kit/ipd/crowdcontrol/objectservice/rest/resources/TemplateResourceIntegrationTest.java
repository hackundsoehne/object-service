package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import com.mashape.unirest.http.exceptions.UnirestException;
import edu.kit.ipd.crowdcontrol.objectservice.proto.*;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.resources.ResourceIntegrationTest.httpDelete;
import static edu.kit.ipd.crowdcontrol.objectservice.rest.resources.ResourceIntegrationTest.httpGet;
import static edu.kit.ipd.crowdcontrol.objectservice.rest.resources.ResourceIntegrationTest.httpPut;
import static org.junit.Assert.*;

public class TemplateResourceIntegrationTest {
    @Test
    public void test() throws NoSuchMethodException, UnirestException, IllegalAccessException, IOException, InvocationTargetException {
        TemplateList list = httpGet("/templates", TemplateList.class);
        assertSame(0, list.getItemsCount());

        Template put = Template.newBuilder()
                .setName("name")
                .setContent("content")
                .setAnswerType(AnswerType.TEXT)
                .addConstraints(Constraint.newBuilder().setName("constraint").build())
                .addTags(Tag.newBuilder().setName("tag").build())
                .build();

        Template template = httpPut("/templates", put, Template.class);

        assertTrue(template.getId() > 0);
        assertEquals(template.toBuilder().setId(0).build(), put);

        Template received = httpGet("/templates/" + template.getId(), Template.class);
        assertEquals(template, received);

        list = httpGet("/templates", TemplateList.class);
        assertSame(1, list.getItemsCount());

        assertNull(httpDelete("/templates/" + template.getId(), Template.class));

        list = httpGet("/templates", TemplateList.class);
        assertSame(0, list.getItemsCount());
    }
}
