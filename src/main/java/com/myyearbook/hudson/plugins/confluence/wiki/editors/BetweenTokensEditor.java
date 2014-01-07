/*
 * Copyright 2011-2012 MeetMe, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.myyearbook.hudson.plugins.confluence.wiki.editors;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

import org.kohsuke.stapler.DataBoundConstructor;

import com.myyearbook.hudson.plugins.confluence.wiki.generators.MarkupGenerator;

/**
 * Represents a token-based Wiki markup editor that inserts the new content between two (start/end)
 * replacement marker tokens.
 *
 * @author Joe Hansche <jhansche@myyearbook.com>
 */
public class BetweenTokensEditor extends MarkupEditor {
    private final static String V4_MACRO_BLOCK = "<ac:structured-macro ac:name=\"jenkins-between\"><ac:parameter ac:name=\"id\">%s</ac:parameter><ac:parameter ac:name=\"atlassian-macro-output-type\">\\w+</ac:parameter><ac:rich-text-body>(.*?)</ac:rich-text-body></ac:structured-macro>";

    public final String startMarkerToken;
    public final String endMarkerToken;

    @DataBoundConstructor
    public BetweenTokensEditor(final MarkupGenerator generator, final String startMarkerToken,
            final String endMarkerToken) {
        super(generator);
        this.startMarkerToken = unquoteToken(Util.fixEmptyAndTrim(startMarkerToken));
        this.endMarkerToken = unquoteToken(Util.fixEmptyAndTrim(endMarkerToken));
    }

    /**
     * Inserts the generated content in the section between the {@link #startMarkerToken} and
     * {@link #endMarkerToken}.
     *
     * @param listener
     * @param content
     * @param generated
     * @throws TokenNotFoundException
     * @throws InterruptedException 
     * @throws IOException 
     */
    @Override
    public String performEdits(final AbstractBuild<?, ?> build, final BuildListener listener, final String content,
            final String generated, final boolean isNewFormat) throws TokenNotFoundException, IOException, InterruptedException {
        final StringBuffer sb = new StringBuffer(content);
        final String expandedStartMarkerToken = build.getEnvironment(listener).expand(startMarkerToken);

        final String macro = String.format(V4_MACRO_BLOCK, expandedStartMarkerToken);
        final Pattern p = Pattern.compile(".*(" + macro + ").*?");
        final Matcher m = p.matcher(content);

        if (!m.matches()) {
            throw new TokenNotFoundException("between-marker token could not be found in the page content: "
                    + expandedStartMarkerToken);
        }

        // There should be 2 matches: one for the entire 'between' macro and a match within that one
        // for what we're replacing
        if (m.groupCount() >= 2) {
            sb.replace(m.start(2), m.end(2), generated);
        }
        
        return sb.toString();
    }

    @Extension
    public static final class DescriptorImpl extends MarkupEditorDescriptor {
        @Override
        public String getDisplayName() {
            return "Replace content between start/end tokens";
        }
    }
}
