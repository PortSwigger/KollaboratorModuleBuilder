# Kollaborator Module Builder

## Description
**Kollaborator Module Builder** is a Burp Suite extension that empowers you to write your own Python script to handle collaborator interactions. The beauty of this tool is that collaborator interaction data will be automatically replaced in your script.

## Installation
To install the extension, simply import the built jar file into Burp Suite.

## Usage
You can write your Python script in the text area under the KMB tab. This script will be triggered once an interaction is received by the collaborator. 

To use interaction data in your Python script, you can add placeholders in your script. For example, in `print("__clientIp__")`, `__clientIp__` will be replaced by the interaction IP. 

Here are the 20 placeholders you can use:

- `__clientIp__`
- `__clientPort__`
- `__interactionType__`
- `__interactionTime__`
- `__customData__`
- `__httpProtocol__`
- `__httpRequestBodyB64__`
- `__httpRequestContentType__`
- `__httpRequestHeadersB64__`
- `__httpRequestMethod__`
- `__httpRequestVersion__`
- `__httpRequestPathB64__`
- `__httpRequestUrlB64__`
- `__httpRequestHost__`
- `__httpRequestPort__`
- `__dnsQueryType__`
- `__dnsQueryB64__`
- `__smtpConversationB64__`
- `__smtpProtocol__

Please note that some of the interaction data is base64 encoded, for example __httpRequestBodyB64__,  and needs to be decoded before being used in your script.

## Contribution
If you'd like to contribute to this project, please feel free to fork the repository and submit a pull request!

