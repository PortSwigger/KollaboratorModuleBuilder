package kollaborator.module.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.collaborator.DnsDetails;
import burp.api.montoya.collaborator.HttpDetails;
import burp.api.montoya.collaborator.Interaction;
import burp.api.montoya.collaborator.SmtpDetails;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.requests.HttpRequest;


public class MyInteractionHandler{

	
    
	
	private final MontoyaApi api;
    	private final InteractionLogger interactionLogger;
	static boolean otpSet;
	static String otp;

    public MyInteractionHandler(MontoyaApi api, InteractionLogger interactionLogger)
    {
        this.api = api;
        this.interactionLogger = interactionLogger;
	otpSet = false;
        otp = "";
    }
    
    
    public void handleInteraction(Interaction interaction)
    {
        interactionLogger.logInteraction(interaction);

    	String command = KollaboratorModule.code.getText();
    	
    	command = command.replaceAll("__clientIp__", interaction.clientIp().toString());
    	
    	command = command.replaceAll("__clientPort__", Integer.toString(interaction.clientPort()));
    	
    	switch(interaction.type()) {
	    	case DNS:
	    		command = command.replaceAll("__interactionType__", "DNS");
	    		break;
	    	case SMTP: 
	    		command = command.replaceAll("__interactionType__", "SMTP");
	    		break;
	    	case HTTP:
	    		command = command.replaceAll("__interactionType__", "HTTP");
	    		break;
	    	default:
	    		command = command.replaceAll("__interactionType__", "unknown");
	    		break;
    	}
    	
    	command = command.replaceAll("__interactionTime__", interaction.timeStamp().toString());
    	
		/*
		 * Custom data parsing
		 * 
		 */
    	if(interaction.customData().isPresent()) {
    		command = command.replaceAll("__customData__", Base64.getEncoder().encode(interaction.customData().get().getBytes()).toString() );
    	}
    	
		/*
		 * HTTP Data Parsing
		 * 
		 * 
		 */
    	if(interaction.httpDetails().isPresent()) {
    		HttpDetails httpDetails = (HttpDetails) interaction.httpDetails().get();
    		switch(httpDetails.protocol()) {
	    		case HTTP:
	    			command = command.replaceAll("__httpProtocol__", "http");
	    			break;
	    		case HTTPS:
	    			command = command.replaceAll("__httpProtocol__", "https");
	    			break;
	    		default:
	    			command = command.replaceAll("__httpProtocol__", "unknown");
	    			break;
    		}
    		HttpRequest request = httpDetails.requestResponse().request();
    		
			/*
			 * HTTP request Data Parsing
			 * 
			 * Begin
			 */
    		
    		//base64 encoded request body to make sure no harmful character can break python script.
    		command = command.replaceAll("__httpRequestBodyB64__", Base64.getEncoder().encodeToString(request.bodyToString().getBytes()));
    		switch(request.contentType()) {
    		case AMF:
    			command = command.replaceAll("__httpRequestContentType__", "AMF");
    			break;
    		case JSON:
    			command = command.replaceAll("__httpRequestContentType__", "JSON");
    			break;
    		case MULTIPART:
    			command = command.replaceAll("__httpRequestContentType__", "MULTIPART");
    			break;
    		case NONE:
    			command = command.replaceAll("__httpRequestContentType__", "NONE");
    			break;
    		case UNKNOWN:
    			command = command.replaceAll("__httpRequestContentType__", "UNKNOWN");
    			break;
    		case URL_ENCODED:
    			command = command.replaceAll("__httpRequestContentType__", "URL_ENCODED");
    			break;
    		case XML:
    			command = command.replaceAll("__httpRequestContentType__", "XML");
    			break;
    		}
    		List<HttpHeader> headers = request.headers();
    		String finalHeader = "";
    		for (HttpHeader header : headers) {
    			finalHeader = finalHeader + header.toString() + "\n";
    		}
    		command = command.replaceAll("__httpRequestHeadersB64__", Base64.getEncoder().encodeToString(finalHeader.getBytes()));
    		
    		command = command.replaceAll("__httpRequestMethod__", httpDetails.requestResponse().request().method());
    		
    		command = command.replaceAll("__httpRequestVersion__", httpDetails.requestResponse().request().httpVersion());
    		
    		command = command.replaceAll("__httpRequestPathB64__", Base64.getEncoder().encodeToString(httpDetails.requestResponse().request().path().getBytes()));
    		
    		command = command.replaceAll("__httpRequestUrlB64__", Base64.getEncoder().encodeToString(httpDetails.requestResponse().request().url().getBytes()));
    		
    		command = command.replaceAll("__httpRequestHost__", httpDetails.requestResponse().request().httpService().host());
    		
    		command = command.replaceAll("__httpRequestPort__", Integer.toString(httpDetails.requestResponse().request().httpService().port()));
    		
			/*
			 * HTTP Request Data parsing End
			 * 
			 * 
			 */
    		
    	}
    	if(interaction.dnsDetails().isPresent()) {
    		DnsDetails dnsDetails = (DnsDetails) interaction.dnsDetails().get();
    		/*
			 * DNS Request Data parsing start
			 * 
			 * 
			 */
    		switch(dnsDetails.queryType()) {
    		case A:
    			command = command.replaceAll("__dnsQueryType__", "A");
    			break;
    		case AAAA:
    			command = command.replaceAll("__dnsQueryType__", "AAAA");
    			break;
    		case ALL:
    			command = command.replaceAll("__dnsQueryType__", "A");
    			break;
    		case CAA:
    			command = command.replaceAll("__dnsQueryType__", "CAA");
    			break;
    		case CNAME:
    			command = command.replaceAll("__dnsQueryType__", "CNAME");
    			break;
    		case DNSKEY:
    			command = command.replaceAll("__dnsQueryType__", "DNSKEY");
    			break;
    		case DS:
    			command = command.replaceAll("__dnsQueryType__", "DS");
    			break;
    		case HINFO:
    			command = command.replaceAll("__dnsQueryType__", "HINFO");
    			break;
    		case HTTPS:
    			command = command.replaceAll("__dnsQueryType__", "HTTPS");
    			break;
    		case MX:
    			command = command.replaceAll("__dnsQueryType__", "MX");
    			break;
    		case NAPTR:
    			command = command.replaceAll("__dnsQueryType__", "NAPTR");
    			break;
    		case NS:
    			command = command.replaceAll("__dnsQueryType__", "NS");
    			break;
    		case PTR:
    			command = command.replaceAll("__dnsQueryType__", "PTR");
    			break;
    		case SOA:
    			command = command.replaceAll("__dnsQueryType__", "SOA");
    			break;
    		case SRV:
    			command = command.replaceAll("__dnsQueryType__", "SRV");
    			break;
    		case TXT:
    			command = command.replaceAll("__dnsQueryType__", "TXT");
    			break;
    		case UNKNOWN:
    			command = command.replaceAll("__dnsQueryType__", "UNKNOWN");
    			break;
    		
    		}
    		//handle with caution
    		command = command.replaceAll("__dnsQueryB64__", Base64.getEncoder().encodeToString(dnsDetails.query().getBytes()));
    	}
    	if(interaction.smtpDetails().isPresent()) {
    		SmtpDetails smtpDetails = (SmtpDetails) interaction.smtpDetails().get();
    		command = command.replaceAll("__smtpConversationB64__", Base64.getEncoder().encodeToString(smtpDetails.conversation().getBytes()));
    		
    		
    		switch(smtpDetails.protocol()) {
    		case SMTP:
    			command = command.replaceAll("__smtpProtocol__", "SMTP");
    			break;
    		case SMTPS:
    			command = command.replaceAll("__smtpProtocol__", "SMTPS");
    			break;
    		}
    	
    	}
    
    	
    	String tempDir  = System.getProperty("java.io.tmpdir");
    	
    	try {
    		File script = new File(tempDir + File.separator +"script.py");
    		script.createNewFile();
    		
    		FileWriter myWriter = new FileWriter(script);
    		myWriter.write(command);
    		myWriter.close();
    		
    		Process process = Runtime.getRuntime().exec("python " + script.getAbsolutePath());
    		
    		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    		
    		BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    		
    		String line = null;
    		String extracted;
    		String err = null;
    		while ((line = reader.readLine()) != null  || (err = error.readLine()) != null ) {
			if(line  != null) {
				api.logging().logToOutput(line);
				// IF you want OTP to be replaced in any request , In order to tell the extension what is 
				// OTP, you need to make a print statement like print("__extracted__" + otp)
				if(line.matches("__extracted__(.*)")) { 
					extracted = line.replace("__extracted__", ""); 
					this.otp = extracted;
					this.otpSet = true;
				}
			}
			if(err  != null ) {
				api.logging().logToError(err);
			}
		}
    		reader.close();
    		error.close();
    		script.delete();
		} catch (IOException e) {
			e.printStackTrace();
			api.logging().logToError(e.getMessage());
		}
    }

}
