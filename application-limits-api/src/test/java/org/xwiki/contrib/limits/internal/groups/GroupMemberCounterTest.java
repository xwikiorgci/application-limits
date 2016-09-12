/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.limits.internal.groups;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.limits.LimitsConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.observation.event.CancelableEvent;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @version $Id: $
 */
public class GroupMemberCounterTest
{
    @Rule
    public MockitoComponentMockingRule<GroupMemberListener> mocker =
            new MockitoComponentMockingRule<>(GroupMemberListener.class);

    private GroupMemberCounter groupMemberCounter;
    private LimitsConfiguration limitsConfiguration;
    private DocumentReferenceResolver<String> explicitDocumentReferenceResolver;
    private Provider<Execution> executionProvider;
    private Execution execution;

    @Before
    public void setUp() throws Exception
    {
        groupMemberCounter = mocker.getInstance(GroupMemberCounter.class);
        limitsConfiguration = mocker.getInstance(LimitsConfiguration.class);
        explicitDocumentReferenceResolver = mock(DocumentReferenceResolver.class);
        mocker.registerComponent(DocumentReferenceResolver.TYPE_STRING, "explicit", explicitDocumentReferenceResolver);
        executionProvider =
                mocker.registerMockComponent(new DefaultParameterizedType(null, Provider.class, Execution.class));
        execution = mock(Execution.class);
        when(executionProvider.get()).thenReturn(execution);
    }

    @Test
    public void onEvent() throws Exception
    {
        CancelableEvent event = mock(CancelableEvent.class);
        XWikiDocument doc = mock(XWikiDocument.class);

        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "SomeGroup");
        when(doc.getDocumentReference()).thenReturn(documentReference);

        Map<DocumentReference, Number> groupLimits = new HashMap<>();
        when(limitsConfiguration.getGroupsLimits()).thenReturn(groupLimits);
        groupLimits.put(documentReference, 1);

        BaseObject obj1 = mock(BaseObject.class);
        BaseObject obj2 = mock(BaseObject.class);
        BaseObject obj3 = mock(BaseObject.class);
        BaseObject obj4 = mock(BaseObject.class);
        when(doc.getXObjects(new DocumentReference("xwiki", "XWiki", "XWikiGroups"))).thenReturn(
                Arrays.asList(obj1, obj2, null, obj3, obj4));
        when(obj1.getStringValue("member")).thenReturn("XWiki.UserA");
        when(obj2.getStringValue("member")).thenReturn("XWiki.UserB");
        when(obj3.getStringValue("member")).thenReturn("XWiki.UserA");
        when(obj4.getStringValue("member")).thenReturn("");

        DocumentReference userA = new DocumentReference("xwiki", "XWiki", "UserA");
        DocumentReference userB = new DocumentReference("xwiki", "XWiki", "UserB");
        when(explicitDocumentReferenceResolver.resolve("XWiki.UserA")).thenReturn(userA);
        when(explicitDocumentReferenceResolver.resolve("XWiki.UserB")).thenReturn(userB);

        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(execution.getContext()).thenReturn(executionContext);

        XWikiContext xcontext = mock(XWikiContext.class);
        when(executionContext.getProperty(XWikiContext.EXECUTIONCONTEXT_KEY)).thenReturn(xcontext);
        XWiki xwiki = mock(XWiki.class);
        when(xcontext.getWiki()).thenReturn(xwiki);

        XWikiDocument userADoc = mock(XWikiDocument.class);
        XWikiDocument userBDoc = mock(XWikiDocument.class);
        when(xwiki.getDocument(eq(userA), eq(xcontext))).thenReturn(userADoc);
        when(xwiki.getDocument(eq(userB), eq(xcontext))).thenReturn(userBDoc);

        DocumentReference userClassReference = new DocumentReference("xwiki", "XWiki", "XWikiUsers");
        BaseObject userADocObj = mock(BaseObject.class);
        when(userADoc.getXObject(eq(userClassReference))).thenReturn(userADocObj);
        when(userADoc.getDocumentReference()).thenReturn(userA);
        BaseObject userBDocObj = mock(BaseObject.class);
        when(userBDoc.getXObject(eq(userClassReference))).thenReturn(userBDocObj);
        when(userBDoc.getDocumentReference()).thenReturn(userB);

        when(groupMemberCounter.getUserCount(documentReference)).thenReturn(Long.valueOf(1));

        mocker.getComponentUnderTest().onEvent(event, doc, null);

        verify(mocker.getMockedLogger(), never()).error(anyString(), Matchers.anyObject());
        verify(event).cancel(
                "The limit of number of users in the group [xwiki:XWiki.SomeGroup] has been reached [2/1].");
    }

}
