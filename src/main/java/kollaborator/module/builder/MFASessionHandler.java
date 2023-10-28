package kollaborator.module.builder;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;
import static burp.api.montoya.http.handler.RequestToBeSentAction.continueWith;
import static burp.api.montoya.http.handler.ResponseReceivedAction.continueWith;

public class MFASessionHandler implements HttpHandler {

	private final Logging logging;
	private long start;// = System.currentTimeMillis();
	private long timeout;

    public MFASessionHandler(MontoyaApi api) {
        this.logging = api.logging();
        
    }
	
	@Override
	public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
		Annotations annotations = requestToBeSent.annotations();
		try {
			this.timeout = Integer.parseInt(KollaboratorModule.timeout.getText());
		}catch(Exception e) {
			this.timeout = 5;
		}
		if(KollaboratorModule.modifyRequests.isSelected()) {
			logging.logToError( "URL2 =  " +  requestToBeSent.url() + "\n  tool = " + requestToBeSent.toolSource().toolType().name() );
	        //Modify the request by adding url param.
	        HttpRequest modifiedRequest = requestToBeSent.withDefaultHeaders();
	        
	        // if request body contains __extracted__, than replace it with otp.
	        if(modifiedRequest.bodyToString().indexOf("__extracted__") >= 0) {
	        	start = System.currentTimeMillis();
	        	while(! MyInteractionHandler.otpSet ){
	        		if (isTimeOut()) {
	        			break;
	        		}
	        	
	        	}
	        	String body = modifiedRequest.bodyToString();
	        	body = body.replaceAll("__extracted__", MyInteractionHandler.otp);
	        	MyInteractionHandler.otpSet = false;
	        	MyInteractionHandler.otp = "";
	        	modifiedRequest = modifiedRequest.withBody(body);
	        }
	
	        //Return the modified request to burp with updated annotations.
	        return continueWith(modifiedRequest, annotations);
		}
		else {
		String temp = requestToBeSent.bodyToString();
		return continueWith(requestToBeSent.withBody(temp));
		}
	}

	private boolean isTimeOut() {
		long now = System.currentTimeMillis();
		long time_spent = now - start;
		if (time_spent > (this.timeout*1000)) {
			return true;
		}
		return false;
	}

	@Override
	public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
		Annotations annotations = responseReceived.annotations(); 
		annotations = annotations.withHighlightColor(HighlightColor.BLUE);
		
		return ResponseReceivedAction.continueWith(responseReceived, annotations);
	}
}
