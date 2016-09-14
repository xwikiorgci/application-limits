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
package org.xwiki.contrib.limits.internal.ui;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.template.Template;
import org.xwiki.template.TemplateManager;
import org.xwiki.uiextension.UIExtension;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.internal.template.SUExecutor;
import com.xpn.xwiki.user.api.XWikiRightService;

/**
 * @version $Id: $
 */
@Component
@Singleton
@Named("LimitsUIExtension")
public class LimitsUIExtension implements UIExtension, Initializable
{
    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private TemplateManager templateManager;

    @Inject
    private SUExecutor suExecutor;

    private static final String TEMPLATE_NAME = "limits-uix.vm";

    private static final DocumentReference SUPERADMIN_REFERENCE =
            new DocumentReference("xwiki", XWiki.SYSTEM_SPACE, XWikiRightService.SUPERADMIN_USER);

    private String defaultTemplate;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            defaultTemplate = IOUtils.toString(getClass().getResourceAsStream("/templates/limits-uix.vm"));
        } catch (IOException e) {
            throw new InitializationException("Failed to initialize the Limits UI Extension.", e);
        }
    }

    @Override
    public String getId()
    {
        return "org.xwiki.contrib:application-limits";
    }

    @Override
    public String getExtensionPointId()
    {
        return "org.xwiki.platform.template.header.after";
    }

    @Override
    public Map<String, String> getParameters()
    {
        return Collections.emptyMap();
    }

    @Override
    public Block execute()
    {
        Template template = templateManager.getTemplate(TEMPLATE_NAME);
        if (template != null) {
            return templateManager.executeNoException(TEMPLATE_NAME);
        }

        return new RawBlock(executeDefaultTemplate(), Syntax.HTML_5_0);
    }

    private String executeDefaultTemplate()
    {
        try {
            return suExecutor.call(new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    return contextProvider.get().getWiki().evaluateVelocity(defaultTemplate, "limits");
                }
            }, SUPERADMIN_REFERENCE);

        } catch (Exception e) {
            // Cannot happen
            return "";
        }
    }
}
