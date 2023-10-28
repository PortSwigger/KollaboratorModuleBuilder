# Kollaborator Module Builder

## Description
**Kollaborator Module Builder** is a Burp Suite extension that empowers you to write your own Python script to handle collaborator interactions. The beauty of this tool is it will automatically add the interaction data to the script wherever the placeholder for data is written.

## Installation
To install the extension, simply import the built jar file into Burp Suite professional.

Also, please note that this extension utilizes the python and library installed on the device running running burp suite pro. 

## Usage
You can write your Python script in the text area under the KMB tab. This script will be triggered once an interaction is received by the collaborator. 

## Steps (without OTP processing)

- Write the python script
 ![](https://raw.githubusercontent.com/mbkunal/KollaboratorModuleBuilder/main/screenshot1.1.png "")
- Click on "Copy Payload".
- Make interaction with the the collaborator link copied.
 ![](https://raw.githubusercontent.com/mbkunal/KollaboratorModuleBuilder/main/screenshot1.2.png "")
- Observe that the the python script is executed.
![](https://raw.githubusercontent.com/mbkunal/KollaboratorModuleBuilder/main/screenshot1.3.png "")

Note: python output is being redirected to extension's output. 


To use interaction data in your Python script, you can add placeholder for interacation data in your script. For example, in `print("__clientIp__")`, `__clientIp__` will be replaced by the interaction IP. 

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
- `__smtpProtocol__`

Please note that some of the interaction data is base64 encoded, for example `__httpRequestBodyB64__`,  and needs to be decoded before being used in your script.

Also note that the word `__extracted__` is also being reserved and should not be used as python variable or in python scripts. 

## Working

Basic functionality of extension like polling is being used via the Burp Montoya API example. 
Code for interaction have been edited to 
- Read the script from text area
- Replace placeholders with actual Data( base64 encoded in case data is not reliable).
- Store the the script data in temp python file.
- Use process.exec to call python and run the script file.
- Then check for the output of python file, If output of python contains `__extracted__`, then extract otp, set otp to variable, set the `otpflag` to true.
- In parallel, another class is responsible for checking if the `Modify Request` is enabled. 
- If enabled, then check each request body for `__extracted__` value. 
- If present, then check if `otpflag` is set to true. In case `otpflag` is false, wait for `timeout` seconds. 
- If `otpflag` becomes true within that limit, then perform below step. If `otpflag` does not become true within specified `timeout`, then process the request without otp.
- If `otpflag` is set to true, it implies that otp was set by the interaction handler and otp is ready to use. In this case `__extracted__` in request body is replaced with the otp set by interaction handler and request is dispatched. 
- delete the file after running.

Apart from that UI is added to provide user textarea for python script, checkbox to specify if all requests should be processed to add otp, input field to specify timeout and buton to copy collaborator link. 

## Changes done

 Added ability to create/manage session identifiers(assist in creating session which are created via email OTPs).

## How to replace OTP in requests

### Prerequisites 
- User shouls be able to register an account with collaborator link as email address. For example (admin@collaborator.oastify.com)
- Check the "Modify Requests" checkbox under "KMB" tab
- Set the timeout to appropriate value under "KMB" tab.

### Steps

- Copy the collaborator link from the KMB tab. This link will be used to register the user account, like abcd@collaborator.oastify.com
- Register an account with copied collaborator link.
- Write a python script to parse the smtp interction and extract the OTP from SMTP interaction
- Print into console in format    `__extracted__OTP` where `OTP` is the OTP extracted from SMTP interaction
- Now, whenever request is passed through Burp with  `__extracted__` in request body, it will be intercepted by the extension and then extension will wait till timeout foe the SMTP interaction. If extension received interaction, then OTP will be extracted and replaced in request body wherever `__extracted__` is present.

## Known Issue

http2 request causes some issued with `Modify Requests` function. Hence, it should be used with HTTP/1.1 requests only.


## Contribution
If you'd like to contribute to this project, please feel free to fork the repository and submit a pull request!

## References used

https://github.com/PortSwigger/burp-extensions-montoya-api-examples/tree/main/collaborator

