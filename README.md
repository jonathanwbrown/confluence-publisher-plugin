confluence-publisher-plugin
===========================

Jenkins Plugin to publish artifacts to Atlassian Confluence

This is a hacked version of the standard confluence-publisher-plugin.  

We moved from a 3.x version of Confluence to a 5.x version which broke our 
infrastructure. I found the integration with 5.x unwieldy so I created this 
fork.  I didn't have time to support backwards compatibility or do UI edits so 
it's in an as-is state.  It works well enough in our environment and I will try
to explain the differences in adequate detail below.

The changes/enhancements include:
  1. A bug fix in com.myyearbook.hudson.plugins.confluence.ConfluencePublisher.findArtifacts()
     a. In new projects having no SCM details artifactsDir will be non-null but 
        it won't be a directory so a NPE is thrown.  An additional isDirectory() check 
        resolves this.
  2. Use a regular expression for the BetweenTokensEditor implementation.
     a. Performing a substitution was gnarly and included a bunch of boilerplate
        macro code. The implementation now allows the user to simple specify only 
        the ID to find in the page and it does the right thing.  See the comparison 
        later in this document.
  3. Allow use of build parameters in the start marker token for between marker
     replacement.
     a.  Most of our substitutions are boilerplate but leveraging a parameterized 
         build wasn't an option when it came to specifying which token to replace.
         An enhancement was made to support this use case.  


Comparison of between-marker substitution in original plugin and this version:

  Original:
  The start marker token needed to be similar to: 
      \<ac:structured-macro ac:name="jenkins-between">\<ac:parameter ac:name="id">MY_START_TOKEN\</ac:parameter>\<ac:parameter ac:name="atlassian-macro-output-type">\</ac:parameter>\<ac:rich-text-body>
      
  The end marker token needed to be similar to:
      \</ac:rich-text-body>\</ac:structured-macro>
  
  The only way to find the above is to view the storage format for the page which is painful on the eyes. 

  Current:
  
  The start marker token is the id of macro in the page: 
    MY_START_TOKEN
  
  The end marker token is not needed.
  
  My implementation assumes the macro is named "jenkins-between" per the instructions 
  at https://wiki.jenkins-ci.org/display/JENKINS/Confluence+Publisher+Plugin.
  
  I attempt to retain the original format options (if I drop them or otherwise 
  don't match it's an unintentional bug but hey, it works on my machine).  
  
  
In closing, I will say that if you're trying to insert an image dynamically 
(I do) you *will* need to stoop to using the raw storage format.  At least I 
did and doesn't matter which version of the plugin you use.  Here is an example
of the content I insert into my page to dynamically modify an image in our page:
<p><ac:image ac:align="center"><ri:attachment ri:filename="myfile.png" /></ac:image></p>

<brain_dump />

    
    
    
