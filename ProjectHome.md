> ABOUT
----======----
mobile-google-talk-http is an extension of the MGTalk J2ME application to support the HTTP Binding (JEP-124). The original MGTalk application is a well-done Jabber client for J2ME. It also supports some Google Talk specific features. However, it does not support an HTTP communication mechanism for Jabber (a.k.a HTTP Binding or JEP-124). For more info on the original MGTalk project see here: http://mgtalk.sourceforge.net

The initial version (Release 0.1) of mobile-google-talk-http just adds basic HTTP Binding support to MGTalk. I've talked with the owner of MGTalk and he will include it in the original project.

The aim of mobile-google-talk-http is to provide a neat J2ME application for Google Talk, specialized on Google Talk. The next release will include a fullscreen, more Google Talk style user interface. Also, HTTP Binding support will be improved (spec not fully implemented yet).

> USING MOBILE-GOOGLE-TALK-HTTP
----=============================----
There are now two additional settings in the configuration options:
**Use HTTP Binding? [checkbox](checkbox.md)** HTTP Binding url  [textfield](textfield.md)
Check the first option 'Use HTTP Binding?' if you want to connect via a HTTP Binding gateway, specify the url of the HTTP Binding gateway in the 'HTTP Binding url' text field. Use the 'http[s](s.md)://host[:port]/path' format for the url. If the checkbox is unchecked then the url has no effect.

Using the HTTP Binding means, that the J2ME app connects to the gateway specified at 'HTTP Binding url' and routes the XMPP messages to the XMPP server (specified with 'Host name', 'Port' and 'SSL?'). Note that Google is not running an HTTP Binding gateway (AFAIK -- please correct me if I am wrong. Basic network analysis shows me that the Gmail web chat does not use HTTP Binding), thus you either need to run your own gateway, or use one that is publicly available.
**e.g. Punjab (http://punjab.sourceforge.net/) is such a HTTP Binding gateway** the only public http binding gateway i am aware of is 'https://www.butterfat.net:443/punjab/httpb/'. please drop me a note if you know more...

> CONTACT
----=======----
Please use the mailinglist to send feedback, comments etc.






